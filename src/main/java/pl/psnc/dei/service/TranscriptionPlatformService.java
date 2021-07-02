package pl.psnc.dei.service;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonValue;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import pl.psnc.dei.exception.DEIHttpException;
import pl.psnc.dei.exception.NotFoundException;
import pl.psnc.dei.model.DAO.DatasetsRepository;
import pl.psnc.dei.model.DAO.ImportsRepository;
import pl.psnc.dei.model.DAO.ProjectsRepository;
import pl.psnc.dei.model.DAO.RecordsRepository;
import pl.psnc.dei.model.*;
import pl.psnc.dei.model.exception.TranscriptionPlatformException;
import pl.psnc.dei.queue.task.TasksFactory;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.TcpClient;

import javax.transaction.Transactional;
import java.util.*;

/**
 * Service responsible for communication with Transcription Platform.
 *
 * <p>
 * Created by pwozniak on 3/20/19
 */
@Service
@Transactional
public class TranscriptionPlatformService {

	public static final int READ_TIMEOUT_IN_SECONDS = 15;
	private static final Logger logger = LoggerFactory.getLogger(TranscriptionPlatformService.class);
	private static final int WRITE_TIMEOUT_IN_SECONDS = 5;
	private static final int CONNECTION_TIMEOUT_IN_SECONDS = 2;

	@Autowired
	private ProjectsRepository projectsRepository;

	@Autowired
	private DatasetsRepository datasetsRepository;

	@Autowired
	private RecordsRepository recordsRepository;

	@Autowired
	private ImportsRepository importsRepository;

	@Autowired
	@Lazy
	private TasksQueueService taskQueueService;

	@Autowired
	private TasksFactory tasksFactory;

	@Value("${europeana.api.tp.authorization-token}")
	private String authToken;

	private List<Project> availableProjects;
	private UrlBuilder urlBuilder;
	private WebClient webClient;


	public TranscriptionPlatformService(UrlBuilder urlBuilder,
										WebClient.Builder webClientBuilder) {
		this.urlBuilder = urlBuilder;
		configureWebClient(urlBuilder, webClientBuilder);
	}

	/**
	 * Creates and configures web client, uses url builder as service for link building
	 * @param urlBuilder service for assemblation of links
	 * @param webClientBuilder web client builder
	 */
	private void configureWebClient(UrlBuilder urlBuilder, WebClient.Builder webClientBuilder) {
		TcpClient tcpClient = TcpClient.create()
				.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, CONNECTION_TIMEOUT_IN_SECONDS * 1000)
				.doOnConnected(con -> con.addHandlerLast(new ReadTimeoutHandler(READ_TIMEOUT_IN_SECONDS))
						.addHandlerLast(new WriteTimeoutHandler(WRITE_TIMEOUT_IN_SECONDS)));

		this.webClient = webClientBuilder
				.clientConnector(new ReactorClientHttpConnector(HttpClient.from(tcpClient)))
				.baseUrl(urlBuilder.getBaseUrl())
				.build();
	}

	/**
	 * Returns all projects available in system, fetch only (?) on first try
	 * @return available projects
	 */
	public List<Project> getProjects() {
		if (availableProjects == null || availableProjects.isEmpty())
			refreshAvailableProjects();

		return availableProjects;
	}

	/**
	 * Fetches projects from Transcription Platform and saves them if not exists
	 */
	public void refreshAvailableProjects() {
		// fetch projects from TP
		availableProjects = new ArrayList<>();
		Project[] projects = webClient.get()
				.uri(urlBuilder.urlForAllProjects())
				.header("Authorization", authToken)
				.retrieve()
				.onStatus(HttpStatus::is4xxClientError, clientResponse -> {
					logger.error("Error {} while getting project. Cause: {}", clientResponse.rawStatusCode(), clientResponse.statusCode().getReasonPhrase());
					return Mono.error(new DEIHttpException(clientResponse.rawStatusCode(), clientResponse.statusCode().getReasonPhrase()));
				})
				.onStatus(HttpStatus::is5xxServerError, clientResponse -> {
					logger.error("Error {} while gerring project. Cause: {}", clientResponse.rawStatusCode(), clientResponse.statusCode().getReasonPhrase());
					return Mono.error(new DEIHttpException(clientResponse.rawStatusCode(), clientResponse.statusCode().getReasonPhrase()));
				})
				.bodyToMono(Project[].class).block();
		// owh error :/
		if (projects == null)
			return;

		// check which projects are not present
		for (Project tempProject : projects) {
			Project project = projectsRepository.findByName(tempProject.getName());
			if (project == null)
				project = tempProject;
			getDatasetsFor(project);
			project = projectsRepository.save(project);
			Hibernate.initialize(project.getDatasets());
			availableProjects.add(project);
		}
	}

	/**
	 * Loads datasets for given project
	 * Fetches them from Transcription Platform, and saves if not exists
	 * @param project name of project which datasets should be fetched
	 */
	public void getDatasetsFor(Project project) {
		Dataset[] projectDatasets = this.webClient.get().uri(urlBuilder.urlForProjectDatasets(project)).retrieve().bodyToMono(Dataset[].class).block();
		if (projectDatasets != null) {
			for (Dataset projectDataset : projectDatasets) {
				Dataset dataset = datasetsRepository.findDatasetByDatasetId(projectDataset.getDatasetId());
				// no given ds in database then override missing one
				if (dataset == null) {
					dataset = projectDataset;
				}
				dataset.setProject(project);
				datasetsRepository.save(dataset);
				addDatasetToProject(project, dataset);
			}
		}
	}

	/**
	 * Creates bidirectional link for HB
	 * @param project project to which dataset should be added
	 * @param dataset dataset which should be added to after mentioned project
	 */
	private void addDatasetToProject(Project project, Dataset dataset) {
		if (project
				.getDatasets()
				.stream()
				.noneMatch(existing -> existing.getDatasetId().equals(dataset.getDatasetId()))) {
			project.getDatasets().add(dataset);
		}
	}

	/**
	 * Sends record to TP
	 *
	 * @param record record that will be send to TP
	 * @throws TranscriptionPlatformException
	 */

	public void sendRecord(JsonObject recordBody, Record record) throws TranscriptionPlatformException {
		Hibernate.initialize(record.getAnImport());
		this.webClient.post()
				.uri(urlBuilder.urlForSendingRecord(record))
				.header("Authorization", authToken)
				.contentType(MediaType.APPLICATION_JSON)
				.body(BodyInserters.fromObject(recordBody.toString()))
				.retrieve()
				.onStatus(HttpStatus::is4xxClientError, clientResponse -> {
					logger.info("Error while sending record {} {}",clientResponse.rawStatusCode(), clientResponse.statusCode().getReasonPhrase());
					return Mono.error(new DEIHttpException(clientResponse.rawStatusCode(), clientResponse.statusCode().getReasonPhrase()));
				})
				.onStatus(HttpStatus::is5xxServerError, clientResponse -> {
					logger.info("Error while sending record {} {}",clientResponse.rawStatusCode(), clientResponse.statusCode().getReasonPhrase());
					return Mono.error(new DEIHttpException(clientResponse.rawStatusCode(), clientResponse.statusCode().getReasonPhrase()));
				})
				.bodyToMono(String.class)
				.doOnError(cause -> {
					if (cause instanceof TranscriptionPlatformException) {
						throw new TranscriptionPlatformException("Error while communicating with Transcription Platform");
					} else {
						throw new TranscriptionPlatformException("Error while communicating with Transcription Platform", cause);
					}
				})
				.block();
	}

	/**
	 * Reads all the transcriptions generated by the Transcription Platform for the given record
	 *
	 * @param record record that will be used for transcriptions fetching
	 * @return list of all transcriptions for the given record
	 * @throws TranscriptionPlatformException
	 */
	public JsonArray fetchTranscriptionsFor(Record record) throws TranscriptionPlatformException {
		logger.info("Retrieving transcriptions from TP for record {}", record.getIdentifier());
		String recordTranscriptions =
				this.webClient
						.get()
						.uri(urlBuilder.urlForRecordEnrichments(record, null,  null))
						.header("Authorization", authToken)
						.retrieve()
						.onStatus(HttpStatus::is4xxClientError, clientResponse -> {
							logger.info("Error while fetching transcription {} {}",clientResponse.rawStatusCode(), clientResponse.statusCode().getReasonPhrase());
							return Mono.error(new TranscriptionPlatformException());
						})
						.onStatus(HttpStatus::is5xxServerError, clientResponse -> {
							logger.info("Error while fetching transcription {} {}",clientResponse.rawStatusCode(), clientResponse.statusCode().getReasonPhrase());
							return Mono.error(new TranscriptionPlatformException());
						})
						.bodyToMono(String.class)
						.doOnError(cause -> {
							if (cause instanceof TranscriptionPlatformException) {
								throw new TranscriptionPlatformException("Error while communicating with Transcription Platform");
							} else {
								throw new TranscriptionPlatformException("Error while communicating with Transcription Platform", cause);
							}
						})
						.block();
		JsonValue value = JSON.parseAny(recordTranscriptions);
		return value.getAsArray();
	}

	/**
	 * Sends annotation url (generated by Annotations API) to the Transcription Platform
	 *
	 * @param transcription
	 * @throws TranscriptionPlatformException
	 */
	public void sendAnnotationUrl(Transcription transcription) throws TranscriptionPlatformException {
		logger.info("Sending annotation id to TP: \n" +
				"Record id: " + (transcription.getRecord() != null ? transcription.getRecord().getIdentifier() : "missing") +
				"\nAnnotation id: " + transcription.getAnnotationId());
		this.webClient
				.post()
				.uri(urlBuilder.urlForTranscription(transcription))
				.header("Authorization", authToken)
				.contentType(MediaType.APPLICATION_JSON)
				.body(BodyInserters.fromObject(convertToJson(transcription.getAnnotationId())))
				.retrieve()
				.onStatus(HttpStatus::is4xxClientError, clientResponse -> {
					logger.info("Error while sending annotation url {} {}",clientResponse.rawStatusCode(), clientResponse.statusCode().getReasonPhrase());
					return Mono.error(new TranscriptionPlatformException());
				})
				.onStatus(HttpStatus::is5xxServerError, clientResponse -> {
					logger.info("Error while sending annotation url {} {}",clientResponse.rawStatusCode(), clientResponse.statusCode().getReasonPhrase());
					return Mono.error(new TranscriptionPlatformException());
				})
				.bodyToMono(String.class)
				.doOnError(cause -> {
					if (cause instanceof TranscriptionPlatformException) {
						throw new TranscriptionPlatformException("Error while communicating with Transcription Platform");
					} else {
						throw new TranscriptionPlatformException("Error while communicating with Transcription Platform", cause);
					}
				})
				.block();
	}

	private String convertToJson(String annotationId) {
		JsonObject annotation = new JsonObject();
		annotation.put("EuropeanaAnnotationId", annotationId);
		return annotation.toString();
	}

	public JsonObject fetchTranscriptionUpdate(Transcription transcription) {
		String response = this.webClient
				.get()
				.uri(urlBuilder.urlForRecordEnrichments(transcription.getRecord(), transcription.getAnnotationId(), "transcribing"))
				.header("Authorization", authToken)
				.retrieve()
				.onStatus(HttpStatus::is4xxClientError, clientResponse -> {
					logger.info("Error while fetching transcription update {} {}",clientResponse.rawStatusCode(), clientResponse.statusCode().getReasonPhrase());
					return Mono.error(new TranscriptionPlatformException());
				})
				.onStatus(HttpStatus::is5xxServerError, clientResponse -> {
					logger.info("Error while fetching transcription update {} {}",clientResponse.rawStatusCode(), clientResponse.statusCode().getReasonPhrase());
					return Mono.error(new TranscriptionPlatformException());
				})
				.bodyToMono(String.class)
				.doOnError(cause -> {
					if (cause instanceof TranscriptionPlatformException) {
						throw new TranscriptionPlatformException("Error while communicating with Transcription Platform");
					} else {
						throw new TranscriptionPlatformException("Error while communicating with Transcription Platform", cause);
					}
				})
				.block();

		final String currentTranscription = retrieveCurrentTranscription(transcription.getTp_id(), response);
		if (currentTranscription != null) {
			return JSON.parse(currentTranscription);
		}
		return null;
	}

	private String retrieveCurrentTranscription(String transcriptionId, String enrichments) {
		JSONArray enrichmentsJson = null;
		try {
			JSONParser parser = new JSONParser();
			enrichmentsJson = (JSONArray) parser.parse(enrichments);
		} catch (ParseException e) {
			logger.error("Parsing transcription error", e);
		}

		if(enrichmentsJson == null) {
			logger.info("Empty transcription object {}", transcriptionId);
			return null;
		}

		for (Object en : enrichmentsJson) {
			JSONObject json = (JSONObject) en;
			final String annotationId = json.getAsString("AnnotationId");
			if(annotationId == null) {
				logger.info("Missing field AnnotationId for transcription {}", transcriptionId);
				return null;
			}
			if (annotationId.equals(transcriptionId)) {
				return json.toJSONString();
			}
		}
		return null;
	}

	public void createNewEnrichTask(String recordId) throws NotFoundException {
		Optional<Record> record = recordsRepository.findByIdentifier(recordId);
		if (record.isPresent()) {
			Record savedRecord = record.get();
			savedRecord.setState(Record.RecordState.E_PENDING);
			recordsRepository.save(savedRecord);
			taskQueueService.addTaskToQueue(tasksFactory.getTask(savedRecord));
		} else {
			throw new NotFoundException("Record not found " + recordId);
		}
	}

	public JsonObject getManifest(String recordId) throws NotFoundException {
		Optional<Record> oRecord = recordsRepository.findByIdentifier(recordId);
		if (oRecord.isPresent()) {
			Record record = oRecord.get();
			if (StringUtils.isNotBlank(record.getIiifManifest())) {
				return JSON.parse(record.getIiifManifest());
			} else {
				throw new NotFoundException("Manifest for record " + recordId + " doesn't exists!");
			}
		}
		throw new NotFoundException("Record " + recordId + " not found!");
	}


	public void sendImport(String importName) throws NotFoundException {
		Optional<Import> anImport = importsRepository.findImportByName(importName);
		if (!anImport.isPresent()) {
			throw new NotFoundException("Import " + importName + " not found");
		}
		// sending import can be done only for imports that are in progress
		if (ImportStatus.IN_PROGRESS.equals(anImport.get().getStatus())) {
			// We have to start the process of sending records by setting record state to T_PENDING and creating TranscribeTask
			// Only records in NORMAL or T_FAILED state are considered
			Set<Record> toSend = recordsRepository.findAllByAnImportAndStateIsIn(anImport.get(), Arrays.asList(Record.RecordState.NORMAL, Record.RecordState.T_FAILED, Record.RecordState.C_FAILED));
			toSend.forEach(record -> {
				record.setState(Record.RecordState.T_PENDING);
				recordsRepository.save(record);
				taskQueueService.addTaskToQueue(tasksFactory.getTask(record));
			});
		}
	}

	/**
	 * Update import state based on records. This is possible only when the import is in IN_PROGRESS state.
	 *
	 * @param anImport import object
	 */
	public void updateImportState(Import anImport) {
		if (ImportStatus.IN_PROGRESS.equals(anImport.getStatus())) {
			logger.info("Updating import status from IN_PROGRESS...");
			// if there is any record in state T_PENDING import is still IN_PROGRESS
			Set<Record> pendingRecords = recordsRepository.findAllByAnImportAndState(anImport, Record.RecordState.T_PENDING);
			if (pendingRecords.isEmpty()) {
				if (!recordsRepository.findAllByAnImportAndStateIsIn(anImport, Arrays.asList(Record.RecordState.T_FAILED, Record.RecordState.C_FAILED)).isEmpty()) {
					anImport.setStatus(ImportStatus.FAILED);
				} else {
					anImport.setStatus(ImportStatus.SENT);
				}
				importsRepository.save(anImport);
			}
		}
		logger.info("Import status updated to" + anImport.getStatus());
	}

	/**
	 * Adds failure information to the specified import. The failure is related to the record specified as recordIdentifier
	 * and the reason of failure is passed in message parameter.
	 *
	 * @param importName       import name
	 * @param record 			record
	 * @param message          failure reason
	 * @throws NotFoundException when import not found
	 */
	public void addFailure(String importName, Record record, String message) throws NotFoundException {
		Optional<Import> anImport = importsRepository.findImportByName(importName);
		if (!anImport.isPresent()) {
			throw new NotFoundException("Import " + importName + " not found!");
		}
		ImportFailure importFailure = new ImportFailure();
		importFailure.setAnImport(anImport.get());
		importFailure.setOccurenceDate(new Date());
		importFailure.setReason("Sending record " + record.getTitle() + " failed. Reason: " + message);
		Hibernate.initialize(anImport.get().getFailures());
		anImport.get().getFailures().add(importFailure);
		importsRepository.save(anImport.get());
	}
}

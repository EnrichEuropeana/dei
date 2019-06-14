package pl.psnc.dei.service;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonValue;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import pl.psnc.dei.exception.DEIHttpException;
import pl.psnc.dei.exception.NotFoundException;
import pl.psnc.dei.model.DAO.ProjectsRepository;
import pl.psnc.dei.model.DAO.RecordsRepository;
import pl.psnc.dei.model.Dataset;
import pl.psnc.dei.model.Project;
import pl.psnc.dei.model.Record;
import pl.psnc.dei.model.Transcription;
import pl.psnc.dei.model.exception.TranscriptionPlatformException;
import pl.psnc.dei.queue.task.TasksFactory;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.TcpClient;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service responsible for communication with Transcription Platform.
 *
 * <p>
 * Created by pwozniak on 3/20/19
 */
@Service
@Transactional
public class TranscriptionPlatformService {

	private static final int READ_TIMEOUT_IN_SECONDS = 5;
	private static final int WRITE_TIMEOUT_IN_SECONDS = 5;
	private static final int CONNECTION_TIMEOUT_IN_SECONDS = 1;

	@Autowired
	private ProjectsRepository projectsRepository;

	@Autowired
	private RecordsRepository recordsRepository;

	@Autowired
	private TasksQueueService taskQueueService;

	@Autowired
	private TasksFactory tasksFactory;

	private List<Project> availableProjects;
	private UrlBuilder urlBuilder;
	private WebClient webClient;

	public TranscriptionPlatformService(UrlBuilder urlBuilder,
										WebClient.Builder webClientBuilder) {
		this.urlBuilder = urlBuilder;
		configureWebClient(urlBuilder, webClientBuilder);
	}

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

	public List<Project> getProjects() {
		if (availableProjects == null || availableProjects.isEmpty())
			refreshAvailableProjects();

		return availableProjects;
	}

	public void refreshAvailableProjects() {
		availableProjects = new ArrayList<>();
		Project[] projects = webClient.get().uri(urlBuilder.urlForAllProjects()).retrieve().bodyToMono(Project[].class).block();
		if (projects == null)
			return;

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

	public void getDatasetsFor(Project project) {
		Dataset[] projectDatasets = this.webClient.get().uri(urlBuilder.urlForProjectDatasets(project)).retrieve().bodyToMono(Dataset[].class).block();
		if (projectDatasets != null) {
			for (Dataset projectDataset : projectDatasets) {
				projectDataset.setProject(project);
				project.getDatasets().add(projectDataset);
			}
		}
	}

	/**
	 * Sends record to TP
	 *
	 * @param record record that will be send to TP
	 * @throws TranscriptionPlatformException
	 */

	public void sendRecord(JsonObject record) throws TranscriptionPlatformException {
		this.webClient.post()
				.uri(urlBuilder.urlForSendingRecord())
				.body(BodyInserters.fromObject(record))
				.retrieve()
				.onStatus(HttpStatus::is4xxClientError, clientResponse -> Mono.error(new DEIHttpException(clientResponse.rawStatusCode(), clientResponse.statusCode().getReasonPhrase())))
				.onStatus(HttpStatus::is5xxServerError, clientResponse -> Mono.error(new DEIHttpException(clientResponse.rawStatusCode(), clientResponse.statusCode().getReasonPhrase())))
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
		String recordTranscriptions =
				this.webClient
						.get()
						.uri(urlBuilder.urlForRecord(record))
						.retrieve()
						.onStatus(HttpStatus::is5xxServerError, clientResponse -> Mono.error(new TranscriptionPlatformException()))
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
		this.webClient
				.post()
				.uri(urlBuilder.urlForTranscription(transcription))
				.body(BodyInserters.fromObject(transcription.getAnnotationId()))
				.retrieve()
				.onStatus(HttpStatus::is5xxServerError, clientResponse -> Mono.error(new TranscriptionPlatformException()))
				.bodyToMono(Object.class)
				.doOnError(cause -> {
					if (cause instanceof TranscriptionPlatformException) {
						throw new TranscriptionPlatformException("Error while communicating with Transcription Platform");
					} else {
						throw new TranscriptionPlatformException("Error while communicating with Transcription Platform", cause);
					}
				})
				.block();
	}

	public JsonObject fetchTranscriptionUpdate(Transcription transcription) {
		String response = this.webClient
				.get()
				.uri(urlBuilder.urlForTranscriptionUpdate(transcription))
				.retrieve()
				.onStatus(HttpStatus::is4xxClientError, clientResponse -> Mono.error(new TranscriptionPlatformException()))
				.onStatus(HttpStatus::is5xxServerError, clientResponse -> Mono.error(new TranscriptionPlatformException()))
				.bodyToMono(String.class)
				.doOnError(cause -> {
					if (cause instanceof TranscriptionPlatformException) {
						throw new TranscriptionPlatformException("Error while communicating with Transcription Platform");
					} else {
						throw new TranscriptionPlatformException("Error while communicating with Transcription Platform", cause);
					}
				})
				.block();

		return JSON.parse(response);
	}

	public void createNewEnrichTask(String recordId) throws NotFoundException {
		Optional<Record> record = recordsRepository.findByIdentifier(recordId);
		if (record.isPresent()) {
			Record savedRecord = record.get();
			savedRecord.setState(Record.RecordState.E_PENDING);
			recordsRepository.save(savedRecord);
			taskQueueService.addTaskToQueue(tasksFactory.getTask(record.get()));
		} else {
			throw new NotFoundException("Record not found.");
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
}

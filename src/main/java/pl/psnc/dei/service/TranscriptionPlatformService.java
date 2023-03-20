package pl.psnc.dei.service;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
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
import pl.psnc.dei.queue.task.EnrichTask;
import pl.psnc.dei.queue.task.MetadataEnrichTask;
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

    public static final int READ_TIMEOUT_IN_SECONDS = 100;
    private static final Logger logger = LoggerFactory.getLogger(TranscriptionPlatformService.class);
    private static final int WRITE_TIMEOUT_IN_SECONDS = 100;
    private static final int CONNECTION_TIMEOUT_IN_SECONDS = 100;

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

    @Autowired
    private ImportProgressService importProgressService;

    @Value("${europeana.api.tp.authorization-token}")
    private String authToken;

    @Value("${europeana.new.api.tp.authorization-token}")
    private String authNewApiToken;

    private List<Project> availableProjects;
    private final UrlBuilder urlBuilder;
    private WebClient webClient;


    public TranscriptionPlatformService(UrlBuilder urlBuilder,
            WebClient.Builder webClientBuilder) {
        this.urlBuilder = urlBuilder;
        configureWebClient(urlBuilder, webClientBuilder);
    }

    /**
     * Creates and configures web client, uses url builder as service for link building
     *
     * @param urlBuilder       service for assemblation of links
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
     *
     * @return available projects
     */
    public List<Project> getProjects() {
        if (availableProjects == null || availableProjects.isEmpty()) {
            refreshAvailableProjects();
        }

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
                    logger.error("Error {} while getting project. Cause: {}", clientResponse.rawStatusCode(),
                            clientResponse.statusCode().getReasonPhrase());
                    return Mono.error(new DEIHttpException(clientResponse.rawStatusCode(),
                            clientResponse.statusCode().getReasonPhrase()));
                })
                .onStatus(HttpStatus::is5xxServerError, clientResponse -> {
                    logger.error("Error {} while gerring project. Cause: {}", clientResponse.rawStatusCode(),
                            clientResponse.statusCode().getReasonPhrase());
                    return Mono.error(new DEIHttpException(clientResponse.rawStatusCode(),
                            clientResponse.statusCode().getReasonPhrase()));
                })
                .bodyToMono(Project[].class).block();
        // owh error :/
        if (projects == null) {
            return;
        }

        // check which projects are not present
        for (Project tempProject : projects) {
            Project project = projectsRepository.findByName(tempProject.getName());
            if (project == null) {
                project = tempProject;
            }
            getDatasetsFor(project);
            project = projectsRepository.save(project);
            Hibernate.initialize(project.getDatasets());
            availableProjects.add(project);
        }
    }

    /**
     * Loads datasets for given project
     * Fetches them from Transcription Platform, and saves if not exists
     *
     * @param project name of project which datasets should be fetched
     */
    public void getDatasetsFor(Project project) {
        Dataset[] projectDatasets = this.webClient.get().uri(urlBuilder.urlForProjectDatasets(project)).retrieve()
                .bodyToMono(Dataset[].class).block();
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
     *
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
        String storyId = this.webClient.post()
                .uri(urlBuilder.urlForSendingRecord(record))
                .header("Authorization", authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromObject(recordBody.toString()))
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, clientResponse -> {
                    logger.info("Error while sending record {} {}", clientResponse.rawStatusCode(),
                            clientResponse.statusCode().getReasonPhrase());
                    return Mono.error(new DEIHttpException(clientResponse.rawStatusCode(),
                            clientResponse.statusCode().getReasonPhrase()));
                })
                .onStatus(HttpStatus::is5xxServerError, clientResponse -> {
                    logger.info("Error while sending record {} {}", clientResponse.rawStatusCode(),
                            clientResponse.statusCode().getReasonPhrase());
                    return Mono.error(new DEIHttpException(clientResponse.rawStatusCode(),
                            clientResponse.statusCode().getReasonPhrase()));
                })
                .bodyToMono(String.class)
                .doOnError(cause -> {
                    if (cause instanceof TranscriptionPlatformException) {
                        throw new TranscriptionPlatformException(
                                "Error while communicating with Transcription Platform");
                    } else if (cause instanceof DEIHttpException) {
                        DEIHttpException ex = (DEIHttpException) cause;
                        throw new TranscriptionPlatformException(
                                "Error while communicating with Transcription Platform. " + ex);
                    } else {
                        throw new TranscriptionPlatformException(
                                "Error while communicating with Transcription Platform", cause);
                    }
                })
                .block();
        try {
            record.setStoryId(Long.parseLong(storyId));
            recordsRepository.save(record);
        } catch (NumberFormatException e) {
            // wrong value from the response, get story id from API
            record.setStoryId(retrieveStoryId(record));
            recordsRepository.save(record);
        }
    }

    public long retrieveStoryId(Record record) {
        JsonObject storyEnrichments = fetchMetadataEnrichmentsFor(record).getAsArray().get(0).getAsObject();
        return storyEnrichments.get("StoryId").getAsNumber().value().longValue();
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
                        .uri(urlBuilder.urlForRecordEnrichments(record, null, null))
                        .header("Authorization", authToken)
                        .retrieve()
                        .onStatus(HttpStatus::is4xxClientError, clientResponse -> {
                            logger.info("Error while fetching transcription {} {}", clientResponse.rawStatusCode(),
                                    clientResponse.statusCode().getReasonPhrase());
                            return Mono.error(new TranscriptionPlatformException());
                        })
                        .onStatus(HttpStatus::is5xxServerError, clientResponse -> {
                            logger.info("Error while fetching transcription {} {}", clientResponse.rawStatusCode(),
                                    clientResponse.statusCode().getReasonPhrase());
                            return Mono.error(new TranscriptionPlatformException());
                        })
                        .bodyToMono(String.class)
                        .doOnError(cause -> {
                            if (cause instanceof TranscriptionPlatformException) {
                                throw new TranscriptionPlatformException(
                                        "Error while communicating with Transcription Platform");
                            } else {
                                throw new TranscriptionPlatformException(
                                        "Error while communicating with Transcription Platform", cause);
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
                "Record id: " +
                (transcription.getRecord() != null ? transcription.getRecord().getIdentifier() : "missing") +
                "\nAnnotation id: " + transcription.getAnnotationId());
        this.webClient
                .post()
                .uri(urlBuilder.urlForTranscription(transcription))
                .header("Authorization", authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromObject(convertToJson(transcription.getAnnotationId())))
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, clientResponse -> {
                    logger.info("Error while sending annotation url {} {}", clientResponse.rawStatusCode(),
                            clientResponse.statusCode().getReasonPhrase());
                    return Mono.error(new TranscriptionPlatformException());
                })
                .onStatus(HttpStatus::is5xxServerError, clientResponse -> {
                    logger.info("Error while sending annotation url {} {}", clientResponse.rawStatusCode(),
                            clientResponse.statusCode().getReasonPhrase());
                    return Mono.error(new TranscriptionPlatformException());
                })
                .bodyToMono(String.class)
                .doOnError(cause -> {
                    if (cause instanceof TranscriptionPlatformException) {
                        throw new TranscriptionPlatformException(
                                "Error while communicating with Transcription Platform");
                    } else {
                        throw new TranscriptionPlatformException(
                                "Error while communicating with Transcription Platform", cause);
                    }
                })
                .block();
    }

    private String convertToJson(String annotationId) {
        JsonObject annotation = new JsonObject();
        annotation.put("EuropeanaAnnotationId", annotationId);
        return annotation.toString();
    }

    /**
     * Fetch content of updated transcription
     *
     * @param transcription transcription to fetch content for
     * @return content of transcription
     */
    public JsonObject fetchTranscriptionUpdate(Transcription transcription) {
        String response = this.webClient
                .get()
                .uri(urlBuilder.urlForRecordEnrichments(transcription.getRecord(), transcription.getAnnotationId(),
                        "transcribing"))
                .header("Authorization", authToken)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, clientResponse -> {
                    logger.info("Error while fetching transcription update {} {}", clientResponse.rawStatusCode(),
                            clientResponse.statusCode().getReasonPhrase());
                    return Mono.error(new TranscriptionPlatformException());
                })
                .onStatus(HttpStatus::is5xxServerError, clientResponse -> {
                    logger.info("Error while fetching transcription update {} {}", clientResponse.rawStatusCode(),
                            clientResponse.statusCode().getReasonPhrase());
                    return Mono.error(new TranscriptionPlatformException());
                })
                .bodyToMono(String.class)
                .doOnError(cause -> {
                    if (cause instanceof TranscriptionPlatformException) {
                        throw new TranscriptionPlatformException(
                                "Error while communicating with Transcription Platform");
                    } else {
                        throw new TranscriptionPlatformException(
                                "Error while communicating with Transcription Platform", cause);
                    }
                })
                .block();

        final String currentTranscription = retrieveCurrentTranscription(transcription.getTpId(), response);
        if (currentTranscription != null) {
            return JSON.parse(currentTranscription);
        }
        return null;
    }

    /**
     * Fetch content of updated HTR transcription
     *
     * @param transcription transcription to fetch content for
     * @return content of transcription
     */
    public JsonObject fetchHTRTranscriptionUpdate(Transcription transcription) {
        logger.info(
                "Fetching HTR transcription update based on HtrDataId (ItemId={}) {} and Europeana Annotation Id {}. Record identifier {}.",
                transcription.getItemId(), transcription.getTpId(), transcription.getAnnotationId(), transcription.getRecord().getIdentifier());
        // TODO maybe another endpoint will have to be used...for now it's getHTRData, no Europeana Annotation is used
        return Objects.requireNonNullElse(getHTRData(transcription.getItemId()).getAsObject(), null);
    }

    private String retrieveCurrentTranscription(String transcriptionId, String enrichments) {
        JSONArray enrichmentsJson = null;
        try {
            JSONParser parser = new JSONParser();
            enrichmentsJson = (JSONArray) parser.parse(enrichments);
        } catch (ParseException e) {
            logger.error("Parsing transcription error", e);
        }

        if (enrichmentsJson == null) {
            logger.info("Empty transcription object {}", transcriptionId);
            return null;
        }

        for (Object en : enrichmentsJson) {
            JSONObject json = (JSONObject) en;
            final String annotationId = json.getAsString("AnnotationId");
            if (annotationId == null) {
                logger.info("Missing field AnnotationId for transcription {}", transcriptionId);
                return null;
            }
            if (annotationId.equals(transcriptionId)) {
                return json.toJSONString();
            }
        }
        return null;
    }

    /**
     * Fetch and create task to enrich record
     *
     * @param recordId id of record to be enriched
     * @throws NotFoundException if record was not found
     */
    public void createNewEnrichTask(String recordId) throws NotFoundException {
        Optional<Record> record = recordsRepository.findByIdentifier(recordId);
        if (record.isPresent()) {
            Record savedRecord = record.get();
            // when metadata enrichment is in progress we set ME_PENDING state which means that transcription enrichment is pending at the same time
            if (savedRecord.getState() == Record.RecordState.M_PENDING) {
                savedRecord.setState(Record.RecordState.ME_PENDING);
            } else {
                savedRecord.setState(Record.RecordState.E_PENDING);
            }
            recordsRepository.save(savedRecord);
            tasksFactory.getTask(savedRecord).stream()
                    .filter(task -> task instanceof EnrichTask)
                    .forEach(taskQueueService::addTaskToQueue);
        } else {
            throw new NotFoundException("Record not found " + recordId);
        }
    }

    /**
     * Fetch and create task to enrich record
     *
     * @param recordId id of record to be enriched
     * @throws NotFoundException if record was not found
     */
    public void createNewMetadataEnrichTask(String recordId) throws NotFoundException {
        Optional<Record> record = recordsRepository.findByIdentifier(recordId);
        if (record.isPresent()) {
            Record savedRecord = record.get();
            // when transcription enrichment is in progress we set ME_PENDING state which means that metadata enrichment is pending at the same time
            if (savedRecord.getState() == Record.RecordState.E_PENDING) {
                savedRecord.setState(Record.RecordState.ME_PENDING);
            } else {
                savedRecord.setState(Record.RecordState.M_PENDING);
            }
            recordsRepository.save(savedRecord);
            tasksFactory.getTask(savedRecord).stream()
                    .filter(task -> task instanceof MetadataEnrichTask)
                    .forEach(taskQueueService::addTaskToQueue);
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

    /**
     * Start sending procedure of import in which import goes through consecutive states
     *
     * @param importName name of import to send
     * @throws NotFoundException thrown if import was not found
     */
    public void sendImport(String importName) throws NotFoundException {
        Optional<Import> importOptional = importsRepository.findImportByName(importName);
        if (importOptional.isEmpty()) {
            throw new NotFoundException("Import " + importName + " not found");
        }
        Import anImport = importOptional.get();
        // sending import can be done only for imports that are in progress
        if (ImportStatus.IN_PROGRESS.equals(anImport.getStatus())) {
            // We have to start the process of sending records by setting record state to T_PENDING and creating TranscribeTask
            // Only records in NORMAL, T_FAILED, C_FAILED or V_FAILED state are considered
            Set<Record> toSend = recordsRepository.findAllByAnImportAndStateIsIn(anImport,
                    Arrays.asList(Record.RecordState.NORMAL, Record.RecordState.T_FAILED, Record.RecordState.C_FAILED,
                            Record.RecordState.V_FAILED));
            if (toSend.isEmpty()) {
                updateImportState(anImport);
            } else {
                initializeImportProgress(anImport, toSend.size());
                toSend.forEach(record -> {
                    if (record.getState().equals(Record.RecordState.V_FAILED)) {
                        record.setState(Record.RecordState.V_PENDING);
                    } else {
                        record.setState(Record.RecordState.T_PENDING);
                    }
                    recordsRepository.save(record);
                    tasksFactory.getTask(record).forEach(taskQueueService::addTaskToQueue);
                });
            }
        }
    }

    private void initializeImportProgress(Import anImport, int records) {
        anImport.setProgress(importProgressService.initImportProgress(records));
        importsRepository.save(anImport);
    }

    /**
     * Update import state based on records. This is possible only when the import is in IN_PROGRESS state.
     *
     * @param anImport import object
     */
    public void updateImportState(Import anImport) {
        if (ImportStatus.IN_PROGRESS.equals(anImport.getStatus())) {
            logger.info("Updating import status from IN_PROGRESS...");
            // if there is any record in state T_PENDING, C_PENDING or V_PENDING import is still IN_PROGRESS
            Set<Record> pendingRecords = recordsRepository.findAllByAnImportAndStateIsIn(anImport,
                    Arrays.asList(Record.RecordState.T_PENDING, Record.RecordState.C_PENDING,
                            Record.RecordState.V_PENDING));
            if (pendingRecords.isEmpty()) {
                if (!recordsRepository.findAllByAnImportAndStateIsIn(anImport,
                        Arrays.asList(Record.RecordState.T_FAILED, Record.RecordState.C_FAILED,
                                Record.RecordState.V_FAILED)).isEmpty()) {
                    anImport.setStatus(ImportStatus.FAILED);
                } else {
                    anImport.setStatus(ImportStatus.SENT);
                }
                anImport.setProgress(null);
                importsRepository.save(anImport);
            }
        }
        logger.info("Import status updated to " + anImport.getStatus());
    }

    /**
     * Adds failure information to the specified import. The failure is related to the record specified as recordIdentifier
     * and the reason of failure is passed in message parameter.
     *
     * @param importName import name
     * @param record     record
     * @param cause      failure reason
     * @throws NotFoundException when import not found
     */
    public void addFailure(String importName, Record record, Throwable cause) throws NotFoundException {
        Optional<Import> anImport = importsRepository.findImportByName(importName);
        if (anImport.isEmpty()) {
            throw new NotFoundException("Import " + importName + " not found!");
        }
        ImportFailure importFailure = new ImportFailure();
        importFailure.setAnImport(anImport.get());
        importFailure.setOccurenceDate(new Date());
        importFailure.buildReason(record.getTitle(), cause);
        Hibernate.initialize(anImport.get().getFailures());
        anImport.get().getFailures().add(importFailure);
        importsRepository.save(anImport.get());
    }

    public JsonValue fetchMetadataEnrichmentsFor(Record record) throws TranscriptionPlatformException {
        logger.info("Retrieving metadata enrichments from TP for record {}", record.getIdentifier());
        String recordMetadataEnrichments =
                this.webClient
                        .get()
                        .uri(urlBuilder.urlForRecordMetadataEnrichments(record))
                        .header("Authorization", authToken)
                        .retrieve()
                        .onStatus(HttpStatus::is4xxClientError, clientResponse -> {
                            logger.info("Error while fetching metadata enrichments {} {}",
                                    clientResponse.rawStatusCode(), clientResponse.statusCode().getReasonPhrase());
                            return Mono.error(new TranscriptionPlatformException());
                        })
                        .onStatus(HttpStatus::is5xxServerError, clientResponse -> {
                            logger.info("Error while fetching metadata enrichments {} {}",
                                    clientResponse.rawStatusCode(), clientResponse.statusCode().getReasonPhrase());
                            return Mono.error(new TranscriptionPlatformException());
                        })
                        .bodyToMono(String.class)
                        .doOnError(cause -> {
                            if (cause instanceof TranscriptionPlatformException) {
                                throw new TranscriptionPlatformException(
                                        "Error while communicating with Transcription Platform while fetching metadata enrichments");
                            } else {
                                throw new TranscriptionPlatformException(
                                        "Error while communicating with Transcription Platform while fetching metadata enrichments",
                                        cause);
                            }
                        })
                        .block();
        return JSON.parseAny(recordMetadataEnrichments);
    }

    public JsonValue fetchMetadataEnrichmentsForItem(long itemId) throws TranscriptionPlatformException {
        logger.info("Retrieving item metadata enrichments from TP for item {}", itemId);
        String itemMetadataEnrichments =
                this.webClient
                        .get()
                        .uri(urlBuilder.urlForItemMetadataEnrichments(itemId))
                        .header("Authorization", authToken)
                        .retrieve()
                        .onStatus(HttpStatus::is4xxClientError, clientResponse -> {
                            logger.info("Error while fetching item metadata enrichments {} {}",
                                    clientResponse.rawStatusCode(), clientResponse.statusCode().getReasonPhrase());
                            return Mono.error(new TranscriptionPlatformException());
                        })
                        .onStatus(HttpStatus::is5xxServerError, clientResponse -> {
                            logger.info("Error while fetching item metadata enrichments {} {}",
                                    clientResponse.rawStatusCode(), clientResponse.statusCode().getReasonPhrase());
                            return Mono.error(new TranscriptionPlatformException());
                        })
                        .bodyToMono(String.class)
                        .doOnError(cause -> {
                            if (cause instanceof TranscriptionPlatformException) {
                                throw new TranscriptionPlatformException(
                                        "Error while communicating with Transcription Platform while fetching item metadata enrichments");
                            } else {
                                throw new TranscriptionPlatformException(
                                        "Error while communicating with Transcription Platform while fetching item metadata enrichments",
                                        cause);
                            }
                        })
                        .block();
        return JSON.parseAny(itemMetadataEnrichments);
    }

    public JsonValue getItem(long itemId) throws TranscriptionPlatformException {
        logger.info("Retrieving information from New TP API about item {}", itemId);
        String itemInfo = this.webClient
                .get()
                .uri(urlBuilder.urlForItem(itemId))
                .header("Authorization", authNewApiToken)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, clientResponse -> {
                    logger.info("Error while fetching item information {} {}", clientResponse.rawStatusCode(),
                            clientResponse.statusCode().getReasonPhrase());
                    return Mono.error(new TranscriptionPlatformException());
                })
                .onStatus(HttpStatus::is5xxServerError, clientResponse -> {
                    logger.info("Error while fetching item information {} {}", clientResponse.rawStatusCode(),
                            clientResponse.statusCode().getReasonPhrase());
                    return Mono.error(new TranscriptionPlatformException());
                })
                .bodyToMono(String.class)
                .doOnError(cause -> {
                    if (cause instanceof TranscriptionPlatformException) {
                        throw new TranscriptionPlatformException(
                                "Error while communicating with Transcription Platform while fetching item information");
                    } else {
                        throw new TranscriptionPlatformException(
                                "Error while communicating with Transcription Platform while fetching item information",
                                cause);
                    }
                })
                .block();
        return JSON.parseAny(itemInfo);
    }

    public JsonArray fetchHTRTranscriptions(Record record, List<Long> exclude) {
        logger.info("Retrieving HTR transcriptions from TP for record {}", record.getIdentifier());
        if (record.getStoryId() == null) {
            throw new IllegalArgumentException("No story id in record " + record.getIdentifier());
        }
        JsonArray htrs = new JsonArray();
        List<Long> itemIds = fetchItemIds(record.getStoryId());
        itemIds.stream().filter(id -> !exclude.contains(id)).map(id -> Pair.of(id, getItemFromTP(id)))
                .filter(pair -> isHTR(
                        pair.getValue())).map(pair -> getHTRData(pair.getKey())).forEach(htrs::add);
        return htrs;
    }

    private JsonValue getHTRData(Long itemId) {
        String response = this.webClient
                .get()
                .uri(urlBuilder.urlForItemHTR(itemId))
                .header("Authorization", authNewApiToken)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, clientResponse -> {
                    logger.info("Error while fetching transcription {} {}", clientResponse.rawStatusCode(),
                            clientResponse.statusCode().getReasonPhrase());
                    return Mono.error(new TranscriptionPlatformException());
                })
                .onStatus(HttpStatus::is5xxServerError, clientResponse -> {
                    logger.info("Error while fetching transcription {} {}", clientResponse.rawStatusCode(),
                            clientResponse.statusCode().getReasonPhrase());
                    return Mono.error(new TranscriptionPlatformException());
                })
                .bodyToMono(String.class)
                .doOnError(cause -> {
                    if (cause instanceof TranscriptionPlatformException) {
                        throw new TranscriptionPlatformException(
                                "Error while communicating with Transcription Platform");
                    } else {
                        throw new TranscriptionPlatformException(
                                "Error while communicating with Transcription Platform", cause);
                    }
                })
                .block();
        return JSON.parseAny(Objects.requireNonNullElse(response, "{}"));
    }

    private boolean isHTR(JsonValue jsonValue) {
        JsonValue source = jsonValue.getAsObject().get("TranscriptionSource");
        return source != null && TranscriptionType.HTR.equals(
                TranscriptionType.from(source.getAsString().value()));
    }

    private JsonValue getItemFromTP(Long id) {
        String response = this.webClient
                .get()
                .uri(urlBuilder.urlForItem(id))
                .header("Authorization", authNewApiToken)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, clientResponse -> {
                    logger.info("Error while fetching transcription {} {}", clientResponse.rawStatusCode(),
                            clientResponse.statusCode().getReasonPhrase());
                    return Mono.error(new TranscriptionPlatformException());
                })
                .onStatus(HttpStatus::is5xxServerError, clientResponse -> {
                    logger.info("Error while fetching transcription {} {}", clientResponse.rawStatusCode(),
                            clientResponse.statusCode().getReasonPhrase());
                    return Mono.error(new TranscriptionPlatformException());
                })
                .bodyToMono(String.class)
                .doOnError(cause -> {
                    if (cause instanceof TranscriptionPlatformException) {
                        throw new TranscriptionPlatformException(
                                "Error while communicating with Transcription Platform");
                    } else {
                        throw new TranscriptionPlatformException(
                                "Error while communicating with Transcription Platform", cause);
                    }
                })
                .block();
        if (response != null) {
            return JSON.parseAny(response);
        }
        return JSON.parseAny("{}");
    }

    private List<Long> fetchItemIds(Long storyId) {
        String story = this.webClient
                .get()
                .uri(urlBuilder.urlForStory(storyId))
                .header("Authorization", authNewApiToken)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, clientResponse -> {
                    logger.info("Error while fetching transcription {} {}", clientResponse.rawStatusCode(),
                            clientResponse.statusCode().getReasonPhrase());
                    return Mono.error(new TranscriptionPlatformException());
                })
                .onStatus(HttpStatus::is5xxServerError, clientResponse -> {
                    logger.info("Error while fetching transcription {} {}", clientResponse.rawStatusCode(),
                            clientResponse.statusCode().getReasonPhrase());
                    return Mono.error(new TranscriptionPlatformException());
                })
                .bodyToMono(String.class)
                .doOnError(cause -> {
                    if (cause instanceof TranscriptionPlatformException) {
                        throw new TranscriptionPlatformException(
                                "Error while communicating with Transcription Platform");
                    } else {
                        throw new TranscriptionPlatformException(
                                "Error while communicating with Transcription Platform", cause);
                    }
                })
                .block();
        Objects.requireNonNull(story);
        List<Long> itemIds = new ArrayList<>();
        for (JsonValue itemId : JSON.parseAny(story).getAsObject().get("data").getAsObject().get("ItemIds")
                .getAsArray()) {
            itemIds.add(itemId.getAsNumber().value().longValue());
        }
        return itemIds;
    }
}

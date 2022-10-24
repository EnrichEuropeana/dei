package pl.psnc.dei.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.psnc.dei.controllers.requests.RecordMetadataEnrichmentValidation;
import pl.psnc.dei.exception.NotFoundException;
import pl.psnc.dei.model.enrichments.DTO.RecordEnrichmentsDTO;
import pl.psnc.dei.model.enrichments.MetadataEnrichment;
import pl.psnc.dei.service.MetadataEnrichmentService;
import pl.psnc.dei.service.TranscriptionPlatformService;
import pl.psnc.dei.util.EuropeanaRecordIdValidator;
import pl.psnc.dei.util.OAIRecordIdValidator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@RestController
@RequestMapping("/api/enrichments")
public class MetadataEnrichmentController {

    private final Logger logger = LoggerFactory.getLogger(MetadataEnrichmentController.class);

    private final TranscriptionPlatformService tps;

    private final MetadataEnrichmentService mes;

    @Autowired
    public MetadataEnrichmentController(@Qualifier("transcriptionPlatformService") TranscriptionPlatformService tps,
            MetadataEnrichmentService mes) {
        this.tps = tps;
        this.mes = mes;
    }

    /**
     * Notify server about set of newly available transcription
     *
     * @param recordId
     * @return
     */
    @PostMapping
    public ResponseEntity enrichmentReady(@RequestParam(value = "recordId") String recordId) {

        logger.info("Enrichment ready {}", recordId);

        if (!EuropeanaRecordIdValidator.validate(recordId)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        try {
            tps.createNewMetadataEnrichTask(recordId);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (NotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Notify server about set of newly available enrichments
     *
     * @param recordsIds ids of records transcripted
     * @return
     */
    @PostMapping("/batch")
    public ResponseEntity<String> enrichmentsReady(@RequestBody Set<String> recordsIds) {
        if (!recordsIds.stream().allMatch(EuropeanaRecordIdValidator::validate)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Set<String> notFound = new HashSet<>();

        for (String recordId : recordsIds) {
            logger.info("Creating metadata enrich task for record {}", recordId);
            try {
                tps.createNewMetadataEnrichTask(recordId);
            } catch (NotFoundException e) {
                notFound.add(e.getMessage());
            }
        }
        if (!notFound.isEmpty()) {
            return new ResponseEntity<>(String.join(",", notFound), HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<RecordEnrichmentsDTO> getRecordEnrichments(@RequestParam(value = "recordId") String recordId,
            @RequestParam(value = "state", required = false, defaultValue = "PENDING") MetadataEnrichment.EnrichmentState state) {
        logger.info("Getting enrichments for record");
        if (!EuropeanaRecordIdValidator.validate(recordId) && !OAIRecordIdValidator.validate(recordId)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        RecordEnrichmentsDTO pendingEnrichments;
        try {
            pendingEnrichments = mes.getEnrichmentsForRecord(recordId, state);
        } catch (NotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(pendingEnrichments, HttpStatus.OK);
    }

    @GetMapping("/domain")
    public ResponseEntity<List<RecordEnrichmentsDTO>> getRecordEnrichmentsForDomain(
            @RequestParam(value = "domain") String domain,
            @RequestParam(value = "state", required = false, defaultValue = "PENDING") MetadataEnrichment.EnrichmentState state) {
        logger.info("Getting enrichments for domain");
        List<RecordEnrichmentsDTO> pendingEnrichments = mes.getEnrichmentsForDomain(domain, state);
        if (pendingEnrichments.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(pendingEnrichments, HttpStatus.OK);
    }

    @PostMapping("/validate")
    public ResponseEntity validate(
            @RequestBody List<RecordMetadataEnrichmentValidation> recordMetadataEnrichmentValidations) {
        List<String> notFound = new ArrayList<>();
        recordMetadataEnrichmentValidations.forEach(recordMetadataEnrichmentValidation -> {
            try {
                mes.validateMetadataEnrichments(recordMetadataEnrichmentValidation);
            } catch (NotFoundException e) {
                notFound.add(recordMetadataEnrichmentValidation.getRecordId());
            }
        });
        if (notFound.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>("Records not found: " + notFound, HttpStatus.NOT_FOUND);
    }
}

package pl.psnc.dei.service;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pl.psnc.dei.controllers.requests.RecordMetadataEnrichmentValidation;
import pl.psnc.dei.exception.NotFoundException;
import pl.psnc.dei.model.DAO.AllMetadataEnrichmentRepository;
import pl.psnc.dei.model.DAO.RecordsRepository;
import pl.psnc.dei.model.Record;
import pl.psnc.dei.model.enrichments.DTO.RecordEnrichmentsDTO;
import pl.psnc.dei.model.enrichments.MetadataEnrichment;
import pl.psnc.dei.util.EuropeanaRecordIdValidator;
import pl.psnc.dei.util.OAIRecordIdValidator;

import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Transactional
@AllArgsConstructor
public class MetadataEnrichmentService {
    private final Logger logger = LoggerFactory.getLogger(MetadataEnrichmentService.class);

    private final RecordsRepository recordsRepository;

    private final AllMetadataEnrichmentRepository metadataEnrichmentRepository;

    /**
     * This is used for getting enrichments available for records with OAI-PMH identifier which contains certain domain.
     * Only enrichments in PENDING state are exposed.
     *
     * @param domain
     * @return list of DTOs for record enrichments
     */
    public List<RecordEnrichmentsDTO> getEnrichmentsForDomain(String domain, MetadataEnrichment.EnrichmentState state) {
        Map<Record, List<MetadataEnrichment>> recordEnrichments = metadataEnrichmentRepository.findAllByExternalIdContainingAndState(
                domain, state).stream().collect(Collectors.groupingBy(MetadataEnrichment::getRecord));

        return recordEnrichments.values().stream().map(RecordEnrichmentsDTO::fromRecordEnrichments).collect(
                Collectors.toList());
    }

    public RecordEnrichmentsDTO getEnrichmentsForRecord(String recordId,
            MetadataEnrichment.EnrichmentState state) throws
            NotFoundException {
        if (EuropeanaRecordIdValidator.validate(recordId)) {
            return getEnrichmentsByEuropeanaIdentifier(recordId, state);
        }
        return getEnrichmentsByExternalIdentifier(recordId, state);
    }

    private RecordEnrichmentsDTO getEnrichmentsByExternalIdentifier(String externalId,
            MetadataEnrichment.EnrichmentState state) throws
            NotFoundException {
        if (OAIRecordIdValidator.validate(externalId)) {
            RecordEnrichmentsDTO recordEnrichmentsDTO = RecordEnrichmentsDTO.fromRecordEnrichments(
                    metadataEnrichmentRepository.findAllByExternalIdAndState(externalId, state));
            if (recordEnrichmentsDTO.getPlaces().isEmpty() && recordEnrichmentsDTO.getTimespans().isEmpty() &&
                    recordEnrichmentsDTO.getPersons().isEmpty()) {
                recordEnrichmentsDTO.setExternalId(externalId);
            }
            return recordEnrichmentsDTO;
        }
        throw new NotFoundException(externalId);
    }

    private RecordEnrichmentsDTO getEnrichmentsByEuropeanaIdentifier(String recordId,
            MetadataEnrichment.EnrichmentState state) throws
            NotFoundException {
        Optional<Record> optionalRecord = recordsRepository.findByIdentifier(recordId);
        if (optionalRecord.isEmpty()) {
            throw new NotFoundException(recordId);
        }
        RecordEnrichmentsDTO recordEnrichmentsDTO = RecordEnrichmentsDTO.fromRecordEnrichments(
                metadataEnrichmentRepository.findAllByRecordAndState(optionalRecord.get(), state));
        if (recordEnrichmentsDTO.getPlaces().isEmpty() && recordEnrichmentsDTO.getTimespans().isEmpty() &&
                recordEnrichmentsDTO.getPersons().isEmpty()) {
            recordEnrichmentsDTO.setRecordId(recordId);
        }
        return recordEnrichmentsDTO;
    }

    public void validateMetadataEnrichments(
            RecordMetadataEnrichmentValidation recordMetadataEnrichmentValidation) throws
            NotFoundException {
        if (recordMetadataEnrichmentValidation.getRecordId() != null) {
            Optional<Record> optionalRecord = recordsRepository.findByIdentifier(
                    recordMetadataEnrichmentValidation.getRecordId());
            if (optionalRecord.isEmpty()) {
                throw new NotFoundException(recordMetadataEnrichmentValidation.getRecordId());
            }
            acceptMetadataEnrichments(
                    metadataEnrichmentRepository.findAllByRecordAndStateAndIdIn(optionalRecord.get(),
                            MetadataEnrichment.EnrichmentState.PENDING,
                            extractAccepted(recordMetadataEnrichmentValidation)));
            rejectMetadataEnrichments(
                    metadataEnrichmentRepository.findAllByRecordAndStateAndIdIn(optionalRecord.get(),
                            MetadataEnrichment.EnrichmentState.PENDING,
                            extractRejected(recordMetadataEnrichmentValidation)));
        } else {
            acceptMetadataEnrichments(
                    metadataEnrichmentRepository.findAllByExternalIdAndStateAndIdIn(
                            recordMetadataEnrichmentValidation.getExternalId(),
                            MetadataEnrichment.EnrichmentState.PENDING,
                            extractAccepted(recordMetadataEnrichmentValidation)));
            rejectMetadataEnrichments(
                    metadataEnrichmentRepository.findAllByExternalIdAndStateAndIdIn(
                            recordMetadataEnrichmentValidation.getExternalId(),
                            MetadataEnrichment.EnrichmentState.PENDING,
                            extractRejected(recordMetadataEnrichmentValidation)));
        }
    }

    private void acceptMetadataEnrichments(Set<MetadataEnrichment> enrichments) {
        enrichments.forEach(metadataEnrichment -> {
            logger.info("Accepting enrichment {}", metadataEnrichment.getId());
            metadataEnrichment.setState(MetadataEnrichment.EnrichmentState.ACCEPTED);
        });
        metadataEnrichmentRepository.saveAll(enrichments);
    }

    private void rejectMetadataEnrichments(Set<MetadataEnrichment> enrichments) {
        enrichments.forEach(metadataEnrichment -> {
            logger.info("Rejecting enrichment {}", metadataEnrichment.getId());
            metadataEnrichment.setState(MetadataEnrichment.EnrichmentState.REJECTED);
        });
        metadataEnrichmentRepository.saveAll(enrichments);
    }

    private Set<Long> extractAccepted(RecordMetadataEnrichmentValidation recordMetadataEnrichmentValidation) {
        Set<Long> accepted = new HashSet<>();
        if (recordMetadataEnrichmentValidation.getTimespans() != null) {
            accepted.addAll(recordMetadataEnrichmentValidation.getTimespans().getAccept());
        }
        if (recordMetadataEnrichmentValidation.getPlaces() != null) {
            accepted.addAll(recordMetadataEnrichmentValidation.getPlaces().getAccept());
        }
        if (recordMetadataEnrichmentValidation.getPersons() != null) {
            accepted.addAll(recordMetadataEnrichmentValidation.getPersons().getAccept());
        }
        return accepted;
    }

    private Set<Long> extractRejected(RecordMetadataEnrichmentValidation recordMetadataEnrichmentValidation) {
        Set<Long> rejected = new HashSet<>();
        if (recordMetadataEnrichmentValidation.getTimespans() != null) {
            rejected.addAll(recordMetadataEnrichmentValidation.getTimespans().getReject());
        }
        if (recordMetadataEnrichmentValidation.getPlaces() != null) {
            rejected.addAll(recordMetadataEnrichmentValidation.getPlaces().getReject());
        }
        if (recordMetadataEnrichmentValidation.getPersons() != null) {
            rejected.addAll(recordMetadataEnrichmentValidation.getPersons().getReject());
        }
        return rejected;
    }
}

package pl.psnc.dei.service;

import org.apache.jena.atlas.json.JsonObject;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.psnc.dei.exception.NotFoundException;
import pl.psnc.dei.exception.TranscriptionDuplicationException;
import pl.psnc.dei.iiif.Converter;
import pl.psnc.dei.model.DAO.*;
import pl.psnc.dei.model.*;
import pl.psnc.dei.model.enrichments.DateEnrichment;
import pl.psnc.dei.model.enrichments.MetadataEnrichment;
import pl.psnc.dei.model.enrichments.PlaceEnrichment;

import javax.transaction.Transactional;
import java.util.*;

@Service
@Transactional
public class QueueRecordService {

    @Autowired
    private RecordsRepository recordsRepository;

    @Autowired
    private TranscriptionRepository transcriptionRepository;

    @Autowired
    private DateEnrichmentRepository dateEnrichmentRepository;

    @Autowired
    private PlaceEnrichmentRepository placeEnrichmentRepository;

    @Autowired
    private AllMetadataEnrichmentRepository allMetadataEnrichmentRepository;

    @Autowired
    private Converter converter;

    /**
     * Returns records that are not in terminal state due to add when QueueRecordService was down or due to system crash during processing them
     *
     * @return records
     */
    public List<Record> getRecordsToProcess() {
        return recordsRepository.findAllByStateIsNotIn(
                Arrays.asList(Record.RecordState.NORMAL, Record.RecordState.C_FAILED, Record.RecordState.T_SENT,
                        Record.RecordState.T_FAILED));
    }

    /**
     * Bidirectional relation update for record
     *
     * @param recordId id of record to update
     * @param state    new state to save
     * @throws NotFoundException if record was not found
     */
    public void setNewStateForRecord(long recordId, Record.RecordState state) throws NotFoundException {
        Optional<Record> record = recordsRepository.findById(recordId);
        if (record.isPresent()) {
            Record newRecord = record.get();
            newRecord.setState(state);
            recordsRepository.save(newRecord);
        } else {
            throw new NotFoundException("Record not found, id: " + recordId);
        }
    }

    /**
     * return record with given name
     *
     * @param identifier record identifier
     * @return found identifier
     * @throws NotFoundException if none matching record exist
     */
    public Record getRecord(String identifier) throws NotFoundException {
        Optional<Record> record = recordsRepository.findByIdentifier(identifier);
        if (record.isPresent()) {
            Record result = record.get();
            Hibernate.initialize(result.getTranscriptions());
            return result;
        }
        throw new NotFoundException("Record not found, id: " + identifier);
    }

    public void saveRecord(Record record) {
        recordsRepository.save(record);
    }

    public void saveTranscription(Transcription transcription) {
        transcriptionRepository.save(transcription);
    }

    public void saveTranscriptions(Collection<Transcription> transcriptions) {
        this.transcriptionRepository.saveAll(transcriptions);
    }

    /**
     * Deletes all transcriptions
     *
     * @param transcriptions transcriptions to delete
     */
    public void deleteAllTranscriptions(Collection<Transcription> transcriptions) {
        this.transcriptionRepository.deleteAll(transcriptions);
    }

    /**
     * Function adds records but not update them
     *
     * @param transcription transcription to add
     * @return true if transcription was added, otherwise false
     */
    public boolean saveTranscriptionIfNotExist(Transcription transcription) {
        // not all transcriptions have annotationId
        if (transcription.getAnnotationId() != null) {
            if (!transcriptionRepository.existsByTpIdAndAnnotationId(transcription.getTpId(),
                    transcription.getAnnotationId())) {
                this.saveTranscription(transcription);
                return true;
            }
        } else {
            if (!transcriptionRepository.existsByTpId(transcription.getTpId())) {
                this.saveTranscription(transcription);
                return true;
            }
        }
        return false;
    }

    /**
     * Throws error if given record has any transcription. Useful for exception driven validation
     *
     * @param recordIdentifier
     * @throws TranscriptionDuplicationException
     */
    public void throwIfTranscriptionExistFor(String recordIdentifier) throws TranscriptionDuplicationException {
        if (this.transcriptionRepository.existsByRecord_Identifier(recordIdentifier)) {
            throw new TranscriptionDuplicationException(recordIdentifier);
        }
    }

    /**
     * Adds to json information about newly generated IIIF as manifest
     *
     * @param record  record to which data should be add
     * @param json    json to which data shoudl be add
     * @param jsonRaw raw json to which data should be add
     */
    public void fillRecordJsonData(Record record, JsonObject json, JsonObject jsonRaw) {
        converter.fillJsonData(record, json, jsonRaw);
    }

    /**
     * Function adds records but not update them
     *
     * @return true if transcription was added, otherwise false
     */
    public boolean saveMetadataEnrichmentIfNotExist(MetadataEnrichment enrichment) {

        // different repositories are used for different enrichment classes
        if (enrichment instanceof DateEnrichment) {
            return saveDateEnrichmentIfNotExists((DateEnrichment) enrichment);
        }
        if (enrichment instanceof PlaceEnrichment) {
            return savePlaceEnrichmentIfNotExists((PlaceEnrichment) enrichment);
        }
        return false;
    }

    private boolean savePlaceEnrichmentIfNotExists(PlaceEnrichment enrichment) {
        if (!placeEnrichmentRepository.existsByRecordAndLatitudeAndLongitudeAndNameAndItemLink(enrichment.getRecord(),
                enrichment.getLatitude(), enrichment.getLongitude(),
                enrichment.getName(), enrichment.getItemLink())) {
            placeEnrichmentRepository.save(enrichment);
            return true;
        }
        return false;
    }

    private boolean saveDateEnrichmentIfNotExists(DateEnrichment enrichment) {
		if (!dateEnrichmentRepository.existsByRecordAndDateEndAndDateStartAndItemLink(enrichment.getRecord(),
				enrichment.getDateEnd(), enrichment.getDateStart(),
				enrichment.getItemLink())) {
			dateEnrichmentRepository.save(enrichment);
			return true;
		}
		return false;
    }

    public void saveMetadataEnrichments(List<MetadataEnrichment> enrichments) {
        allMetadataEnrichmentRepository.saveAll(enrichments);
    }
}

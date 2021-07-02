package pl.psnc.dei.service;

import org.apache.jena.atlas.json.JsonObject;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.psnc.dei.exception.NotFoundException;
import pl.psnc.dei.iiif.Converter;
import pl.psnc.dei.model.DAO.RecordsRepository;
import pl.psnc.dei.model.DAO.TranscriptionRepository;
import pl.psnc.dei.model.Record;
import pl.psnc.dei.model.Transcription;

import javax.transaction.Transactional;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class QueueRecordService {

	@Autowired
	private RecordsRepository recordsRepository;

	@Autowired
	private TranscriptionRepository transcriptionRepository;

	@Autowired
	private Converter converter;

	/**
	 * Returns records that are not in terminal state due to add when QueueRecordService was down or due to system crash during processing them
	 * @return records 
	 */
	public List<Record> getRecordsToProcess() {
		return recordsRepository.findAllByStateIsNotIn(Arrays.asList(Record.RecordState.NORMAL, Record.RecordState.C_FAILED, Record.RecordState.T_SENT, Record.RecordState.T_FAILED));
	}

	/**
	 * Bidirectional relation update for record
	 * @param recordId id of record to update
	 * @param state new state to save
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

	/**
	 * Adds to json information about newly generated IIIF as manifest
	 * @param record record to which data should be add
	 * @param json json to which data shoudl be add
	 * @param jsonRaw raw json to which data should be add
	 */
	public void fillRecordJsonData(Record record, JsonObject json, JsonObject jsonRaw) {
		converter.fillJsonData(record, json, jsonRaw);
	}
}

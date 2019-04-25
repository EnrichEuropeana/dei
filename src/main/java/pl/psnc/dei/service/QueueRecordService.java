package pl.psnc.dei.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.psnc.dei.exception.NotFoundException;
import pl.psnc.dei.model.DAO.RecordsRepository;
import pl.psnc.dei.model.Record;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class QueueRecordService {

	@Autowired
	private RecordsRepository recordsRepository;

    public List<Record> getRecordsToProcess() {
		return recordsRepository.findAllByStateIsNot(Record.RecordState.NORMAL);
    }

    public void setNewStateForRecord(long recordId, Record.RecordState state) throws NotFoundException {
    	Optional<Record> record = recordsRepository.findById(recordId);
    	if(record.isPresent()) {
    		Record newRecord = record.get();
    		newRecord.setState(state);
    		recordsRepository.save(newRecord);
		} else {
    		throw new NotFoundException("Record not found, id: " + recordId);
		}
	}

}

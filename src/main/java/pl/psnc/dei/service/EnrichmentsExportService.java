package pl.psnc.dei.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.psnc.dei.enrichments.EnrichmentsExport;
import pl.psnc.dei.enrichments.types.Transcription;
import pl.psnc.dei.exception.NotFoundException;
import pl.psnc.dei.model.DAO.RecordsRepository;
import pl.psnc.dei.model.DAO.TranscriptionRepository;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EnrichmentsExportService {

    private final TranscriptionRepository transcriptionRepository;
    private final RecordsRepository recordsRepository;

    @Autowired
    public EnrichmentsExportService(TranscriptionRepository transcriptionRepository, RecordsRepository recordsRepository) {
        this.transcriptionRepository = transcriptionRepository;
        this.recordsRepository = recordsRepository;
    }

    public EnrichmentsExport getEnrichments(String recordId) throws NotFoundException {
        long recordIdLong = Long.parseLong(recordId);
        if (!recordsRepository.existsById(recordIdLong)) {
            throw new NotFoundException("Invalid record ID");
        }
        EnrichmentsExport enrichmentsExport = new EnrichmentsExport();
        List<Transcription> transcriptions = transcriptionRepository.findAllByRecordId(recordIdLong)
                .stream()
                .map(Transcription::fromModelTranscription)
                .collect(Collectors.toList());
        enrichmentsExport.setTranscriptions(transcriptions);
        return enrichmentsExport;
    }

}

package pl.psnc.dei.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.psnc.dei.enrichments.EnrichmentsExport;
import pl.psnc.dei.exception.NotFoundException;
import pl.psnc.dei.service.EnrichmentsExportService;

@RestController
@RequestMapping("/api/enrichments")
public class RecordEnchrichmentsController {

    private final EnrichmentsExportService enrichmentsExportService;

    @Autowired
    public RecordEnchrichmentsController(EnrichmentsExportService enrichmentsExportService) {
        this.enrichmentsExportService = enrichmentsExportService;
    }

    @GetMapping("/{recordId}")
    public ResponseEntity<EnrichmentsExport> getEnrichments(@PathVariable("recordId") String recordId) {
        try {
            EnrichmentsExport enrichments = enrichmentsExportService.getEnrichments(recordId);
            return ResponseEntity.ok(enrichments);
        } catch (NotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

}

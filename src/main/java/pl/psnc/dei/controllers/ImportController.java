package pl.psnc.dei.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.psnc.dei.exception.NotFoundException;
import pl.psnc.dei.model.Import;
import pl.psnc.dei.model.Record;
import pl.psnc.dei.service.ImportPackageService;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ImportController {

    private ImportPackageService importService;

    @Autowired
    public ImportController(ImportPackageService importService) {
        this.importService = importService;
    }

    @PostMapping("/project/{projectId}/import")
    public ResponseEntity<Import> createImport(@PathVariable("projectId") String projectId, @RequestParam(value = "name", required = false) String name, @RequestBody List<Record> records) {
        return new ResponseEntity<>(importService.createImport(name, projectId, records), HttpStatus.OK);
    }

    @GetMapping("/import/candidates/projectId/{projectId}")
    public ResponseEntity<List<Record>> getCandidates(@PathVariable String projectId, @RequestParam(value = "datasetId", required = false) String datasetId) {
        return new ResponseEntity<>(importService.getCandidates(projectId, datasetId), HttpStatus.OK);
    }

    @GetMapping("/import/status/{importName}")
    public ResponseEntity getImportReport(@PathVariable String importName) {
        try {
            return new ResponseEntity<>(importService.getStatusWithFailure(importName), HttpStatus.OK);
        } catch (NotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/import/send/{importName}")
    public ResponseEntity sendImport(@PathVariable String importName) {
        try {
            importService.sendExistingImport(importName);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (NotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}

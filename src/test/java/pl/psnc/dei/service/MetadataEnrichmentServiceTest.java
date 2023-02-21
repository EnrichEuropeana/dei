package pl.psnc.dei.service;

import org.custommonkey.xmlunit.NodeTestException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import pl.psnc.dei.exception.NotFoundException;
import pl.psnc.dei.model.DAO.AllMetadataEnrichmentRepository;
import pl.psnc.dei.model.DAO.RecordsRepository;
import pl.psnc.dei.model.Record;
import pl.psnc.dei.model.enrichments.DTO.RecordEnrichmentsDTO;
import pl.psnc.dei.model.enrichments.DateEnrichment;
import pl.psnc.dei.model.enrichments.MetadataEnrichment;
import pl.psnc.dei.model.enrichments.PlaceEnrichment;
import pl.psnc.dei.model.exception.TranscriptionPlatformException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@ContextConfiguration
public class MetadataEnrichmentServiceTest {

    private Record record;

    @InjectMocks
    private MetadataEnrichmentService metadataEnrichmentService;

    @Mock
    private RecordsRepository recordsRepository;

    @Mock
    private AllMetadataEnrichmentRepository metadataEnrichmentRepository;

    public void setUp(String recordId) {
        record = new Record(recordId);

        DateEnrichment dateEnrichment = new DateEnrichment();
        dateEnrichment.setAttribute("dc:date");
        dateEnrichment.setState(MetadataEnrichment.EnrichmentState.PENDING);
        dateEnrichment.setRecord(record);
        dateEnrichment.setId(1L);
        dateEnrichment.setItemLink("Item link");
        dateEnrichment.setDateStart(LocalDateTime.of(1914, 10, 10, 10, 0, 0).atZone(ZoneId.of("UTC")).toInstant());
        dateEnrichment.setDateEnd(LocalDateTime.of(1914, 10, 10, 10, 0, 0).atZone(ZoneId.of("UTC")).toInstant());

        DateEnrichment recordDateEnrichment = new DateEnrichment();
        recordDateEnrichment.setAttribute("dc:date");
        recordDateEnrichment.setState(MetadataEnrichment.EnrichmentState.PENDING);
        recordDateEnrichment.setRecord(record);
        recordDateEnrichment.setId(2L);
        recordDateEnrichment.setDateStart(LocalDateTime.of(1910, 1, 31, 10, 0, 0).atZone(ZoneId.of("UTC")).toInstant());
        recordDateEnrichment.setDateEnd(LocalDateTime.of(1914, 11, 1, 10, 0, 0).atZone(ZoneId.of("UTC")).toInstant());

        PlaceEnrichment placeEnrichment = new PlaceEnrichment();
        placeEnrichment.setAttribute("dcterms:spatial");
        placeEnrichment.setState(MetadataEnrichment.EnrichmentState.PENDING);
        placeEnrichment.setRecord(record);
        placeEnrichment.setId(3L);
        placeEnrichment.setZoom(10);
        placeEnrichment.setLanguage("pl");
        placeEnrichment.setLatitude(40.567);
        placeEnrichment.setLongitude(13.45);
        placeEnrichment.setName("Nakłość");
        placeEnrichment.setItemLink("Item link");
        placeEnrichment.setWikidataId("Q234");

        Set<MetadataEnrichment> enrichments = Set.of(dateEnrichment, recordDateEnrichment, placeEnrichment);
        when(metadataEnrichmentRepository.findAllByExternalIdContainingAndState("europeana1989.eu",
                MetadataEnrichment.EnrichmentState.PENDING)).thenReturn(enrichments);
        when(recordsRepository.findByIdentifier("/12345/xyz345")).thenReturn(Optional.of(record));
        when(metadataEnrichmentRepository.findAllByRecordAndState(record,
                MetadataEnrichment.EnrichmentState.PENDING)).thenReturn(enrichments);
    }

    @Test
    public void shouldCreateRecordEnrichmentsDTO() throws TranscriptionPlatformException, NotFoundException {
        setUp("oai:europeana1989.eu:390");
        when(metadataEnrichmentRepository.existsByExternalIdContaining("europeana1989.eu")).thenReturn(true);

        List<RecordEnrichmentsDTO> dtos = metadataEnrichmentService.getEnrichmentsForDomain("europeana1989.eu",
                MetadataEnrichment.EnrichmentState.PENDING);

        Assert.assertEquals(1, dtos.size());
        Assert.assertEquals(record.getIdentifier(), dtos.get(0).getRecordId());
        Assert.assertEquals(2, dtos.get(0).getTimespans().size());
        Assert.assertEquals(1, dtos.get(0).getPlaces().size());
    }

    @Test(expected = NotFoundException.class)
    public void shouldNotCreateRecordEnrichmentsDTO() throws NotFoundException {
        when(metadataEnrichmentRepository.existsByExternalIdContaining("europeana1989.eu")).thenReturn(false);

        metadataEnrichmentService.getEnrichmentsForDomain("europeana1989.eu",
                MetadataEnrichment.EnrichmentState.PENDING);

        Assert.fail();
    }

    @Test
    public void shouldCreateRecordEnrichmentDTO() throws TranscriptionPlatformException, NotFoundException {
        setUp("/12345/xyz345");
        RecordEnrichmentsDTO dto = metadataEnrichmentService.getEnrichmentsForRecord("/12345/xyz345",
                MetadataEnrichment.EnrichmentState.PENDING);

        Assert.assertNotNull(dto);
        Assert.assertEquals(record.getIdentifier(), dto.getRecordId());
        Assert.assertEquals(2, dto.getTimespans().size());
        Assert.assertEquals(1, dto.getPlaces().size());
    }

    @Test(expected = NotFoundException.class)
    public void shouldNotCreateRecordEnrichmentDTO() throws NotFoundException {
        when(recordsRepository.findByIdentifier("/12345/abc345")).thenReturn(Optional.empty());

        metadataEnrichmentService.getEnrichmentsForRecord("/12345/abc345",
                MetadataEnrichment.EnrichmentState.PENDING);

        Assert.fail();
    }
}

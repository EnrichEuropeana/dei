package pl.psnc.dei.service;

import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import pl.psnc.dei.iiif.ConversionDataHolder;
import pl.psnc.dei.model.DAO.IIIFMappingsRepository;
import pl.psnc.dei.model.IIIFMapping;
import pl.psnc.dei.model.Record;

import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)

public class IIIFMappingServiceTest {

    private static final String IIIF_IMAGE_SERVER_URL = "https://server.iiif.com/";

    private static final List<String> IMAGE_PATHS_PDF = Arrays.asList(
            "11/22//33333/some_sample_europeana_record_id/converted_pdf_file_Page0000.tif",
            "11/22//33333/some_sample_europeana_record_id/converted_pdf_file_Page0001.tif",
            "11/22//33333/some_sample_europeana_record_id/converted_pdf_file_Page0002.tif",
            "11/22//33333/some_sample_europeana_record_id/converted_pdf_file_Page0003.tif",
            "11/22//33333/some_sample_europeana_record_id/converted_pdf_file_Page0004.tif",
            "11/22//33333/some_sample_europeana_record_id/converted_pdf_file_Page0005.tif"
    );
    private static final String PDF_SOURCE_URL = "https://resources-server.example.com/file.pdf";

    private static final String IMG_SOURCE_URL_FORMAT = "https://resources-server.example.com/image_%d.jpg";
    private static final String IMG_CONVERTED_NAME_FORMAT =
            "11/22//33333/some_sample_europeana_record_id/converted_img_file_%d.tif";

    private IIIFMappingService iiifMappingService;

    @Mock
    private IIIFMappingsRepository iiifMappingsRepository;

    @Before
    public void setupTests() {
        MockitoAnnotations.initMocks(this);
        iiifMappingService = new IIIFMappingService(iiifMappingsRepository, IIIF_IMAGE_SERVER_URL);
        when(iiifMappingsRepository.saveAll(anyIterable())).thenAnswer(i -> i.getArguments()[0]);
    }

    @Test
    public void shouldSaveMappings() {
        List<ConversionDataHolder.ConversionData> pdfConversionDataHolders = preparePDFConversionDataHolders();
        List<ConversionDataHolder.ConversionData> imgConversionDataHolders = prepareImgConversionDataHolders();
        List<ConversionDataHolder.ConversionData> dataHolders = new ArrayList<>();
        dataHolders.addAll(pdfConversionDataHolders);
        dataHolders.addAll(imgConversionDataHolders);
        ArgumentCaptor<List<IIIFMapping>> captor = ArgumentCaptor.forClass(List.class);

        iiifMappingService.saveMappings(prepareRecord(), dataHolders);
        verify(iiifMappingsRepository, times(1)).saveAll(captor.capture());

        long expectedMappedSavings = dataHolders.stream()
                .mapToLong(conversionData -> conversionData.getImagePath().size())
                .sum();

        List<IIIFMapping> mappingsToSave = captor.getValue();
        assertEquals(expectedMappedSavings, mappingsToSave.size());
    }

    @Test
    public void addPageToPdfSourceLink() {
        List<ConversionDataHolder.ConversionData> pdfConversionDataHolders = preparePDFConversionDataHolders();
        List<ConversionDataHolder.ConversionData> imgConversionDataHolders = prepareImgConversionDataHolders();
        List<ConversionDataHolder.ConversionData> dataHolders = new ArrayList<>();
        dataHolders.addAll(pdfConversionDataHolders);
        dataHolders.addAll(imgConversionDataHolders);
        ArgumentCaptor<List<IIIFMapping>> captor = ArgumentCaptor.forClass(List.class);

        iiifMappingService.saveMappings(prepareRecord(), dataHolders);
        verify(iiifMappingsRepository, times(1)).saveAll(captor.capture());

        List<IIIFMapping> mappingsToSave = captor.getValue();
        Pattern pagesPattern = Pattern.compile("#page=(\\d*)");
        mappingsToSave.forEach(iiifMapping -> {
            String sourceUrl = iiifMapping.getSourceUrl();
            Matcher matcher = pagesPattern.matcher(sourceUrl);
            if (sourceUrl.contains(".pdf")) {
                assertTrue(matcher.find());
            } else {
                assertFalse(matcher.find());
            }
        });
    }

    @Test
    public void shouldReturnSavedInMappingSourceLink_whenMappingFound() {
        String sourceLinkFromTp = String.format(IMG_SOURCE_URL_FORMAT, 1);
        String sourceLinkSaved = String.format(IMG_SOURCE_URL_FORMAT, 2);
        doReturn(preapreMapping(String.format(IMG_CONVERTED_NAME_FORMAT, 1), sourceLinkSaved,1))
                .when(iiifMappingsRepository).findIIIFMappingByRecordIdentifierAndOrderIndex(anyString(), anyInt());
        String link = iiifMappingService.getSourceLink(prepareRecord(), 0, sourceLinkFromTp);
        assertEquals(sourceLinkSaved, link);
    }

    @Test
    public void shouldReturnTpSourceLink_whenMappingNotFound() {
        when(iiifMappingsRepository.findIIIFMappingByRecordIdentifierAndOrderIndex(anyString(), anyInt()))
                .thenAnswer(i -> Optional.empty());
        String iiifSourceLink = "https://source-link.example.com/img.jpg";
        String foundLink = iiifMappingService.getSourceLink(prepareRecord(), 0, iiifSourceLink);
        assertEquals(iiifSourceLink, foundLink);
    }

    @SneakyThrows
    private List<ConversionDataHolder.ConversionData> preparePDFConversionDataHolders() {
        ConversionDataHolder.ConversionData conversionData = new ConversionDataHolder.ConversionData();
        conversionData.setSrcFileUrl(new URL(PDF_SOURCE_URL));
        conversionData.setMediaType("pdf");
        conversionData.setImagePath(IMAGE_PATHS_PDF);
        return Collections.singletonList(conversionData);
    }

    private List<ConversionDataHolder.ConversionData> prepareImgConversionDataHolders() {
        return IntStream.range(1, 5)
                .mapToObj(this::prepareImgConversionDataHolder)
                .collect(Collectors.toList());
    }

    @SneakyThrows
    private ConversionDataHolder.ConversionData prepareImgConversionDataHolder(int number) {
        ConversionDataHolder.ConversionData conversionData = new ConversionDataHolder.ConversionData();
        conversionData.setSrcFileUrl(new URL(String.format(IMG_SOURCE_URL_FORMAT, number)));
        conversionData.setMediaType("jpg");
        conversionData.setImagePath(
                Collections.singletonList(String.format(IMG_CONVERTED_NAME_FORMAT, number))
        );
        return conversionData;
    }

    private Record prepareRecord() {
        Record record = new Record();
        record.setIdentifier("11/22//33/record");
        return record;
    }

    private Optional<IIIFMapping> preapreMapping(String iiifResourceUrl, String sourceUrl, int orderIndex) {
        IIIFMapping mapping = new IIIFMapping();
        mapping.setRecord(prepareRecord());
        mapping.setIiifResourceUrl(iiifResourceUrl);
        mapping.setSourceUrl(sourceUrl);
        mapping.setOrderIndex(orderIndex);
        return Optional.of(mapping);
    }
}
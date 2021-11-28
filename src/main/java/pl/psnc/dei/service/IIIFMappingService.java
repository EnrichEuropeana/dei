package pl.psnc.dei.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pl.psnc.dei.iiif.ConversionDataHolder;
import pl.psnc.dei.model.DAO.IIIFMappingsRepository;
import pl.psnc.dei.model.IIIFMapping;
import pl.psnc.dei.model.Record;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class IIIFMappingService {

    private final static Pattern PAGES_PATTERN = Pattern.compile("_Page(\\d{4})");
    private static final String PAGES_FORMAT = "#page=%d";
    private final IIIFMappingsRepository mappingsRepository;
    private final String iiifImageServerUrl;

    @Autowired
    public IIIFMappingService(IIIFMappingsRepository mappingsRepository,
                              @Value("${conversion.iiif.server.url}") String iiifImageServerUrl) {
        this.mappingsRepository = mappingsRepository;
        this.iiifImageServerUrl = iiifImageServerUrl;
    }

    public String getResourceImagePath(String imagePath) {
        return iiifImageServerUrl +
                "/fcgi-bin/iipsrv.fcgi?IIIF=" +
                imagePath +
                "/full/full/0/default.jpg";
    }

    public void saveMappings(Record record, List<ConversionDataHolder.ConversionData> convertedFiles) {
        AtomicInteger counter = new AtomicInteger(1);
        List<IIIFMapping> mappings = new ArrayList<>();
        convertedFiles.forEach(conversionData -> {
            conversionData.getImagePath().forEach(imagePath -> {
                IIIFMapping mapping = new IIIFMapping();
                mapping.setRecord(record);
                mapping.setOrderIndex(counter.getAndIncrement());
                mapping.setIiifResourceUrl(getResourceImagePath(imagePath));
                mapping.setSourceUrl(buildSourceUrl(imagePath, conversionData.getSrcFileUrl().toString(), conversionData.getMediaType()));
                mappings.add(mapping);
            });
        });
        mappingsRepository.saveAll(mappings);
    }

    private String buildSourceUrl(String imagePath, String sourceUrl, String mimetype) {
        Matcher matcher = PAGES_PATTERN.matcher(imagePath);
        if (mimetype.equalsIgnoreCase("pdf") && matcher.find()) {
            sourceUrl += String.format(PAGES_FORMAT, Integer.parseInt(matcher.group(1)) + 1);
        }
        return sourceUrl;
    }

    public String getSourceLink(Record record, int orderIndex, String tpSourceLinkProposal) {
        Optional<IIIFMapping> mappingOptional =
                mappingsRepository.findIIIFMappingByRecordIdentifierAndOrderIndex(record.getIdentifier(), orderIndex);
        if (mappingOptional.isEmpty()) {
            log.error("Mapping for RecordId={} and OrderIndex={} not found!", record.getIdentifier(), orderIndex);
            return tpSourceLinkProposal;
        }
        String mappingSourceUrl = mappingOptional.get().getSourceUrl();
        if (!mappingSourceUrl.equals(tpSourceLinkProposal)) {
            log.warn("Source link from TP does not match internal mapping.");
        }
        return mappingSourceUrl;
    }
}

package pl.psnc.dei.iiif;

import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonValue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.test.context.junit4.SpringRunner;
import pl.psnc.dei.model.Aggregator;
import pl.psnc.dei.model.Record;
import pl.psnc.dei.model.conversion.ConversionData;
import pl.psnc.dei.model.conversion.ConversionTaskContext;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
public class ConversionDataHolderTransformerTest {
    private final String RECORD_IDENTIFIER = "/11602/HERBARWUXWUXAUSTRIAX102495";

    @Value("classpath:iiif/europeana_record.json")
    private Resource europeanaRecordResource;

    @Value("classpath:iiif/europeana_record_raw.json")
    private Resource europeanaRecordRawResource;

    private JsonObject europeanaRecordJson;
    private JsonObject europeanaRecordJsonRaw;

    private JsonObject europeanaAggregatorData;

    // this should be filled if integration with DDB will be implemented
    // so far there is no way to obtain this data from programme runtime
//    private final JsonObject ddbRecordJson = null;
//
//    private final JsonObject ddbAggregatorData = this.DDB_RECORD_JSON;

    private ConversionDataHolderTransformer conversionDataHolderTransformer;

    @Before
    @SneakyThrows
    public void prepareEuropeanaData() {
        String europeanaRecordResourceString =
                String.join("",
                        IOUtils.readLines(
                                this.europeanaRecordResource.getInputStream(),
                                StandardCharsets.UTF_8
                        )
                );
        this.europeanaRecordJson = JSON.parse(europeanaRecordResourceString);

        String europeanaRecordRawResourceString =
                String.join("",
                        IOUtils.readLines(
                                this.europeanaRecordRawResource.getInputStream(),
                                StandardCharsets.UTF_8
                        )
                );
        this.europeanaRecordJsonRaw = JSON.parse(europeanaRecordRawResourceString);

        this.europeanaAggregatorData = this.europeanaRecordJson.get("@graph").getAsArray().stream()
                .map(JsonValue::getAsObject)
                .filter(e -> e.get("edm:isShownBy") != null)
                .findFirst().get();
    }

    @Before
    public void initConversionDataHolderTransformer() {
        this.conversionDataHolderTransformer = new ConversionDataHolderTransformer();
    }

    @SneakyThrows
    @Test
    public void ifPresentedContextWithEuropeanaAggregatorSet_willReturnEuropeanaDH() {
        Record record = new Record();
        record.setAggregator(Aggregator.EUROPEANA);
        ConversionTaskContext sampleContext = new ConversionTaskContext();
        sampleContext.setRecordJson(this.europeanaRecordJson.toString());
        sampleContext.setRecordJsonRaw(this.europeanaRecordJsonRaw.toString());
        sampleContext.setRawConversionData(new ArrayList<>());
        sampleContext.setRecord(record);

        ConversionDataHolder conversionDataHolder = this.conversionDataHolderTransformer.toConversionDataHolder(sampleContext);
        assertEquals(conversionDataHolder.getClass(), EuropeanaConversionDataHolder.class);
    }

    @SneakyThrows
    @Test
    public void ifPresentedEuropeanaDH_willReturnCorrectConversionData() {
        File exampleFile = null;
        try {
            exampleFile = File.createTempFile("europeanaDataHolderSrcFileTest-", ".txt");
            EuropeanaConversionDataHolder dataHolder = new EuropeanaConversionDataHolder(this.RECORD_IDENTIFIER, this.europeanaAggregatorData, this.europeanaRecordJson, this.europeanaRecordJsonRaw);
            for (ConversionDataHolder.ConversionData el : dataHolder.fileObjects) {
                el.srcFile = exampleFile;
            }
            List<ConversionData> converted = this.conversionDataHolderTransformer.toDBModel(dataHolder);
            exampleFile.delete();
            assertNotNull(converted);
            assertEquals("jpeg", converted.get(0).getMediaType());
        } catch (Exception e) {
            if (exampleFile != null) exampleFile.delete();
            throw e;
        }
    }

//    uncomment if integration with DDB will be implemented
//    remember to fill DDB_RECORD_JSON before run
//    @SneakyThrows
//    @Test
//    public void IfPresentedContextWithDDBAggregatorSet_willReturnDDBDH() {
//        Record record = new Record();
//        record.setAggregator(Aggregator.DDB);
//        ConversionTaskContext sampleContext = new ConversionTaskContext();
//        sampleContext.setRecordJson(this.DDB_RECORD_JSON.toString());
//        sampleContext.setRecordJsonRaw(this.DDB_RECORD_JSON_RAW.toString());
//        sampleContext.setRawConversionData(new ArrayList<>());
//        sampleContext.setRecord(record);
//
//        ConversionDataHolder conversionDataHolder = this.conversionDataHolderTransformer.toConversionDataHolder(sampleContext);
//        assertEquals(conversionDataHolder.getClass(), DDBConversionDataHolder.class);
//    }

//    uncomment if integration with DDB will be implemented
//    remember to fill DDB_RECORD_JSON before run
//    @SneakyThrows
//    @Test
//    public void ifPresentedDDBDH_willReturnCorrectConversionData() {
//        File exampleFile = null;
//        try {
//            exampleFile = File.createTempFile("DDBDataHolderSrcFileTest-", ".txt");
//            DDBConversionDataHolder dataHolder = new DDBConversionDataHolder(this.RECORD_IDENTIFIER, this.DDB_AGGREGATOR_DATA);
//            for (ConversionDataHolder.ConversionData el : dataHolder.fileObjects) {
//                el.srcFile = exampleFile;
//            }
//            List<ConversionData> converted = this.conversionDataHolderTransformer.toDBModel(dataHolder);
//            exampleFile.delete();
//            assertNotNull(converted);
//            assertEquals("jpeg", converted.get(0).getMediaType());
//        } catch (Exception e) {
//            if (exampleFile != null) exampleFile.delete();
//            throw e;
//        }
//    }
}

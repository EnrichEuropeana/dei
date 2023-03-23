package pl.psnc.dei.util;

import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.atlas.json.JsonBuilder;
import org.apache.jena.atlas.json.JsonObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import pl.psnc.dei.model.Record;
import pl.psnc.dei.model.factory.HTRTranscriptionFactory;
import pl.psnc.dei.model.factory.ManualTranscriptionFactory;
import pl.psnc.dei.service.IIIFMappingService;
import pl.psnc.dei.service.TranscriptionPlatformService;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static pl.psnc.dei.util.AnnotationFieldsNames.*;
import static pl.psnc.dei.util.EuropeanaConstants.EUROPEANA_ITEM_URL;

@RunWith(SpringJUnit4ClassRunner.class)
public class TranscriptionConverterTest {

    private TranscriptionConverter transcriptionConverter;

    @MockBean
    private IIIFMappingService mappingService;

    @MockBean
    private TranscriptionPlatformService transcriptionPlatformService;

    @MockBean
    private ManualTranscriptionFactory manualTranscriptionFactory;

    @MockBean
    private HTRTranscriptionFactory htrTranscriptionFactory;

    @Captor
    ArgumentCaptor<String> imageLinkCaptor = ArgumentCaptor.forClass(String.class);;

    @Before
    public void setupMocks() {
        MockitoAnnotations.initMocks(this);
        transcriptionConverter = new TranscriptionConverter(mappingService, transcriptionPlatformService);
        when(mappingService.getSourceLink(any(Record.class), anyInt(), anyString()))
                .thenAnswer(i -> i.getArguments()[2]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionForNonExistingTranscription() {
        transcriptionConverter.convert(new Record(), null, manualTranscriptionFactory);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionForEmptyTranscription() {
        transcriptionConverter.convert(new Record(), new JsonObject(), manualTranscriptionFactory);
        Assert.fail();
    }

    @Test
    public void shouldProperlyPrepareAnnotation() {
        JsonObject transformation = new JsonObject();
        transformation.put(TranscriptionFieldsNames.TEXT_NO_TAGS, "sample text");

        transformation.put(TranscriptionFieldsNames.MOTIVATION, " sample motivation");
        transformation.put(TranscriptionFieldsNames.IMAGE_LINK, "sample item id");
        transformation.put(TranscriptionFieldsNames.STORY_ID, "/73636/story_id");
        transformation.put(TranscriptionFieldsNames.ORDER_INDEX, 1);
        transformation.put(TranscriptionFieldsNames.LANGUAGES, new JsonArray());
        transformation.get(TranscriptionFieldsNames.LANGUAGES).getAsArray().add(new JsonObject());
        transformation.get(TranscriptionFieldsNames.LANGUAGES).getAsArray().get(0).getAsObject().put("Name", "Polski");
        transformation.get(TranscriptionFieldsNames.LANGUAGES).getAsArray().get(0).getAsObject().put("Code", "pl");

        //
        JsonObject result = transcriptionConverter.convert(new Record(), transformation, manualTranscriptionFactory);
        //

        Assert.assertEquals("pl", result.get(BODY).getAsObject().get(BODY_LANGUAGE).getAsString().value());
        Assert.assertEquals("text/plain", result.get(BODY).getAsObject().get(BODY_FORMAT).getAsString().value());
        Assert.assertEquals("sample text", result.get(BODY).getAsObject().get(BODY_VALUE).getAsString().value());


        Assert.assertTrue(result.get(TARGET).getAsObject().get(TARGET_SCOPE).getAsString().value().endsWith("/73636/story_id"));
        Assert.assertEquals("sample item id", result.get(TARGET).getAsObject().get(TARGET_SOURCE).getAsString().value());
    }

    @Test
    public void shouldConvertRealTranscriptionToAnnotation() {
        JsonBuilder jsonBuilder = new JsonBuilder();
        JsonObject transcription = jsonBuilder
                .startObject()
                .pair("EuropeanaAnnotationId", 0)
                .pair("AnnotationId", 507)
                .pair("Text", "<p class=\"center\"><span class=\"bold\">B&Uuml;NDNIS&nbsp; 90</span></p>\n<p class=\"center\">B&uuml;rger f&uuml;r B&uuml;rger</p>\n<p class=\"center\">Initiative Frieden<br />und&nbsp; Menschenrechte</p>")
                .pair("TextNoTags", "BÜNDNIS  90\n\n\nBürger für Bürger\n\n\nInitiative Frieden\nund  Menschenrechte\n")
                .pair("Timestamp", "Oct 14, 2019 2:25:50 PM")
                .pair("X_Coord", 0.0)
                .pair("Y_Coord", 0.0)
                .pair("Width", 0.0)
                .pair("Height", 0.0)
                .pair("Motivation", "transcribing")
                .pair("OrderIndex", 2)
                .pair("TranscribathonItemId", 435038)
                .pair("TranscribathonStoryId", 12856)
                .pair("StoryUrl", "https://www.europeana.eu/portal/record/135/_nnVvTdx.html")
                .pair("StoryId", "/135/_nnVvTdx")
                .pair("ImageLink", "rhus-209.man.poznan.pl/fcgi-bin/iipsrv.fcgi?IIIF=1//135/_nnVvTdx/2_DSC_0214_crop_web.tif/full/full/0/default.jpg")
                .key("Languages").startArray().startObject().pair("Name", "Deutsch").pair("Code", "de").finishObject().finishArray()
                .finishObject()
                .build().getAsObject();

        JsonObject converted = transcriptionConverter.convert(new Record(), transcription, manualTranscriptionFactory);
        Assert.assertNotNull(converted);
        Assert.assertEquals("transcribing", converted.get(MOTIVATION).getAsString().value());
        Assert.assertEquals("FullTextResource", converted.get(BODY).getAsObject().get(BODY_TYPE).getAsString().value());
        Assert.assertEquals("de", converted.get(BODY).getAsObject().get(BODY_LANGUAGE).getAsString().value());
        Assert.assertEquals("BÜNDNIS  90\n\n\nBürger für Bürger\n\n\nInitiative Frieden\nund  Menschenrechte\n", converted.get(BODY).getAsObject().get(BODY_VALUE).getAsString().value());
        Assert.assertEquals(EUROPEANA_ITEM_URL + "/135/_nnVvTdx", converted.get(TARGET).getAsObject().get(TARGET_SCOPE).getAsString().value());
        Assert.assertEquals("rhus-209.man.poznan.pl/fcgi-bin/iipsrv.fcgi?IIIF=1//135/_nnVvTdx/2_DSC_0214_crop_web.tif/full/full/0/default.jpg", converted.get(TARGET).getAsObject().get(TARGET_SOURCE).getAsString().value());
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenNoText() {
        JsonBuilder jsonBuilder = new JsonBuilder();
        JsonObject transcription = jsonBuilder
                .startObject()
                .pair("EuropeanaAnnotationId", 0)
                .pair("AnnotationId", 507)
                .pair("Text", "")
                .pair("TextNoTags", "")
                .pair("Timestamp", "Oct 14, 2019 2:25:50 PM")
                .pair("X_Coord", 0.0)
                .pair("Y_Coord", 0.0)
                .pair("Width", 0.0)
                .pair("Height", 0.0)
                .pair("Motivation", "transcribing")
                .pair("OrderIndex", 2)
                .pair("TranscribathonItemId", 435038)
                .pair("TranscribathonStoryId", 12856)
                .pair("StoryUrl", "https://www.europeana.eu/portal/record/135/_nnVvTdx.html")
                .pair("StoryId", "/135/_nnVvTdx")
                .pair("ImageLink", "rhus-209.man.poznan.pl/fcgi-bin/iipsrv.fcgi?IIIF=1//135/_nnVvTdx/2_DSC_0214_crop_web.tif/full/full/0/default.jpg")
                .key("Languages").startArray().startObject().pair("Name", "Deutsch").pair("Code", "de").finishObject().finishArray()
                .finishObject()
                .build().getAsObject();

        transcriptionConverter.convert(new Record(), transcription, manualTranscriptionFactory);
        Assert.fail();
    }
}
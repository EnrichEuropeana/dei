package pl.psnc.dei.util;

import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.atlas.json.JsonBuilder;
import org.apache.jena.atlas.json.JsonObject;
import org.junit.Assert;
import org.junit.Test;

import static pl.psnc.dei.util.AnnotationFieldsNames.*;
import static pl.psnc.dei.util.EuropeanaConstants.EUROPEANA_ITEM_URL;

public class TranscriptionConverterTest {

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionForNonExistingTranscription() {
        TranscriptionConverter.convert(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionForEmptyTranscription() {
        TranscriptionConverter.convert(new JsonObject());
        Assert.fail();
    }

    @Test
    public void shouldProperlyPrepareAnnotation() {
        JsonObject transformation = new JsonObject();
        transformation.put(TranscriptionFieldsNames.TEXT_NO_TAGS, "sample text");

        transformation.put(TranscriptionFieldsNames.MOTIVATION, " sample motivation");
        transformation.put(TranscriptionFieldsNames.IMAGE_LINK, "sample item id");
        transformation.put(TranscriptionFieldsNames.STORY_ID, "/73636/story_id");
        transformation.put(TranscriptionFieldsNames.LANGUAGES, new JsonArray());
        transformation.get(TranscriptionFieldsNames.LANGUAGES).getAsArray().add(new JsonObject());
        transformation.get(TranscriptionFieldsNames.LANGUAGES).getAsArray().get(0).getAsObject().put("Name", "Polski");
        transformation.get(TranscriptionFieldsNames.LANGUAGES).getAsArray().get(0).getAsObject().put("Code", "pl");

        //
        JsonObject result = TranscriptionConverter.convert(transformation);
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

        JsonObject converted = TranscriptionConverter.convert(transcription);
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

        TranscriptionConverter.convert(transcription);
        Assert.fail();
    }
}
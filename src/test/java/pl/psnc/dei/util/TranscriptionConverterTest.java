package pl.psnc.dei.util;

import org.apache.jena.atlas.json.JsonObject;
import org.junit.Assert;
import org.junit.Test;

import static pl.psnc.dei.util.AnnotationFieldsNames.*;

public class TranscriptionConverterTest {

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionForNonExistingTranscription() {
        TranscriptionConverter.convert(null);
    }

    @Test
    public void shouldThrowExceptionForEmptyTranscription() {
        JsonObject result = TranscriptionConverter.convert(new JsonObject());
        Assert.assertNull(result.get(MOTIVATION));
        Assert.assertEquals("sample", result.get(GENERATOR).getAsObject().get(GENERATOR_TYPE).getAsString().value());
        Assert.assertEquals("sample", result.get(GENERATOR).getAsObject().get(GENERATOR_HOMEPAGE).getAsString().value());
        Assert.assertEquals("sample", result.get(GENERATOR).getAsObject().get(GENERATOR_NAME).getAsString().value());
        Assert.assertNull(result.get(BODY).getAsObject().get(BODY_ID));
        Assert.assertEquals("text/html", result.get(BODY).getAsObject().get(BODY_FORMAT).getAsString().value());
        Assert.assertEquals("pl", result.get(BODY).getAsObject().get(BODY_LANGUAGE).getAsString().value());
        Assert.assertNull(result.get(TARGET).getAsObject().get(TARGET_SCOPE));
        Assert.assertNull(result.get(TARGET).getAsObject().get(TARGET_SOURCE));
    }

    @Test
    public void shouldProperlyPrepareAnnotation() {
        JsonObject transformation = new JsonObject();
        transformation.put(TranscriptionFieldsNames.TEXT, "sample text");

        transformation.put(TranscriptionFieldsNames.MOTIVATION, " sample motivation");
        transformation.put(TranscriptionFieldsNames.ITEM_ID, "sample item id");
        transformation.put(TranscriptionFieldsNames.STORY_ID, "sample story id");
        //
        JsonObject result = TranscriptionConverter.convert(transformation);
        //
        Assert.assertEquals("sample", result.get(GENERATOR).getAsObject().get(GENERATOR_NAME).getAsString().value());
        Assert.assertEquals("sample", result.get(GENERATOR).getAsObject().get(GENERATOR_HOMEPAGE).getAsString().value());
        Assert.assertEquals("sample", result.get(GENERATOR).getAsObject().get(GENERATOR_TYPE).getAsString().value());

        Assert.assertEquals("pl", result.get(BODY).getAsObject().get(BODY_LANGUAGE).getAsString().value());
        Assert.assertEquals("text/html", result.get(BODY).getAsObject().get(BODY_FORMAT).getAsString().value());
        Assert.assertEquals("sample text", result.get(BODY).getAsObject().get(BODY_ID).getAsString().value());


        Assert.assertEquals("sample story id", result.get(TARGET).getAsObject().get(TARGET_SCOPE).getAsString().value());
        Assert.assertEquals("sample item id", result.get(TARGET).getAsObject().get(TARGET_SOURCE).getAsString().value());
    }
}
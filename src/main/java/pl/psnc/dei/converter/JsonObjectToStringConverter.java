package pl.psnc.dei.converter;

import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonObject;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
public class JsonObjectToStringConverter implements AttributeConverter<JsonObject, String> {
    @Override
    public String convertToDatabaseColumn(JsonObject jsonObject) {
        return jsonObject.toString();
    }

    @Override
    public JsonObject convertToEntityAttribute(String s) {
        return s == null ? new JsonObject() : JSON.parse(s);
    }
}

package pl.psnc.dei.model.converter;

import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonObject;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
public class JsonObjectToStringConverter implements AttributeConverter<String, JsonObject> {
    @Override
    public JsonObject convertToDatabaseColumn(String s) {
        return JSON.parse(s);
    }

    @Override
    public String convertToEntityAttribute(JsonObject jsonObject) {
        return jsonObject.toString();
    }
}

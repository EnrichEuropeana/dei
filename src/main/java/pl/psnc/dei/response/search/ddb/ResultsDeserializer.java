package pl.psnc.dei.response.search.ddb;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class ResultsDeserializer extends StdDeserializer<List<DDBItem>> {

	protected ResultsDeserializer() {
		this(null);
	}

	protected ResultsDeserializer(Class<?> vc) {
		super(vc);
	}

	@Override
	public List<DDBItem> deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
		JsonNode productNode = jsonParser.getCodec().readTree(jsonParser);
		ObjectMapper jsonObjectMapper = new ObjectMapper();
		jsonObjectMapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
		JsonNode ddbItem = productNode.get(0).get("docs");
		return Arrays.asList(jsonObjectMapper.treeToValue(ddbItem, DDBItem[].class));
	}
}


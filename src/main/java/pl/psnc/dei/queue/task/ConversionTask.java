package pl.psnc.dei.queue.task;

import org.apache.jena.atlas.json.JsonObject;
import pl.psnc.dei.iiif.Converter;
import pl.psnc.dei.model.Record;

public class ConversionTask extends Task {

	private JsonObject recordJson;

	public ConversionTask(Record record, JsonObject recordJson) {
		super(record);
		this.recordJson = recordJson;
	}

	@Override
	public void process() {
		Converter converter = new Converter(record, recordJson);
//		converter.convertAndGenerateManifest();
	}
}

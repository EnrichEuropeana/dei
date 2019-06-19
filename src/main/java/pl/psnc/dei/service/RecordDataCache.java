package pl.psnc.dei.service;

import org.springframework.stereotype.Service;
import pl.psnc.dei.util.IiifAvailability;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RecordDataCache {

	private Map<String, RecordData> cache = new ConcurrentHashMap<>();

	public RecordData getValidationResult(String recordId) {
		return cache.get(recordId);
	}

	public void addValidationResult(String recordId, String mimeType, IiifAvailability iiifAvailability) {
		cache.putIfAbsent(recordId, new RecordData(mimeType, iiifAvailability));
	}

	public void addValue(String recordId, String key, String value) {
		cache.computeIfPresent(recordId, (k, v) -> {
			v.putValue(key, value);
			return v;
		});
	}

	public void clear() {
		cache.clear();
	}

	public class RecordData {

		private String mimeType;
		private IiifAvailability iiifAvailability;
		private Map<String, String> otherValues = new HashMap<>();

		RecordData(String mimeType, IiifAvailability iiifAvailability) {
			this.mimeType = mimeType;
			this.iiifAvailability = iiifAvailability;
		}

		public String getValue(String key) {
			return otherValues.get(key);
		}

		public void putValue(String key, String values) {
			otherValues.put(key, values);
		}

		public String getMimeType() {
			return mimeType;
		}

		public IiifAvailability getIiifAvailability() {
			return iiifAvailability;
		}
	}
}

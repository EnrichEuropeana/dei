package pl.psnc.dei.service;

import org.springframework.stereotype.Service;
import pl.psnc.dei.util.IiifAvailability;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RecordTransferValidationCache {

	private Map<String, ValidationResult> cache = new ConcurrentHashMap<>();

	public ValidationResult getValidationResult(String recordId) {
		return cache.get(recordId);
	}

	public void addValidationResult(String recordId, String mimeType, IiifAvailability iiifAvailability) {
		cache.put(recordId, new ValidationResult(mimeType, iiifAvailability));
	}

	public void clear() {
		cache.clear();
	}

	public class ValidationResult {

		private String mimeType;
		private IiifAvailability iiifAvailability;

		ValidationResult(String mimeType, IiifAvailability iiifAvailability) {
			this.mimeType = mimeType;
			this.iiifAvailability = iiifAvailability;
		}

		public String getMimeType() {
			return mimeType;
		}

		public IiifAvailability getIiifAvailability() {
			return iiifAvailability;
		}
	}
}

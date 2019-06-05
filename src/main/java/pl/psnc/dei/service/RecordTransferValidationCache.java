package pl.psnc.dei.service;

import org.springframework.stereotype.Service;
import pl.psnc.dei.util.TransferPossibility;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RecordTransferValidationCache {

	private Map<String, ValidationResult> cache = new ConcurrentHashMap<>();

	public ValidationResult getValidationResult(String recordId) {
		return cache.get(recordId);
	}

	public void addValidationResult(String recordId, String mimeType, TransferPossibility transferPossibility) {
		cache.put(recordId, new ValidationResult(mimeType, transferPossibility));
	}

	public void clear() {
		cache.clear();
	}

	public class ValidationResult {

		private String mimeType;
		private TransferPossibility transferPossibility;

		ValidationResult(String mimeType, TransferPossibility transferPossibility) {
			this.mimeType = mimeType;
			this.transferPossibility = transferPossibility;
		}

		public String getMimeType() {
			return mimeType;
		}

		public TransferPossibility getTransferPossibility() {
			return transferPossibility;
		}
	}
}

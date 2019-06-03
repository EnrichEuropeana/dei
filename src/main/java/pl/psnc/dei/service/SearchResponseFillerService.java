package pl.psnc.dei.service;

import org.apache.jena.atlas.json.JsonObject;
import org.springframework.stereotype.Service;
import pl.psnc.dei.model.Aggregator;
import pl.psnc.dei.schema.search.SearchResult;
import pl.psnc.dei.util.RecordTransferValidationUtil;

@Service
public class SearchResponseFillerService {

	private EuropeanaRestService europeanaRestService;

	private RecordTransferValidationCache recordTransferValidationCache;

	public SearchResponseFillerService(EuropeanaRestService europeanaRestService,
									   RecordTransferValidationCache recordTransferValidationCache) {
		this.europeanaRestService = europeanaRestService;
		this.recordTransferValidationCache = recordTransferValidationCache;
	}

	public SearchResult fillMissingDataAndValidate(int aggregatorId, SearchResult searchResult) {
		Aggregator aggregator = Aggregator.getById(aggregatorId);

		switch (aggregator) {
			case EUROPEANA:
				String recordId = searchResult.getId();
				String mimeType;
				RecordTransferValidationUtil.TransferPossibility transferPossibility;
				RecordTransferValidationCache.ValidationResult validationResult = recordTransferValidationCache.getValidationResult(recordId);
				if (validationResult == null) {
					JsonObject recordObject = europeanaRestService.retrieveRecordFromEuropeanaAndConvertToJsonLd(recordId);
					mimeType = RecordTransferValidationUtil.getMimeType(recordObject);
					transferPossibility = RecordTransferValidationUtil.checkIfTransferPossible(recordObject, mimeType);
					recordTransferValidationCache.addValidationResult(recordId, mimeType, transferPossibility);
					return searchResult;
				} else {
					mimeType = validationResult.getMimeType();
					transferPossibility = validationResult.getTransferPossibility();
				}
				searchResult.setTransferPossibility(transferPossibility);
				searchResult.setFormat(mimeType);

			default:
				return searchResult;
		}
	}

	public void clearCache() {
		recordTransferValidationCache.clear();
	}
}

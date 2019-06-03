package pl.psnc.dei.service;

import org.apache.jena.atlas.json.JsonObject;
import org.springframework.stereotype.Service;
import pl.psnc.dei.model.Aggregator;
import pl.psnc.dei.schema.search.SearchResult;
import pl.psnc.dei.util.EuropeanaRecordTransferValidationUtil;
import pl.psnc.dei.util.TransferPossibility;

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
				TransferPossibility transferPossibility;
				RecordTransferValidationCache.ValidationResult validationResult = recordTransferValidationCache.getValidationResult(recordId);
				if (validationResult == null) {
					JsonObject recordObject = europeanaRestService.retrieveRecordFromEuropeanaAndConvertToJsonLd(recordId);
					mimeType = EuropeanaRecordTransferValidationUtil.getMimeType(recordObject);
					transferPossibility = EuropeanaRecordTransferValidationUtil.checkIfTransferPossible(recordObject, mimeType);
					recordTransferValidationCache.addValidationResult(recordId, mimeType, transferPossibility);
				} else {
					mimeType = validationResult.getMimeType();
					transferPossibility = validationResult.getTransferPossibility();
				}
				searchResult.setTransferPossibility(transferPossibility);
				searchResult.setFormat(mimeType);
				return searchResult;
			case DDB:
				//todo implement

			default:
				return searchResult;
		}
	}

	public void clearCache() {
		recordTransferValidationCache.clear();
	}
}

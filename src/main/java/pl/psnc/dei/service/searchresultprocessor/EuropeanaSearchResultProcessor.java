package pl.psnc.dei.service.searchresultprocessor;

import org.apache.jena.atlas.json.JsonObject;
import org.springframework.stereotype.Service;
import pl.psnc.dei.schema.search.SearchResult;
import pl.psnc.dei.service.EuropeanaRestService;
import pl.psnc.dei.service.RecordTransferValidationCache;
import pl.psnc.dei.util.EuropeanaRecordTransferValidationUtil;
import pl.psnc.dei.util.TransferPossibility;

@Service
public class EuropeanaSearchResultProcessor implements AggregatorSearchResultProcessor {

	private RecordTransferValidationCache recordTransferValidationCache;

	private EuropeanaRestService europeanaRestService;

	public EuropeanaSearchResultProcessor(EuropeanaRestService europeanaRestService,
										  RecordTransferValidationCache recordTransferValidationCache) {
		this.recordTransferValidationCache = recordTransferValidationCache;
		this.europeanaRestService = europeanaRestService;
	}

	@Override
	public SearchResult fillMissingDataAndValidate(SearchResult searchResult) {
		String recordId = searchResult.getId();
		String mimeType;
		TransferPossibility transferPossibility;
		RecordTransferValidationCache.ValidationResult validationResult = recordTransferValidationCache.getValidationResult(recordId);
		if (validationResult == null) {
			JsonObject recordObject = europeanaRestService.retrieveRecordFromEuropeanaAndConvertToJsonLd(recordId);
			//in case of Europeana we only need to fill format (mimeType)
			mimeType = EuropeanaRecordTransferValidationUtil.getMimeType(recordObject);
			transferPossibility = validateRecord(recordId, mimeType, recordObject);
		} else {
			mimeType = validationResult.getMimeType();
			transferPossibility = validationResult.getTransferPossibility();
		}
		searchResult.setTransferPossibility(transferPossibility);
		searchResult.setFormat(mimeType);
		return searchResult;
	}

	private TransferPossibility validateRecord(String recordId, String mimeType, JsonObject recordObject) {
		TransferPossibility transferPossibility = EuropeanaRecordTransferValidationUtil.checkIfTransferPossible(recordObject, mimeType);
		recordTransferValidationCache.addValidationResult(recordId, mimeType, transferPossibility);
		return transferPossibility;
	}
}

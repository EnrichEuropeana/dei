package pl.psnc.dei.service;

import org.apache.jena.atlas.json.JsonObject;
import org.springframework.stereotype.Service;
import pl.psnc.dei.model.Aggregator;
import pl.psnc.dei.schema.search.SearchResult;
import pl.psnc.dei.util.RecordTransferValidationUtil;
import pl.psnc.dei.util.ddb.DDBFormatResolver;

@Service
public class SearchResponseFillerService {

	private EuropeanaRestService europeanaRestService;
	private DDBRestService ddbRestService;
	private DDBFormatResolver ddbFormatResolver;
	private RecordTransferValidationCache recordTransferValidationCache;

	public SearchResponseFillerService(EuropeanaRestService europeanaRestService,
									   DDBRestService ddbRestService, DDBFormatResolver ddbFormatResolver, RecordTransferValidationCache recordTransferValidationCache) {
		this.europeanaRestService = europeanaRestService;
		this.ddbRestService = ddbRestService;
		this.ddbFormatResolver = ddbFormatResolver;
		this.recordTransferValidationCache = recordTransferValidationCache;
	}

	public SearchResult fillMissingDataAndValidate(int aggregatorId, SearchResult searchResult) {
		Aggregator aggregator = Aggregator.getById(aggregatorId);
		String recordId = searchResult.getId();

		switch (aggregator) {
			case EUROPEANA:
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
				return searchResult;
			case DDB:
				String format = ddbFormatResolver.getRecordFormat(searchResult.getId());
				JsonObject recordObject = ddbRestService.retrieveRecordFromDDBAndConvertToJsonLd(recordId);
				transferPossibility = RecordTransferValidationUtil.checkIfTransferPossible(recordObject, format);
				recordTransferValidationCache.addValidationResult(recordId, format, transferPossibility);
				searchResult.setFormat(format);
				searchResult.setTransferPossibility(transferPossibility);
				return searchResult;

			default:
				return searchResult;
		}
	}

	public void clearCache() {
		recordTransferValidationCache.clear();
	}
}

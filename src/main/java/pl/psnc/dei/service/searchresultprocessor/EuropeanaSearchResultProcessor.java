package pl.psnc.dei.service.searchresultprocessor;

import org.apache.jena.atlas.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pl.psnc.dei.schema.search.SearchResult;
import pl.psnc.dei.service.EuropeanaRestService;
import pl.psnc.dei.service.RecordTransferValidationCache;
import pl.psnc.dei.util.EuropeanaRecordTransferValidationUtil;
import pl.psnc.dei.util.IiifAvailability;

@Service
public class EuropeanaSearchResultProcessor implements AggregatorSearchResultProcessor {

	private final Logger logger = LoggerFactory.getLogger(EuropeanaSearchResultProcessor.class);

	private RecordTransferValidationCache recordTransferValidationCache;

	private EuropeanaRestService europeanaRestService;

	public EuropeanaSearchResultProcessor(EuropeanaRestService europeanaRestService,
										  RecordTransferValidationCache recordTransferValidationCache) {
		this.recordTransferValidationCache = recordTransferValidationCache;
		this.europeanaRestService = europeanaRestService;
	}

	@Override
	public SearchResult fillMissingDataAndValidate(SearchResult searchResult, boolean onlyIiif) {
		String recordId = searchResult.getId();
		String mimeType;
		IiifAvailability iiifAvailability;
		RecordTransferValidationCache.ValidationResult validationResult = recordTransferValidationCache.getValidationResult(recordId);
		if (validationResult == null || validationResult.getIiifAvailability() == IiifAvailability.DATA_UNAVAILABLE) {
			JsonObject recordObject = getRecordData(recordId);
			//in case of Europeana we only need to fill format (mimeType)
			mimeType = recordObject != null ? EuropeanaRecordTransferValidationUtil.getMimeType(recordObject) : "Data unavailable";
			iiifAvailability = validateRecord(recordId, mimeType, recordObject, onlyIiif);
		} else {
			mimeType = validationResult.getMimeType();
			iiifAvailability = validationResult.getIiifAvailability();
		}
		searchResult.setIiifAvailability(iiifAvailability);
		searchResult.setFormat(mimeType);
		return searchResult;
	}

	private JsonObject getRecordData(String recordId) {
		try {
			return europeanaRestService.retrieveRecordFromEuropeanaAndConvertToJsonLd(recordId);
		} catch (Exception e) {
			logger.warn("Cannot retrieve record data from Europeana Record API.", e);
			return null;
		}
	}

	private IiifAvailability validateRecord(String recordId, String mimeType, JsonObject recordObject, boolean onlyIiif) {
		IiifAvailability iiifAvailability = onlyIiif ? IiifAvailability.AVAILABLE : EuropeanaRecordTransferValidationUtil.checkIfIiifAvailable(recordObject, mimeType);
		recordTransferValidationCache.addValidationResult(recordId, mimeType, iiifAvailability);
		return iiifAvailability;
	}
}

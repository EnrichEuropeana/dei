package pl.psnc.dei.service.searchresultprocessor;

import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pl.psnc.dei.model.Aggregator;
import pl.psnc.dei.schema.search.SearchResult;
import pl.psnc.dei.service.EuropeanaRestService;
import pl.psnc.dei.service.RecordTransferValidationCache;
import pl.psnc.dei.util.RecordTransferValidator;
import pl.psnc.dei.util.IiifAvailability;

import java.util.Optional;

@Service
public class EuropeanaSearchResultProcessor implements AggregatorSearchResultProcessor {

	private final Logger logger = LoggerFactory.getLogger(EuropeanaSearchResultProcessor.class);

	private static final String KEY_GRAPH = "@graph";
	private static final String KEY_TYPE = "@type";
	private static final String KEY_MIME_TYPE = "hasMimeType";
	private static final String TYPE_WEB_RESOURCE = "edm:WebResource";

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
			mimeType = recordObject != null ? getMimeType(recordObject) : DATA_UNAVAILABLE_VALUE;
			iiifAvailability = validateRecord(recordId, mimeType, recordObject, onlyIiif);
		} else {
			mimeType = validationResult.getMimeType();
			iiifAvailability = validationResult.getIiifAvailability();
		}
		searchResult.setIiifAvailability(iiifAvailability);
		searchResult.setFormat(mimeType);
		//sometimes there is no dcCreator (author)
		if (searchResult.getAuthor() == null) {
			searchResult.setAuthor(DATA_UNAVAILABLE_VALUE);
		}
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
		IiifAvailability iiifAvailability = onlyIiif ? IiifAvailability.AVAILABLE : RecordTransferValidator.checkIfIiifAvailable(Aggregator.EUROPEANA, recordObject, mimeType);
		recordTransferValidationCache.addValidationResult(recordId, mimeType, iiifAvailability);
		return iiifAvailability;
	}

	/**
	 * Get mimeType for given record
	 *
	 * @param record record json-ld object
	 * @return record's mimeType
	 */
	private String getMimeType(JsonObject record) {
		Optional<JsonObject> mimeTypeEntry = record.get(KEY_GRAPH).getAsArray().stream()
				.map(JsonValue::getAsObject)
				.filter(o -> o.get(KEY_TYPE).getAsString().value().equals(TYPE_WEB_RESOURCE)
						&& o.keySet().stream().anyMatch(k -> k.contains(KEY_MIME_TYPE)))
				.findFirst();
		if (mimeTypeEntry.isPresent()) {
			JsonObject object = mimeTypeEntry.get();
			return extractMimeType(object);
		}
		return null;
	}

	private String extractMimeType(JsonObject mimeTypeEntryObject) {
		JsonValue jsonValue = mimeTypeEntryObject.get("http://www.ebu.ch/metadata/ontologies/ebucore/ebucore#" + KEY_MIME_TYPE);
		if (jsonValue != null) {
			return jsonValue.getAsString().value();
		}
		jsonValue = mimeTypeEntryObject.get("ebucore:" + KEY_MIME_TYPE);
		if (jsonValue != null) {
			return jsonValue.getAsString().value();
		}
		jsonValue = mimeTypeEntryObject.get(KEY_MIME_TYPE);
		if (jsonValue != null) {
			return jsonValue.getAsString().value();
		}
		logger.error("Cannot extract mimeType value from Europeana record.");
		throw new IllegalStateException("Cannot extract mimeType value from Europeana record.");
	}
}

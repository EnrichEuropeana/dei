package pl.psnc.dei.service.searchresultprocessor;

import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pl.psnc.dei.iiif.EuropeanaConversionDataHolder;
import pl.psnc.dei.model.Aggregator;
import pl.psnc.dei.schema.search.SearchResult;
import pl.psnc.dei.service.RecordDataCache;
import pl.psnc.dei.service.search.EuropeanaSearchService;
import pl.psnc.dei.util.IiifAvailability;
import pl.psnc.dei.util.IiifValidator;

import java.util.Optional;

import static pl.psnc.dei.iiif.EuropeanaConversionDataHolder.*;

@Service
public class EuropeanaSearchResultProcessor implements AggregatorSearchResultProcessor {

	private final Logger logger = LoggerFactory.getLogger(EuropeanaSearchResultProcessor.class);

	private RecordDataCache recordDataCache;

	private EuropeanaSearchService europeanaSearchService;

	public EuropeanaSearchResultProcessor(EuropeanaSearchService europeanaSearchService,
										  RecordDataCache recordDataCache) {
		this.recordDataCache = recordDataCache;
		this.europeanaSearchService = europeanaSearchService;
	}

	@Override
	public SearchResult fillMissingDataAndValidate(SearchResult searchResult, boolean onlyIiif) {
		String recordId = searchResult.getId();
		String mimeType;
		IiifAvailability iiifAvailability;
		RecordDataCache.RecordData recordData = recordDataCache.getValidationResult(recordId);
		if (recordData == null || recordData.getIiifAvailability() == IiifAvailability.DATA_UNAVAILABLE) {
			JsonObject recordObject = getRecordData(recordId);
			//in case of Europeana we only need to fill format (mimeType)
			mimeType = recordObject != null ? getMimeType(recordObject) : DATA_UNAVAILABLE_VALUE;
			iiifAvailability = validateRecord(recordId, mimeType, recordObject, onlyIiif);
		} else {
			mimeType = recordData.getMimeType();
			iiifAvailability = recordData.getIiifAvailability();
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
			return europeanaSearchService.retrieveRecordAndConvertToJsonLd(recordId);
		} catch (Exception e) {
			logger.warn("Cannot retrieve record data from Europeana Record API.", e);
			return null;
		}
	}

	private IiifAvailability validateRecord(String recordId, String mimeType, JsonObject recordObject, boolean onlyIiif) {
		IiifAvailability iiifAvailability = onlyIiif ? IiifAvailability.AVAILABLE : IiifValidator.checkIfIiifAvailable(Aggregator.EUROPEANA, recordObject, mimeType);
		recordDataCache.addValidationResult(recordId, mimeType, iiifAvailability);
		return iiifAvailability;
	}


	/**
	 * Get mime type from EDM record. This is based on the web resource related to either isShownBy or hasView.
	 * IsShownBy has bigger priority. There should not be situation that none of those fields are available.
	 *
	 * @param record json representation of the record
	 * @return mime type of the record determined by either mime type of isShownBy or hasView web resource
	 */
	private String getMimeType(JsonObject record) {
		Optional<JsonObject> aggregatorData = record.get(KEY_GRAPH).getAsArray().stream()
				.map(JsonValue::getAsObject)
				.filter(e -> e.get(EDM_IS_SHOWN_BY) != null)
				.findFirst();

		if (!aggregatorData.isPresent()) {
			return IiifAvailability.DATA_UNAVAILABLE.getMessage();
		}

		Optional<String> mimeType = Optional.ofNullable(IiifValidator.getMimeTypeFromShort(EuropeanaConversionDataHolder.detectType(aggregatorData.get().get(EDM_IS_SHOWN_BY).getAsObject().get(KEY_ID).getAsString().value(), record)));
		if (mimeType.isPresent()) {
			return mimeType.get();
		}

		if (aggregatorData.get().get(EDM_HAS_VIEW) != null) {
			Optional<String> type;
			if (aggregatorData.get().get(EDM_HAS_VIEW).isArray()) {
				type = aggregatorData.get().get(EDM_HAS_VIEW).getAsArray().stream()
						.map(e -> Optional.ofNullable(IiifValidator.getMimeTypeFromShort(EuropeanaConversionDataHolder.detectType(e.getAsObject().get(KEY_ID).getAsString().value(), record))))
						.filter(Optional::isPresent)
						.map(Optional::get)
						.findFirst();
			} else {
				JsonObject object = aggregatorData.get().get(EDM_HAS_VIEW).getAsObject();
				type = Optional.ofNullable(IiifValidator.getMimeTypeFromShort(EuropeanaConversionDataHolder.detectType(object.getAsObject().get(KEY_ID).getAsString().value(), record)));
			}
			if (type.isPresent()) {
				return type.get();
			}
		}
		return IiifAvailability.DATA_UNAVAILABLE.getMessage();
	}
}

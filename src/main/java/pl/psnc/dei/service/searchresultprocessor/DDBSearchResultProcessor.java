package pl.psnc.dei.service.searchresultprocessor;

import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pl.psnc.dei.model.Aggregator;
import pl.psnc.dei.schema.search.SearchResult;
import pl.psnc.dei.service.DDBRestService;
import pl.psnc.dei.service.RecordTransferValidationCache;
import pl.psnc.dei.util.IiifAvailability;
import pl.psnc.dei.util.RecordTransferValidator;
import pl.psnc.dei.service.DDBFormatResolver;

@Service
public class DDBSearchResultProcessor implements AggregatorSearchResultProcessor {

	private final Logger logger = LoggerFactory.getLogger(DDBSearchResultProcessor.class);

	private static final String KEY_GRAPH = "@graph";
	private static final String KEY_TYPE = "@type";
	private static final String TYPE_AGGREGATION = "ore:Aggregation";

	private DDBRestService ddbRestService;
	private DDBFormatResolver ddbFormatResolver;
	private RecordTransferValidationCache recordTransferValidationCache;

	public DDBSearchResultProcessor(DDBRestService ddbRestService, DDBFormatResolver ddbFormatResolver, RecordTransferValidationCache recordTransferValidationCache) {
		this.ddbRestService = ddbRestService;
		this.ddbFormatResolver = ddbFormatResolver;
		this.recordTransferValidationCache = recordTransferValidationCache;
	}

	@Override
	public SearchResult fillMissingDataAndValidate(SearchResult searchResult, boolean onlyIiif) {
		String recordId = searchResult.getId();
		String mimeType;
		String author;
		String provider;
		String language;
		String license;
		IiifAvailability iiifAvailability;

		RecordTransferValidationCache.ValidationResult validationResult = recordTransferValidationCache.getValidationResult(recordId);
		if (validationResult == null || validationResult.getIiifAvailability() == IiifAvailability.DATA_UNAVAILABLE) {
			mimeType = getMimeType(searchResult.getId());
			JsonObject recordObject = getRecordData(recordId);
			author = DATA_UNAVAILABLE_VALUE; //todo find author?
			provider = getMetadataValue(recordObject, TYPE_AGGREGATION, "edm:dataProvider");
			language = getMetadataValue(recordObject, "edm:ProvidedCHO", "dc:language");
			license = getMetadataValue(recordObject, TYPE_AGGREGATION, "rights");
			iiifAvailability = RecordTransferValidator.checkIfIiifAvailable(Aggregator.DDB, recordObject, mimeType);

			recordTransferValidationCache.addValidationResult(recordId, mimeType, iiifAvailability);
			recordTransferValidationCache.addValue(recordId, "author", author);
			recordTransferValidationCache.addValue(recordId, "provider", provider);
			recordTransferValidationCache.addValue(recordId, "language", language);
			recordTransferValidationCache.addValue(recordId, "license", license);
		} else {
			mimeType = validationResult.getMimeType();
			author = validationResult.getValue("author");
			provider = validationResult.getValue("provider");
			language = validationResult.getValue("language");
			license = validationResult.getValue("license");
			iiifAvailability = validationResult.getIiifAvailability();
		}

		recordTransferValidationCache.addValidationResult(recordId, mimeType, iiifAvailability);
		searchResult.setFormat(mimeType);
		searchResult.setAuthor(author);
		searchResult.setProvider(provider);
		searchResult.setLanguage(language);
		searchResult.setLicense(license);
		searchResult.setIiifAvailability(iiifAvailability);
		return searchResult;
	}

	private String getMimeType(String recordId) {
		try {
			String mimeType = ddbFormatResolver.getRecordFormat(recordId);
			if (mimeType != null && !mimeType.isEmpty()) {
				return mimeType;
			}
		} catch (Exception e) {
			logger.warn("Cannot retrieve mimeType for record {} from DDB items binaries endpoint", recordId, e);
		}
		return DATA_UNAVAILABLE_VALUE;
	}

	private JsonObject getRecordData(String recordId) {
		try {
			return ddbRestService.retrieveRecordFromDDBAndConvertToJsonLd(recordId);
		} catch (Exception e) {
			logger.warn("Cannot retrieve record data for record {} from DDB items edm endpoint.", recordId, e);
			return null;
		}
	}

	private String getMetadataValue(JsonObject recordObject, String type, String fieldName) {
		if (recordObject == null) {
			return DATA_UNAVAILABLE_VALUE;
		}
		return recordObject.get(KEY_GRAPH).getAsArray().stream()
				.map(JsonValue::getAsObject)
				.filter(o -> (o.get(KEY_TYPE).getAsString().value().equals(type)))
				.findFirst()
				.map(a -> {
					JsonValue dataProvider = a.get(fieldName);
					return dataProvider != null ? dataProvider.getAsString().value() : DATA_UNAVAILABLE_VALUE;
				}).orElse(DATA_UNAVAILABLE_VALUE);
	}
}

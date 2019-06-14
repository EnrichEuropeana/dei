package pl.psnc.dei.service.searchresultprocessor;

import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pl.psnc.dei.model.Aggregator;
import pl.psnc.dei.schema.search.SearchResult;
import pl.psnc.dei.service.DDBRestService;
import pl.psnc.dei.service.RecordDataCache;
import pl.psnc.dei.util.IiifAvailability;
import pl.psnc.dei.util.RecordTransferValidator;
import pl.psnc.dei.service.DDBFormatResolver;

@Service
public class DDBSearchResultProcessor implements AggregatorSearchResultProcessor {

	private final Logger logger = LoggerFactory.getLogger(DDBSearchResultProcessor.class);

	private static final String KEY_GRAPH = "@graph";
	private static final String KEY_TYPE = "@type";
	private static final String TYPE_AGGREGATION = "ore:Aggregation";
	private static final String TYPE_EDM_PROVIDED_CHO = "edm:ProvidedCHO";
	private static final String EDM_DATA_PROVIDER = "edm:dataProvider";
	private static final String DC_LANGUAGE = "dc:language";
	private static final String RIGHTS = "rights";

	private static final String AUTHOR = "author";
	private static final String PROVIDER = "provider";
	private static final String LANGUAGE = "language";
	private static final String LICENSE = "license";

	private DDBRestService ddbRestService;
	private DDBFormatResolver ddbFormatResolver;
	private RecordDataCache recordDataCache;

	public DDBSearchResultProcessor(DDBRestService ddbRestService, DDBFormatResolver ddbFormatResolver, RecordDataCache recordDataCache) {
		this.ddbRestService = ddbRestService;
		this.ddbFormatResolver = ddbFormatResolver;
		this.recordDataCache = recordDataCache;
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

		RecordDataCache.RecordData recordData = recordDataCache.getValidationResult(recordId);
		if (recordData == null || recordData.getIiifAvailability() == IiifAvailability.DATA_UNAVAILABLE) {
			mimeType = getMimeType(searchResult.getId());
			JsonObject recordObject = getRecordData(recordId);
			//there is no dcCreator (author) IN RDF
			author = DATA_UNAVAILABLE_VALUE;
			provider = getMetadataValue(recordObject, TYPE_AGGREGATION, EDM_DATA_PROVIDER);
			language = getMetadataValue(recordObject, TYPE_EDM_PROVIDED_CHO, DC_LANGUAGE);
			license = getMetadataValue(recordObject, TYPE_AGGREGATION, RIGHTS);
			iiifAvailability = RecordTransferValidator.checkIfIiifAvailable(Aggregator.DDB, recordObject, mimeType);

			recordDataCache.addValidationResult(recordId, mimeType, iiifAvailability);
			recordDataCache.addValue(recordId, AUTHOR, author);
			recordDataCache.addValue(recordId, PROVIDER, provider);
			recordDataCache.addValue(recordId, LANGUAGE, language);
			recordDataCache.addValue(recordId, LICENSE, license);
		} else {
			mimeType = recordData.getMimeType();
			author = recordData.getValue(AUTHOR);
			provider = recordData.getValue(PROVIDER);
			language = recordData.getValue(LANGUAGE);
			license = recordData.getValue(LICENSE);
			iiifAvailability = recordData.getIiifAvailability();
		}

		recordDataCache.addValidationResult(recordId, mimeType, iiifAvailability);
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

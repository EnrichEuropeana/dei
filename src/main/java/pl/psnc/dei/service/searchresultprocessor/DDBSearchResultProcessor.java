package pl.psnc.dei.service.searchresultprocessor;

import org.apache.jena.atlas.json.JsonObject;
import org.springframework.stereotype.Service;
import pl.psnc.dei.schema.search.SearchResult;
import pl.psnc.dei.service.DDBRestService;
import pl.psnc.dei.service.RecordTransferValidationCache;
import pl.psnc.dei.util.EuropeanaRecordTransferValidationUtil;
import pl.psnc.dei.util.IiifAvailability;
import pl.psnc.dei.util.ddb.DDBFormatResolver;

@Service
public class DDBSearchResultProcessor implements AggregatorSearchResultProcessor {

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
		IiifAvailability iiifAvailability;
		String format = ddbFormatResolver.getRecordFormat(searchResult.getId());
		JsonObject recordObject = ddbRestService.retrieveRecordFromDDBAndConvertToJsonLd(recordId);
		iiifAvailability = EuropeanaRecordTransferValidationUtil.checkIfIiifAvailable(recordObject, format); //todo
		recordTransferValidationCache.addValidationResult(recordId, format, iiifAvailability);
		searchResult.setFormat(format);
		searchResult.setIiifAvailability(iiifAvailability);
		return searchResult;
	}
}

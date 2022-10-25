package pl.psnc.dei.util;

import lombok.NonNull;
import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.atlas.json.JsonNumber;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonValue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pl.psnc.dei.model.Record;
import pl.psnc.dei.model.enrichments.DateEnrichment;
import pl.psnc.dei.model.enrichments.MetadataEnrichment;
import pl.psnc.dei.model.enrichments.PlaceEnrichment;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static pl.psnc.dei.util.EnrichmentAttributeNames.DCTERMS_SPATIAL;
import static pl.psnc.dei.util.EnrichmentAttributeNames.DC_DATE;

/**
 * Extracts metadata enrichment of the given type from the Item. Original values are kept.
 */
@Component
public class MetadataEnrichmentExtractor {
    private static final String URL_TEMPLATE = "%s/documents/story/item?item=%s";

    public static final Pattern IS_SHOWN_AT_PATTERN = Pattern.compile(
            "https?://(?:fbc.pionier.net.pl/id/|doi.org/)(.*)");

    public static final Pattern IS_SHOWN_AT_PATTERN_FOR_DLIBRA = Pattern.compile(
            "https?://(?:fbc.pionier.net.pl/id/)(.*)");

    public static final Pattern IS_SHOWN_AT_PATTERN_FOR_DRI = Pattern.compile(
            "https?://(?:doi.org/)(.*)");

    @Value("${transcribathon.url}")
    private String transcribathonLocation;

    private String getItemLink(@NonNull String itemId) {
        return String.format(URL_TEMPLATE, transcribathonLocation, itemId);
    }

    private Optional<JsonNumber> extractItemId(@NonNull JsonObject item) {
        return Optional.ofNullable(item.get(ItemFieldsNames.ITEM_ID).getAsNumber());
    }

    private String extractRecordId(JsonObject storyDetails) {
        return Optional.ofNullable(storyDetails.get("edmIsShownAt")).stream()
                .map(jsonValue -> jsonValue.getAsString().value())
                .flatMap(s -> IS_SHOWN_AT_PATTERN.matcher(s).results()).map(s -> s.group(1))
                .findFirst().orElse(null);
    }

    private String extractLanguage(JsonObject storyDetails) {
        return storyDetails.get("edmLanguage").getAsString().value();
    }

    private boolean isDateEnrichmentAvailable(JsonObject item) {
        return item != null &&
                (item.get(ItemFieldsNames.DATE_START) != null ||
                        item.get(ItemFieldsNames.DATE_END) != null);
    }

    private boolean isPlaceEnrichmentAvailable(JsonObject item) {
        return item != null &&
                item.get(ItemFieldsNames.PLACES) != null && !item.get(ItemFieldsNames.PLACES).getAsArray().isEmpty();
    }

    private MetadataEnrichment initMetadataEnrichment(MetadataEnrichment enrichment, Record record, String externalId,
            String attribute, JsonNumber itemId) {
        enrichment.setRecord(record);
        enrichment.setExternalId(externalId);
        enrichment.setAttribute(attribute);
        enrichment.setState(MetadataEnrichment.EnrichmentState.PENDING);
        if (itemId == null) {
            enrichment.setItemLink(null);
        } else {
            enrichment.setItemLink(
                    getItemLink(itemId.toString()));
        }
        return enrichment;
    }

    private DateEnrichment createDateEnrichment(Record record, String recordId, JsonValue item, JsonNumber itemId) {
        DateEnrichment enrichment = (DateEnrichment) initMetadataEnrichment(
                new DateEnrichment(), record, recordId, DC_DATE, itemId);
        extractPageNumber(item.getAsObject()).map(jsonValue -> jsonValue.getAsNumber().value().intValue())
                .ifPresent(enrichment::setPageNo);
        extractStartDate(item.getAsObject()).map(
                        jsonValue -> Instant.from(DateTimeFormatter.ISO_ZONED_DATE_TIME.parse(jsonValue.getAsString().value())))
                .ifPresent(
                        enrichment::setDateStart);
        extractEndDate(item.getAsObject()).map(
                        jsonValue -> Instant.from(DateTimeFormatter.ISO_ZONED_DATE_TIME.parse(jsonValue.getAsString().value())))
                .ifPresent(
                        enrichment::setDateEnd);
        return enrichment;
    }

    public JsonArray getItems(JsonObject storyDetails) {
        return storyDetails.get(ItemFieldsNames.ITEMS_ENTRY).getAsArray();
    }

    public Optional<JsonValue> extractStartDate(JsonObject item) {
        return Optional.ofNullable(item.get(ItemFieldsNames.DATE_START));
    }

    public Optional<JsonValue> extractEndDate(JsonObject item) {
        return Optional.ofNullable(item.get(ItemFieldsNames.DATE_END));
    }

    public Optional<JsonValue> extractLatitude(JsonObject item) {
        return Optional.ofNullable(item.get(ItemFieldsNames.LATITUDE));
    }

    public Optional<JsonValue> extractLongitude(JsonObject item) {
        return Optional.ofNullable(item.get(ItemFieldsNames.LONGITUDE));
    }

    public Optional<JsonValue> extractName(JsonObject item) {
        return Optional.ofNullable(item.get(ItemFieldsNames.NAME));
    }

    public Optional<JsonValue> extractWikidataId(JsonObject item) {
        return Optional.ofNullable(item.get(ItemFieldsNames.WIKIDATA_ID));
    }

    public Optional<JsonValue> extractZoom(JsonObject item) {
        return Optional.ofNullable(item.get(ItemFieldsNames.ZOOM));
    }

    private List<PlaceEnrichment> createPlaceEnrichments(Record record, String recordId, JsonValue item,
            JsonNumber itemId,
            String language) {
        List<PlaceEnrichment> result = new ArrayList<>();
        item.getAsObject().get(ItemFieldsNames.PLACES).getAsArray().forEach(place -> {
            PlaceEnrichment enrichment = (PlaceEnrichment) initMetadataEnrichment(
                    new PlaceEnrichment(), record, recordId, DCTERMS_SPATIAL, itemId);
            extractPageNumber(item.getAsObject()).map(jsonValue -> jsonValue.getAsNumber().value().intValue())
                    .ifPresent(enrichment::setPageNo);
            extractLatitude(place.getAsObject()).map(jsonValue -> jsonValue.getAsNumber().value().doubleValue())
                    .ifPresent(
                            enrichment::setLatitude);
            extractLongitude(place.getAsObject()).map(jsonValue -> jsonValue.getAsNumber().value().doubleValue())
                    .ifPresent(
                            enrichment::setLongitude);
            extractName(place.getAsObject()).map(jsonValue -> jsonValue.getAsString().value()).ifPresent(
                    enrichment::setName);
            enrichment.setLanguage(language);
            extractWikidataId(place.getAsObject()).map(jsonValue -> jsonValue.getAsString().value().trim()).ifPresent(
                    enrichment::setWikidataId);
            extractZoom(place.getAsObject()).map(jsonValue -> jsonValue.getAsNumber().value().intValue()).ifPresent(
                    enrichment::setZoom);
            result.add(enrichment);
        });
        return result;
    }

    private Optional<JsonValue> extractPageNumber(JsonObject item) {
        return Optional.ofNullable(item.get(ItemFieldsNames.ORDER_INDEX));
    }

    public List<MetadataEnrichment> extractEnrichments(Record record, JsonObject metadataEnrichments) {
        List<MetadataEnrichment> enrichments = new ArrayList<>();
        String externalId = extractRecordId(metadataEnrichments);
        String language = extractLanguage(metadataEnrichments);
        for (JsonValue item : metadataEnrichments.get(ItemFieldsNames.ITEMS_ENTRY).getAsArray()) {
            extractItemId(item.getAsObject()).ifPresent(itemId -> {
                if (isDateEnrichmentAvailable(item.getAsObject())) {
                    enrichments.add(createDateEnrichment(record, externalId, item, itemId));
                }
                if (isPlaceEnrichmentAvailable(item.getAsObject())) {
                    enrichments.addAll(createPlaceEnrichments(record, externalId, item, itemId, language));
                }
            });
        }

        List<Instant> dates = enrichments.stream()
                .filter(enrichment -> enrichment instanceof DateEnrichment)
                .flatMap(enrichment -> Stream.of(((DateEnrichment) enrichment).getDateStart(),
                        ((DateEnrichment) enrichment).getDateEnd()).filter(Objects::nonNull))
                .collect(Collectors.toList());

        DateEnrichment generalDateEnrichment = (DateEnrichment) initMetadataEnrichment(new DateEnrichment(), record,
                externalId, DC_DATE, null);
        dates.stream().max(Instant::compareTo).ifPresent(generalDateEnrichment::setDateEnd);
        dates.stream().min(Instant::compareTo).ifPresent(generalDateEnrichment::setDateStart);
        enrichments.add(generalDateEnrichment);

        return enrichments;
    }
}
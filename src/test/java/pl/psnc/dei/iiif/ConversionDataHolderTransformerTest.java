package pl.psnc.dei.iiif;

import lombok.SneakyThrows;
import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonValue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import pl.psnc.dei.model.Aggregator;
import pl.psnc.dei.model.Record;
import pl.psnc.dei.model.conversion.ConversionTaskContext;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
public class ConversionDataHolderTransformerTest {
    private final String RECORD_IDENTIFIER = "/11602/HERBARWUXWUXAUSTRIAX102495";

    private final JsonObject RECORD_JSON = JSON.parse("{ \n" +
            "  \"@graph\" : [ \n" +
            "      { \n" +
            "        \"@id\" : \"http://data.europeana.eu/aggregation/europeana/11602/HERBARWUXWUXAUSTRIAX102495\" ,\n" +
            "        \"@type\" : \"edm:EuropeanaAggregation\" ,\n" +
            "        \"dcterms:created\" : \"2021-07-20T07:37:31.787Z\" ,\n" +
            "        \"dcterms:modified\" : \"2021-07-20T07:37:31.787Z\" ,\n" +
            "        \"edm:aggregatedCHO\" : {\"@id\" : \"http://data.europeana.eu/item/11602/HERBARWUXWUXAUSTRIAX102495\" } ,\n" +
            "        \"edm:completeness\" : \"10\" ,\n" +
            "        \"edm:country\" : \"Austria\" ,\n" +
            "        \"edm:datasetName\" : \"11602_Herbarium_WU\" ,\n" +
            "        \"edm:landingPage\" : {\"@id\" : \"https://www.europeana.eu/item/11602/HERBARWUXWUXAUSTRIAX102495\" } ,\n" +
            "        \"edm:language\" : \"mul\" ,\n" +
            "        \"edm:preview\" : {\"@id\" : \"https://api.europeana.eu/thumbnail/v2/url.json?uri=https%3A%2F%2Fwww.jacq.org%2Fimage.php%3Fmethod%3Deuropeana%26filename%3Dwu_0033248&type=IMAGE\" } ,\n" +
            "        \"dqv:hasQualityAnnotation\" : [ \n" +
            "            { \"@id\" : \"http://data.europeana.eu/item/11602/HERBARWUXWUXAUSTRIAX102495#metadataTier\" } ,\n" +
            "            { \"@id\" : \"http://data.europeana.eu/item/11602/HERBARWUXWUXAUSTRIAX102495#contentTier\" }\n" +
            "          ]\n" +
            "      } ,\n" +
            "      { \n" +
            "        \"@id\" : \"http://data.europeana.eu/aggregation/provider/11602/HERBARWUXWUXAUSTRIAX102495\" ,\n" +
            "        \"@type\" : \"ore:Aggregation\" ,\n" +
            "        \"edm:aggregatedCHO\" : {\"@id\" : \"http://data.europeana.eu/item/11602/HERBARWUXWUXAUSTRIAX102495\" } ,\n" +
            "        \"edm:dataProvider\" : \"University of Vienna, Institute for Botany - Herbarium WU\" ,\n" +
            "        \"edm:isShownAt\" : {\"@id\" : \"https://www.jacq.org/detail.php?ID=102495\" } ,\n" +
            "        \"edm:isShownBy\" : {\"@id\" : \"https://www.jacq.org/image.php?method=europeana&filename=wu_0033248\" } ,\n" +
            "        \"edm:object\" : {\"@id\" : \"https://www.jacq.org/image.php?method=europeana&filename=wu_0033248\" } ,\n" +
            "        \"edm:provider\" : \"OpenUp!\" ,\n" +
            "        \"edm:rights\" : {\"@id\" : \"http://creativecommons.org/licenses/by-sa/3.0/\" }\n" +
            "      } ,\n" +
            "      { \n" +
            "        \"@id\" : \"http://data.europeana.eu/item/11602/HERBARWUXWUXAUSTRIAX102495\" ,\n" +
            "        \"@type\" : \"edm:ProvidedCHO\"\n" +
            "      } ,\n" +
            "      { \n" +
            "        \"@id\" : \"http://data.europeana.eu/item/11602/HERBARWUXWUXAUSTRIAX102495#contentTier\" ,\n" +
            "        \"@type\" : \"dqv:QualityAnnotation\" ,\n" +
            "        \"dcterms:created\" : \"2021-07-20T14:39:13.812923Z\" ,\n" +
            "        \"oa:hasBody\" : {\"@id\" : \"http://www.europeana.eu/schemas/epf/contentTier4\" } ,\n" +
            "        \"oa:hasTarget\" : {\"@id\" : \"file:///aggregation/provider/11602/HERBARWUXWUXAUSTRIAX102495\" }\n" +
            "      } ,\n" +
            "      { \n" +
            "        \"@id\" : \"http://data.europeana.eu/item/11602/HERBARWUXWUXAUSTRIAX102495#metadataTier\" ,\n" +
            "        \"@type\" : \"dqv:QualityAnnotation\" ,\n" +
            "        \"dcterms:created\" : \"2021-07-20T14:39:13.813242Z\" ,\n" +
            "        \"oa:hasBody\" : {\"@id\" : \"http://www.europeana.eu/schemas/epf/metadataTierA\" } ,\n" +
            "        \"oa:hasTarget\" : {\"@id\" : \"file:///aggregation/provider/11602/HERBARWUXWUXAUSTRIAX102495\" }\n" +
            "      } ,\n" +
            "      { \n" +
            "        \"@id\" : \"http://data.europeana.eu/proxy/europeana/11602/HERBARWUXWUXAUSTRIAX102495\" ,\n" +
            "        \"@type\" : \"ore:Proxy\" ,\n" +
            "        \"dc:identifier\" : \"HERBARWUXWUXAUSTRIAX102495\" ,\n" +
            "        \"edm:europeanaProxy\" : \"true\" ,\n" +
            "        \"edm:type\" : \"IMAGE\" ,\n" +
            "        \"ore:proxyFor\" : {\"@id\" : \"http://data.europeana.eu/item/11602/HERBARWUXWUXAUSTRIAX102495\" } ,\n" +
            "        \"ore:proxyIn\" : {\"@id\" : \"http://data.europeana.eu/aggregation/europeana/11602/HERBARWUXWUXAUSTRIAX102495\" }\n" +
            "      } ,\n" +
            "      { \n" +
            "        \"@id\" : \"http://data.europeana.eu/proxy/provider/11602/HERBARWUXWUXAUSTRIAX102495\" ,\n" +
            "        \"@type\" : \"ore:Proxy\" ,\n" +
            "        \"dc:contributor\" : [ \n" +
            "            \"Bornmüller,J.F.N. (collector)\" ,\n" +
            "            \"Sintenis,P.E.E. (collector)\" ,\n" +
            "            \"W. Greuter (B) 1976-02-08 (identifier)\"\n" +
            "          ] ,\n" +
            "        \"dc:description\" : \"M. Lachmayer & D. Reich (WU) 2018-08-26: Lectotypification by W. Greuter (1997) in Strid & Tan Fl. Hellenica 1 pg. 290, however he cites the voucher in the herbarium WU generale but noted \\u201Eholotypus\\u201C on the one in WU-Hal and \\u201Eisotypus\\u201C on the one in WU generale. Thus, we here designate \\u2026.\" ,\n" +
            "        \"dc:identifier\" : \"WU - Herbarium WU - 102495\" ,\n" +
            "        \"dc:relation\" : {\"@id\" : \"http://www.biodiversitylibrary.org/name/Silene_genistifolia_Hal%E1csy\" } ,\n" +
            "        \"dc:source\" : \"University of Vienna, Institute for Botany - Herbarium WU\" ,\n" +
            "        \"dc:title\" : {\n" +
            "            \"@language\" : \"la\" ,\n" +
            "            \"@value\" : \"Silene genistifolia Halácsy\"\n" +
            "          } ,\n" +
            "        \"dc:type\" : {\n" +
            "            \"@language\" : \"en\" ,\n" +
            "            \"@value\" : \"Preserved Specimen\"\n" +
            "          } ,\n" +
            "        \"dcterms:spatial\" : \"Peninsula Hagion Oros. Mt. Athos: Stratidochi, in prat. subalpin.\" ,\n" +
            "        \"edm:currentLocation\" : \"Rennweg 14, A-1030 Vienna, Austria / Europe\" ,\n" +
            "        \"edm:europeanaProxy\" : \"false\" ,\n" +
            "        \"edm:hasMet\" : {\"@id\" : \"https://sws.geonames.org/390903/\" } ,\n" +
            "        \"edm:hasType\" : {\"@id\" : \"http://rs.tdwg.org/dwc/terms/PreservedSpecimen\" } ,\n" +
            "        \"edm:type\" : \"IMAGE\" ,\n" +
            "        \"ore:proxyFor\" : {\"@id\" : \"http://data.europeana.eu/item/11602/HERBARWUXWUXAUSTRIAX102495\" } ,\n" +
            "        \"ore:proxyIn\" : {\"@id\" : \"http://data.europeana.eu/aggregation/provider/11602/HERBARWUXWUXAUSTRIAX102495\" }\n" +
            "      } ,\n" +
            "      { \n" +
            "        \"@id\" : \"https://sws.geonames.org/390903/\" ,\n" +
            "        \"@type\" : \"edm:Place\" ,\n" +
            "        \"skos:prefLabel\" : {\n" +
            "            \"@language\" : \"en\" ,\n" +
            "            \"@value\" : \"Greece\"\n" +
            "          }\n" +
            "      } ,\n" +
            "      { \n" +
            "        \"@id\" : \"https://www.jacq.org/detail.php?ID=102495\" ,\n" +
            "        \"@type\" : \"edm:WebResource\" ,\n" +
            "        \"dc:format\" : \"text/html\" ,\n" +
            "        \"dc:rights\" : \"This work is licensed under a Creative Commons Attribution-ShareAlike 3.0 Unported License.\" ,\n" +
            "        \"ebucore:fileByteSize\" : {\n" +
            "            \"@type\" : \"http://www.w3.org/2001/XMLSchema#long\" ,\n" +
            "            \"@value\" : \"0\"\n" +
            "          } ,\n" +
            "        \"ebucore:hasMimeType\" : \"text/html\" ,\n" +
            "        \"edm:rights\" : {\"@id\" : \"http://creativecommons.org/licenses/by-sa/3.0/\" }\n" +
            "      } ,\n" +
            "      { \n" +
            "        \"@id\" : \"https://www.jacq.org/image.php?method=europeana&filename=wu_0033248\" ,\n" +
            "        \"@type\" : \"edm:WebResource\" ,\n" +
            "        \"dc:format\" : {\n" +
            "            \"@language\" : \"en\" ,\n" +
            "            \"@value\" : \"image\"\n" +
            "          } ,\n" +
            "        \"dc:rights\" : \"This work is licensed under a Creative Commons Attribution-ShareAlike 3.0 Unported License.\" ,\n" +
            "        \"ebucore:fileByteSize\" : {\n" +
            "            \"@type\" : \"http://www.w3.org/2001/XMLSchema#long\" ,\n" +
            "            \"@value\" : \"466687\"\n" +
            "          } ,\n" +
            "        \"ebucore:hasMimeType\" : \"image/jpeg\" ,\n" +
            "        \"ebucore:height\" : 1800 ,\n" +
            "        \"ebucore:orientation\" : \"portrait\" ,\n" +
            "        \"ebucore:width\" : 1200 ,\n" +
            "        \"edm:componentColor\" : [ \n" +
            "            { \n" +
            "              \"@type\" : \"http://www.w3.org/2001/XMLSchema#hexBinary\" ,\n" +
            "              \"@value\" : \"#EEE8AA\"\n" +
            "            } ,\n" +
            "            { \n" +
            "              \"@type\" : \"http://www.w3.org/2001/XMLSchema#hexBinary\" ,\n" +
            "              \"@value\" : \"#F5DEB3\"\n" +
            "            } ,\n" +
            "            { \n" +
            "              \"@type\" : \"http://www.w3.org/2001/XMLSchema#hexBinary\" ,\n" +
            "              \"@value\" : \"#D2B48C\"\n" +
            "            } ,\n" +
            "            { \n" +
            "              \"@type\" : \"http://www.w3.org/2001/XMLSchema#hexBinary\" ,\n" +
            "              \"@value\" : \"#BDB76B\"\n" +
            "            } ,\n" +
            "            { \n" +
            "              \"@type\" : \"http://www.w3.org/2001/XMLSchema#hexBinary\" ,\n" +
            "              \"@value\" : \"#696969\"\n" +
            "            } ,\n" +
            "            { \n" +
            "              \"@type\" : \"http://www.w3.org/2001/XMLSchema#hexBinary\" ,\n" +
            "              \"@value\" : \"#DEB887\"\n" +
            "            }\n" +
            "          ] ,\n" +
            "        \"edm:hasColorSpace\" : \"sRGB\" ,\n" +
            "        \"edm:rights\" : {\"@id\" : \"http://creativecommons.org/licenses/by-sa/3.0/\" }\n" +
            "      }\n" +
            "    ] ,\n" +
            "  \"@context\" : {\n" +
            "      \"rdf\" : \"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" ,\n" +
            "      \"dc\" : \"http://purl.org/dc/elements/1.1/\" ,\n" +
            "      \"dcterms\" : \"http://purl.org/dc/terms/\" ,\n" +
            "      \"edm\" : \"http://www.europeana.eu/schemas/edm/\" ,\n" +
            "      \"owl\" : \"http://www.w3.org/2002/07/owl#\" ,\n" +
            "      \"wgs84_pos\" : \"http://www.w3.org/2003/01/geo/wgs84_pos#\" ,\n" +
            "      \"skos\" : \"http://www.w3.org/2004/02/skos/core#\" ,\n" +
            "      \"rdaGr2\" : \"http://rdvocab.info/ElementsGr2/\" ,\n" +
            "      \"foaf\" : \"http://xmlns.com/foaf/0.1/\" ,\n" +
            "      \"ebucore\" : \"http://www.ebu.ch/metadata/ontologies/ebucore/ebucore#\" ,\n" +
            "      \"doap\" : \"http://usefulinc.com/ns/doap#\" ,\n" +
            "      \"odrl\" : \"http://www.w3.org/ns/odrl/2/\" ,\n" +
            "      \"cc\" : \"http://creativecommons.org/ns#\" ,\n" +
            "      \"ore\" : \"http://www.openarchives.org/ore/terms/\" ,\n" +
            "      \"svcs\" : \"http://rdfs.org/sioc/services#\" ,\n" +
            "      \"oa\" : \"http://www.w3.org/ns/oa#\" ,\n" +
            "      \"dqv\" : \"http://www.w3.org/ns/dqv#\"\n" +
            "    } ,\n" +
            "  \"iiif_url\" : \"https://fresenia.man.poznan.pl/api/transcription/iiif/manifest?recordId=/11602/HERBARWUXWUXAUSTRIAX102495\"\n" +
            "}");

    private final JsonObject RECORD_JSON_RAW = JSON.parse("{ \n" +
            "  \"apikey\" : \"api2demo\" ,\n" +
            "  \"success\" : true ,\n" +
            "  \"statsDuration\" : 160 ,\n" +
            "  \"requestNumber\" : 999 ,\n" +
            "  \"object\" : {\n" +
            "      \"about\" : \"/11602/HERBARWUXWUXAUSTRIAX102495\" ,\n" +
            "      \"aggregations\" : [ \n" +
            "          { \n" +
            "            \"about\" : \"/aggregation/provider/11602/HERBARWUXWUXAUSTRIAX102495\" ,\n" +
            "            \"edmDataProvider\" : {\"def\" : [ \"University of Vienna, Institute for Botany - Herbarium WU\" ] } ,\n" +
            "            \"edmIsShownBy\" : \"https://www.jacq.org/image.php?method=europeana&filename=wu_0033248\" ,\n" +
            "            \"edmIsShownAt\" : \"https://www.jacq.org/detail.php?ID=102495\" ,\n" +
            "            \"edmObject\" : \"https://www.jacq.org/image.php?method=europeana&filename=wu_0033248\" ,\n" +
            "            \"edmProvider\" : {\"def\" : [ \"OpenUp!\" ] } ,\n" +
            "            \"edmRights\" : {\"def\" : [ \"http://creativecommons.org/licenses/by-sa/3.0/\" ] } ,\n" +
            "            \"edmUgc\" : \"false\" ,\n" +
            "            \"aggregatedCHO\" : \"/item/11602/HERBARWUXWUXAUSTRIAX102495\" ,\n" +
            "            \"webResources\" : [ \n" +
            "                { \n" +
            "                  \"webResourceDcRights\" : {\"def\" : [ \"This work is licensed under a Creative Commons Attribution-ShareAlike 3.0 Unported License.\" ] } ,\n" +
            "                  \"webResourceEdmRights\" : {\"def\" : [ \"http://creativecommons.org/licenses/by-sa/3.0/\" ] } ,\n" +
            "                  \"about\" : \"https://www.jacq.org/detail.php?ID=102495\" ,\n" +
            "                  \"dcFormat\" : {\"def\" : [ \"text/html\" ] } ,\n" +
            "                  \"textAttributionSnippet\" : \"(la) Silene genistifolia Halácsy - https://www.europeana.eu/item/11602/HERBARWUXWUXAUSTRIAX102495. University of Vienna, Institute for Botany - Herbarium WU - https://www.jacq.org/detail.php?ID=102495. CC BY-SA - http://creativecommons.org/licenses/by-sa/3.0/\" ,\n" +
            "                  \"htmlAttributionSnippet\" : \"<link rel='stylesheet' type='text/css' href='https://api.europeana.eu/attribution/style.css'/><dl class='europeana-attribution' lang='en'><dt>Title<\\/dt><dd lang='la'><a href='http://data.europeana.eu/item/11602/HERBARWUXWUXAUSTRIAX102495' target='_blank' rel='noopener'>Silene genistifolia Halácsy<\\/a><\\/dd><dt>Institution<\\/dt><dd lang=''><a href='https://www.jacq.org/detail.php?ID=102495' target='_blank' rel='noopener'>University of Vienna, Institute for Botany - Herbarium WU<\\/a><\\/dd><dt>Country<\\/dt><dd lang=''>Austria<\\/dd><dt>Rights<\\/dt><dd><a href='http://creativecommons.org/licenses/by-sa/3.0/' target='_blank' rel='noopener'><span class='icon-cc'/><span class='icon-by'/><span class='icon-sa'/>CC BY-SA<\\/a><\\/dd><\\/dl>\" ,\n" +
            "                  \"ebucoreHasMimeType\" : \"text/html\" ,\n" +
            "                  \"ebucoreFileByteSize\" : 0\n" +
            "                } ,\n" +
            "                { \n" +
            "                  \"webResourceDcRights\" : {\"def\" : [ \"This work is licensed under a Creative Commons Attribution-ShareAlike 3.0 Unported License.\" ] } ,\n" +
            "                  \"webResourceEdmRights\" : {\"def\" : [ \"http://creativecommons.org/licenses/by-sa/3.0/\" ] } ,\n" +
            "                  \"about\" : \"https://www.jacq.org/image.php?method=europeana&filename=wu_0033248\" ,\n" +
            "                  \"dcFormat\" : {\"en\" : [ \"image\" ] } ,\n" +
            "                  \"textAttributionSnippet\" : \"(la) Silene genistifolia Halácsy - https://www.europeana.eu/item/11602/HERBARWUXWUXAUSTRIAX102495. University of Vienna, Institute for Botany - Herbarium WU - https://www.jacq.org/detail.php?ID=102495. CC BY-SA - http://creativecommons.org/licenses/by-sa/3.0/\" ,\n" +
            "                  \"htmlAttributionSnippet\" : \"<link rel='stylesheet' type='text/css' href='https://api.europeana.eu/attribution/style.css'/><dl class='europeana-attribution' lang='en'><dt>Title<\\/dt><dd lang='la'><a href='http://data.europeana.eu/item/11602/HERBARWUXWUXAUSTRIAX102495' target='_blank' rel='noopener'>Silene genistifolia Halácsy<\\/a><\\/dd><dt>Institution<\\/dt><dd lang=''><a href='https://www.jacq.org/detail.php?ID=102495' target='_blank' rel='noopener'>University of Vienna, Institute for Botany - Herbarium WU<\\/a><\\/dd><dt>Country<\\/dt><dd lang=''>Austria<\\/dd><dt>Rights<\\/dt><dd><a href='http://creativecommons.org/licenses/by-sa/3.0/' target='_blank' rel='noopener'><span class='icon-cc'/><span class='icon-by'/><span class='icon-sa'/>CC BY-SA<\\/a><\\/dd><\\/dl>\" ,\n" +
            "                  \"edmComponentColor\" : [ \n" +
            "                      \"#D2B48C\" ,\n" +
            "                      \"#F5DEB3\" ,\n" +
            "                      \"#EEE8AA\" ,\n" +
            "                      \"#DEB887\" ,\n" +
            "                      \"#BDB76B\" ,\n" +
            "                      \"#696969\"\n" +
            "                    ] ,\n" +
            "                  \"ebucoreOrientation\" : \"portrait\" ,\n" +
            "                  \"ebucoreHasMimeType\" : \"image/jpeg\" ,\n" +
            "                  \"ebucoreFileByteSize\" : 466687 ,\n" +
            "                  \"ebucoreWidth\" : 1200 ,\n" +
            "                  \"ebucoreHeight\" : 1800 ,\n" +
            "                  \"edmHasColorSpace\" : \"sRGB\"\n" +
            "                }\n" +
            "              ]\n" +
            "          }\n" +
            "        ] ,\n" +
            "      \"edmDatasetName\" : [ \"11602_Herbarium_WU\" ] ,\n" +
            "      \"europeanaAggregation\" : {\n" +
            "          \"about\" : \"/aggregation/europeana/11602/HERBARWUXWUXAUSTRIAX102495\" ,\n" +
            "          \"aggregatedCHO\" : \"/item/11602/HERBARWUXWUXAUSTRIAX102495\" ,\n" +
            "          \"edmCountry\" : {\"def\" : [ \"Austria\" ] } ,\n" +
            "          \"edmLanguage\" : {\"def\" : [ \"mul\" ] } ,\n" +
            "          \"edmPreview\" : \"https://api.europeana.eu/thumbnail/v2/url.json?uri=https%3A%2F%2Fwww.jacq.org%2Fimage.php%3Fmethod%3Deuropeana%26filename%3Dwu_0033248&type=IMAGE\" ,\n" +
            "          \"edmLandingPage\" : \"https://www.europeana.eu/item/11602/HERBARWUXWUXAUSTRIAX102495\" ,\n" +
            "          \"dqvHasQualityAnnotation\" : [ \n" +
            "              \"/item/11602/HERBARWUXWUXAUSTRIAX102495#contentTier\" ,\n" +
            "              \"/item/11602/HERBARWUXWUXAUSTRIAX102495#metadataTier\"\n" +
            "            ]\n" +
            "        } ,\n" +
            "      \"europeanaCollectionName\" : [ \"11602_Herbarium_WU\" ] ,\n" +
            "      \"europeanaCompleteness\" : 10 ,\n" +
            "      \"places\" : [ \n" +
            "          { \n" +
            "            \"about\" : \"https://sws.geonames.org/390903/\" ,\n" +
            "            \"prefLabel\" : {\"en\" : [ \"Greece\" ] }\n" +
            "          }\n" +
            "        ] ,\n" +
            "      \"providedCHOs\" : [ \n" +
            "          { \"about\" : \"/item/11602/HERBARWUXWUXAUSTRIAX102495\" }\n" +
            "        ] ,\n" +
            "      \"proxies\" : [ \n" +
            "          { \n" +
            "            \"about\" : \"/proxy/europeana/11602/HERBARWUXWUXAUSTRIAX102495\" ,\n" +
            "            \"dcIdentifier\" : {\"def\" : [ \"HERBARWUXWUXAUSTRIAX102495\" ] } ,\n" +
            "            \"proxyIn\" : [ \"/aggregation/europeana/11602/HERBARWUXWUXAUSTRIAX102495\" ] ,\n" +
            "            \"proxyFor\" : \"/item/11602/HERBARWUXWUXAUSTRIAX102495\" ,\n" +
            "            \"lineage\" : [ \"/proxy/provider/11602/HERBARWUXWUXAUSTRIAX102495\" ] ,\n" +
            "            \"europeanaProxy\" : true\n" +
            "          } ,\n" +
            "          { \n" +
            "            \"about\" : \"/proxy/provider/11602/HERBARWUXWUXAUSTRIAX102495\" ,\n" +
            "            \"dcContributor\" : {\n" +
            "                \"def\" : [ \n" +
            "                    \"Sintenis,P.E.E. (collector)\" ,\n" +
            "                    \"Bornmüller,J.F.N. (collector)\" ,\n" +
            "                    \"W. Greuter (B) 1976-02-08 (identifier)\"\n" +
            "                  ]\n" +
            "              } ,\n" +
            "            \"dcDescription\" : {\"def\" : [ \"M. Lachmayer & D. Reich (WU) 2018-08-26: Lectotypification by W. Greuter (1997) in Strid & Tan Fl. Hellenica 1 pg. 290, however he cites the voucher in the herbarium WU generale but noted \\u201Eholotypus\\u201C on the one in WU-Hal and \\u201Eisotypus\\u201C on the one in WU generale. Thus, we here designate \\u2026.\" ] } ,\n" +
            "            \"dcIdentifier\" : {\"def\" : [ \"WU - Herbarium WU - 102495\" ] } ,\n" +
            "            \"dcRelation\" : {\"def\" : [ \"http://www.biodiversitylibrary.org/name/Silene_genistifolia_Hal%E1csy\" ] } ,\n" +
            "            \"dcSource\" : {\"def\" : [ \"University of Vienna, Institute for Botany - Herbarium WU\" ] } ,\n" +
            "            \"dcTitle\" : {\"la\" : [ \"Silene genistifolia Halácsy\" ] } ,\n" +
            "            \"dcType\" : {\"en\" : [ \"Preserved Specimen\" ] } ,\n" +
            "            \"dctermsSpatial\" : {\"def\" : [ \"Peninsula Hagion Oros. Mt. Athos: Stratidochi, in prat. subalpin.\" ] } ,\n" +
            "            \"edmHasMet\" : {\"def\" : [ \"https://sws.geonames.org/390903/\" ] } ,\n" +
            "            \"edmHasType\" : {\"def\" : [ \"http://rs.tdwg.org/dwc/terms/PreservedSpecimen\" ] } ,\n" +
            "            \"edmCurrentLocation\" : {\"def\" : [ \"Rennweg 14, A-1030 Vienna, Austria / Europe\" ] } ,\n" +
            "            \"proxyIn\" : [ \"/aggregation/provider/11602/HERBARWUXWUXAUSTRIAX102495\" ] ,\n" +
            "            \"proxyFor\" : \"/item/11602/HERBARWUXWUXAUSTRIAX102495\" ,\n" +
            "            \"edmType\" : \"IMAGE\" ,\n" +
            "            \"europeanaProxy\" : false\n" +
            "          }\n" +
            "        ] ,\n" +
            "      \"qualityAnnotations\" : [ \n" +
            "          { \n" +
            "            \"about\" : \"/item/11602/HERBARWUXWUXAUSTRIAX102495#contentTier\" ,\n" +
            "            \"created\" : \"2021-07-20T14:39:13.812923Z\" ,\n" +
            "            \"target\" : [ \"/aggregation/provider/11602/HERBARWUXWUXAUSTRIAX102495\" ] ,\n" +
            "            \"body\" : \"http://www.europeana.eu/schemas/epf/contentTier4\"\n" +
            "          } ,\n" +
            "          { \n" +
            "            \"about\" : \"/item/11602/HERBARWUXWUXAUSTRIAX102495#metadataTier\" ,\n" +
            "            \"created\" : \"2021-07-20T14:39:13.813242Z\" ,\n" +
            "            \"target\" : [ \"/aggregation/provider/11602/HERBARWUXWUXAUSTRIAX102495\" ] ,\n" +
            "            \"body\" : \"http://www.europeana.eu/schemas/epf/metadataTierA\"\n" +
            "          }\n" +
            "        ] ,\n" +
            "      \"timestamp_created\" : \"2021-07-20T07:37:31.787Z\" ,\n" +
            "      \"timestamp_created_epoch\" : 1626766651787 ,\n" +
            "      \"timestamp_update\" : \"2021-07-20T07:37:31.787Z\" ,\n" +
            "      \"timestamp_update_epoch\" : 1626766651787 ,\n" +
            "      \"type\" : \"IMAGE\"\n" +
            "    }\n" +
            "}");

    private final JsonObject AGGREGATOR_DATA = this.RECORD_JSON.get("@graph").getAsArray().stream()
            .map(JsonValue::getAsObject)
            .filter(e -> e.get("edm:isShownBy") != null)
            .findFirst().get();

    private ConversionDataHolderTransformer conversionDataHolderTransformer;

    @Before
    public void initConversionDataHolderTransformer() {
        this.conversionDataHolderTransformer = new ConversionDataHolderTransformer();
    }

    @SneakyThrows
    @Test
    public void ifPresentedContextWithEuropeanaAggregatorSet_willReturnEuropeanaDH() {
        Record record = new Record();
        record.setAggregator(Aggregator.EUROPEANA);
        ConversionTaskContext sampleContext = new ConversionTaskContext();
        sampleContext.setRecordJson(this.RECORD_JSON.toString());
        sampleContext.setRecordJsonRaw(this.RECORD_JSON_RAW.toString());
        sampleContext.setRawConversionData(new ArrayList<>());
        sampleContext.setRecord(record);

        ConversionDataHolder conversionDataHolder = this.conversionDataHolderTransformer.toConversionDataHolder(sampleContext);
        assertEquals(conversionDataHolder.getClass(), EuropeanaConversionDataHolder.class);
    }

    // TODO: discus how to access srcFile property on this dataholder
//    @Test
//    public void ifPresentedEuropeanaDH_willTransformCorrectly() {
//        EuropeanaConversionDataHolder europeanaConversionDataHolder = new EuropeanaConversionDataHolder(this.RECORD_IDENTIFIER, this.AGGREGATOR_DATA, this.RECORD_JSON, this.RECORD_JSON_RAW);
//        List<ConversionData> conversionData = this.conversionDataHolderTransformer.toDBModel(europeanaConversionDataHolder);
//        assertNotNull(conversionData);
//    }
}

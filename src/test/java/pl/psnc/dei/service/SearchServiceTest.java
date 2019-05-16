package pl.psnc.dei.service;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import pl.psnc.dei.response.search.Facet;
import pl.psnc.dei.response.search.FacetField;
import pl.psnc.dei.response.search.Item;
import pl.psnc.dei.response.search.SearchResponse;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.ArrayList;
import java.util.function.Function;
import java.util.function.Predicate;

import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class SearchServiceTest {

    private static final String RESPONSE_OK = "{\"apikey\":\"api2demo\",\"success\":true,\"requestNumber\":999,\"itemsCount\":12,\"totalResults\":2160,\"nextCursor\":\"AoQhVHzEgrz84wJKPC8yMDIwNjAxL2NvbnRyaWJ1dGlvbnNfMTg1MTg=\",\"items\":[{\"id\":\"/2084002/contributions_166844b0_0b40_0136_5317_4a53da2989f4\",\"ugc\":[true],\"completeness\":10,\"country\":[\"Europe\"],\"europeanaCollectionName\":[\"2084002_Ag_EU_Migration_ugc\"],\"dcLanguage\":[\"eng\"],\"edmConceptPrefLabelLangAware\":{\"no\":[\"Migrasjon\"],\"sv\":[\"Mänsklig migration\"],\"fi\":[\"Kansainvaellus\"],\"pt\":[\"Migração humana\"],\"bg\":[\"Миграция (хора)\"],\"en\":[\"Human migration\"],\"lv\":[\"Cilvēku migrācija\"],\"fr\":[\"Migration humaine\"],\"es\":[\"Migración humana\"],\"zh\":[\"人口遷徙\"],\"et\":[\"Ränne\"],\"ar\":[\"هجرة بشرية\"],\"ka\":[\"მიგრაცია\"],\"uk\":[\"Міграція населення\"],\"sl\":[\"Migracije\"],\"pl\":[\"Migracja ludności\"],\"he\":[\"נדידת עמים\"],\"nl\":[\"Menselijke migratie\"],\"mk\":[\"Статистички миграции\"],\"sq\":[\"Emigrimi\"],\"tr\":[\"İnsan göçleri\"],\"ca\":[\"Moviments migratoris\"],\"sr\":[\"Миграција становништва\"]},\"edmPlaceAltLabelLangAware\":{\"yi\":[\"װאַרשע\"],\"eu\":[\"Varsovia\"],\"def\":[\"Varsavia\",\"Warszaw\",\"Varshava\"],\"uk\":[\"Брюссель\"],\"io\":[\"Bruxelles\"],\"en\":[\"City of Brussels\"],\"eo\":[\"Bruselo\"],\"it\":[\"Città di Bruxelles\"],\"id\":[\"Kota Brusel\"],\"zh\":[\"華沙\"],\"ca\":[\"Brussel·les\"]},\"dcDescriptionLangAware\":{\"en\":[\"Ces photos montrent une coupe d'argent, un objet très précieux pour ma famille. Elle raconte l'histoire de migration de mon arrière-grand-père, Henry Merzbach (né Henryk Merzbacha) de la Pologne à la Belgique. La coupe témoigne d’une tradition de ma famille polonaise-belge depuis 1836. Mon arrière-arrière-grand-père Sigismond-Henry Merzbach l’a commandé pour ses parents pour célébrer leurs vingt-cinquièmes anniversaires de mariage. Il l’a laissé graver (traduit de l’allemand) : «SHM et Hélène à leurs parents bien-aimés en souvenir de leurs noces d’argent, le 18 mars 1836, en attendant celles d’or.» La gravure est en allemand parce que la famille Merzbach origine de Poznan (Posnanie), une ville polonaise proche de la frontière allemande. Avec ce cadeau, ils ont commencé une longue tradition familiale. Pour plus d’un siècle, la coupe était offerte aux différents membres de la famille Merzbach pour célébrer leurs noces d’argent. La deuxième inscription était pour Klara Merzbach qui la recevait en 1880 de sa maman Hélène Lesser (veuve de Sigismond-Henry depuis 1852). A cette époque, la coupe était encore en Pologne. En 1895, quand Klara donnait la coupe à son frère, Henry Merzbach, pour ses noces d’argent avec Pauline Le Hardy de Beaulieu, la coupe passait alors de la Pologne en Belgique. Celle-ci est la dernière inscription en allemand. La dernière gravure sur la coupe date de 1937, quand Pauline Merzbach l’offrit à son fils et à sa belle-fille, Charles et Jeanne Merzbach à l’occasion de leur noce d’argent. La gravure témoigne de l’émigration de la coupe comme c’est la première en français. Après 1937, la tradition s’est interrompue. Mes grands-parents sont décédés trop jeunes pour pouvoir offrir la coupe à mes parents pour leurs noces d’argent. Et comme c’est la coutume que les parents ou famille l’offrent au couple, ils n’eurent pas d’inscription. Mon père, Guy Merzbach, l’a hérité néanmoins et l’a gardé précieusement. Il m’a raconté la coutume qui veut que la coupe soit remplie de champagne lors de la cérémonie des noces d’argent et tous les invités sont amenés à y boire. J’espère que la tradition de la coupe d’argent pourra continuer au travers des générations à venir.\"]},\"dcSubjectLangAware\":{\"def\":[\"http://vocabularies.unesco.org/thesaurus/concept9602\",\"http://vocabularies.unesco.org/thesaurus/concept299\",\"http://vocabularies.unesco.org/thesaurus/concept2474\",\"Emigration from Poland\",\"http://vocabularies.unesco.org/thesaurus/concept434\",\"Immigration to Belgium\",\"http://contribute.europeana.eu/contributions/166844b0-0b40-0136-5317-4a53da2989f4#agent-27a024a0-0dac-0136-7c66-36c6ef73fcc6\",\"http://vocabularies.unesco.org/thesaurus/concept5473\",\"Brussels\",\"http://contribute.europeana.eu/contributions/166844b0-0b40-0136-5317-4a53da2989f4#agent-279fbf20-0dac-0136-7c66-36c6ef73fcc6\",\"http://vocabularies.unesco.org/thesaurus/concept893\",\"http://vocabularies.unesco.org/thesaurus/concept918\",\"http://contribute.europeana.eu/contributions/166844b0-0b40-0136-5317-4a53da2989f4#agent-279e7d50-0dac-0136-7c66-36c6ef73fcc6\",\"http://contribute.europeana.eu/contributions/166844b0-0b40-0136-5317-4a53da2989f4#agent-279f5480-0dac-0136-7c66-36c6ef73fcc6\",\"Heirlooms\",\"http://contribute.europeana.eu/contributions/166844b0-0b40-0136-5317-4a53da2989f4#agent-166931b0-0b40-0136-5317-4a53da2989f4\",\"http://contribute.europeana.eu/contributions/166844b0-0b40-0136-5317-4a53da2989f4#agent-279eec30-0dac-0136-7c66-36c6ef73fcc6\",\"http://contribute.europeana.eu/contributions/166844b0-0b40-0136-5317-4a53da2989f4#agent-27a08510-0dac-0136-7c66-36c6ef73fcc6\",\"http://data.europeana.eu/concept/base/128\"]},\"edmIsShownBy\":[\"http://contribute.europeana.eu/media/166739c0-0b40-0136-5317-4a53da2989f4\"],\"dcDescription\":[\"Ces photos montrent une coupe d'argent, un objet très précieux pour ma famille. Elle raconte l'histoire de migration de mon arrière-grand-père, Henry Merzbach (né Henryk Merzbacha) de la Pologne à la Belgique. La coupe témoigne d’une tradition de ma famille polonaise-belge depuis 1836. Mon arrière-arrière-grand-père Sigismond-Henry Merzbach l’a commandé pour ses parents pour célébrer leurs vingt-cinquièmes anniversaires de mariage. Il l’a laissé graver (traduit de l’allemand) : «SHM et Hélène à leurs parents bien-aimés en souvenir de leurs noces d’argent, le 18 mars 1836, en attendant celles d’or.» La gravure est en allemand parce que la famille Merzbach origine de Poznan (Posnanie), une ville polonaise proche de la frontière allemande. Avec ce cadeau, ils ont commencé une longue tradition familiale. Pour plus d’un siècle, la coupe était offerte aux différents membres de la famille Merzbach pour célébrer leurs noces d’argent. La deuxième inscription était pour Klara Merzbach qui la recevait en 1880 de sa maman Hélène Lesser (veuve de Sigismond-Henry depuis 1852). A cette époque, la coupe était encore en Pologne. En 1895, quand Klara donnait la coupe à son frère, Henry Merzbach, pour ses noces d’argent avec Pauline Le Hardy de Beaulieu, la coupe passait alors de la Pologne en Belgique. Celle-ci est la dernière inscription en allemand. La dernière gravure sur la coupe date de 1937, quand Pauline Merzbach l’offrit à son fils et à sa belle-fille, Charles et Jeanne Merzbach à l’occasion de leur noce d’argent. La gravure témoigne de l’émigration de la coupe comme c’est la première en français. Après 1937, la tradition s’est interrompue. Mes grands-parents sont décédés trop jeunes pour pouvoir offrir la coupe à mes parents pour leurs noces d’argent. Et comme c’est la coutume que les parents ou famille l’offrent au couple, ils n’eurent pas d’inscription. Mon père, Guy Merzbach, l’a hérité néanmoins et l’a gardé précieusement. Il m’a raconté la coutume qui veut que la coupe soit remplie de champagne lors de la cérémonie des noces d’argent et tous les invités sont amenés à y boire. J’espère que la tradition de la coupe d’argent pourra continuer au travers des générations à venir.\"],\"edmConcept\":[\"http://data.europeana.eu/concept/base/128\"],\"edmConceptLabel\":[{\"def\":\"Migrasjon\"},{\"def\":\"Mänsklig migration\"},{\"def\":\"Kansainvaellus\"},{\"def\":\"Migração humana\"},{\"def\":\"Миграция (хора)\"},{\"def\":\"Human migration\"},{\"def\":\"Cilvēku migrācija\"},{\"def\":\"Migration humaine\"},{\"def\":\"Migración humana\"},{\"def\":\"人口遷徙\"},{\"def\":\"Ränne\"},{\"def\":\"هجرة بشرية\"},{\"def\":\"მიგრაცია\"},{\"def\":\"Міграція населення\"},{\"def\":\"Migracije\"},{\"def\":\"Migracja ludności\"},{\"def\":\"נדידת עמים\"},{\"def\":\"Menselijke migratie\"},{\"def\":\"Статистички миграции\"},{\"def\":\"Emigrimi\"},{\"def\":\"İnsan göçleri\"},{\"def\":\"Moviments migratoris\"},{\"def\":\"Миграција становништва\"}],\"title\":[\"La coupe d'argent de ma famille européenne\"],\"rights\":[\"http://creativecommons.org/licenses/by-sa/4.0/\"],\"dctermsSpatial\":[\"http://data.europeana.eu/place/base/134235\",\"http://data.europeana.eu/place/base/203157\"],\"dataProvider\":[\"Europeana Foundation\"],\"dcTitleLangAware\":{\"en\":[\"La coupe d'argent de ma famille européenne\"]},\"dcContributorLangAware\":{\"def\":[\"-Georges Merzbach\"]},\"dcLanguageLangAware\":{\"def\":[\"eng\"]},\"europeanaCompleteness\":10,\"edmPlace\":[\"http://data.europeana.eu/place/base/134235\",\"http://data.europeana.eu/place/base/203157\"],\"edmPlaceLabel\":[{\"def\":\"Warschau\"},{\"def\":\"Brüssel\"},{\"def\":\"Warsaw\"},{\"def\":\"Brussels\"},{\"def\":\"Varsóvia\"},{\"def\":\"Bruxelas\"},{\"def\":\"Varšuva\"},{\"def\":\"Briuselis\"},{\"def\":\"Varšava\"},{\"def\":\"Bruxelles\"},{\"def\":\"Varsó\"},{\"def\":\"Brüsszel\"},{\"def\":\"ווארשע\"},{\"def\":\"Վարշավա\"},{\"def\":\"Բրյուսել\"},{\"def\":\"ۋارشاۋا\"},{\"def\":\"بريۇسسېل\"},{\"def\":\"Варшава\"},{\"def\":\"Брюсель\"},{\"def\":\"Varsovia\"},{\"def\":\"Warsawa\"},{\"def\":\"Brusel\"},{\"def\":\"Варшава\"},{\"def\":\"Брисел\"},{\"def\":\"Warskou\"},{\"def\":\"Brussel\"},{\"def\":\"वॉर्सो\"},{\"def\":\"ब्रसेल्स\"},{\"def\":\"Warszawa\"},{\"def\":\"Bruxel\"},{\"def\":\"Βαρσοβία\"},{\"def\":\"Βρυξέλλες\"},{\"def\":\"Varsavja\"},{\"def\":\"Warsaw\"},{\"def\":\"Brussels\"},{\"def\":\"Varsovio\"},{\"def\":\"Brukselo\"},{\"def\":\"Varsjá\"},{\"def\":\"Brussel\"},{\"def\":\"Varsavia\"},{\"def\":\"Bruxelles\"},{\"def\":\"ዋርሶው\"},{\"def\":\"Varsovia\"},{\"def\":\"Bruselas\"},{\"def\":\"华沙\"},{\"def\":\"Varssavi\"},{\"def\":\"Brüssel\"},{\"def\":\"Barsobia\"},{\"def\":\"Brusela\"},{\"def\":\"وارسو\"},{\"def\":\"بروكسل\"},{\"def\":\"Warszawa\"},{\"def\":\"Warsaw\"},{\"def\":\"ワルシャワ\"},{\"def\":\"ブリュッセル\"},{\"def\":\"Warszawa\"},{\"def\":\"ورشو\"},{\"def\":\"بروکسل\"},{\"def\":\"Varșovia\"},{\"def\":\"Bruxelles\"},{\"def\":\"Warschau\"},{\"def\":\"Brussel\"},{\"def\":\"Warszawa\"},{\"def\":\"Brussel\"},{\"def\":\"Warszawa\"},{\"def\":\"Brussel\"},{\"def\":\"Варшава\"},{\"def\":\"Брусэль\"},{\"def\":\"Varsova\"},{\"def\":\"Bryssel\"},{\"def\":\"Варшава\"},{\"def\":\"Брюссель\"},{\"def\":\"Варшава\"},{\"def\":\"Брюксел\"},{\"def\":\"Varsovie\"},{\"def\":\"Bruxelles\"},{\"def\":\"Warsawa\"},{\"def\":\"Brusel\"},{\"def\":\"Varsovia\"},{\"def\":\"Varšava\"},{\"def\":\"Brisel\"},{\"def\":\"ვარშავა\"},{\"def\":\"ბრიუსელი\"},{\"def\":\"Varšava\"},{\"def\":\"Varšava\"},{\"def\":\"Bruselj\"},{\"def\":\"Vársá\"},{\"def\":\"An Bhruiséil\"},{\"def\":\"Warsaw\"},{\"def\":\"Bruiseal\"},{\"def\":\"Varsòvia\"},{\"def\":\"Brusel·les\"},{\"def\":\"Varshava\"},{\"def\":\"Brukseli\"},{\"def\":\"Варшава\"},{\"def\":\"Брисел\"},{\"def\":\"Warszawa\"},{\"def\":\"Bryssel\"},{\"def\":\"바르샤바\"},{\"def\":\"브뤼셀\"},{\"def\":\"Варшавæ\"},{\"def\":\"Varsovia - Warszawa\"},{\"def\":\"Bruxelas\"},{\"def\":\"Varšava\"},{\"def\":\"Brusel\"},{\"def\":\"Варшава\"},{\"def\":\"Варшава\"},{\"def\":\"วอร์ซอ\"},{\"def\":\"บรัสเซลส์\"},{\"def\":\"Varsovia\"},{\"def\":\"Bruxellae\"},{\"def\":\"Warschau\"},{\"def\":\"Bréissel\"},{\"def\":\"Warsaw\"},{\"def\":\"Brwsel\"},{\"def\":\"Warszawa\"},{\"def\":\"Bruksela\"},{\"def\":\"Warszawa\"},{\"def\":\"Bruxelles\"},{\"def\":\"ורשה\"},{\"def\":\"בריסל\"},{\"def\":\"Warschau\"},{\"def\":\"Brussel\"},{\"def\":\"Varşova\"},{\"def\":\"Brüksel\"},{\"def\":\"Brisele\"},{\"def\":\"Brussel\"},{\"def\":\"Brussels\"},{\"def\":\"Brussele\"},{\"def\":\"Brussel\"},{\"def\":\"Bruksel\"}],\"edmPlaceLatitude\":[\"52.22977\",\"50.85045\"],\"edmPlaceLongitude\":[\"21.01178\",\"4.34878\"],\"edmAgent\":[\"http://contribute.europeana.eu/contributions/166844b0-0b40-0136-5317-4a53da2989f4#agent-166931b0-0b40-0136-5317-4a53da2989f4\",\"http://contribute.europeana.eu/contributions/166844b0-0b40-0136-5317-4a53da2989f4#agent-279e7d50-0dac-0136-7c66-36c6ef73fcc6\",\"http://contribute.europeana.eu/contributions/166844b0-0b40-0136-5317-4a53da2989f4#agent-279eec30-0dac-0136-7c66-36c6ef73fcc6\",\"http://contribute.europeana.eu/contributions/166844b0-0b40-0136-5317-4a53da2989f4#agent-279f5480-0dac-0136-7c66-36c6ef73fcc6\",\"http://contribute.europeana.eu/contributions/166844b0-0b40-0136-5317-4a53da2989f4#agent-279fbf20-0dac-0136-7c66-36c6ef73fcc6\",\"http://contribute.europeana.eu/contributions/166844b0-0b40-0136-5317-4a53da2989f4#agent-27a024a0-0dac-0136-7c66-36c6ef73fcc6\",\"http://contribute.europeana.eu/contributions/166844b0-0b40-0136-5317-4a53da2989f4#agent-27a08510-0dac-0136-7c66-36c6ef73fcc6\"],\"dcContributor\":[\"-Georges Merzbach\"],\"edmPreview\":[\"https://api.europeana.eu/api/v2/thumbnail-by-url.json?uri=http%3A%2F%2Fcontribute.europeana.eu%2Fmedia%2F166739c0-0b40-0136-5317-4a53da2989f4&type=IMAGE\"],\"edmPlaceLabelLangAware\":{\"de\":[\"Warschau\",\"Brüssel\"],\"def\":[\"Warsaw\",\"Brussels\"],\"pt\":[\"Varsóvia\",\"Bruxelas\"],\"lt\":[\"Varšuva\",\"Briuselis\"],\"hr\":[\"Varšava\",\"Bruxelles\"],\"lv\":[\"Brisele\"],\"hu\":[\"Varsó\",\"Brüsszel\"],\"yi\":[\"ווארשע\"],\"hy\":[\"Վարշավա\",\"Բրյուսել\"],\"ug\":[\"ۋارشاۋا\",\"بريۇسسېل\"],\"uk\":[\"Варшава\",\"Брюсель\"],\"ia\":[\"Varsovia\"],\"id\":[\"Warsawa\",\"Brusel\"],\"mk\":[\"Варшава\",\"Брисел\"],\"qu\":[\"Brussel\"],\"af\":[\"Warskou\",\"Brussel\"],\"mr\":[\"वॉर्सो\",\"ब्रसेल्स\"],\"io\":[\"Warszawa\",\"Bruxel\"],\"ms\":[\"Brussels\"],\"el\":[\"Βαρσοβία\",\"Βρυξέλλες\"],\"mt\":[\"Varsavja\"],\"en\":[\"Warsaw\",\"Brussels\"],\"eo\":[\"Varsovio\",\"Brukselo\"],\"is\":[\"Varsjá\",\"Brussel\"],\"it\":[\"Varsavia\",\"Bruxelles\"],\"am\":[\"ዋርሶው\"],\"es\":[\"Varsovia\",\"Bruselas\"],\"zh\":[\"华沙\"],\"et\":[\"Varssavi\",\"Brüssel\"],\"eu\":[\"Barsobia\",\"Brusela\"],\"ar\":[\"وارسو\",\"بروكسل\"],\"vi\":[\"Warszawa\"],\"na\":[\"Warsaw\"],\"ja\":[\"ワルシャワ\",\"ブリュッセル\"],\"vo\":[\"Warszawa\"],\"fa\":[\"ورشو\",\"بروکسل\"],\"ro\":[\"Varșovia\",\"Bruxelles\"],\"nl\":[\"Warschau\",\"Brussel\"],\"nn\":[\"Warszawa\",\"Brussel\"],\"no\":[\"Warszawa\",\"Brussel\"],\"be\":[\"Варшава\",\"Брусэль\"],\"fi\":[\"Varsova\",\"Bryssel\"],\"ru\":[\"Варшава\",\"Брюссель\"],\"bg\":[\"Варшава\",\"Брюксел\"],\"wa\":[\"Brussele\"],\"fr\":[\"Varsovie\",\"Bruxelles\"],\"jv\":[\"Warsawa\",\"Brusel\"],\"br\":[\"Varsovia\"],\"bs\":[\"Varšava\",\"Brisel\"],\"fy\":[\"Brussel\"],\"ka\":[\"ვარშავა\",\"ბრიუსელი\"],\"sk\":[\"Varšava\"],\"sl\":[\"Varšava\",\"Bruselj\"],\"ga\":[\"Vársá\",\"An Bhruiséil\"],\"gd\":[\"Warsaw\",\"Bruiseal\"],\"ca\":[\"Varsòvia\",\"Brusel·les\"],\"sq\":[\"Varshava\",\"Brukseli\"],\"sr\":[\"Варшава\",\"Брисел\"],\"sv\":[\"Warszawa\",\"Bryssel\"],\"ko\":[\"바르샤바\",\"브뤼셀\"],\"os\":[\"Варшавæ\"],\"gl\":[\"Varsovia - Warszawa\",\"Bruxelas\"],\"ku\":[\"Bruksel\"],\"cs\":[\"Varšava\",\"Brusel\"],\"cu\":[\"Варшава\"],\"cv\":[\"Варшава\"],\"th\":[\"วอร์ซอ\",\"บรัสเซลส์\"],\"la\":[\"Varsovia\",\"Bruxellae\"],\"lb\":[\"Warschau\",\"Bréissel\"],\"cy\":[\"Warsaw\",\"Brwsel\"],\"pl\":[\"Warszawa\",\"Bruksela\"],\"da\":[\"Warszawa\",\"Bruxelles\"],\"he\":[\"ורשה\",\"בריסל\"],\"li\":[\"Warschau\",\"Brussel\"],\"tr\":[\"Varşova\",\"Brüksel\"]},\"previewNoDistribute\":false,\"provider\":[\"Europeana Foundation\"],\"timestamp\":1547723110050,\"score\":2.7857666,\"language\":[\"mul\"],\"type\":\"IMAGE\",\"edmDatasetName\":[\"2084002_Ag_EU_Migration_ugc\"],\"guid\":\"https://www.europeana.eu/portal/record/2084002/contributions_166844b0_0b40_0136_5317_4a53da2989f4.html?utm_source=api&utm_medium=api&utm_campaign=api2demo\",\"link\":\"https://api.europeana.eu/api/v2/record/2084002/contributions_166844b0_0b40_0136_5317_4a53da2989f4.json?wskey=api2demo\",\"timestamp_created_epoch\":1547723107533,\"timestamp_update_epoch\":1547723107533,\"timestamp_created\":\"2019-01-17T11:05:07.533Z\",\"timestamp_update\":\"2019-01-17T11:05:07.533Z\"}],\"facets\":[{\"name\":\"YEAR\",\"fields\":[{\"label\":\"1925\",\"count\":31},{\"label\":\"1832\",\"count\":27},{\"label\":\"1926\",\"count\":25},{\"label\":\"1924\",\"count\":18},{\"label\":\"1872\",\"count\":14},{\"label\":\"1928\",\"count\":12},{\"label\":\"1930\",\"count\":12},{\"label\":\"1929\",\"count\":7},{\"label\":\"1931\",\"count\":7},{\"label\":\"1948\",\"count\":6},{\"label\":\"1874\",\"count\":5},{\"label\":\"1914\",\"count\":5},{\"label\":\"1923\",\"count\":5},{\"label\":\"1937\",\"count\":5},{\"label\":\"1949\",\"count\":5},{\"label\":\"1950\",\"count\":5},{\"label\":\"1858\",\"count\":4},{\"label\":\"1859\",\"count\":4},{\"label\":\"1871\",\"count\":4},{\"label\":\"1917\",\"count\":4},{\"label\":\"1918\",\"count\":4},{\"label\":\"1920\",\"count\":4},{\"label\":\"1921\",\"count\":4},{\"label\":\"1936\",\"count\":4},{\"label\":\"1938\",\"count\":4},{\"label\":\"1863\",\"count\":3},{\"label\":\"1865\",\"count\":3},{\"label\":\"1875\",\"count\":3},{\"label\":\"1905\",\"count\":3},{\"label\":\"1910\",\"count\":3},{\"label\":\"1912\",\"count\":3},{\"label\":\"1919\",\"count\":3},{\"label\":\"1939\",\"count\":3},{\"label\":\"2009\",\"count\":3},{\"label\":\"1700\",\"count\":2},{\"label\":\"1855\",\"count\":2},{\"label\":\"1870\",\"count\":2},{\"label\":\"1873\",\"count\":2},{\"label\":\"1878\",\"count\":2},{\"label\":\"1896\",\"count\":2},{\"label\":\"1908\",\"count\":2},{\"label\":\"1911\",\"count\":2},{\"label\":\"1913\",\"count\":2},{\"label\":\"1916\",\"count\":2},{\"label\":\"1922\",\"count\":2},{\"label\":\"1927\",\"count\":2},{\"label\":\"1934\",\"count\":2},{\"label\":\"2015\",\"count\":2},{\"label\":\"1598\",\"count\":1},{\"label\":\"1651\",\"count\":1}]},{\"name\":\"COLOURPALETTE\",\"fields\":[{\"label\":\"#D2B48C\",\"count\":263},{\"label\":\"#BDB76B\",\"count\":222},{\"label\":\"#BC8F8F\",\"count\":191},{\"label\":\"#C0C0C0\",\"count\":170},{\"label\":\"#696969\",\"count\":153},{\"label\":\"#A9A9A9\",\"count\":145},{\"label\":\"#808080\",\"count\":145},{\"label\":\"#F5DEB3\",\"count\":133},{\"label\":\"#EEE8AA\",\"count\":125},{\"label\":\"#DEB887\",\"count\":107},{\"label\":\"#D3D3D3\",\"count\":90},{\"label\":\"#2F4F4F\",\"count\":84},{\"label\":\"#DCDCDC\",\"count\":65},{\"label\":\"#CD853F\",\"count\":50},{\"label\":\"#000000\",\"count\":48},{\"label\":\"#E9967A\",\"count\":45},{\"label\":\"#556B2F\",\"count\":41},{\"label\":\"#F0E68C\",\"count\":34},{\"label\":\"#FFE4C4\",\"count\":31},{\"label\":\"#8FBC8F\",\"count\":31},{\"label\":\"#F5F5DC\",\"count\":26},{\"label\":\"#F5F5F5\",\"count\":25},{\"label\":\"#FFFFFF\",\"count\":24},{\"label\":\"#800000\",\"count\":22},{\"label\":\"#FAEBD7\",\"count\":20},{\"label\":\"#FFDEAD\",\"count\":17},{\"label\":\"#A0522D\",\"count\":16},{\"label\":\"#A52A2A\",\"count\":15},{\"label\":\"#FFE4B5\",\"count\":15},{\"label\":\"#FAFAD2\",\"count\":14},{\"label\":\"#FAF0E6\",\"count\":14},{\"label\":\"#708090\",\"count\":13},{\"label\":\"#E6E6FA\",\"count\":12},{\"label\":\"#8B4513\",\"count\":12},{\"label\":\"#FFFACD\",\"count\":11},{\"label\":\"#CD5C5C\",\"count\":10},{\"label\":\"#FFFAFA\",\"count\":8},{\"label\":\"#FFB6C1\",\"count\":7},{\"label\":\"#FFEBCD\",\"count\":5},{\"label\":\"#F0FFF0\",\"count\":5},{\"label\":\"#F5FFFA\",\"count\":5},{\"label\":\"#D8BFD8\",\"count\":5},{\"label\":\"#778899\",\"count\":4},{\"label\":\"#191970\",\"count\":4},{\"label\":\"#FFE4E1\",\"count\":4},{\"label\":\"#F8F8FF\",\"count\":3},{\"label\":\"#FFDAB9\",\"count\":3},{\"label\":\"#F0F8FF\",\"count\":2},{\"label\":\"#6495ED\",\"count\":2},{\"label\":\"#FFF8DC\",\"count\":2}]},{\"name\":\"DATA_PROVIDER\",\"fields\":[{\"label\":\"Biblioteka Cyfrowa - Regionalia Ziemi Łódzkiej\",\"count\":1491},{\"label\":\"Elbląska Biblioteka Cyfrowa\",\"count\":191},{\"label\":\"Radomska Biblioteka Cyfrowa\",\"count\":174},{\"label\":\"Wielkopolska Biblioteka Cyfrowa\",\"count\":138},{\"label\":\"Biblioteka Cyfrowa Politechniki Warszawskiej\",\"count\":57},{\"label\":\"Repozytorium Cyfrowe Instytutów Naukowych\",\"count\":14},{\"label\":\"Universitätsbibliothek JCS Frankfurt am Main\",\"count\":14},{\"label\":\"Biblioteka Cyfrowa UMCS\",\"count\":11},{\"label\":\"Europeana 1914-1918\",\"count\":10},{\"label\":\"The British Library\",\"count\":10},{\"label\":\"Biblioteka Cyfrowa Uniwersytetu Wrocławskiego\",\"count\":9},{\"label\":\"Zachodniopomorska Biblioteka Cyfrowa \\\"Pomerania\\\"\",\"count\":9},{\"label\":\"e-biblioteka Uniwersytetu Warszawskiego\",\"count\":5},{\"label\":\"Staatsbibliothek zu Berlin - Preußischer Kulturbesitz\",\"count\":4},{\"label\":\"Universitätsbibliothek Heidelberg\",\"count\":3},{\"label\":\"Arbetets museum\",\"count\":2},{\"label\":\"Kujawsko-Pomorska Biblioteka Cyfrowa\",\"count\":2},{\"label\":\"Narodowy Instytut Dziedzictwa\",\"count\":2},{\"label\":\"Podlaska Biblioteka Cyfrowa\",\"count\":2},{\"label\":\"Europeana 1989\",\"count\":1},{\"label\":\"Europeana Foundation\",\"count\":1},{\"label\":\"Missouri Botanical Garden, Peter H. Raven Library\",\"count\":1},{\"label\":\"Museon\",\"count\":1},{\"label\":\"National and University Library of Slovenia\",\"count\":1},{\"label\":\"OAPEN Foundation\",\"count\":1},{\"label\":\"Rijksmuseum\",\"count\":1},{\"label\":\"Russian State Library\",\"count\":1},{\"label\":\"Sörmlands museum\",\"count\":1},{\"label\":\"The Trustees of the Natural History Museum, London\",\"count\":1},{\"label\":\"University of Graz, Institute of Plant Sciences - Herbarium GZU\",\"count\":1},{\"label\":\"Śląska Biblioteka Cyfrowa\",\"count\":1}]},{\"name\":\"PROVIDER\",\"fields\":[{\"label\":\"Federacja Bibliotek Cyfrowych\",\"count\":2104},{\"label\":\"The European Library\",\"count\":15},{\"label\":\"Judaica Europeana\",\"count\":14},{\"label\":\"Europeana 1914-1918\",\"count\":10},{\"label\":\"Swedish Open Cultural Heritage | K-samsök\",\"count\":3},{\"label\":\"Universitätsbibliothek Heidelberg\",\"count\":3},{\"label\":\"CARARE\",\"count\":2},{\"label\":\"OpenUp!\",\"count\":2},{\"label\":\"Biodiversity Heritage Library\",\"count\":1},{\"label\":\"Digitale Collectie\",\"count\":1},{\"label\":\"Europeana 1989\",\"count\":1},{\"label\":\"Europeana Foundation\",\"count\":1},{\"label\":\"OAPEN Foundation\",\"count\":1},{\"label\":\"Rijksmuseum\",\"count\":1},{\"label\":\"Slovenian National E-content Aggregator\",\"count\":1}]},{\"name\":\"RIGHTS\",\"fields\":[{\"label\":\"http://creativecommons.org/publicdomain/mark/1.0/\",\"count\":2131},{\"label\":\"http://creativecommons.org/publicdomain/zero/1.0/\",\"count\":10},{\"label\":\"http://creativecommons.org/licenses/by/3.0/pl/\",\"count\":6},{\"label\":\"http://creativecommons.org/licenses/by-sa/3.0/de\",\"count\":3},{\"label\":\"http://creativecommons.org/licenses/by-sa/4.0/\",\"count\":3},{\"label\":\"http://creativecommons.org/licenses/by/2.5/\",\"count\":2},{\"label\":\"http://creativecommons.org/licenses/by/4.0/\",\"count\":2},{\"label\":\"http://creativecommons.org/licenses/by-sa/2.5/\",\"count\":1},{\"label\":\"http://creativecommons.org/licenses/by-sa/3.0/\",\"count\":1},{\"label\":\"http://creativecommons.org/licenses/by/3.0/\",\"count\":1}]},{\"name\":\"COUNTRY\",\"fields\":[{\"label\":\"Poland\",\"count\":2107},{\"label\":\"Germany\",\"count\":21},{\"label\":\"Europe\",\"count\":11},{\"label\":\"United Kingdom\",\"count\":11},{\"label\":\"Netherlands\",\"count\":3},{\"label\":\"Sweden\",\"count\":3},{\"label\":\"Austria\",\"count\":1},{\"label\":\"Russia\",\"count\":1},{\"label\":\"Slovenia\",\"count\":1},{\"label\":\"United States of America\",\"count\":1}]},{\"name\":\"LANGUAGE\",\"fields\":[{\"label\":\"pl\",\"count\":2107},{\"label\":\"de\",\"count\":21},{\"label\":\"en\",\"count\":14},{\"label\":\"mul\",\"count\":11},{\"label\":\"sv\",\"count\":3},{\"label\":\"nl\",\"count\":2},{\"label\":\"ru\",\"count\":1},{\"label\":\"sl\",\"count\":1}]},{\"name\":\"MIME_TYPE\",\"fields\":[{\"label\":\"application/pdf\",\"count\":1770},{\"label\":\"image/png\",\"count\":327},{\"label\":\"image/jpeg\",\"count\":56},{\"label\":\"image/gif\",\"count\":9},{\"label\":\"audio/mpeg\",\"count\":2},{\"label\":\"text/plain\",\"count\":2}]},{\"name\":\"IMAGE_SIZE\",\"fields\":[{\"label\":\"large\",\"count\":25},{\"label\":\"small\",\"count\":16},{\"label\":\"extra_large\",\"count\":12},{\"label\":\"medium\",\"count\":3}]},{\"name\":\"TYPE\",\"fields\":[{\"label\":\"TEXT\",\"count\":2133},{\"label\":\"IMAGE\",\"count\":25},{\"label\":\"SOUND\",\"count\":2}]},{\"name\":\"REUSABILITY\",\"fields\":[{\"label\":\"open\",\"count\":2160},{\"label\":\"restricted\",\"count\":1401},{\"label\":\"permission\",\"count\":1867}]},{\"name\":\"TEXT_FULLTEXT\",\"fields\":[{\"label\":\"true\",\"count\":1479},{\"label\":\"false\",\"count\":681}]},{\"name\":\"LANDINGPAGE\",\"fields\":[{\"label\":\"true\",\"count\":2159},{\"label\":\"false\",\"count\":1}]},{\"name\":\"MEDIA\",\"fields\":[{\"label\":\"false\",\"count\":62789},{\"label\":\"true\",\"count\":2160}]},{\"name\":\"THUMBNAIL\",\"fields\":[{\"label\":\"true\",\"count\":2158},{\"label\":\"false\",\"count\":2}]},{\"name\":\"UGC\",\"fields\":[{\"label\":\"false\",\"count\":2148},{\"label\":\"true\",\"count\":12}]},{\"name\":\"IMAGE_ASPECTRATIO\",\"fields\":[{\"label\":\"portrait\",\"count\":301},{\"label\":\"landscape\",\"count\":99}]},{\"name\":\"IMAGE_COLOUR\",\"fields\":[{\"label\":\"true\",\"count\":348},{\"label\":\"false\",\"count\":44}]},{\"name\":\"SOUND_HQ\",\"fields\":[{\"label\":\"true\",\"count\":2}]},{\"name\":\"SOUND_DURATION\",\"fields\":[{\"label\":\"short\",\"count\":2}]}]}";

    @Mock
    private WebClient webTestClient;

    @InjectMocks
    private SearchService searchService = new SearchService(WebClient.builder());

    private void mockWebClientResponse(final SearchResponse resp) {
        final WebClient.RequestHeadersUriSpec uriSpecMock = Mockito.mock(WebClient.RequestHeadersUriSpec.class);
        final WebClient.RequestHeadersSpec headersSpecMock = Mockito.mock(WebClient.RequestHeadersSpec.class);
        final WebClient.ResponseSpec responseSpecMock = Mockito.mock(WebClient.ResponseSpec.class);

        when(webTestClient.get()).thenReturn(uriSpecMock);
//        when(uriSpecMock.uri(ArgumentMatchers.<String>notNull(), ArgumentMatchers.<String>notNull(), ArgumentMatchers.any())).thenReturn(headersSpecMock);
        when(uriSpecMock.uri(ArgumentMatchers.<Function<UriBuilder, URI>>notNull())).thenReturn(headersSpecMock);
        when(headersSpecMock.header(notNull(), notNull())).thenReturn(headersSpecMock);
        when(headersSpecMock.headers(notNull())).thenReturn(headersSpecMock);
        when(headersSpecMock.retrieve()).thenReturn(responseSpecMock);
        when(responseSpecMock.onStatus(ArgumentMatchers.any(Predicate.class), ArgumentMatchers.any(Function.class)))
                .thenReturn(responseSpecMock);
        when(responseSpecMock.bodyToMono(ArgumentMatchers.<Class<SearchResponse>>notNull()))
                .thenReturn(Mono.just(resp));
    }

    private SearchResponse getResponseOK() {
        SearchResponse searchResponse = new SearchResponse();
        searchResponse.setApikey("api2demo");
        searchResponse.setRequestNumber(999);
        searchResponse.setSuccess(true);
        searchResponse.setItemsCount(1);
        searchResponse.setTotalResults(1);
        searchResponse.setNextCursor("abc");
        searchResponse.setItems(new ArrayList<>());
        Item item = new Item();
        item.setId("1");
        searchResponse.getItems().add(item);
        searchResponse.setFacets(new ArrayList<>());
        Facet facet = new Facet();
        facet.setName("a");
        facet.setFields(new ArrayList<>());
        FacetField field = new FacetField();
        field.setCount(1);
        field.setLabel("label");
        facet.getFields().add(field);
        searchResponse.getFacets().add(facet);

        return searchResponse;
    }
    @Test(expected = IllegalStateException.class)
    public void searchWhenQueryEmpty() {
        mockWebClientResponse(getResponseOK());
        searchService.search("", null, "*");
    }

    @Test(expected = IllegalStateException.class)
    public void searchWhenCursorEmpty() {
        mockWebClientResponse(getResponseOK());
        searchService.search("abc", null, null);
    }

    @Test
    public void searchWhenResultsOK() {
        SearchResponse responseOK = getResponseOK();
        mockWebClientResponse(responseOK);
        Mono<SearchResponse> response = searchService.search("abc", null, "*");

        Assert.assertNotNull(response);
        Assert.assertEquals(response.block(), responseOK);
    }
}
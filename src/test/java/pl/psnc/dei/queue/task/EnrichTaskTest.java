package pl.psnc.dei.queue.task;

import org.apache.jena.atlas.json.JSON;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import pl.psnc.dei.model.DAO.TranscriptionRepository;
import pl.psnc.dei.model.Record;
import pl.psnc.dei.model.Transcription;
import pl.psnc.dei.service.EuropeanaAnnotationsService;
import pl.psnc.dei.service.QueueRecordService;
import pl.psnc.dei.service.TranscriptionPlatformService;
import pl.psnc.dei.service.search.EuropeanaSearchService;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("integration")
public class EnrichTaskTest {
    private final String transcribathonResponse = "[\n" +
            "   {\n" +
            "      \"EuropeanaAnnotationId\":20191,\n" +
            "      \"AnnotationId\":203544,\n" +
            "      \"Text\":\"\\u003cp class\\u003d\\\"left\\\"\\u003e\\u003cspan class\\u003d\\\"pos-in-text\\\"\\u003eOfficial buff letter from War Office relating where Jack Mallett was buried in France\\u003c/span\\u003e\\u003c/p\\u003e\\u003cp class\\u003d\\\"right\\\"\\u003e\\u003cbr\\u003e\\u003c/p\\u003e\\u003cp class\\u003d\\\"right\\\"\\u003eWar Office,\\u003c/p\\u003e\\u003cp class\\u003d\\\"right\\\"\\u003eWinchester House,\\u003c/p\\u003e\\u003cp class\\u003d\\\"right\\\"\\u003eSt. James square,\\u003c/p\\u003e\\u003cp class\\u003d\\\"right\\\"\\u003eLondon S.W.I.\\u003c/p\\u003e\\u003cp\\u003e\\u003cbr\\u003e\\u003c/p\\u003e\\u003cp\\u003eReference S.L/18/8891.\\u003c/p\\u003e\\u003cp\\u003e\\u003cbr\\u003e\\u003c/p\\u003e\\u003cp class\\u003d\\\"left\\\"\\u003eDear Madam,\\u0026nbsp; \\u003cspan class\\u003d\\\"pos-in-text\\\"\\u003eStamp in green ink with date\\u003c/span\\u003e DIRECTOR OF GRAVES REGISTRATION 15 MAR 1919\\u003c/p\\u003e\\u003cp\\u003e\\u003cbr\\u003e\\u003c/p\\u003e\\u003cp class\\u003d\\\"left\\\"\\u003e\\u0026nbsp; \\u0026nbsp; \\u0026nbsp; In reply to your letter, I have to say that\\u003c/p\\u003e\\u003cp class\\u003d\\\"left\\\"\\u003ePrivate J.H. Mallett is reported as buried in Vendegies\\u003c/p\\u003e\\u003cp class\\u003d\\\"left\\\"\\u003eCommunal Cemetery, North of Le Cateau.\\u003c/p\\u003e\\u003cp class\\u003d\\\"left\\\"\\u003e\\u0026nbsp; \\u0026nbsp; \\u0026nbsp; If at a later date the officers of the Graves\\u003c/p\\u003e\\u003cp class\\u003d\\\"left\\\"\\u003eRegistration Unit in that area are able to locate the\\u003c/p\\u003e\\u003cp class\\u003d\\\"left\\\"\\u003egrave, it will be registered, and information will be\\u003c/p\\u003e\\u003cp class\\u003d\\\"left\\\"\\u003esent to you at the first possible moment.\\u0026nbsp;\\u003c/p\\u003e\\u003cp class\\u003d\\\"left\\\"\\u003e\\u0026nbsp; \\u0026nbsp; \\u0026nbsp; I am very sorry not to be able to send you a\\u003c/p\\u003e\\u003cp class\\u003d\\\"left\\\"\\u003emore satisfactory reply.\\u003c/p\\u003e\\u003cp class\\u003d\\\"left\\\"\\u003e\\u003cbr\\u003e\\u003c/p\\u003e\\u003cp class\\u003d\\\"right\\\"\\u003eYours Faithfully,\\u0026nbsp;\\u003c/p\\u003e\\u003cp class\\u003d\\\"right\\\"\\u003e\\u0026nbsp;\\u003cspan class\\u003d\\\"pos-in-text\\\"\\u003esignature\\u003c/span\\u003e\\u0026nbsp;\\u003c/p\\u003e\\u003cp class\\u003d\\\"right\\\"\\u003eMajor for Major General,\\u003c/p\\u003e\\u003cp class\\u003d\\\"right\\\"\\u003eD.G.G.R. \\u0026amp; E.\\u003c/p\\u003e\\u003cp class\\u003d\\\"right\\\"\\u003e\\u003cbr\\u003e\\u003c/p\\u003e\\u003cp class\\u003d\\\"left\\\"\\u003eMrs. C. F. Mallett\\u003c/p\\u003e\\u003cp class\\u003d\\\"left\\\"\\u003e17 \\u0026nbsp;\\u003cspan class\\u003d\\\"pos-in-text\\\"\\u003ehandwritten\\u003c/span\\u003e\\u0026nbsp; /7\\u003c/p\\u003e\\u003cp class\\u003d\\\"left\\\"\\u003eCarlisle Street,\\u003c/p\\u003e\\u003cp class\\u003d\\\"left\\\"\\u003eSheffield.\\u003c/p\\u003e\\u003cp\\u003e\\u003cbr\\u003e\\u003c/p\\u003e\",\n" +
            "      \"TextNoTags\":\"\\n Official buff letter from War Office relating where Jack Mallett was buried in France \\n\\n\\n\\n\\nWar Office,\\n\\nWinchester House,\\n\\nSt. James square,\\n\\nLondon S.W.I.\\n\\n\\n\\n\\nReference S.L/18/8891.\\n\\n\\n\\n\\nDear Madam,   Stamp in green ink with date  DIRECTOR OF GRAVES REGISTRATION 15 MAR 1919\\n\\n\\n\\n\\n      In reply to your letter, I have to say that\\n\\nPrivate J.H. Mallett is reported as buried in Vendegies\\n\\nCommunal Cemetery, North of Le Cateau.\\n\\n      If at a later date the officers of the Graves\\n\\nRegistration Unit in that area are able to locate the\\n\\ngrave, it will be registered, and information will be\\n\\nsent to you at the first possible moment. \\n\\n      I am very sorry not to be able to send you a\\n\\nmore satisfactory reply.\\n\\n\\n\\n\\nYours Faithfully, \\n\\n  signature  \\n\\nMajor for Major General,\\n\\nD.G.G.R. \\u0026 E.\\n\\n\\n\\n\\nMrs. C. F. Mallett\\n\\n17   handwritten   /7\\n\\nCarlisle Street,\\n\\nSheffield.\\n\\n\\n\\n\",\n" +
            "      \"Timestamp\":\"Oct 12, 2018 8:43:08 AM\",\n" +
            "      \"X_Coord\":0.0,\n" +
            "      \"Y_Coord\":0.0,\n" +
            "      \"Width\":0.0,\n" +
            "      \"Height\":0.0,\n" +
            "      \"Motivation\":\"transcribing\",\n" +
            "      \"OrderIndex\":1,\n" +
            "      \"TranscribathonItemId\":1180076,\n" +
            "      \"TranscribathonStoryId\":117173,\n" +
            "      \"StoryUrl\":\"https://www.europeana.eu/item/2020601/https___1914_1918_europeana_eu_contributions_17173\",\n" +
            "      \"StoryId\":\"/2020601/https___1914_1918_europeana_eu_contributions_17173\",\n" +
            "      \"ImageLink\":\"https://europeana1914-1918.s3.amazonaws.com/attachments/180076/17173.180076.original.jpg\",\n" +
            "      \"Languages\":[\n" +
            "         {\n" +
            "            \"Name\":\"English\",\n" +
            "            \"Code\":\"en\"\n" +
            "         }\n" +
            "      ]\n" +
            "   },\n" +
            "   {\n" +
            "      \"EuropeanaAnnotationId\":20187,\n" +
            "      \"AnnotationId\":203548,\n" +
            "      \"Text\":\"\\u003cp class\\u003d\\\"center\\\"\\u003e\\u003cspan class\\u003d\\\"underline bold\\\"\\u003e\\u003cbr\\u003e\\u003c/span\\u003e\\u003c/p\\u003e\\u003cp class\\u003d\\\"left\\\"\\u003e\\u003cspan class\\u003d\\\"pos-in-text\\\"\\u003eback of official notice of death in service printed on buff paper\\u003c/span\\u003e\\u003c/p\\u003e\\u003cp class\\u003d\\\"center\\\"\\u003e\\u003cspan class\\u003d\\\"underline bold\\\"\\u003eNOTE.\\u003c/span\\u003e\\u003c/p\\u003e\\u003cp class\\u003d\\\"center\\\"\\u003e\\u003cbr\\u003e\\u003c/p\\u003e\\u003cp class\\u003d\\\"left\\\"\\u003eIf any articles of private property left by the\\u003c/p\\u003e\\u003cp class\\u003d\\\"left\\\"\\u003edeceased are found, they will be forwarded to this Office,\\u003c/p\\u003e\\u003cp class\\u003d\\\"left\\\"\\u003ebut some time will probably elapse before their receipt,\\u003c/p\\u003e\\u003cp class\\u003d\\\"left\\\"\\u003eand when received they cannot be disposed of until\\u003c/p\\u003e\\u003cp class\\u003d\\\"left\\\"\\u003eauthority is received from the War Office. Some delay is\\u003c/p\\u003e\\u003cp class\\u003d\\\"left\\\"\\u003etherefore inevitable.\\u0026nbsp;\\u003c/p\\u003e\\u003cp class\\u003d\\\"left\\\"\\u003e\\u003cbr\\u003e\\u003c/p\\u003e\\u003cp class\\u003d\\\"left\\\"\\u003eAny application regarding the disposal of any such\\u003c/p\\u003e\\u003cp class\\u003d\\\"left\\\"\\u003epersonal effects, or any amount that may eventually be\\u003c/p\\u003e\\u003cp class\\u003d\\\"left\\\"\\u003efound due to the late soldier\\u0027s estate, should be\\u003c/p\\u003e\\u003cp class\\u003d\\\"left\\\"\\u003eaddressed to \\\"The Secretary, War Office, Imperial Institute,\\u003c/p\\u003e\\u003cp class\\u003d\\\"left\\\"\\u003eSouth Kensington, London, S.W.7.\\\"\\u003c/p\\u003e\",\n" +
            "      \"TextNoTags\":\"\\n \\n \\n\\n back of official notice of death in service printed on buff paper \\n\\n NOTE. \\n\\n\\n\\n\\nIf any articles of private property left by the\\n\\ndeceased are found, they will be forwarded to this Office,\\n\\nbut some time will probably elapse before their receipt,\\n\\nand when received they cannot be disposed of until\\n\\nauthority is received from the War Office. Some delay is\\n\\ntherefore inevitable. \\n\\n\\n\\n\\nAny application regarding the disposal of any such\\n\\npersonal effects, or any amount that may eventually be\\n\\nfound due to the late soldier\\u0027s estate, should be\\n\\naddressed to \\\"The Secretary, War Office, Imperial Institute,\\n\\nSouth Kensington, London, S.W.7.\\\"\\n\",\n" +
            "      \"Timestamp\":\"Oct 12, 2018 8:27:04 AM\",\n" +
            "      \"X_Coord\":0.0,\n" +
            "      \"Y_Coord\":0.0,\n" +
            "      \"Width\":0.0,\n" +
            "      \"Height\":0.0,\n" +
            "      \"Motivation\":\"transcribing\",\n" +
            "      \"OrderIndex\":2,\n" +
            "      \"TranscribathonItemId\":1180077,\n" +
            "      \"TranscribathonStoryId\":117173,\n" +
            "      \"StoryUrl\":\"https://www.europeana.eu/item/2020601/https___1914_1918_europeana_eu_contributions_17173\",\n" +
            "      \"StoryId\":\"/2020601/https___1914_1918_europeana_eu_contributions_17173\",\n" +
            "      \"ImageLink\":\"https://europeana1914-1918.s3.amazonaws.com/attachments/180077/17173.180077.original.jpg\",\n" +
            "      \"Languages\":[\n" +
            "         {\n" +
            "            \"Name\":\"English\",\n" +
            "            \"Code\":\"en\"\n" +
            "         }\n" +
            "      ]\n" +
            "   },\n" +
            "   {\n" +
            "      \"EuropeanaAnnotationId\":20186,\n" +
            "      \"AnnotationId\":203550,\n" +
            "      \"Text\":\"\\u003cp\\u003e\\u003cspan class\\u003d\\\"pos-in-text\\\"\\u003eOfficial Army Letter notifying death in service on buff paper\\u003c/span\\u003e\\u003c/p\\u003e\\u003cp\\u003e\\u003cspan class\\u003d\\\"italic\\\"\\u003e\\u003cbr\\u003e\\u003c/span\\u003e\\u003c/p\\u003e\\u003cp\\u003e\\u003cspan class\\u003d\\\"italic\\\"\\u003eNo.________________\\u003c/span\\u003e\\u003cbr\\u003e\\u003c/p\\u003e\\u003cp\\u003e(If replying, please\\u003c/p\\u003e\\u003cp\\u003equote above No.)\\u003c/p\\u003e\\u003cp\\u003e\\u003cbr\\u003e\\u003c/p\\u003e\\u003cp class\\u003d\\\"right\\\"\\u003e\\u0026nbsp;\\u003cspan class\\u003d\\\"pos-in-text\\\"\\u003ehandwritten\\u003c/span\\u003e \\u0026nbsp; \\u003cspan class\\u003d\\\"bold\\\"\\u003eLich 50590 Lieu.\\u003c/span\\u003e\\u0026nbsp;\\u003c/p\\u003e\\u003cp class\\u003d\\\"right\\\"\\u003e\\u0026nbsp;\\u003cspan class\\u003d\\\"pos-in-text\\\"\\u003ehandwritten\\u003c/span\\u003e\\u0026nbsp; Infantry \\u0026nbsp;\\u003cspan class\\u003d\\\"pos-in-text\\\"\\u003etyped\\u003c/span\\u003e\\u0026nbsp; Record Office,\\u003c/p\\u003e\\u003cp class\\u003d\\\"right\\\"\\u003e\\u0026nbsp;\\u003cspan class\\u003d\\\"pos-in-text\\\"\\u003estamped\\u003c/span\\u003e\\u0026nbsp; LICHFIELD.\\u003c/p\\u003e\\u003cp class\\u003d\\\"right\\\"\\u003e\\u0026nbsp;\\u003cspan class\\u003d\\\"pos-in-text\\\"\\u003ehandwritten\\u003c/span\\u003e\\u0026nbsp; 18/11/\\u0026nbsp;\\u0026nbsp;\\u003cspan class\\u003d\\\"pos-in-text\\\"\\u003etyped\\u003c/span\\u003e\\u0026nbsp; 191 \\u0026nbsp;\\u003cspan class\\u003d\\\"pos-in-text\\\"\\u003ehandwritten\\u003c/span\\u003e\\u0026nbsp; 8\\u003c/p\\u003e\\u003cp class\\u003d\\\"left\\\"\\u003e\\u0026nbsp;\\u003cspan class\\u003d\\\"pos-in-text\\\"\\u003ehandwritten\\u003c/span\\u003e\\u0026nbsp; Madam.\\u003c/p\\u003e\\u003cp class\\u003d\\\"left\\\"\\u003e\\u0026nbsp;\\u003cspan class\\u003d\\\"pos-in-text\\\"\\u003etyped\\u003c/span\\u003e\\u0026nbsp; It is my painful duty to inform you that a report has been received\\u0026nbsp;\\u003c/p\\u003e\\u003cp class\\u003d\\\"left\\\"\\u003efrom the War Office notifying the death of:-\\u003c/p\\u003e\\u003cp class\\u003d\\\"left\\\"\\u003e(No.) \\u0026nbsp;\\u003cspan class\\u003d\\\"pos-in-text\\\"\\u003ehandwritten\\u003c/span\\u003e\\u0026nbsp; \\u003cspan class\\u003d\\\"bold\\\"\\u003e42273 \\u0026nbsp;\\u003c/span\\u003e\\u003cspan class\\u003d\\\"pos-in-text\\\"\\u003etyped\\u003c/span\\u003e\\u0026nbsp; (Rank) \\u0026nbsp;\\u003cspan class\\u003d\\\"pos-in-text\\\"\\u003ehandwritten\\u003c/span\\u003e\\u0026nbsp; \\u003cspan class\\u003d\\\"bold\\\"\\u003ePte\\u003c/span\\u003e\\u003c/p\\u003e\\u003cp class\\u003d\\\"left\\\"\\u003e\\u0026nbsp;\\u003cspan class\\u003d\\\"pos-in-text\\\"\\u003etyped\\u003c/span\\u003e\\u0026nbsp; (Name) handwritten\\u003cspan class\\u003d\\\"bold\\\"\\u003e John Henry Mallett\\u003c/span\\u003e\\u003c/p\\u003e\\u003cp class\\u003d\\\"left\\\"\\u003e\\u0026nbsp;\\u003cspan class\\u003d\\\"pos-in-text\\\"\\u003etyped\\u003c/span\\u003e\\u0026nbsp; (Regiment) \\u0026nbsp;\\u003cspan class\\u003d\\\"pos-in-text\\\"\\u003ehandwritten\\u003c/span\\u003e\\u0026nbsp; \\u003cspan class\\u003d\\\"bold\\\"\\u003e6. Leicestershire\\u003c/span\\u003e\\u003c/p\\u003e\\u003cp class\\u003d\\\"left\\\"\\u003e\\u0026nbsp;\\u003cspan class\\u003d\\\"pos-in-text\\\"\\u003etyped\\u003c/span\\u003e\\u0026nbsp; which occurred handwritten \\u003cspan class\\u003d\\\"bold\\\"\\u003ePlace not stated\\u003c/span\\u003e\\u003c/p\\u003e\\u003cp class\\u003d\\\"left\\\"\\u003e\\u0026nbsp;\\u003cspan class\\u003d\\\"pos-in-text\\\"\\u003etyped\\u003c/span\\u003e\\u0026nbsp; on the \\u0026nbsp;\\u003cspan class\\u003d\\\"pos-in-text\\\"\\u003ehandwritten\\u003c/span\\u003e\\u003cspan class\\u003d\\\"bold\\\"\\u003e23. Octobre, 1918\\u003c/span\\u003e\\u003c/p\\u003e\\u003cp class\\u003d\\\"left\\\"\\u003e\\u0026nbsp;\\u003cspan class\\u003d\\\"pos-in-text\\\"\\u003etyped\\u003c/span\\u003e\\u0026nbsp; The report is to the effect that he \\u0026nbsp;\\u003cspan class\\u003d\\\"pos-in-text\\\"\\u003estamp\\u003c/span\\u003e\\u0026nbsp; \\u003cspan class\\u003d\\\"italic bold\\\"\\u003eKilled in Action\\u003c/span\\u003e\\u003c/p\\u003e\\u003cp class\\u003d\\\"left\\\"\\u003e\\u003cspan class\\u003d\\\"italic bold\\\"\\u003e\\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp;\\u0026nbsp; Place not stated.\\u003c/span\\u003e\\u003c/p\\u003e\\u003cp class\\u003d\\\"left\\\"\\u003e\\u0026nbsp;\\u003cspan class\\u003d\\\"pos-in-text\\\"\\u003etyped\\u003c/span\\u003e\\u0026nbsp; By His Majesty\\u0027s command I am to forward the enclosed\\u003c/p\\u003e\\u003cp class\\u003d\\\"left\\\"\\u003emessage of sympathy from Their Gracious Majesties the King and Queen.\\u003c/p\\u003e\\u003cp class\\u003d\\\"left\\\"\\u003eI am at the same time to express the regret of the Army Council at the\\u003c/p\\u003e\\u003cp class\\u003d\\\"left\\\"\\u003esoldier\\u0027s death in his Country\\u0027s service.\\u003c/p\\u003e\\u003cp class\\u003d\\\"left\\\"\\u003e\\u003cbr\\u003e\\u003c/p\\u003e\\u003cp class\\u003d\\\"left\\\"\\u003eI am to add that any information that may be received as to the\\u003c/p\\u003e\\u003cp class\\u003d\\\"left\\\"\\u003esoldier\\u0027s burial will be communicated to you in due course. A separate\\u003c/p\\u003e\\u003cp class\\u003d\\\"left\\\"\\u003eleaflet dealing more fully with this subject is enclosed.\\u003c/p\\u003e\\u003cp class\\u003d\\\"right\\\"\\u003eI am,\\u003c/p\\u003e\\u003cp class\\u003d\\\"right\\\"\\u003e\\u0026nbsp;\\u003cspan class\\u003d\\\"pos-in-text\\\"\\u003ehandwritten\\u003c/span\\u003e\\u0026nbsp; Madam.\\u003c/p\\u003e\\u003cp class\\u003d\\\"right\\\"\\u003e\\u0026nbsp;\\u003cspan class\\u003d\\\"pos-in-text\\\"\\u003etyped\\u003c/span\\u003e\\u0026nbsp; Your \\u0026nbsp;obedient Servant,\\u003c/p\\u003e\\u003cp class\\u003d\\\"right\\\"\\u003e\\u0026nbsp;\\u003cspan class\\u003d\\\"pos-in-text\\\"\\u003esignature\\u003c/span\\u003e \\u003cspan class\\u003d\\\"bold\\\"\\u003eJ C Harper\\u0026nbsp;\\u003c/span\\u003e \\u003cspan class\\u003d\\\"pos-in-text\\\"\\u003ehandwritten\\u003c/span\\u003e\\u0026nbsp;\\u0026nbsp;Lt\\u003c/p\\u003e\\u003cp class\\u003d\\\"right\\\"\\u003efor \\u0026nbsp;\\u003cspan class\\u003d\\\"pos-in-text\\\"\\u003etyped\\u003c/span\\u003e\\u0026nbsp; \\u003cspan class\\u003d\\\"italic\\\"\\u003eOfficer in charge of Records.\\u003c/span\\u003e\\u003c/p\\u003e\\u003cp class\\u003d\\\"left\\\"\\u003e\\u0026nbsp;\\u003cspan class\\u003d\\\"pos-in-text\\\"\\u003ehandwritten\\u003c/span\\u003e\\u0026nbsp; \\u003cspan class\\u003d\\\"bold\\\"\\u003eMrs. F.\\u0026nbsp;Mallett\\u003c/span\\u003e\\u003c/p\\u003e\\u003cp class\\u003d\\\"left\\\"\\u003e\\u003cspan class\\u003d\\\"bold\\\"\\u003e17/7. Carlisle St\\u003c/span\\u003e\\u003c/p\\u003e\\u003cp class\\u003d\\\"left\\\"\\u003e\\u003cspan class\\u003d\\\"bold\\\"\\u003eSheffield\\u003c/span\\u003e\\u003c/p\\u003e\\u003cp class\\u003d\\\"left\\\"\\u003e8830 Wt. W4153/P548\\u0026nbsp; 1904\\u0026nbsp; 1/18 \\u0026nbsp; T. \\u0026amp; W/ Ltd.\\u0026nbsp; Forms 11 104 -\\u0026nbsp; 882 \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp;\\u003cspan style\\u003d\\\"float: none;background-color: transparent;color: #666666;cursor: text;font-style: normal;font-variant: normal;font-weight: 400;letter-spacing: normal;text-align: left;text-decoration: none;text-indent: 0px\\\"\\u003e \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; P.T.O\\u003c/span\\u003e \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp;\\u003c/p\\u003e\",\n" +
            "      \"TextNoTags\":\"\\n Official Army Letter notifying death in service on buff paper \\n\\n \\n \\n\\n No.________________ \\n\\n\\n(If replying, please\\n\\nquote above No.)\\n\\n\\n\\n\\n  handwritten     Lich 50590 Lieu.  \\n\\n  handwritten   Infantry   typed   Record Office,\\n\\n  stamped   LICHFIELD.\\n\\n  handwritten   18/11/   typed   191   handwritten   8\\n\\n  handwritten   Madam.\\n\\n  typed   It is my painful duty to inform you that a report has been received \\n\\nfrom the War Office notifying the death of:-\\n\\n(No.)   handwritten    42273    typed   (Rank)   handwritten    Pte \\n\\n  typed   (Name) handwritten  John Henry Mallett \\n\\n  typed   (Regiment)   handwritten    6. Leicestershire \\n\\n  typed   which occurred handwritten  Place not stated \\n\\n  typed   on the   handwritten  23. Octobre, 1918 \\n\\n  typed   The report is to the effect that he   stamp    Killed in Action \\n\\n                                                                                                      Place not stated. \\n\\n  typed   By His Majesty\\u0027s command I am to forward the enclosed\\n\\nmessage of sympathy from Their Gracious Majesties the King and Queen.\\n\\nI am at the same time to express the regret of the Army Council at the\\n\\nsoldier\\u0027s death in his Country\\u0027s service.\\n\\n\\n\\n\\nI am to add that any information that may be received as to the\\n\\nsoldier\\u0027s burial will be communicated to you in due course. A separate\\n\\nleaflet dealing more fully with this subject is enclosed.\\n\\nI am,\\n\\n  handwritten   Madam.\\n\\n  typed   Your  obedient Servant,\\n\\n  signature   J C Harper    handwritten   Lt\\n\\nfor   typed    Officer in charge of Records. \\n\\n  handwritten    Mrs. F. Mallett \\n\\n 17/7. Carlisle St \\n\\n Sheffield \\n\\n8830 Wt. W4153/P548  1904  1/18   T. \\u0026 W/ Ltd.  Forms 11 104 -  882                                                    P.T.O                                               \\n\",\n" +
            "      \"Timestamp\":\"Oct 12, 2018 8:25:24 AM\",\n" +
            "      \"X_Coord\":0.0,\n" +
            "      \"Y_Coord\":0.0,\n" +
            "      \"Width\":0.0,\n" +
            "      \"Height\":0.0,\n" +
            "      \"Motivation\":\"transcribing\",\n" +
            "      \"OrderIndex\":3,\n" +
            "      \"TranscribathonItemId\":1180078,\n" +
            "      \"TranscribathonStoryId\":117173,\n" +
            "      \"StoryUrl\":\"https://www.europeana.eu/item/2020601/https___1914_1918_europeana_eu_contributions_17173\",\n" +
            "      \"StoryId\":\"/2020601/https___1914_1918_europeana_eu_contributions_17173\",\n" +
            "      \"ImageLink\":\"https://europeana1914-1918.s3.amazonaws.com/attachments/180078/17173.180078.original.jpg\",\n" +
            "      \"Languages\":[\n" +
            "         {\n" +
            "            \"Name\":\"English\",\n" +
            "            \"Code\":\"en\"\n" +
            "         }\n" +
            "      ]\n" +
            "   },\n" +
            "   {\n" +
            "      \"EuropeanaAnnotationId\":20189,\n" +
            "      \"AnnotationId\":203557,\n" +
            "      \"Text\":\"\\u003cp\\u003eeating army bread.\\u003c/p\\u003e\\u003cp\\u003eDear Mother we are in\\u003c/p\\u003e\\u003cp\\u003ea village that the germans\\u003c/p\\u003e\\u003cp\\u003ehave held since Sept 1914\\u003c/p\\u003e\\u003cp\\u003efull of aves and we are still\\u003c/p\\u003e\\u003cp\\u003egoing forward.\\u003c/p\\u003e\\u003cp class\\u003d\\\"left\\\"\\u003eYour Loving Son\\u003c/p\\u003e\\u003cp class\\u003d\\\"left\\\"\\u003e\\u003cspan class\\u003d\\\"underline\\\"\\u003eJack.\\u003c/span\\u003e\\u003cbr\\u003e\\u003c/p\\u003e\\u003cp class\\u003d\\\"left\\\"\\u003exxxxxxxxx\\u003c/p\\u003e\\u003cp class\\u003d\\\"left\\\"\\u003exxxxxxxx\\u003c/p\\u003e\\u003cp class\\u003d\\\"left\\\"\\u003eget my grandmother that \\u003cspan class\\u003d\\\"underline\\\"\\u003egill\\u003c/span\\u003e\\u003c/p\\u003e\",\n" +
            "      \"TextNoTags\":\"\\neating army bread.\\n\\nDear Mother we are in\\n\\na village that the germans\\n\\nhave held since Sept 1914\\n\\nfull of aves and we are still\\n\\ngoing forward.\\n\\nYour Loving Son\\n\\n Jack. \\n\\n\\nxxxxxxxxx\\n\\nxxxxxxxx\\n\\nget my grandmother that  gill \\n\",\n" +
            "      \"Timestamp\":\"Oct 12, 2018 7:52:22 AM\",\n" +
            "      \"X_Coord\":0.0,\n" +
            "      \"Y_Coord\":0.0,\n" +
            "      \"Width\":0.0,\n" +
            "      \"Height\":0.0,\n" +
            "      \"Motivation\":\"transcribing\",\n" +
            "      \"OrderIndex\":4,\n" +
            "      \"TranscribathonItemId\":1180079,\n" +
            "      \"TranscribathonStoryId\":117173,\n" +
            "      \"StoryUrl\":\"https://www.europeana.eu/item/2020601/https___1914_1918_europeana_eu_contributions_17173\",\n" +
            "      \"StoryId\":\"/2020601/https___1914_1918_europeana_eu_contributions_17173\",\n" +
            "      \"ImageLink\":\"https://europeana1914-1918.s3.amazonaws.com/attachments/180079/17173.180079.original.jpg\",\n" +
            "      \"Languages\":[\n" +
            "         {\n" +
            "            \"Name\":\"English\",\n" +
            "            \"Code\":\"en\"\n" +
            "         }\n" +
            "      ]\n" +
            "   },\n" +
            "   {\n" +
            "      \"EuropeanaAnnotationId\":20188,\n" +
            "      \"AnnotationId\":203559,\n" +
            "      \"Text\":\"\\u003cp\\u003eallright write and let\\u003c/p\\u003e\\u003cp\\u003eme know as soon as poss\\u003c/p\\u003e\\u003cp\\u003eI have sent our Alf and\\u003c/p\\u003e\\u003cp\\u003eFlorence a shilling or two\\u003c/p\\u003e\\u003cp\\u003ewhen I send you the 200.\\u003c/p\\u003e\\u003cp\\u003e\\u003cbr\\u003e\\u003c/p\\u003e\\u003cp\\u003eDear Mother,\\u003c/p\\u003e\\u003cp\\u003e\\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp;you can send\\u003c/p\\u003e\\u003cp\\u003eme abit of homemade\\u003c/p\\u003e\\u003cp\\u003ebread it would\\u003c/p\\u003e\\u003cp\\u003ego down grand for\\u003c/p\\u003e\\u003cp\\u003ea change. I am sick of\\u003c/p\\u003e\",\n" +
            "      \"TextNoTags\":\"\\nallright write and let\\n\\nme know as soon as poss\\n\\nI have sent our Alf and\\n\\nFlorence a shilling or two\\n\\nwhen I send you the 200.\\n\\n\\n\\n\\nDear Mother,\\n\\n                         you can send\\n\\nme abit of homemade\\n\\nbread it would\\n\\ngo down grand for\\n\\na change. I am sick of\\n\",\n" +
            "      \"Timestamp\":\"Jul 8, 2018 5:02:31 AM\",\n" +
            "      \"X_Coord\":0.0,\n" +
            "      \"Y_Coord\":0.0,\n" +
            "      \"Width\":0.0,\n" +
            "      \"Height\":0.0,\n" +
            "      \"Motivation\":\"transcribing\",\n" +
            "      \"OrderIndex\":5,\n" +
            "      \"TranscribathonItemId\":1180080,\n" +
            "      \"TranscribathonStoryId\":117173,\n" +
            "      \"StoryUrl\":\"https://www.europeana.eu/item/2020601/https___1914_1918_europeana_eu_contributions_17173\",\n" +
            "      \"StoryId\":\"/2020601/https___1914_1918_europeana_eu_contributions_17173\",\n" +
            "      \"ImageLink\":\"https://europeana1914-1918.s3.amazonaws.com/attachments/180080/17173.180080.original.jpg\",\n" +
            "      \"Languages\":[\n" +
            "         {\n" +
            "            \"Name\":\"English\",\n" +
            "            \"Code\":\"en\"\n" +
            "         }\n" +
            "      ]\n" +
            "   },\n" +
            "   {\n" +
            "      \"EuropeanaAnnotationId\":20183,\n" +
            "      \"AnnotationId\":203562,\n" +
            "      \"Text\":\"\\u003cp class\\u003d\\\"center\\\"\\u003e\\u003cspan class\\u003d\\\"underline\\\"\\u003eOct 20 18\\u003c/span\\u003e\\u003cbr\\u003e\\u003c/p\\u003e\\u003cp class\\u003d\\\"center\\\"\\u003e\\u003cspan class\\u003d\\\"underline\\\"\\u003e\\u003cbr\\u003e\\u003c/span\\u003e\\u003c/p\\u003e\\u003cp\\u003e\\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp;Dear Mother,\\u003c/p\\u003e\\u003cp\\u003e\\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; just a few lines\\u003c/p\\u003e\\u003cp\\u003ehoping you are in the best\\u003c/p\\u003e\\u003cp\\u003eof health as I am I have\\u0026nbsp;\\u003c/p\\u003e\\u003cp\\u003enot had a letter from my\\u003c/p\\u003e\\u003cp\\u003eDad or our Frank I cannot\\u003c/p\\u003e\\u003cp\\u003emake them out all they\\u003c/p\\u003e\\u003cp\\u003emust have forgot me.\\u0026nbsp;\\u003c/p\\u003e\\u003cp\\u003eDear Mother I hope you\\u003c/p\\u003e\\u003cp\\u003ehave got that money\\u003c/p\\u003e\\u003cp\\u003e\\u003cbr\\u003e\\u003c/p\\u003e\\u003cp\\u003e\\u003cbr\\u003e\\u003c/p\\u003e\",\n" +
            "      \"TextNoTags\":\"\\n Oct 20 18 \\n\\n\\n \\n \\n\\n           Dear Mother,\\n\\n                                    just a few lines\\n\\nhoping you are in the best\\n\\nof health as I am I have \\n\\nnot had a letter from my\\n\\nDad or our Frank I cannot\\n\\nmake them out all they\\n\\nmust have forgot me. \\n\\nDear Mother I hope you\\n\\nhave got that money\\n\\n\\n\\n\\n\\n\\n\",\n" +
            "      \"Timestamp\":\"Jul 8, 2018 5:04:17 AM\",\n" +
            "      \"X_Coord\":0.0,\n" +
            "      \"Y_Coord\":0.0,\n" +
            "      \"Width\":0.0,\n" +
            "      \"Height\":0.0,\n" +
            "      \"Motivation\":\"transcribing\",\n" +
            "      \"OrderIndex\":6,\n" +
            "      \"TranscribathonItemId\":1180081,\n" +
            "      \"TranscribathonStoryId\":117173,\n" +
            "      \"StoryUrl\":\"https://www.europeana.eu/item/2020601/https___1914_1918_europeana_eu_contributions_17173\",\n" +
            "      \"StoryId\":\"/2020601/https___1914_1918_europeana_eu_contributions_17173\",\n" +
            "      \"ImageLink\":\"https://europeana1914-1918.s3.amazonaws.com/attachments/180081/17173.180081.original.jpg\",\n" +
            "      \"Languages\":[\n" +
            "         {\n" +
            "            \"Name\":\"English\",\n" +
            "            \"Code\":\"en\"\n" +
            "         }\n" +
            "      ]\n" +
            "   },\n" +
            "   {\n" +
            "      \"EuropeanaAnnotationId\":20192,\n" +
            "      \"AnnotationId\":203566,\n" +
            "      \"Text\":\"\\u003cp class\\u003d\\\"center\\\"\\u003eBest Wishes\\u003cbr\\u003e\\u003c/p\\u003e\\u003cp class\\u003d\\\"center\\\"\\u003efor\\u003c/p\\u003e\\u003cp class\\u003d\\\"center\\\"\\u003eChristmas\\u003c/p\\u003e\\u003cp class\\u003d\\\"center\\\"\\u003eand the\\u0026nbsp;\\u003c/p\\u003e\\u003cp class\\u003d\\\"center\\\"\\u003eNew Year. \\u003c/p\\u003e\\u003cp class\\u003d\\\"center\\\"\\u003e\\u003cbr\\u003e\\u003c/p\\u003e\\u003cp class\\u003d\\\"left\\\"\\u003e\\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp;\\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp;From\\u0026nbsp; \\u0026nbsp;\\u003cspan class\\u003d\\\"pos-in-text\\\"\\u003ehandwritten\\u003c/span\\u003e\\u0026nbsp; Jack\\u003c/p\\u003e\\u003cp class\\u003d\\\"left\\\"\\u003e\\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp;\\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp;\\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp;\\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp;\\u003cspan class\\u003d\\\"pos-in-text\\\"\\u003ehandwritten\\u003c/span\\u003e\\u0026nbsp; to his Loving Mother\\u003c/p\\u003e\\u003cp class\\u003d\\\"left\\\"\\u003e\\u003cspan class\\u003d\\\"italic\\\"\\u003e\\u003cbr\\u003e\\u003c/span\\u003e\\u003c/p\\u003e\\u003cp class\\u003d\\\"left\\\"\\u003e\\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp;\\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp;\\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp;\\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp;\\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp;\\u003cspan class\\u003d\\\"pos-in-text\\\"\\u003etyped\\u003c/span\\u003e\\u0026nbsp; BACKWORTH CAMP, 1917\\u003c/p\\u003e\",\n" +
            "      \"TextNoTags\":\"\\nBest Wishes\\n\\n\\nfor\\n\\nChristmas\\n\\nand the \\n\\nNew Year. \\n\\n\\n\\n\\n                                                                          From    handwritten   Jack\\n\\n                                                                           handwritten   to his Loving Mother\\n\\n \\n \\n\\n                                                                            typed   BACKWORTH CAMP, 1917\\n\",\n" +
            "      \"Timestamp\":\"Jul 8, 2018 5:07:10 AM\",\n" +
            "      \"X_Coord\":0.0,\n" +
            "      \"Y_Coord\":0.0,\n" +
            "      \"Width\":0.0,\n" +
            "      \"Height\":0.0,\n" +
            "      \"Motivation\":\"transcribing\",\n" +
            "      \"OrderIndex\":7,\n" +
            "      \"TranscribathonItemId\":1180082,\n" +
            "      \"TranscribathonStoryId\":117173,\n" +
            "      \"StoryUrl\":\"https://www.europeana.eu/item/2020601/https___1914_1918_europeana_eu_contributions_17173\",\n" +
            "      \"StoryId\":\"/2020601/https___1914_1918_europeana_eu_contributions_17173\",\n" +
            "      \"ImageLink\":\"https://europeana1914-1918.s3.amazonaws.com/attachments/180082/17173.180082.original.jpg\",\n" +
            "      \"Languages\":[\n" +
            "         {\n" +
            "            \"Name\":\"English\",\n" +
            "            \"Code\":\"en\"\n" +
            "         }\n" +
            "      ]\n" +
            "   },\n" +
            "   {\n" +
            "      \"EuropeanaAnnotationId\":20190,\n" +
            "      \"AnnotationId\":203569,\n" +
            "      \"Text\":\"\\u003cp\\u003e\\u003cspan class\\u003d\\\"pos-in-text\\\"\\u003ea buff coloured envelope with pencil handwriting, a postmark and official Censor marks\\u003c/span\\u003e\\u003cspan class\\u003d\\\"pos-in-text\\\"\\u003e …\\u003c/span\\u003e\\u003c/p\\u003e\\u003cp\\u003e\\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; O.A.S. \\u0026nbsp;  \\u003cspan class\\u003d\\\"pos-in-text\\\"\\u003eOn Active Service \\u0026nbsp;\\u003c/span\\u003e \\u0026nbsp; \\u0026nbsp;\\u003c/p\\u003e\\u003cp\\u003e\\u003cbr\\u003e\\u003c/p\\u003e\\u003cp\\u003e\\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp;\\u0026nbsp; Mrs. J. Mallett\\u003c/p\\u003e\\u003cp\\u003e\\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; 17ct 7 hse Carlisle Street\\u003c/p\\u003e\\u003cp\\u003e\\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp;\\u0026nbsp; off\\u003c/p\\u003e\\u003cp\\u003e\\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; Spital Hill\\u003c/p\\u003e\\u003cp\\u003e\\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; Sheffield\\u003c/p\\u003e\\u003cp\\u003e\\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp;\\u0026nbsp; \\u003cspan class\\u003d\\\"underline\\\"\\u003eEng\\u003c/span\\u003e \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u003c/p\\u003e\\u003cp\\u003e\\u003cbr\\u003e\\u003c/p\\u003e\\u003cp\\u003e\\u003cspan class\\u003d\\\"pos-in-text\\\"\\u003eround black postmark\\u003c/span\\u003e\\u003c/p\\u003e\\u003cp\\u003eFORCES POST OFFICE\\u003c/p\\u003e\\u003cp\\u003e\\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp;\\u0026nbsp; A\\u003c/p\\u003e\\u003cp\\u003e\\u0026nbsp; \\u0026nbsp; \\u0026nbsp; 13 OCT \\u0026nbsp; \\u0026nbsp; \\u0026nbsp;\\u003c/p\\u003e\\u003cp\\u003e\\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp;\\u0026nbsp; 18 \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp;\\u003cbr\\u003e\\u003c/p\\u003e\\u003cp\\u003e\\u003cbr\\u003e\\u003c/p\\u003e\\u003cp\\u003e\\u003cspan class\\u003d\\\"pos-in-text\\\"\\u003erectangular black Censor postmark\\u003c/span\\u003e\\u003c/p\\u003e\\u003cp\\u003e\\u003cspan class\\u003d\\\"pos-in-text\\\"\\u003e\\u0026nbsp; crown\\u003c/span\\u003e\\u003c/p\\u003e\\u003cp\\u003e\\u0026nbsp; \\u0026nbsp;\\u0026nbsp; PASSED\\u0026nbsp;\\u003c/p\\u003e\\u003cp\\u003e\\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp;\\u0026nbsp; BY\\u0026nbsp;\\u003c/p\\u003e\\u003cp\\u003e\\u0026nbsp; \\u0026nbsp; CENSOR\\u003c/p\\u003e\\u003cp\\u003e\\u0026nbsp; \\u0026nbsp; \\u0026nbsp;\\u0026nbsp; 362\\u003c/p\\u003e\\u003cp\\u003e\\u003cbr\\u003e\\u003c/p\\u003e\\u003cp\\u003e\\u003cspan class\\u003d\\\"pos-in-text\\\"\\u003eunknown signature in purple ink\\u003c/span\\u003e\\u003c/p\\u003e\",\n" +
            "      \"TextNoTags\":\"\\n a buff coloured envelope with pencil handwriting, a postmark and official Censor marks   … \\n\\n                                                                        O.A.S.     On Active Service       \\n\\n\\n\\n\\n                                                               Mrs. J. Mallett\\n\\n                                                                      17ct 7 hse Carlisle Street\\n\\n                                                                                 off\\n\\n                                                                          Spital Hill\\n\\n                                                                                Sheffield\\n\\n                                                                                          Eng        \\n\\n\\n\\n\\n round black postmark \\n\\nFORCES POST OFFICE\\n\\n           A\\n\\n      13 OCT      \\n\\n           18                    \\n\\n\\n\\n\\n\\n rectangular black Censor postmark \\n\\n   crown \\n\\n     PASSED \\n\\n         BY \\n\\n    CENSOR\\n\\n       362\\n\\n\\n\\n\\n unknown signature in purple ink \\n\",\n" +
            "      \"Timestamp\":\"Oct 12, 2018 6:51:38 AM\",\n" +
            "      \"X_Coord\":0.0,\n" +
            "      \"Y_Coord\":0.0,\n" +
            "      \"Width\":0.0,\n" +
            "      \"Height\":0.0,\n" +
            "      \"Motivation\":\"transcribing\",\n" +
            "      \"OrderIndex\":8,\n" +
            "      \"TranscribathonItemId\":1180083,\n" +
            "      \"TranscribathonStoryId\":117173,\n" +
            "      \"StoryUrl\":\"https://www.europeana.eu/item/2020601/https___1914_1918_europeana_eu_contributions_17173\",\n" +
            "      \"StoryId\":\"/2020601/https___1914_1918_europeana_eu_contributions_17173\",\n" +
            "      \"ImageLink\":\"https://europeana1914-1918.s3.amazonaws.com/attachments/180083/17173.180083.original.jpg\",\n" +
            "      \"Languages\":[\n" +
            "         {\n" +
            "            \"Name\":\"English\",\n" +
            "            \"Code\":\"en\"\n" +
            "         }\n" +
            "      ]\n" +
            "   },\n" +
            "   {\n" +
            "      \"EuropeanaAnnotationId\":20184,\n" +
            "      \"AnnotationId\":203573,\n" +
            "      \"Text\":\"\\u003cp\\u003e\\u003cspan class\\u003d\\\"pos-in-text\\\"\\u003eThe front of a buff coloured official printed Army Christmas Card\\u003c/span\\u003e\\u003c/p\\u003e\\u003cp\\u003e\\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp;\\u003c/p\\u003e\\u003cp\\u003e\\u003cbr\\u003e\\u003c/p\\u003e\\u003cp\\u003e\\u003cspan class\\u003d\\\"pos-in-text\\\"\\u003eblue printed emblem of the South Staffordshire Regiment (centred)\\u003c/span\\u003e\\u003c/p\\u003e\\u003cp\\u003e\\u003cbr\\u003e\\u003c/p\\u003e\\u003cp\\u003e\\u0026nbsp;A knot surmounted by a crown, below which is a scroll containing 3rd SOUTH STAFFORDSHIRE REGT\\u003c/p\\u003e\\u003cp\\u003e\\u003cbr\\u003e\\u003c/p\\u003e\\u003cp\\u003e\\u003cbr\\u003e\\u003c/p\\u003e\",\n" +
            "      \"TextNoTags\":\"\\n The front of a buff coloured official printed Army Christmas Card \\n\\n                                     \\n\\n\\n\\n\\n blue printed emblem of the South Staffordshire Regiment (centred) \\n\\n\\n\\n\\n A knot surmounted by a crown, below which is a scroll containing 3rd SOUTH STAFFORDSHIRE REGT\\n\\n\\n\\n\\n\\n\\n\",\n" +
            "      \"Timestamp\":\"Oct 12, 2018 6:23:22 PM\",\n" +
            "      \"X_Coord\":0.0,\n" +
            "      \"Y_Coord\":0.0,\n" +
            "      \"Width\":0.0,\n" +
            "      \"Height\":0.0,\n" +
            "      \"Motivation\":\"transcribing\",\n" +
            "      \"OrderIndex\":9,\n" +
            "      \"TranscribathonItemId\":1180084,\n" +
            "      \"TranscribathonStoryId\":117173,\n" +
            "      \"StoryUrl\":\"https://www.europeana.eu/item/2020601/https___1914_1918_europeana_eu_contributions_17173\",\n" +
            "      \"StoryId\":\"/2020601/https___1914_1918_europeana_eu_contributions_17173\",\n" +
            "      \"ImageLink\":\"https://europeana1914-1918.s3.amazonaws.com/attachments/180084/17173.180084.original.jpg\",\n" +
            "      \"Languages\":[\n" +
            "         {\n" +
            "            \"Name\":\"English\",\n" +
            "            \"Code\":\"en\"\n" +
            "         }\n" +
            "      ]\n" +
            "   },\n" +
            "   {\n" +
            "      \"EuropeanaAnnotationId\":20185,\n" +
            "      \"AnnotationId\":203574,\n" +
            "      \"Text\":\"\\u003cp\\u003eSepia photograph of a young WW1 soldier dressed in uniform, his is wearing the cap badge for the South Staffordshire Regiment.\\u003c/p\\u003e\\u003cp\\u003e\\u003cbr\\u003e\\u003c/p\\u003e\\u003cp\\u003ewritten below:\\u003c/p\\u003e\\u003cp\\u003e\\u0026nbsp; \\u0026nbsp; PRIVATE JOHN HENRY MALLETT\\u003c/p\\u003e\\u003cp\\u003e42273, 6th BN, LEICESTSHIRE\\u0026nbsp;\\u003c/p\\u003e\\u003cp\\u003e\\u0026nbsp; \\u0026nbsp; REGIMENT. DIED AGE 19\\u003c/p\\u003e\\u003cp\\u003e\\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp; \\u0026nbsp;ON 23 OCTOBER 1918.\\u003c/p\\u003e\",\n" +
            "      \"TextNoTags\":\"\\nSepia photograph of a young WW1 soldier dressed in uniform, his is wearing the cap badge for the South Staffordshire Regiment.\\n\\n\\n\\n\\nwritten below:\\n\\n    PRIVATE JOHN HENRY MALLETT\\n\\n42273, 6th BN, LEICESTSHIRE \\n\\n    REGIMENT. DIED AGE 19\\n\\n             ON 23 OCTOBER 1918.\\n\",\n" +
            "      \"Timestamp\":\"Oct 12, 2018 6:21:32 PM\",\n" +
            "      \"X_Coord\":0.0,\n" +
            "      \"Y_Coord\":0.0,\n" +
            "      \"Width\":0.0,\n" +
            "      \"Height\":0.0,\n" +
            "      \"Motivation\":\"transcribing\",\n" +
            "      \"OrderIndex\":10,\n" +
            "      \"TranscribathonItemId\":1180085,\n" +
            "      \"TranscribathonStoryId\":117173,\n" +
            "      \"StoryUrl\":\"https://www.europeana.eu/item/2020601/https___1914_1918_europeana_eu_contributions_17173\",\n" +
            "      \"StoryId\":\"/2020601/https___1914_1918_europeana_eu_contributions_17173\",\n" +
            "      \"ImageLink\":\"https://europeana1914-1918.s3.amazonaws.com/attachments/180085/17173.180085.original.jpg\",\n" +
            "      \"Languages\":[\n" +
            "         {\n" +
            "            \"Name\":\"English\",\n" +
            "            \"Code\":\"en\"\n" +
            "         }\n" +
            "      ]\n" +
            "   }\n" +
            "]";
    private EnrichTask enrichTask;
    private Record record;
    @Autowired
    private QueueRecordService qrs;

    @Autowired
    private EuropeanaSearchService ess;

    @Autowired
    private EuropeanaAnnotationsService eas;

    @Autowired
    private TranscriptionRepository transcriptionRepository;

//    @Qualifier("transcriptionPlatformService")
//    @Autowired
//    private TranscriptionPlatformService tps;

    @Mock
    // mocked as Transcribathon dev platform not always work, sometimes drop records, or returns 5xx codes
    private TranscriptionPlatformService tps;

    @Before
    public void prepareMock() {
        when(tps.fetchTranscriptionsFor(any())).thenReturn(JSON.parseAny(this.transcribathonResponse).getAsArray());
    }

    @Before
    public void init() {
        this.record = new Record();
        this.record.setIdentifier("/2020601/https___1914_1918_europeana_eu_contributions_17173");
        this.record.setTranscriptions(new ArrayList<>());
        this.qrs.saveRecord(this.record);
        this.enrichTask = new EnrichTask(this.record, this.qrs, this.tps, this.ess, this.eas);
    }

    @Test
    public void whenPostedTwice_notDuplicateRecords() {
        // if post method is called this task will be created
        this.enrichTask.process();
        this.enrichTask.process();
        List<Transcription> transcriptionsFound = this.transcriptionRepository.findAllByTpId("203544");
        assertEquals(1, transcriptionsFound.size());
    }
}

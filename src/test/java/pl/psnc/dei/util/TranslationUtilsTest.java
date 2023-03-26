package pl.psnc.dei.util;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.*;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class TranslationUtilsTest {
    @Test
    public void test() throws ParserConfigurationException, IOException, SAXException, XPathExpressionException,
            TransformerException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        Document input = factory
                .newDocumentBuilder()
                .parse("/home/helin/work/fbc/Europeana/14-18/04082022/69699-nnnnhX2.xml");
        XPath xpath = XPathFactory
                .newInstance()
                .newXPath();
        XPathExpression expr = xpath.compile("//*[name()='edm:ProvidedCHO']/*[name()='dc:description']");
        NodeList nodes = (NodeList) expr.evaluate(input, XPathConstants.NODESET);
        for (int i = 0; i < nodes.getLength(); i++) {
            Element value = (Element) nodes.item(i);
            value.setAttribute("xml:lang", "en");
        }
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        Transformer xformer = transformerFactory.newTransformer();
        xformer.setOutputProperty(OutputKeys.INDENT, "yes");
        Writer output = new StringWriter();//new FileWriter("/home/helin/work/fbc/Europeana/14-18/04082022/69699-nnnnhX2-transformed.xml");
        xformer.transform(new DOMSource(input), new StreamResult(output));
        Files.writeString(Paths.get("/home/helin/work/fbc/Europeana/14-18", "69699-nnnnhX2-transformed.xml"),
                output.toString(), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
    }

}
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
import java.io.InputStream;
import java.io.StringWriter;

import static org.junit.Assert.*;

public class TranslationUtilsTest {

    @Test
    public void test() throws ParserConfigurationException, IOException, SAXException, XPathExpressionException,
            TransformerException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("records/69699-nnnnhX2.xml");
        assertNotNull("Record XML resource should be found", inputStream);
        Document input = factory
                .newDocumentBuilder()
                .parse(inputStream);
        XPath xpath = XPathFactory
                .newInstance()
                .newXPath();
        XPathExpression expr = xpath.compile("//*[name()='edm:ProvidedCHO']/*[name()='dc:description']");
        NodeList nodes = (NodeList) expr.evaluate(input, XPathConstants.NODESET);
        assertEquals(1, nodes.getLength());
        for (int i = 0; i < nodes.getLength(); i++) {
            Element value = (Element) nodes.item(i);
            value.setAttribute("xml:lang", "en");
        }
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        Transformer xformer = transformerFactory.newTransformer();
        xformer.setOutputProperty(OutputKeys.INDENT, "yes");
        StringWriter output = new StringWriter();
        xformer.transform(new DOMSource(input), new StreamResult(output));

        String resultXml = output.toString();
        assertTrue(resultXml.contains("xml:lang=\"en\""));
        assertTrue(resultXml.contains("Foto-Postkarte mit Familie Keller"));
    }

}
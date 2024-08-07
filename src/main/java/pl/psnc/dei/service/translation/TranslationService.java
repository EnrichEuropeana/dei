package pl.psnc.dei.service.translation;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Transactional
public class TranslationService {
    private final static Logger log = LoggerFactory.getLogger(TranslationService.class);
    private static final String XML_LANG = "xml:lang";
    private static final String EDM_WAS_GENERATED_BY = "edm:wasGeneratedBy";
    private static final String SOFTWARE_AGENT = "SoftwareAgent";
    private static final String WHITESPACE_REGEX = "[ \n\t]";
    private static final String XML_DECLARATION = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
    private static final Pattern PATTERN = Pattern.compile(
            "(?s)<edm:ProvidedCHO.*?(<dc:description.*</dc:description>).*?</edm:ProvidedCHO>");
    private static final String GLOB_PATTERN = "glob:*-%s.*";

    private ObjectMapper objectMapper;
    private DocumentBuilderFactory factory;
    private Transformer transformer;

    @PostConstruct
    public void init() {
        objectMapper = new ObjectMapper();
        factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        try {
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            transformer = transformerFactory.newTransformer(new StreamSource(new StringReader(readPrettyPrintXslt())));
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        } catch (TransformerConfigurationException | ParserConfigurationException e) {
            throw new IllegalStateException("Could not create transformer");
        }
    }

    private String readPrettyPrintXslt() {
        try (InputStream is = TranslationService.class.getResourceAsStream("/xslt/prettyprint.xsl")) {
            return IOUtils.toString(Objects.requireNonNull(is), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.warn("Pretty print XSL not loaded");
        }
        return "";
    }

    /**
     * Applies translations of field fieldName to the records stored in xmlFolder. Translations are located in JSON files in translationsFolder.
     *
     * @param xmlFolder
     * @param translationsFolder
     * @param fieldName
     * @return
     */
    public void applyTranslations(@NonNull String xmlFolder, @NonNull String translationsFolder,
                                  @NonNull String fieldName, String mappingFile) throws IOException, XPathExpressionException {
        XPath xpath = XPathFactory
                .newInstance()
                .newXPath();
        final XPathExpression expr = xpath.compile(
                String.format("//*[name()='edm:ProvidedCHO']/*[local-name()='%s']", fieldName));

        List<TranslationsDTO> translationsDTOs = streamTranslations(translationsFolder);
        if (mappingFile != null) {
            remapIdentifiers(translationsDTOs, mappingFile);
        }
        translationsDTOs
                .forEach(translationsDTO -> {
                    try {
                        applyTranslation(translationsDTO, xmlFolder, expr);
                    } catch (ParserConfigurationException | IOException | SAXException | XPathExpressionException |
                             TransformerException e) {
                        log.error("Applying translation failed", e);
                    }
                });
    }

    private void remapIdentifiers(List<TranslationsDTO> translationsDTOs, String mappingFile) {
        Map<String, String> mapping = readMappingFile(mappingFile);
        translationsDTOs.forEach(translationsDTO ->
                translationsDTO.setIdentifier(mapping.getOrDefault(translationsDTO.getRecordId(), translationsDTO.getIdentifier())));
    }

    /**
     * Mapping file should have key=value in each line
     *
     * @param mappingFile
     * @return
     */
    private Map<String, String> readMappingFile(String mappingFile) {
        Map<String, String> map = new HashMap<>();

        try {
            List<String> lines = Files.readAllLines(Paths.get(mappingFile));
            for (String line : lines) {
                String[] parts = line.split("=", 2);
                if (parts.length == 2) {
                    String key = parts[0];
                    String value = parts[1];
                    map.put(key, value);
                }
            }
        } catch (IOException e) {
            log.error("Cannot read mapping file", e);
        }
        return map;
    }

    private void applyTranslation(TranslationsDTO translationsDTO, String xmlFolder, XPathExpression expr) throws
            ParserConfigurationException, IOException, SAXException, XPathExpressionException, TransformerException {
        log.info("Applying translations for {}", translationsDTO.getIdentifier());

        boolean applied = true;
        Document input = factory
                .newDocumentBuilder()
                .parse(getFilePath(translationsDTO, xmlFolder, "xml").toString());
        NodeList nodes = (NodeList) expr.evaluate(input, XPathConstants.NODESET);
        for (int i = 0; i < translationsDTO.getOriginalValues().size(); i++) {
            String translation = translationsDTO.getTranslations().size() > i
                    ? translationsDTO.getTranslations().get(i)
                    : null;
            Element element = (Element) nodes.item(i);
            if (element == null) {
                log.warn("No element for i={}. Nodes={}", i, nodes);
                continue;
            }
            applied = applyTranslation(translationsDTO.getOriginalValues().get(i),
                    translation,
                    translationsDTO.getDetectedLanguages().get(i), element);
            if (!applied) {
                log.warn("Translation {} was not be applied to record {}", i, translationsDTO.getIdentifier());
            }
        }

        if (applied) {
            log.info("Applying record {}", translationsDTO.getIdentifier());
            String originalFile = new String(Files.readAllBytes(getFilePath(translationsDTO, xmlFolder, "xml")));
            String appliedValues = getAppliedValues((NodeList) expr.evaluate(input, XPathConstants.NODESET));
            applyToOriginalFile(appliedValues, originalFile).ifPresent(s -> {
                try {
                    Files.writeString(getFilePath(translationsDTO, xmlFolder, "applied"),
                            s, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                } catch (IOException e) {
                    log.error("Could not write file {}", translationsDTO.getIdentifier() + ".applied");
                }
            });
        }
    }

    private Path getFilePath(TranslationsDTO translationsDTO, String folder, String extension) throws IOException {
        PathMatcher matcher = FileSystems.getDefault()
                .getPathMatcher(String.format(GLOB_PATTERN, translationsDTO.getIdentifier()));
        try (Stream<Path> stream = Files.list(Paths.get(folder))) {
            return stream
                    .filter(file -> !Files.isDirectory(file) && matcher.matches(file.getFileName()))
                    .map(path -> Path.of(folder,
                            path.getFileName().toString().replaceAll("(?<!^)[.].*", "") + "." + extension))
                    .findFirst().orElseThrow(() -> new FileNotFoundException(
                            String.format("File for identifier %s does not exist", translationsDTO.getIdentifier())));
        }
    }

    private Optional<String> applyToOriginalFile(String appliedValues, String originalFile) {
        Matcher matcher = PATTERN.matcher(originalFile);
        if (matcher.find()) {
            String found = matcher.group(1);
            return Optional.of(originalFile.replace(found, appliedValues));
        }
        return Optional.empty();
    }

    private String getAppliedValues(NodeList appliedNodes) throws TransformerException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < appliedNodes.getLength(); i++) {
            sb.append(getNodeString(appliedNodes.item(i)));
        }
        return sb.toString().replace("xmlns:dc=\"http://purl.org/dc/elements/1.1/\" ", "").trim();
    }

    private String getNodeString(Node node) throws TransformerException {
        StringWriter buffer = new StringWriter();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.transform(new DOMSource(node),
                new StreamResult(buffer));
        return buffer.toString();
    }

    private String getSnippet(String fullValue, boolean escape) {
        String clean = escape ? StringEscapeUtils.escapeXml(fullValue)
                .replaceAll(WHITESPACE_REGEX, "") : fullValue.replaceAll(WHITESPACE_REGEX, "");
        return clean.substring(0, Math.min(clean.length(), 50));
    }

    private boolean applyTranslation(String originalValue, String translation, String language, Element value) {
        boolean applied = false;

        if (value.getAttribute(XML_LANG).isEmpty()) {
            value.setAttribute(XML_LANG, language);
            applied = true;
        }
        if (!language.equals("en") && translation != null) {
            Element translationElement = (Element) value.cloneNode(true);
            translationElement.getFirstChild().setTextContent(translation);
            translationElement.setAttribute(XML_LANG, "en");
            translationElement.setAttribute(EDM_WAS_GENERATED_BY, SOFTWARE_AGENT);
            value.getParentNode().insertBefore(translationElement, value.getNextSibling());
            applied = true;
        }
        return applied;
    }

    public List<TranslationsDTO> streamTranslations(String dir) throws IOException {
        try (Stream<Path> stream = Files.list(Paths.get(dir))) {
            return stream
                    .filter(file -> !Files.isDirectory(file))
                    .map(path -> {
                        try {
                            return objectMapper.readValue(path.toFile(), TranslationsDTO.class);
                        } catch (IOException e) {
                            log.error("Cannot read {}", path.toAbsolutePath());
                        }
                        return null;
                    }).filter(Objects::nonNull).collect(Collectors.toList());
        }
    }
}

package pl.psnc.dei.util;

import lombok.experimental.UtilityClass;

/**
 * Util class used for applying translation to certain field in EDM XML
 */
@UtilityClass
public class TranslationUtils {
//
//    /**
//     * Method has possibility to trace file for multiple includes.
//     * if provided xsl file has invalid structure, method will return false
//     *
//     * @param mappingService        mapping service
//     * @param xsltMappingToCheck    examined mapping for its includes
//     * @param additionalXSLMappings additional mappings to check against
//     * @return true when xsl include line in examined mapping is not provided at all,
//     * or when include which ends with .xsl exists in database. Otherwise false
//     */
//    @SneakyThrows
//    public ParsingResult isXSLTFileParsable(final MappingService mappingService, final XSLTMapping xsltMappingToCheck,
//            final List<XSLTMapping> additionalXSLMappings) {
//        if (xsltMappingToCheck.getXslt() == null) {
//            return ParsingResult.wrong(Translator.getTranslation(UILabels.FILE_HAS_NOT_BEEN_PROVIDED));
//        }
//
//        List<String> additionalXSLTMappingsNames = additionalXSLMappings.stream().map(XSLTMapping::getName)
//                .collect(Collectors.toList());
//        List<String> invalidIncludeList = new ArrayList<>();
//        List<String> allIncludeAttributesToCheck;
//        try {
//            allIncludeAttributesToCheck = getAllIncludeAttributes(xsltMappingToCheck.getXslt());
//        } catch (WrongFormattedFile e) {
//            return ParsingResult.wrong(e.getMessage());
//        }
//
//        for (String includeLine : allIncludeAttributesToCheck) {
//            if (includeLine.endsWith(".xsl")) {
//                includeLine = (includeLine.substring(0, includeLine.length() - 4));
//            } else if (includeLine.endsWith(".xslt")) {
//                includeLine = (includeLine.substring(0, includeLine.length() - 5));
//            } else {
//                return ParsingResult.wrong(
//                        Translator.getTranslation(UILabels.NOT_SUPPORTED_FILE_EXTENSION, includeLine));
//            }
//
//            // first check if additional mappings do not contain include
//            // to avoid extra DB call
//            if (additionalXSLTMappingsNames.contains(includeLine)) {
//                continue;
//            }
//            // find by name and mapping type in repo
//            else if (mappingService.findByNameAndAndMappingType(includeLine, MappingType.TEXT_XSL).isPresent()) {
//                continue;
//            } else if (mappingService.findByNameAndAndMappingType(includeLine, MappingType.APPLICATION_JSON)
//                    .isPresent()) {
//                return ParsingResult.wrong(
//                        Translator.getTranslation(UILabels.DATABASE_CONTAINS_MAPPING_WITH_JSON_MAPPING_TYPE,
//                                includeLine));
//            }
//
//            invalidIncludeList.add(includeLine);
//        }
//
//        return isInvalidIncludesListEmpty(invalidIncludeList);
//    }
//
//    public ParsingResult isXSLTFileParsable(final MappingService mappingService, final XSLTMapping xsltMappingToCheck) {
//        return isXSLTFileParsable(mappingService, xsltMappingToCheck, Collections.emptyList());
//    }
//
//    public ParsingResult areXSLTFilesParsable(List<XSLTMapping> mappings, MappingService mappingService) {
//        for (XSLTMapping mapping : mappings) {
//            ParsingResult parsingResult = isXSLTFileParsable(mappingService, mapping, mappings);
//            if (!parsingResult.isCorrect()) {
//                return parsingResult;
//            }
//        }
//        return ParsingResult.correct();
//    }
//
//    /**
//     * Method checks if provided json mapping can be parsable - structure of JSON file is complete.
//     *
//     * @param mapping mapping to check pars-ability
//     * @return true when provided file is parsable, false when file does not exist,
//     * throw when file is wrongly formatted
//     */
//    public ParsingResult isJSONMappingParsable(final XSLTMapping mapping) {
//        if (mapping.getXslt() == null) {
//            return ParsingResult.wrong(Translator.getTranslation(UILabels.FILE_HAS_NOT_BEEN_PROVIDED));
//        }
//        try {
//            JsonParser.parseString(mapping.getXslt()).isJsonObject();
//            JsonParser.parseString(mapping.getXslt()).isJsonArray();
//        } catch (Exception e) {
//            return ParsingResult.wrong(Translator.getTranslation(UILabels.JSON_FILE_HAS_INVALID_STRUCTURE));
//        }
//        return ParsingResult.correct();
//    }
//
//    public ParsingResult areJsonMappingsParsable(final List<XSLTMapping> xsltMappings) {
//        return xsltMappings.stream().map(MappingParser::isJSONMappingParsable).filter(result -> !result.isCorrect())
//                .findFirst().orElse(ParsingResult.correct());
//    }
//
//    private ParsingResult isInvalidIncludesListEmpty(List<String> invalidIncludeList) {
//        if (!invalidIncludeList.isEmpty()) {
//            String text = invalidIncludeList.stream().collect(Collectors.joining("\",\"", "\"", "\""));
//            return ParsingResult.wrong(Translator.getTranslation(UILabels.CANNOT_SAVE_FILE_DOES_NOT_EXIST, text));
//        }
//        return ParsingResult.correct();
//    }
//
//    private List<String> getAllIncludeAttributes(String xslt) throws XPathExpressionException {
//        List<String> includesList = new ArrayList<>();
//        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
//        domFactory.setNamespaceAware(true);
//        Document document;
//        try {
//            document = domFactory.newDocumentBuilder().parse(new InputSource(new StringReader(xslt)));
//        } catch (SAXException | ParserConfigurationException | IOException e) {
//            throw new WrongFormattedFile(Translator.getTranslation(UILabels.XML_FILE_HAS_INVALID_STRUCTURE));
//        }
//        XPath xpath = XPathFactory.newInstance().newXPath();
//        XPathExpression expr = xpath.compile("//*[local-name()='include']//@href");
//
//        NodeList list = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
//        for (int i = 0; i < list.getLength(); i++) {
//            Node node = list.item(i);
//            includesList.add(node.getTextContent());
//        }
//
//        return includesList;
//    }
//
//    @RequiredArgsConstructor
//    public static class ParsingResult {
//        @Getter
//        private final String message;
//        @Getter
//        private final boolean correct;
//
//        public static ParsingResult correct() {
//            return new ParsingResult("", true);
//        }
//
//        public static ParsingResult wrong(String message) {
//            return new ParsingResult(message, false);
//        }
//    }
//
//    public static class WrongFormattedFile extends RuntimeException {
//        public WrongFormattedFile(String errorMessage) {
//            super(errorMessage);
//        }
//    }
}

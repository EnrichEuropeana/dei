package pl.psnc.dei.util;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import pl.psnc.dei.exception.ParseRecordsException;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class InputRecordsParser {

    private InputRecordsParser() {}

    public static Set<String> parseRecords(String records) throws ParseRecordsException {
        ObjectMapper mapper = new ObjectMapper();
        Set<String> parsedRecords;
        try {
            parsedRecords = mapper.readValue(records, new TypeReference<LinkedHashSet<String>>() {
            });
        } catch (JsonParseException | JsonMappingException jsonProcessingException) {
            if (inputSeemsToBeJSON(records)) {
                throw new ParseRecordsException("Invalid JSON array structure!");
            } else {
                parsedRecords = readLines(records);
            }
        } catch (IOException e) {
            throw new ParseRecordsException("Cannot read records!");
        }
        Set<String> normalizedRecords = normalizeRecords(parsedRecords);
        normalizedRecords.forEach(record -> {
            if (!isValidRecord(record)) {
                throw new ParseRecordsException("Invalid record: " + record);
            }
        });
        return normalizedRecords;
    }

    private static Set<String> readLines(String input) {
        String[] split = input.split("\n");
        return new LinkedHashSet<>(Arrays.asList(split));
    }

    private static Set<String> normalizeRecords(Set<String> records) {
        return records.stream()
                .map(String::strip)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private static boolean isValidRecord(String s) {
        return EuropeanaRecordIdValidator.validate(s);
    }

    private static boolean inputSeemsToBeJSON(String input) {
        char firstChar = input.strip().charAt(0);
        List<Character> jsonChars = Arrays.asList('[', '\"', '{');
        return jsonChars.contains(firstChar);
    }
}

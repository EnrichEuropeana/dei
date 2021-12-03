package pl.psnc.dei.enrichments;

import lombok.Data;
import pl.psnc.dei.enrichments.types.*;

import java.util.List;

@Data
public class EnrichmentsExport {

    private List<Transcription> transcriptions;
    private List<Place> places;
    private List<Person> persons;
    private List<Topic> topics;
    private List<Keyword> keywords;
}

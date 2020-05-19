package pl.psnc.dei.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pl.psnc.dei.model.Project;
import pl.psnc.dei.model.Record;
import pl.psnc.dei.model.Transcription;

/**
 * Class responsible for building urls to different endpoints;
 * <p>
 * Created by pwozniak on 3/20/19
 */
@Service
public class UrlBuilder {
	private static final String PROJECTS_SUFFIX = "/projects";
	private static final String DATASETS_SUFFIX = "/datasets";
	private static final String IMPORTS_ADD_SUFFIX = "/";
	private static final String STORIES_SUFFIX = "/stories";
	private static final String ENRICHMENTS_SUFFIX = "/enrichments";
	private static final String TRANSCRIPTION_SUFFIX = "/transcription";
	private static final String DATASET_PARAM = "datasetId=";
	private static final String IMPORT_NAME_PARAM = "importName=";
	private static final String STORY_ID_PARAM = "storyId=";
	private static final String EUROPEANA_ANNOTATION_ID_PARAM = "europeanaAnnotationId=";
	private static final String ANNOTATION_ID = "annotationId=";
	private static final String MOTIVATION = "motivation=";
	private static final String INCLUDE_EXPORTED_PARAM = "includeExported=";


	@Value("${transcription.api.url}")
	private String transcriptionPlatformLocation;

	public String getBaseUrl() {
		return transcriptionPlatformLocation;
	}

	public String urlForAllProjects() {
		return transcriptionPlatformLocation + PROJECTS_SUFFIX;
	}

	public String urlForSendingRecord(Record record) {
	    String url = transcriptionPlatformLocation
                + PROJECTS_SUFFIX
                + '/'
                + record.getProject().getProjectId()
                + STORIES_SUFFIX
                + '?'
                + IMPORT_NAME_PARAM
                + record.getAnImport().getName();
		if (record.getDataset() != null) {
            url += '&'
                    + DATASET_PARAM
                    + record.getDataset().getDatasetId();
        }
		return url;
	}

    public String urlForTranscription(Transcription transcription) {
        return transcriptionPlatformLocation + ENRICHMENTS_SUFFIX + TRANSCRIPTION_SUFFIX + "/" + transcription.getTp_id();
    }

    public String urlForProjectDatasets(Project project) {
        return transcriptionPlatformLocation + PROJECTS_SUFFIX + '/' + project.getProjectId() + DATASETS_SUFFIX;
    }

    public String urlForSendingImport() {
        return transcriptionPlatformLocation + IMPORTS_ADD_SUFFIX;
    }

    public String urlForRecordEnrichments(Record record, String europeanaAnnotationId, String motivation) {
        String url = transcriptionPlatformLocation
                + ENRICHMENTS_SUFFIX
                + '?'
                + STORY_ID_PARAM
                + record.getIdentifier();
        if (europeanaAnnotationId != null) {
            url += '&'
                    + EUROPEANA_ANNOTATION_ID_PARAM
                    + europeanaAnnotationId;
            // we have to include this to retrieve already exported transcriptions
            url += '&'
					+ INCLUDE_EXPORTED_PARAM
					+ "1";
        }

        if (motivation != null) {
        	url += '&'
					+ MOTIVATION
					+ motivation;
        }

        return url;
    }
}

package pl.psnc.dei.iiif;

import org.springframework.stereotype.Service;
import pl.psnc.dei.model.Record;

import java.io.File;
import java.util.List;

@Service
public class FilesStore {

	/**
	 * Method used to store files on iiifImageServer
	 * @param record Record which files belongs to
	 * @param files Files to be saved
	 * @return List of images ids under which stored files can be accessed on iiif image server
	 */
	public List<String> storeFiles(Record record, List<File> files) {
//		TODO should be implemented in EN-65
		return null;
	}

}

package pl.psnc.dei.iiif;

import org.apache.commons.io.FileUtils;
import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import pl.psnc.dei.model.DAO.RecordsRepository;
import pl.psnc.dei.model.Record;
import pl.psnc.dei.service.EuropeanaRestService;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Converter needs vips and poppler to run
 * At first install poppler as shown in there: https://gist.github.com/Dayjo/618794d4ff37bb82ddfb02c63b450a81
 * Then install vips as shown in there: https://github.com/jcupitt/libvips/wiki/Build-for-Ubuntu
 */
public class Converter {

	private static final Logger logger = LoggerFactory.getLogger(Converter.class);

	private static final CommandExecutor executor = new CommandExecutor();

	private final Record record;

	@Autowired
	private RecordsRepository recordsRepository;

	@Autowired
	private EuropeanaRestService ers;

	@Autowired
	private FilesStore fs;

	@Value("${conversion.directory}")
	private String conversionDirectory;

	@Value("${conversion.iiif.server.url}")
	private String iiifImageServerUrl;

	public Converter(Record record) {
		this.record = record;
	}

	public void convertAndGenerateManifest() throws ConversionException, IOException, InterruptedException {
		JsonObject recordJson = ers.retrieveRecordFromEuropeanaAndConvertToJsonLd(record.getIdentifier());
		Optional<JsonObject> aggregatorData = recordJson.get("@graph").getAsArray().stream()
				.map(JsonValue::getAsObject)
				.filter(e -> e.get("isShownBy") != null)
				.findFirst();

		if (!aggregatorData.isPresent())
			throw new ConversionException("Can't convert! Record doesn't contain files list!");

		List<URL> filesUrls = extractFilesUrls(aggregatorData.get());
		cleanDirectory(new File(conversionDirectory, record.getIdentifier()));
		List<File> filesToConvert = saveFilesInTempDirectory(filesUrls);

		List<String> storedFilesIds;
//		if(RecordTransferValidationUtil.checkIfTransferPossible(recordJson)) {TODO change after merge with EN-63
		storedFilesIds = fs.storeFiles(record, filesToConvert);    /** IIIF record case */
//		} else {
		storedFilesIds = fs.storeFiles(record, convertFiles(filesToConvert));
//		}

		cleanDirectory(new File(conversionDirectory, record.getIdentifier()));
		record.setIiifManifest(getManifest(storedFilesIds).toString());
		recordsRepository.save(record);
	}

	private List<URL> extractFilesUrls(JsonObject aggregatorData) {
		String mainFileUrl = aggregatorData.get("isShownBy").getAsString().value();
		String mainFileFormat = mainFileUrl.substring(mainFileUrl.lastIndexOf('.'));

		List<String> links = new ArrayList<>();
		links.add(mainFileUrl);
		if (aggregatorData.get("hasView") != null)
			links.addAll(aggregatorData.get("hasView").getAsArray().stream()
					.map(e -> e.getAsString().value())
					.filter(s -> s.endsWith(mainFileFormat))
					.collect(Collectors.toList()));

		List<URL> result = new ArrayList<>();
		for (String link : links) {
			try {
				result.add(new URL(link));
			} catch (MalformedURLException e) {
				logger.error("Incorrect file URL for record: {}, url: {}", record.getIdentifier(), link, e);
			}
		}

		return result;
	}

	private List<File> saveFilesInTempDirectory(List<URL> filesUrls) throws ConversionException {
		File tempDir = new File(conversionDirectory, record.getIdentifier() + "/src");
		tempDir.mkdirs();

		for (URL url : filesUrls) {
			try {
				String filePath = url.getPath();
				String fileName = filePath.substring(filePath.lastIndexOf("/"));
				File tempFile = new File(tempDir, fileName);
				FileUtils.copyURLToFile(url, tempFile);
			} catch (IOException e) {
				logger.error("Couldn't get file: {}", url.toString(), e);
			}
		}

		if (tempDir.listFiles().length == 0) {
			throw new ConversionException("Couldn't get even 1 file, conversion aborted!");
		} else {
			return Arrays.asList(tempDir.listFiles());
		}
	}

	private List<File> convertFiles(List<File> filesToConvert) throws InterruptedException, IOException {
		File outDir = new File(conversionDirectory, record.getIdentifier() + "/out");
		outDir.mkdirs();

		if (filesToConvert.get(0).getAbsolutePath().endsWith("pdf")) {
			String pdfConversionScript = new ClassPathResource("pdf_to_pyramid_tiff.sh").getFile().getAbsolutePath();
			File pdfFile = filesToConvert.get(0);
			try {
				executor.runCommand(Arrays.asList(
						pdfConversionScript,
						pdfFile.getAbsolutePath(),
						outDir.getAbsolutePath()));
			} catch (ConversionException e) {
				logger.error("Conversion failed for file: " + pdfFile.getName() + " from record: " + record.getIdentifier());
			}
		} else {
			for (File file : filesToConvert) {
				try {
					executor.runCommand(Arrays.asList("vips",
							"tiffsave",
							file.getAbsolutePath(),
							outDir.getAbsolutePath() + "/" + getTiffFileName(file),
							"--compression=jpeg",
							"--Q=70",
							"--tile",
							"--tile-width=512",
							"--tile-height=512",
							"--pyramid"));
				} catch (ConversionException e) {
					logger.error("Conversion failed for file: " + file.getName() + " from record: " + record.getIdentifier());
				}
			}
		}

		return Arrays.asList(outDir.listFiles());
	}

	private String getTiffFileName(File file) {
		return file.getName().split(".")[0] + ".tif";
	}

	private JsonObject getManifest(List<String> storedFilesIds) {
		JsonObject manifest = new JsonObject();
		manifest.put("@context", "http://iiif.io/api/presentation/2/context.json");
		manifest.put("@id", "/api/transcription/iiif/manifest?recordId=" + record.getIdentifier());
		manifest.put("@type", "sc:manifest");

		JsonObject sequence = new JsonObject();
		JsonArray sequences = new JsonArray();
		sequences.add(sequence);
		manifest.put("sequences", sequences);
		sequence.put("@type", "sc:Sequence");
		sequence.put("canvases", getSequenceJson(storedFilesIds));

		return manifest;
	}

	private JsonArray getSequenceJson(List<String> storedFilesIds) {
		JsonArray canvases = new JsonArray();

		for (String imageId : storedFilesIds) {
			JsonObject canvas = new JsonObject();
			canvases.add(canvas);

			canvas.put("@id", iiifImageServerUrl + "/canvas/" + imageId);
			canvas.put("@type", "sc:canvas");
			canvas.put("label", imageId);
			canvas.put("width", "1000");
			canvas.put("height", "1000");

			JsonArray images = new JsonArray();
			canvas.put("images", images);
			JsonObject image = new JsonObject();
			images.add(image);

			image.put("@type", "oa:Annotation");
			image.put("motivation", "sc:painting");
			image.put("on", canvas.get("@id"));
			JsonObject resource = new JsonObject();
			image.put("resource", resource);

			resource.put("@id", iiifImageServerUrl + "/fcgi-bin/iipsrv.fcgi?IIIF=" + imageId + "/full/full/0/default.jpg");
			resource.put("@type", "dctypes:Image");
			JsonObject service = new JsonObject();

			service.put("@id", iiifImageServerUrl + "/fcgi-bin/iipsrv.fcgi?IIIF=" + imageId);
			service.put("@context", "http://iiif.io/api/image/2/context.json");
			service.put("profile", "http://iiif.io/api/image/2/level1.json");
		}

		return canvases;
	}

	private void cleanDirectory(File directory) {
		try {
			FileUtils.cleanDirectory(directory);
		} catch (IOException e) {
			logger.info("Cleaning " + directory.getAbsolutePath() + " failed...", e);
		}
	}

}

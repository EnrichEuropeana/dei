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
 * And then install libvips-tools for command-line tools
 */
public class Converter {

	private static final Logger logger = LoggerFactory.getLogger(Converter.class);

	private static final CommandExecutor executor = new CommandExecutor();

	private final Record record;

	private final JsonObject recordJson;

	@Autowired
	private RecordsRepository recordsRepository;

	@Value("${conversion.directory}")
	private String conversionDirectory;

	@Value("${conversion.iiif.server.url}")
	private String iiifImageServerUrl;

	@Value("${application.server.url}")
	private String serverUrl;

	private final File srcDir;

	private final File outDir;

	public Converter(Record record, JsonObject recordJson) {
		this.record = record;
		this.recordJson = recordJson;
		String imagePath = record.getProject().getProjectId() + "/"
				+ (record.getDataset() != null ? record.getDataset().getDatasetId() + "/" : "")
				+ record.getIdentifier();

		srcDir = new File(conversionDirectory, "/src/" + imagePath);
		srcDir.mkdirs();

		outDir = new File(conversionDirectory, "/out/" + imagePath);
		outDir.mkdirs();
	}

	public void convertAndGenerateManifest() throws ConversionException {
		Optional<JsonObject> aggregatorData = recordJson.get("@graph").getAsArray().stream()
				.map(JsonValue::getAsObject)
				.filter(e -> e.get("edm:isShownBy") != null)
				.findFirst();

		if (!aggregatorData.isPresent())
			throw new ConversionImpossibleException("Can't convert! Record doesn't contain files list!");

		try {
			ConversionDataHolder conversionDataHolder = new ConversionDataHolder(aggregatorData.get());
			saveFilesInTempDirectory(conversionDataHolder);
			convertFiles(conversionDataHolder);
			List<ConversionData> convertedFiles = conversionDataHolder.fileObjects.stream()
					.filter(e -> e.outFile != null)
					.collect(Collectors.toList());

			record.setIiifManifest(getManifest(convertedFiles).toString());
			record.setState(Record.RecordState.T_PENDING);
			recordsRepository.save(record);
		} catch (Exception e) {
			FileUtils.deleteQuietly(outDir);
			throw e;
		} finally {
			FileUtils.deleteQuietly(srcDir);
		}
	}

	private void saveFilesInTempDirectory(ConversionDataHolder dataHolder) throws ConversionException {
		for (ConversionData data : dataHolder.fileObjects) {
			if (data.srcFileUrl == null)
				continue;

			try {
				String filePath = data.srcFileUrl.getPath();
				filePath = filePath.endsWith("/") ? filePath.substring(0, filePath.length() - 1) : filePath;
				String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
				File tempFile = new File(srcDir, fileName);
				FileUtils.copyURLToFile(data.srcFileUrl, tempFile);
				data.srcFile = tempFile;
			} catch (IOException e) {
				logger.error("Couldn't get file: {}", data.srcFileUrl.toString(), e);
				throw new ConversionException("Couldn't get file " + data.srcFileUrl.toString(), e);
			}
		}
	}

	private void convertFiles(ConversionDataHolder dataHolder) throws ConversionImpossibleException {
		if (dataHolder.fileObjects.get(0).srcFile != null
				&& dataHolder.fileObjects.get(0).srcFile.getName().endsWith("pdf")) {
			File pdfFile = dataHolder.fileObjects.get(0).srcFile;
			try {
				String pdfConversionScript = new ClassPathResource("pdf_to_pyramid_tiff.sh").getFile().getAbsolutePath();
				executor.runCommand(Arrays.asList(
						pdfConversionScript,
						pdfFile.getAbsolutePath(),
						outDir.getAbsolutePath()));
			} catch (ConversionException | InterruptedException | IOException e) {
				logger.error("Conversion failed for file: " + pdfFile.getName() + " from record: " + record.getIdentifier(), e);
			}
		} else {
			for (ConversionData convData : dataHolder.fileObjects) {
				if (convData.srcFile == null)
					continue;

				try {
					executor.runCommand(Arrays.asList("vips",
							"tiffsave",
							convData.srcFile.getAbsolutePath(),
							outDir.getAbsolutePath() + "/" + getTiffFileName(convData.srcFile),
							"--compression=jpeg",
							"--Q=70",
							"--tile",
							"--tile-width=512",
							"--tile-height=512",
							"--pyramid"));

					File convertedFile = new File(outDir, getTiffFileName(convData.srcFile));
					if (convertedFile.exists()) {
						convData.outFile = convertedFile;
						convData.imagePath = record.getProject().getProjectId() + "/"
								+ (record.getDataset() != null ? record.getDataset().getDatasetId() + "/" : "")
								+ record.getIdentifier() + "/"
								+ getTiffFileName(convData.srcFile);
					} else {
						logger.error("Conversion failed for file: " + convData.srcFile.getName() + " from record: " + record.getIdentifier());
					}
				} catch (ConversionException | InterruptedException | IOException e) {
					logger.error("Conversion failed for file: " + convData.srcFile.getName() + " from record: " + record.getIdentifier());
				}
			}
		}
		if (outDir.listFiles().length == 0)
			throw new ConversionImpossibleException("Couldn't convert any file, conversion not possible");
	}

	private String getTiffFileName(File file) {
		return file.getName().split(".")[0] + ".tif";
	}

	private JsonObject getManifest(List<ConversionData> storedFilesData) {
		JsonObject manifest = new JsonObject();
		manifest.put("@context", "http://iiif.io/api/presentation/2/context.json");
		manifest.put("@id", serverUrl + "/api/transcription/iiif/manifest?recordId=" + record.getIdentifier());
		manifest.put("@type", "sc:manifest");

		JsonObject sequence = new JsonObject();
		JsonArray sequences = new JsonArray();
		sequences.add(sequence);
		manifest.put("sequences", sequences);
		sequence.put("@type", "sc:Sequence");
		sequence.put("canvases", getSequenceJson(storedFilesData));

		return manifest;
	}

	private JsonArray getSequenceJson(List<ConversionData> storedFilesData) {
		JsonArray canvases = new JsonArray();

		for (ConversionData data : storedFilesData) {
			JsonObject canvas = new JsonObject();
			canvases.add(canvas);

			canvas.put("@id", iiifImageServerUrl + "/canvas/" + data.imagePath);
			canvas.put("@type", "sc:canvas");
			canvas.put("label", data.imagePath);
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

			resource.put("@id", iiifImageServerUrl + "/fcgi-bin/iipsrv.fcgi?IIIF=" + data.imagePath + "/full/full/0/default.jpg");
			data.json.put("manifestFileId", iiifImageServerUrl + "/fcgi-bin/iipsrv.fcgi?IIIF=" + data.imagePath + "/full/full/0/default.jpg");
			resource.put("@type", "dctypes:Image");
			JsonObject service = new JsonObject();

			service.put("@id", iiifImageServerUrl + "/fcgi-bin/iipsrv.fcgi?IIIF=" + data.imagePath);
			service.put("@context", "http://iiif.io/api/image/2/context.json");
			service.put("profile", "http://iiif.io/api/image/2/level1.json");
		}

		return canvases;
	}

	private class ConversionDataHolder {

		List<ConversionData> fileObjects = new ArrayList<>();

		ConversionDataHolder(JsonObject aggregatorData) {
			ConversionData isShownBy = new ConversionData();
			isShownBy.json = aggregatorData.get("edm:isShownBy").getAsObject();
			fileObjects.add(isShownBy);
			String mainFileUrl = isShownBy.json.get("@id").getAsString().value();
			String mainFileFormat = mainFileUrl.substring(mainFileUrl.lastIndexOf('.'));

			if (aggregatorData.get("edm:hasView") != null)
				fileObjects.addAll(aggregatorData.get("edm:hasView").getAsArray().stream()
						.filter(e -> e.getAsObject().get("@id").getAsString().value().endsWith(mainFileFormat))
						.map(e -> {
							ConversionData data = new ConversionData();
							data.json = e.getAsObject();
							return data;
						})
						.collect(Collectors.toList()));

			initFileUrls();
		}

		void initFileUrls() {
			for (ConversionData data : fileObjects) {
				String url = data.json.get("@id").getAsString().value();
				try {
					data.srcFileUrl = new URL(url);
				} catch (MalformedURLException e) {
					logger.error("Incorrect file URL for record: {}, url: {}", record.getIdentifier(), url, e);
				}
			}
		}

	}

	private class ConversionData {
		JsonObject json;
		URL srcFileUrl;
		File outFile;
		File srcFile;
		String imagePath;
	}

}

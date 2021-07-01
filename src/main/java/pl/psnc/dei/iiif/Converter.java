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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import pl.psnc.dei.model.Aggregator;
import pl.psnc.dei.model.DAO.RecordsRepository;
import pl.psnc.dei.model.Record;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Converter needs vips and poppler to run
 * At first install poppler as shown in there: https://gist.github.com/Dayjo/618794d4ff37bb82ddfb02c63b450a81
 * Then install vips as shown in there: https://github.com/jcupitt/libvips/wiki/Build-for-Ubuntu
 * And then install libvips-tools for command-line tools
 */
@Component
public class Converter {

	private static final Logger logger = LoggerFactory.getLogger(Converter.class);

	private static final CommandExecutor executor = new CommandExecutor();

	private static final Pattern DIMENSIONS_PATTERN = Pattern.compile("Image size\\s*:\\s*(\\d*)\\s*x\\s*(\\d*)");

	private static final int DEFAULT_DIMENSION = 6000;

	private static final int MAX_RETRY_COUNT = 3;

	private Record record;

	@Autowired
	private RecordsRepository recordsRepository;

	@Value("${conversion.directory}")
	private String conversionDirectory;

	@Value("#{${conversion.url.replacements}}")
	private Map<String,String> urlReplacements;

	@Value("${conversion.iiif.server.url}")
	private String iiifImageServerUrl;

	@Value("${application.server.url}")
	private String serverUrl;

	@Value("${server.servlet.context-path}")
	private String serverPath;

	private File srcDir;

	private File outDir;

	@PostConstruct
	private void copyScript() {
		try {
			URL inputUrl = new ClassPathResource("pdf_to_pyramid_tiff.sh").getURL();
			File dest = new File("pdf_to_pyramid_tiff.sh");
			dest.setExecutable(true);
			FileUtils.copyURLToFile(inputUrl, dest);
			logger.info("Conversion script located in " + dest.getAbsolutePath());
		} catch (IOException e) {
			logger.info("Cannot find file.. ", e);
		}

	}

	public synchronized void convertAndGenerateManifest(Record record, JsonObject recordJson, JsonObject recordJsonRaw) throws ConversionException, IOException, InterruptedException {
		this.record = record;
		String imagePath = record.getProject().getProjectId() + "/"
				+ (record.getDataset() != null ? record.getDataset().getDatasetId() + "/" : "")
				+ record.getIdentifier();

		srcDir = new File(conversionDirectory, "/src/" + imagePath);
		srcDir.mkdirs();
		logger.info("Source dir created: " + srcDir.getAbsolutePath());

		outDir = new File(conversionDirectory, "/out/" + imagePath);
		outDir.mkdirs();
		logger.info("Output dir created: " + outDir.getAbsolutePath());

		try {
			ConversionDataHolder conversionDataHolder = createDataHolder(record, recordJson, recordJsonRaw);
			saveFilesInTempDirectory(conversionDataHolder);
			convertAllFiles(conversionDataHolder);
			List<ConversionDataHolder.ConversionData> convertedFiles = conversionDataHolder.fileObjects.stream()
					.filter(e -> e.outFile != null && !e.outFile.isEmpty())
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

	private ConversionDataHolder createDataHolder(Record record, JsonObject recordJson, JsonObject recordJsonRaw) throws ConversionImpossibleException {
		Aggregator aggregator = record.getAggregator();
		switch (aggregator) {
			case EUROPEANA:
				Optional<JsonObject> aggregatorData = recordJson.get("@graph").getAsArray().stream()
						.map(JsonValue::getAsObject)
						.filter(e -> e.get("edm:isShownBy") != null)
						.findFirst();

				if (!aggregatorData.isPresent()) {
					throw new ConversionImpossibleException("Can't convert! Record doesn't contain files list!");
				}

				return new EuropeanaConversionDataHolder(record.getIdentifier(), aggregatorData.get(), recordJson, recordJsonRaw);
			case DDB:
				if (recordJson == null) {
					throw new ConversionImpossibleException("Can't convert! Record doesn't contain files list!");
				}
				return new DDBConversionDataHolder(record.getIdentifier(), recordJson);
			default:
				throw new IllegalStateException("Unsupported aggregator");
		}
	}

	private void saveFilesInTempDirectory(ConversionDataHolder dataHolder) throws ConversionException {
		for (ConversionDataHolder.ConversionData data : dataHolder.fileObjects) {
			if (data.srcFileUrl == null)
				continue;

			try {
				String fileName = getFileName(data);
				File tempFile = new File(srcDir, fileName);

				// temporary solution for records from Portugal
				replaceUrl(data);

				copyURLToFile(data.srcFileUrl, tempFile);
				data.srcFile = tempFile;
			} catch (IOException e) {
				logger.error("Couldn't get file: {}", data.srcFileUrl.toString(), e);
				throw new ConversionException("Couldn't get file " + data.srcFileUrl.toString(), e);
			}
		}
	}

	private void replaceUrl(ConversionDataHolder.ConversionData data) throws MalformedURLException {
		for (Map.Entry<String, String> entry: urlReplacements.entrySet()) {
			if (data.srcFileUrl.toString().contains(entry.getKey())) {
				data.srcFileUrl = new URL(data.srcFileUrl.toString().replace(entry.getKey(), entry.getValue()));
				break;
			}
		}
	}

	private void copyURLToFile(URL url, File file) throws IOException {
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setConnectTimeout(500);
		connection.setReadTimeout(5000);
		connection.setInstanceFollowRedirects(true);
		int code = connection.getResponseCode();
		if (code == HttpStatus.MOVED_PERMANENTLY.value() ||
				code == HttpStatus.SEE_OTHER.value()) {
			connection = (HttpURLConnection) new URL(connection.getHeaderField("Location")).openConnection();
		}
		FileUtils.copyInputStreamToFile(connection.getInputStream(), file);
	}

	private String getFileName(ConversionDataHolder.ConversionData data) {
		String filePath = data.srcFileUrl.getPath();
		filePath = filePath.endsWith("/") ? filePath.substring(0, filePath.length() - 1) : filePath;
		if (filePath.contains("?")) {
			return filePath.substring(filePath.lastIndexOf('/') + 1, filePath.indexOf('?'));
		}
		return filePath.substring(filePath.lastIndexOf('/') + 1).replace(" ", "").replace("%20", "");
	}

	private String  extractFileName(String name) {
		int i = name.lastIndexOf('.');
		return i != -1 ? name.substring(0, i) : name;
	}

	private void convertAllFiles(ConversionDataHolder dataHolder) throws ConversionException, InterruptedException, IOException {
		for (ConversionDataHolder.ConversionData convData : dataHolder.fileObjects) {
			if (convData.srcFile == null || convData.mediaType == null)
				continue;

			if (convData.mediaType.toLowerCase().equals("pdf")) {
				try {
					String pdfConversionScript = "./pdf_to_pyramid_tiff.sh";
					executor.runCommand(Arrays.asList(
							pdfConversionScript,
							convData.srcFile.getAbsolutePath(),
							outDir.getAbsolutePath()));

					prepareImagePaths(convData);
				} catch (ConversionException | InterruptedException | IOException e) {
					logger.error("Conversion failed for file: " + convData.srcFile.getName() + " from record: " + record.getIdentifier(), e);
					throw e;
				}

			} else {
				try {
					executor.runCommand(Arrays.asList("vips",
							"tiffsave",
							convData.srcFile.getAbsolutePath(),
							outDir.getAbsolutePath() + "/" + getTiffFileName(convData.srcFile.getName()),
							"--compression=jpeg",
							"--Q=75",
							"--tile",
							"--tile-width=128",
							"--tile-height=128",
							"--pyramid"));

					File convertedFile = new File(outDir, getTiffFileName(convData.srcFile.getName()));
					if (convertedFile.exists()) {
						convData.outFile.add(convertedFile);
						convData.imagePath.add(record.getProject().getProjectId() + "/"
								+ (record.getDataset() != null ? record.getDataset().getDatasetId() + "/" : "")
								+ record.getIdentifier() + "/"
								+ getTiffFileName(convData.srcFile.getName()));
						convData.dimensions.add(extractDimensions(convertedFile, MAX_RETRY_COUNT));
					} else {
						throw new ConversionException("Conversion failed for file " + convData.srcFile.getName() + " from record: " + record.getIdentifier());
					}
				} catch (ConversionException | InterruptedException | IOException e) {
					logger.error("Conversion failed for file: " + convData.srcFile.getName() + " from record: " + record.getIdentifier() + "cause " + e.getMessage());
					throw e;
				}
			}
		}
		if (outDir.listFiles().length == 0) {
			throw new ConversionImpossibleException("Couldn't convert any file, conversion not possible");
		}
	}

	private void prepareImagePaths(ConversionDataHolder.ConversionData convData) {
		String filterName = extractFileName(convData.srcFile.getName());

		File[] files = outDir.listFiles((file, name) -> name.startsWith(filterName));
		if (files != null) {
			Arrays.sort(files);
			Arrays.stream(files).forEach(file -> {
				convData.outFile.add(file);
				convData.imagePath.add(record.getProject().getProjectId() + "/"
						+ (record.getDataset() != null ? record.getDataset().getDatasetId() + "/" : "")
						+ record.getIdentifier() + "/"
						+ file.getName());
				convData.dimensions.add(extractDimensions(file, MAX_RETRY_COUNT));
				logger.info("Output file for source " + convData.srcFile + ": " + convData.imagePath.get(convData.imagePath.size() - 1));
			});
		}
	}

	private Dimension extractDimensions(File file, int retryCount) {
		if (retryCount > 0) {
			try {
				String output = executor.runCommand(Arrays.asList(
						"exiv2",
						file.getAbsolutePath()));
				return getDimensionFromPattern(DIMENSIONS_PATTERN.matcher(output));
			} catch (Exception e) {
				logger.warn("Could not extract image dimensions. Setting default 6000x6000");
				return extractDimensions(file, --retryCount);
			}
		}
		return new Dimension(DEFAULT_DIMENSION, DEFAULT_DIMENSION);
	}

	private Dimension getDimensionFromPattern(Matcher matcher) {
		int width = DEFAULT_DIMENSION;
		int height = DEFAULT_DIMENSION;

		if (matcher.find()) {
			width = Integer.parseInt(matcher.group(1));
			height = Integer.parseInt(matcher.group(2));
		}
		return new Dimension(width, height);
	}

	private String getTiffFileName(String fileName) {
		int i = fileName.lastIndexOf('.');
		return (i != -1 ? fileName.substring(0, i) : fileName) + ".tif";
	}

	private JsonObject getManifest(List<ConversionDataHolder.ConversionData> storedFilesData) {
		JsonObject manifest = new JsonObject();
		manifest.put("@context", "http://iiif.io/api/presentation/2/context.json");
		manifest.put("@id", serverUrl + serverPath + "/api/transcription/iiif/manifest?recordId=" + record.getIdentifier());
		manifest.put("@type", "sc:manifest");

		JsonObject sequence = new JsonObject();
		JsonArray sequences = new JsonArray();
		sequences.add(sequence);
		manifest.put("sequences", sequences);
		sequence.put("@type", "sc:Sequence");
		sequence.put("canvases", getSequenceJson(storedFilesData));

		return manifest;
	}

	private JsonArray getSequenceJson(List<ConversionDataHolder.ConversionData> storedFilesData) {
		JsonArray canvases = new JsonArray();

		for (ConversionDataHolder.ConversionData data : storedFilesData) {
			for (int i = 0; i < data.imagePath.size(); i++) {
				String imagePath = data.imagePath.get(i);
				JsonObject canvas = new JsonObject();
				canvases.add(canvas);

				canvas.put("@id", iiifImageServerUrl + "/canvas/" + imagePath);
				canvas.put("@type", "sc:canvas");
				canvas.put("label", imagePath);
				canvas.put("width", data.dimensions.get(i).width);
				canvas.put("height", data.dimensions.get(i).height);

				JsonArray images = new JsonArray();
				canvas.put("images", images);
				JsonObject image = new JsonObject();
				images.add(image);

				image.put("@type", "oa:Annotation");
				image.put("motivation", "sc:painting");
				image.put("on", canvas.get("@id"));
				JsonObject resource = new JsonObject();
				image.put("resource", resource);

				resource.put("@id", iiifImageServerUrl + "/fcgi-bin/iipsrv.fcgi?IIIF=" + imagePath + "/full/full/0/default.jpg");
				resource.put("@type", "dctypes:Image");
				resource.put("width", data.dimensions.get(i).width);
				resource.put("height", data.dimensions.get(i).height);
				JsonObject service = new JsonObject();
				resource.put("service", service);

				service.put("@id", iiifImageServerUrl + "/fcgi-bin/iipsrv.fcgi?IIIF=" + imagePath);
				service.put("@context", "http://iiif.io/api/image/2/context.json");
				service.put("profile", "http://iiif.io/api/image/2/level1.json");
			}
		}

		return canvases;
	}

	public void fillJsonData(Record record, JsonObject jsonObject, JsonObject jsonObjectRaw) {
		try {
			ConversionDataHolder conversionData = createDataHolder(record, jsonObject, jsonObjectRaw);
			conversionData.initFileUrls(record.getIdentifier());
			if(!conversionData.fileObjects.isEmpty() && conversionData.fileObjects.get(0).srcFileUrl.toString().toLowerCase().endsWith("pdf"))
				return;
			for (ConversionDataHolder.ConversionData data : conversionData.fileObjects)
				data.json.put("manifestFileId",
						iiifImageServerUrl + "/fcgi-bin/iipsrv.fcgi?IIIF="
								+ record.getProject().getProjectId() + "/"
								+ (record.getDataset() != null ? record.getDataset().getDatasetId() + "/" : "")
								+ record.getIdentifier() + "/"
								+ getTiffFileName(getFileName(data)) + "/full/full/0/default.jpg");
		} catch (ConversionImpossibleException e) {
			logger.warn("Manifest data couldn't be added!", e);
		}
	}
}

package pl.psnc.dei.iiif;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

class CommandExecutor {

	public static final Logger logger = LoggerFactory.getLogger(CommandExecutor.class);

	private ExecutorService printingThread = Executors.newSingleThreadExecutor();

	public void runCommand(List<String> command) throws IOException, InterruptedException, ConversionException {
		Process process = new ProcessBuilder(command).redirectInput(ProcessBuilder.Redirect.INHERIT).redirectErrorStream(true).start();
		printingThread.execute(() -> {
			try (InputStream in = process.getInputStream()) {
				logger.info("\n" + IOUtils.toString(in, Charset.defaultCharset()));
			} catch (IOException e) {
				logger.error("Error while reading script output...", e);
			}
		});

		if(!process.waitFor(2, TimeUnit.HOURS))
			throw new ConversionException("Conversion process timeout!");
		int executionResult = process.exitValue();

		if (executionResult != 0) {
			throw new ConversionException("Command execution failed for: " + command.toString());
		}
	}
}

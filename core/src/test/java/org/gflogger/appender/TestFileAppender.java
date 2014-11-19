package org.gflogger.appender;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.gflogger.Appender;
import org.gflogger.LogEntryItemImpl;
import org.gflogger.LogLevel;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author vladimir.dolzhenko@gmail.com
 */
public class TestFileAppender {

	private static final int BUFFER_SIZE = 16;
	private static final String LAYOUT_PATTERN = "%m"; // only message
	private Appender<LogEntryItemImpl> appender;
	private File tempFile;

	@Before
	public void init() throws Throwable {
		FileAppenderFactory appenderFactory = new FileAppenderFactory();
		tempFile = File.createTempFile("temp-file-name", ".tmp");
		appenderFactory.setFileName(tempFile.getAbsolutePath());
		appenderFactory.setLayoutPattern(LAYOUT_PATTERN);
		appenderFactory.setBufferSize(BUFFER_SIZE);
		appender = createAppender(appenderFactory);
		appender.start();
	}

	@After
	public void tearDown(){
		appender.stop();
		tempFile.delete();
	}

	@Test
	public void testBufferOverflow() throws Throwable {
		String firstMessage = "Hello world!"; // 12 length
		simulateEntryProcessing(firstMessage);
		simulateEntryProcessing("Hello bug!"); // 10 length
		assertOutput(firstMessage);
	}

	private void simulateEntryProcessing(String message) throws Throwable {
		appender.process(createEntry(message));
	}

	private void assertOutput(String expected) throws Throwable {
		final byte[] bytes = Files.readAllBytes(Paths.get(tempFile.getAbsolutePath()));
		assertEquals(expected, new String(bytes));
	}

	private static Appender<LogEntryItemImpl> createAppender(FileAppenderFactory appenderFactory) {
		@SuppressWarnings("unchecked")
		final Appender<LogEntryItemImpl> result = appenderFactory.createAppender(null);
		return result;
	}

	private static LogEntryItemImpl createEntry(String message) throws Throwable {
		LogEntryItemImpl entry = new LogEntryItemImpl(message.length());
		entry.setLogLevel(LogLevel.INFO);
		entry.getBuffer().put(message.getBytes("ascii"));
		return entry;
	}

}

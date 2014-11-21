package org.gflogger.appender;

import java.io.UnsupportedEncodingException;

import org.gflogger.Appender;
import org.gflogger.LogEntryItemImpl;
import org.gflogger.LogLevel;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Artyom Korzun
 */
public class TestConsoleAppender {

	private static final int BUFFER_SIZE = 16;
	private static final String LAYOUT_PATTERN = "%m"; // only message
	private final StringBuilder stream = new StringBuilder();
	private Appender<LogEntryItemImpl> appender;

	@Before
	public void init() {
		ConsoleAppenderFactory appenderFactory = new ConsoleAppenderFactory();
		appenderFactory.setOutputStream(stream);
		appenderFactory.setLayoutPattern(LAYOUT_PATTERN);
		appenderFactory.setBufferSize(BUFFER_SIZE);
		appender = createAppender(appenderFactory);
	}

	@Test
	public void appenderFlushesBufferedMessagesThenNextMessageExceedsBufferCapacity() throws UnsupportedEncodingException {
		final String firstMessage = stringWithLength( BUFFER_SIZE-1 );
		processEntryWithMessage( firstMessage );
		processEntryWithMessage( stringWithLength( 2 ) );
		assertOutput(firstMessage);
	}

	private void processEntryWithMessage( String message ) throws UnsupportedEncodingException {
		appender.process(createEntry(message));
	}

	private static String stringWithLength(final int length){
		final StringBuilder sb = new StringBuilder();
		for( int i = 0; i < length; i++ ) {
			sb.append( '*' );
		}
		return sb.toString();
	}

	private void assertOutput(String expected) {
		assertEquals(expected, stream.toString());
	}

	private static Appender<LogEntryItemImpl> createAppender(ConsoleAppenderFactory appenderFactory) {
		@SuppressWarnings("unchecked")
		final Appender<LogEntryItemImpl> result = appenderFactory.createAppender(null);
		return result;
	}

	private static LogEntryItemImpl createEntry(String message) throws UnsupportedEncodingException {
		LogEntryItemImpl entry = new LogEntryItemImpl(message.length());
		entry.setLogLevel(LogLevel.INFO);
		entry.getBuffer().put(message.getBytes("ascii"));
		return entry;
	}

}

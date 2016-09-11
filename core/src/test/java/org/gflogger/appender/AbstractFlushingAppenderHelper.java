package org.gflogger.appender;

import org.gflogger.LogEntryItemImpl;
import org.gflogger.LogLevel;
import org.gflogger.formatting.StringLoggingStrategy;
import org.junit.Before;
import org.junit.Test;

/**
 * @author cherrus
 *         created 11/21/14 at 3:12 PM
 */
public abstract class AbstractFlushingAppenderHelper<A extends AbstractAsyncAppender> {
	protected static final int BUFFER_SIZE = 16;
	protected static final String LAYOUT_PATTERN = "%m"; // only message
	protected A appender;

	protected abstract A createAppender() throws Exception;

	@Before
	public void setUp() throws Exception {
		this.appender = createAppender();
	}

	@Test
	public void appenderFlushesBufferedMessagesThenNextMessageExceedsBufferCapacity() throws Exception {
		final String firstMessage = stringWithLength( BUFFER_SIZE - 1 );

		processEntryWithMessage( firstMessage );
		assertOutput("");

		processEntryWithMessage( stringWithLength( 2 ) );
		assertOutput(firstMessage);
	}

	private static LogEntryItemImpl createEntry(final String message) throws Exception {
		LogEntryItemImpl entry = new LogEntryItemImpl(message.length(), new StringLoggingStrategy());
		entry.setLogLevel( LogLevel.INFO);
		entry.getBuffer().put(message.getBytes("ascii"));
		return entry;
	}

	private void processEntryWithMessage( String message ) throws Exception {
		appender.process( createEntry( message ));
	}

	private static String stringWithLength(final int length){
		final StringBuilder sb = new StringBuilder();
		for( int i = 0; i < length; i++ ) {
			sb.append( '*' );
		}
		return sb.toString();
	}

	protected abstract void assertOutput( String expected ) throws Exception;
}

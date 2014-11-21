package org.gflogger.appender;


import static org.junit.Assert.assertEquals;

/**
 * @author Artyom Korzun
 */
public class TestConsoleAppender extends AbstractFlushingAppenderHelper<ConsoleAppender> {

	private final StringBuilder stream = new StringBuilder();


	@Override
	protected ConsoleAppender createAppender() {
		ConsoleAppenderFactory appenderFactory = new ConsoleAppenderFactory();
		appenderFactory.setOutputStream(stream);
		appenderFactory.setLayoutPattern(LAYOUT_PATTERN);
		appenderFactory.setBufferSize(BUFFER_SIZE);
		return appenderFactory.createAppender(null);
	}

	@Override
	protected void assertOutput( final String expected ) {
		assertEquals(expected, stream.toString());
	}
}

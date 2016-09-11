package org.gflogger;

import org.gflogger.appender.ConsoleAppenderFactory;
import org.gflogger.slf4j.Slf4jLoggerImpl;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * @author Denis Gburg
 */
public abstract class TestSlf4jFormatLoggerServiceImpl extends AbstractTestLoggerService {
    private Map<String,String> messagePatterns;
    private Map<String,String> expectedOutput;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        messagePatterns = new HashMap<>();
        expectedOutput = new HashMap<>();

        messagePatterns.put("testAppendTruncatedMessageWithDigits","value is {}{}");
        expectedOutput.put("testAppendTruncatedMessageWithDigits","value is >");

        messagePatterns.put("testAppendFormattedWithWithLastMessage","say hello \\{ {} {");
        expectedOutput.put("testAppendFormattedWithWithLastMessage","say hello { world {");

        messagePatterns.put("testAppendFormattedWithLimitedAppenderBufferSize","say hello \\{ {} {");
        expectedOutput.put("testAppendFormattedWithLimitedAppenderBufferSize","say hello { world {");

        messagePatterns.put("testAppendFormattedWithLastMessage","say {} hello \\{ {} {");
        expectedOutput.put("testAppendFormattedWithLastMessage","say a hello { world {");

        messagePatterns.put("testAppendFormattedWithWrongPlaceholder","say hello {0} !");

        messagePatterns.put("testAppendFormattedWithNoMorePlaceholder","say hello {}");
        expectedOutput.put("testAppendFormattedWithNoMorePlaceholder","");

        messagePatterns.put("testAppendFormattedWithLessPlaceholdersThanRequired","say hello {} {}");
        expectedOutput.put("testAppendFormattedWithLessPlaceholdersThanRequired","");

        messagePatterns.put("testAppendFormattedWithArrayPlaceholder","value: {};");
        expectedOutput.put("testAppendFormattedWithArrayPlaceholder","value: [];value: [a];value: [b, a];value: [null, q, null];");

        messagePatterns.put("testAppendFormattedWithIterablePlaceholder","value: {};");
        expectedOutput.put("testAppendFormattedWithIterablePlaceholder","value: [];value: [a];value: [b, a];value: [null, q, null];");

        messagePatterns.put("testAppendObjectFormatter","say hello {} world");
        expectedOutput.put("testAppendObjectFormatter","say hello v:5 world");

        expectedOutput.put("testAppendFormattedWithAutoCommit", "");
    }

    @Override
    public Map<String, String> getExpectedOutput() {
        return expectedOutput;
    }

    @Override
    protected Map<String, String> getMessagePatterns() {
        return messagePatterns;
    }

    @Test
    public void testSlf4jLogger() throws Exception {
		final int maxMessageSize = 32;
		final ConsoleAppenderFactory factory = new ConsoleAppenderFactory();
		factory.setLayoutPattern("%m");
		factory.setMultibyte(false);
		final StringBuffer buffer = new StringBuffer();
		factory.setOutputStream(buffer);
		factory.setLogLevel(LogLevel.INFO);
		final LoggerService loggerService =
			createLoggerService(maxMessageSize,
				new GFLoggerBuilder("com.db", factory), factory);

		GFLogFactory.init(loggerService);

		final Logger slf4jLogger = new Slf4jLoggerImpl(GFLogFactory.getLog("com.db.fxpricing.Logger"));
		slf4jLogger.info("msg0 {}", "a");
		slf4jLogger.info("msg1 {} {}", "a", "c");
		slf4jLogger.info("msg2 {} {} {}", "a", 10, "c");
		slf4jLogger.info("msg3 {} {} {} {}", "a", 11, "b", "d");

		GFLogFactory.stop();

		assertEquals("msg0 amsg1 a cmsg2 a 10 cmsg3 a 11 b d", buffer.toString());
	}

    @Test
    public void testExceptionArgument() throws Exception {
        final int maxMessageSize = 10 << 10;
        final ConsoleAppenderFactory factory = new ConsoleAppenderFactory();
        factory.setLayoutPattern("%m");
        factory.setMultibyte(false);
        final StringBuffer buffer = new StringBuffer();
        factory.setOutputStream(buffer);
        factory.setLogLevel(LogLevel.INFO);
        final LoggerService loggerService =
                createLoggerService(maxMessageSize,
                        new GFLoggerBuilder("com.db", factory), factory);

        GFLogFactory.init(loggerService);

        final Logger slf4jLogger = new Slf4jLoggerImpl(GFLogFactory.getLog("com.db.fxpricing.Logger"));
        slf4jLogger.info("msg0 {}", "a", new RuntimeException());
        slf4jLogger.info("another", new IllegalArgumentException());

        GFLogFactory.stop();

        String str = buffer.toString();
        assertThat(str, containsString("msg0 ajava.lang.RuntimeException\n"
            + "\tat org.gflogger.TestSlf4jFormatLoggerServiceImpl.testExceptionArgument(TestSlf4jFormatLoggerServiceImpl.java:"));
        assertThat(str, containsString("anotherjava.lang.IllegalArgumentException\n"
            + "\tat org.gflogger.TestSlf4jFormatLoggerServiceImpl.testExceptionArgument(TestSlf4jFormatLoggerServiceImpl.java:"));
    }
}

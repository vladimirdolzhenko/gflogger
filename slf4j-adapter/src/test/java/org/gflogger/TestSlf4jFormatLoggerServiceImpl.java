package org.gflogger;

import org.junit.Before;

import java.util.HashMap;
import java.util.Map;

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
        expectedOutput.put("testAppendFormattedWithNoMorePlaceholder","say hello world");

        messagePatterns.put("testAppendFormattedWithLessPlaceholdersThanRequired","say hello {} {}");
        expectedOutput.put("testAppendFormattedWithLessPlaceholdersThanRequired","");

        messagePatterns.put("testAppendFormattedWithArrayPlaceholder","value: {};");
        expectedOutput.put("testAppendFormattedWithArrayPlaceholder","value: [];value: [a];value: [b, a];value: [null, q, null];");

        messagePatterns.put("testAppendFormattedWithIterablePlaceholder","value: {};");
        expectedOutput.put("testAppendFormattedWithIterablePlaceholder","value: [];value: [a];value: [b, a];value: [null, q, null];");

        messagePatterns.put("testAppendObjectFormatter","say hello {} world");
        expectedOutput.put("testAppendObjectFormatter","say hello v:5 world");
    }

    @Override
    public Map<String, String> getExpectedOutput() {
        return expectedOutput;
    }

    @Override
    protected Map<String, String> getMessagePatterns() {
        return messagePatterns;
    }
}

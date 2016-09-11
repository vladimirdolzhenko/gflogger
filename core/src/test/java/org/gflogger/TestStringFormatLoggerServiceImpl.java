package org.gflogger;

import java.util.HashMap;
import java.util.Map;

public abstract class TestStringFormatLoggerServiceImpl extends AbstractTestLoggerService {
    Map<String,String> messagePatterns;
    Map<String,String> expectedOutput;

    {
        messagePatterns = new HashMap<>();
        expectedOutput = new HashMap<>();

        messagePatterns.put("testAppendTruncatedMessageWithDigits","value is %s%s");
        expectedOutput.put("testAppendTruncatedMessageWithDigits","value is >");

        messagePatterns.put("testAppendFormattedWithWithLastMessage","say hello %% %s %");
        expectedOutput.put("testAppendFormattedWithWithLastMessage","say hello % world %");

        messagePatterns.put("testAppendFormattedWithLimitedAppenderBufferSize","say hello %% %s %");
        expectedOutput.put("testAppendFormattedWithLimitedAppenderBufferSize","say hello % world %");

        messagePatterns.put("testAppendFormattedWithLastMessage","say %s hello %% %s %");
        expectedOutput.put("testAppendFormattedWithLastMessage","say a hello % world %");

        messagePatterns.put("testAppendFormattedWithWrongPlaceholder","say hello %d !");

        messagePatterns.put("testAppendFormattedWithNoMorePlaceholder","say hello %s");
        expectedOutput.put("testAppendFormattedWithNoMorePlaceholder","say hello world");

        messagePatterns.put("testAppendFormattedWithLessPlaceholdersThanRequired","say hello %s %s");
        expectedOutput.put("testAppendFormattedWithLessPlaceholdersThanRequired","");

        messagePatterns.put("testAppendFormattedWithArrayPlaceholder","value: %s;");
        expectedOutput.put("testAppendFormattedWithArrayPlaceholder","value: [];value: [a];value: [b, a];value: [null, q, null];");

        messagePatterns.put("testAppendFormattedWithIterablePlaceholder","value: %s;");
        expectedOutput.put("testAppendFormattedWithIterablePlaceholder","value: [];value: [a];value: [b, a];value: [null, q, null];");

        messagePatterns.put("testAppendObjectFormatter","say hello %s world");
        expectedOutput.put("testAppendObjectFormatter","say hello v:5 world");

        expectedOutput.put("testAppendFormattedWithAutoCommit", "say hello world");
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

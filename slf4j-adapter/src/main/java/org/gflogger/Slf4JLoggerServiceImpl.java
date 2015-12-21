package org.gflogger;

import org.gflogger.appender.AppenderFactory;
import org.gflogger.base.LoggerServiceImpl;
import org.gflogger.formatting.Slf4jFormattingStrategy;

/**
 * @author Denis Gburg
 */
public class Slf4JLoggerServiceImpl extends LoggerServiceImpl {

    public Slf4JLoggerServiceImpl(
        int count,
        int maxMessageSize,
        GFLoggerBuilder[] loggerBuilders,
        AppenderFactory... appenderFactories
    ) {
        super(count, maxMessageSize, loggerBuilders, appenderFactories);
    }

    public Slf4JLoggerServiceImpl(
        int count,
        int maxMessageSize,
        ObjectFormatterFactory objectFormatterFactory,
        GFLoggerBuilder[] loggersBuilders,
        AppenderFactory... appenderFactories
    ) {
        super(count, maxMessageSize, objectFormatterFactory, loggersBuilders, appenderFactories);
    }

    @Override
    protected String name() {
        return "gflogger.slf4j";
    }

    @Override
    protected FormattingStrategy getFormattingStrategy() {
        return new Slf4jFormattingStrategy();
    }

    @Override
    public void entryFlushed(LocalLogEntry localEntry) {
        super.entryFlushed(localEntry);
    }
}

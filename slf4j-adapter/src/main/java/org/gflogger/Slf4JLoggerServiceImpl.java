package org.gflogger;

import org.gflogger.appender.AppenderFactory;
import org.gflogger.base.LoggerServiceImpl;
import org.gflogger.formatting.Slf4JLoggingStrategy;

/**
 * @author Denis Gburg
 */
public class Slf4JLoggerServiceImpl extends LoggerServiceImpl {

    public Slf4JLoggerServiceImpl(
        final int count,
        final int maxMessageSize,
        final GFLoggerBuilder[] loggerBuilders,
        final AppenderFactory... appenderFactories
    ) {
        super(count, maxMessageSize, loggerBuilders, appenderFactories);
    }

    public Slf4JLoggerServiceImpl(
        final int count,
        final int maxMessageSize,
        final ObjectFormatterFactory objectFormatterFactory,
        final GFLoggerBuilder[] loggersBuilders,
        AppenderFactory... appenderFactories
    ) {
        super(count, maxMessageSize, objectFormatterFactory, loggersBuilders, appenderFactories);
    }

    @Override
    protected String name() {
        return "gflogger.slf4j";
    }

    @Override
    protected LoggingStrategy getFormattingStrategy() {
        return new Slf4JLoggingStrategy();
    }

    @Override
    public void entryFlushed(LocalLogEntry localEntry) {
        super.entryFlushed(localEntry);
    }
}

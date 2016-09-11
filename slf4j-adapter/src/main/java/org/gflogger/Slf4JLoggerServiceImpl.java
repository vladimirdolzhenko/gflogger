package org.gflogger;

import org.gflogger.appender.AppenderFactory;
import org.gflogger.base.LoggerServiceImpl;
import org.gflogger.formatting.Slf4JFormattingStrategy;

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
    protected FormattingStrategy getFormattingStrategy() {
        return new Slf4JFormattingStrategy();
    }

    @Override
    public void entryFlushed(LocalLogEntry localEntry) {
        super.entryFlushed(localEntry);
    }
}

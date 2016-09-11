package org.gflogger;

import org.gflogger.appender.AppenderFactory;
import org.gflogger.disruptor.LoggerServiceImpl;
import org.gflogger.formatting.Slf4JFormattingStrategy;

/**
 * @author Denis Gburg
 */
public final class Slf4JDLoggerServiceImpl extends LoggerServiceImpl {

    public Slf4JDLoggerServiceImpl(
        int count,
        int maxMessageSize,
        GFLoggerBuilder[] loggerBuilders,
        AppenderFactory... appenderFactories
    ) {
        super(count, maxMessageSize, loggerBuilders, appenderFactories);
    }

    public Slf4JDLoggerServiceImpl(
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
        return new Slf4JFormattingStrategy();
    }

}

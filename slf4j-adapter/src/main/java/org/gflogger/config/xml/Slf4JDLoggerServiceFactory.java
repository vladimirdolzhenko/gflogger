package org.gflogger.config.xml;

import org.gflogger.GFLoggerBuilder;
import org.gflogger.LoggerService;
import org.gflogger.Slf4JDLoggerServiceImpl;
import org.gflogger.appender.AppenderFactory;

public class Slf4JDLoggerServiceFactory extends AbstractLoggerServiceFactory {
    @Override
    public LoggerService createService() {
        return new Slf4JDLoggerServiceImpl(
                count,
                maxMessageSize,
                objectFormatterFactory,
                loggersBuilders.toArray(new GFLoggerBuilder[loggersBuilders.size()]),
                appenderFactories.toArray(new AppenderFactory[appenderFactories.size()]));
    }
}

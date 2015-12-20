package org.gflogger.config.xml;

import org.gflogger.LoggerService;
import org.gflogger.log4j.Log4jLoggerServiceImpl;

public class Log4jServiceFactory extends AbstractLoggerServiceFactory{
    @Override
    public LoggerService createService() {
        return new Log4jLoggerServiceImpl();
    }
}

package org.gflogger.base;

import org.gflogger.AbstractTestLoggerService;
import org.gflogger.GFLogger;
import org.gflogger.LoggerService;
import org.gflogger.ObjectFormatterFactory;
import org.gflogger.appender.AppenderFactory;
import org.gflogger.base.DefaultLoggerServiceImpl;


/**
 * TestDefaultLoggerServiceImpl
 *
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public class TestDefaultLoggerServiceImpl extends AbstractTestLoggerService {

	@Override
	protected LoggerService createLoggerService(int maxMessageSize,
			ObjectFormatterFactory objectFormatterFactory,
			GFLogger[] loggers,
			AppenderFactory... factories) {
	    final LoggerService loggerService =
	    	new DefaultLoggerServiceImpl(4, maxMessageSize, objectFormatterFactory, loggers, factories);
	    return loggerService;
    }
}

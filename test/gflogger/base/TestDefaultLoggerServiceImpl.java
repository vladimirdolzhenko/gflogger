package gflogger.base;

import gflogger.AbstractTestLoggerService;
import gflogger.LoggerService;
import gflogger.ObjectFormatterFactory;
import gflogger.appender.AppenderFactory;

/**
 * TestDefaultLoggerServiceImpl
 *
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public class TestDefaultLoggerServiceImpl extends AbstractTestLoggerService {

	@Override
	protected LoggerService createLoggerService(int maxMessageSize,
			ObjectFormatterFactory objectFormatterFactory,
			AppenderFactory... factories) {
	    final LoggerService loggerService =
	    	new DefaultLoggerServiceImpl(4, maxMessageSize, objectFormatterFactory, factories);
	    return loggerService;
    }
}

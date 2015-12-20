package org.gflogger.base;

import org.gflogger.*;
import org.gflogger.appender.AppenderFactory;


/**
 * TestDefaultLoggerServiceImpl
 *
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public class TestDefaultStingFormatLoggerServiceImpl extends TestStringFormatLoggerServiceImpl {

	@Override
	protected LoggerService createLoggerService(int maxMessageSize,
			ObjectFormatterFactory objectFormatterFactory,
			GFLoggerBuilder[] loggers,
			AppenderFactory... factories) {
		final LoggerService loggerService =
			new LoggerServiceImpl(4, maxMessageSize, objectFormatterFactory, loggers, factories);
		return loggerService;
	}
}

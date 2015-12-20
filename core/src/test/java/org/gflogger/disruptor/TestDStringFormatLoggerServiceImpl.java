package org.gflogger.disruptor;

import org.gflogger.*;
import org.gflogger.appender.AppenderFactory;


/**
 * TestDefaultLoggerServiceImpl
 *
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public class TestDStringFormatLoggerServiceImpl extends TestStringFormatLoggerServiceImpl {

	@Override
	protected LoggerService createLoggerService(int maxMessageSize,
		ObjectFormatterFactory objectFormatterFactory,
		GFLoggerBuilder[] loggers,
		AppenderFactory... factories) {
		return new LoggerServiceImpl(4, maxMessageSize, objectFormatterFactory, loggers, factories);
	}

}

package org.gflogger.disruptor;

import org.gflogger.AbstractTestLoggerService;
import org.gflogger.GFLoggerBuilder;
import org.gflogger.LoggerService;
import org.gflogger.ObjectFormatterFactory;
import org.gflogger.appender.AppenderFactory;


/**
 * TestDefaultLoggerServiceImpl
 *
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public class TestDLoggerServiceImpl extends AbstractTestLoggerService {

	@Override
	protected LoggerService createLoggerService(int maxMessageSize,
		ObjectFormatterFactory objectFormatterFactory,
		GFLoggerBuilder[] loggers,
		AppenderFactory... factories) {
		return new LoggerServiceImpl(4, maxMessageSize, objectFormatterFactory, loggers, factories);
	}

}

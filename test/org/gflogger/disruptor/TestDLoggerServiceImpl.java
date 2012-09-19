package org.gflogger.disruptor;

import org.gflogger.AbstractTestLoggerService;
import org.gflogger.GFLogger;
import org.gflogger.LoggerService;
import org.gflogger.ObjectFormatterFactory;
import org.gflogger.appender.AppenderFactory;
import org.gflogger.disruptor.LoggerServiceImpl;


/**
 * TestDefaultLoggerServiceImpl
 *
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public class TestDLoggerServiceImpl extends AbstractTestLoggerService {

	@Override
	protected LoggerService createLoggerService(int maxMessageSize,
		ObjectFormatterFactory objectFormatterFactory,
		GFLogger[] loggers,
		AppenderFactory... factories) {
		return new LoggerServiceImpl(4, maxMessageSize, objectFormatterFactory, loggers, factories);
	}

}

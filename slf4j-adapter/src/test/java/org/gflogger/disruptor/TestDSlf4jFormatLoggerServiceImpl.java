package org.gflogger.disruptor;

import org.gflogger.*;
import org.gflogger.appender.AppenderFactory;


/**
 * TestDefaultLoggerServiceImpl
 *
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public class TestDSlf4jFormatLoggerServiceImpl extends TestSlf4jFormatLoggerServiceImpl {

	@Override
	protected LoggerService createLoggerService(int maxMessageSize,
		ObjectFormatterFactory objectFormatterFactory,
		GFLoggerBuilder[] loggers,
		AppenderFactory... factories) {
		return new Slf4JDLoggerServiceImpl(4, maxMessageSize, objectFormatterFactory, loggers, factories);
	}

}

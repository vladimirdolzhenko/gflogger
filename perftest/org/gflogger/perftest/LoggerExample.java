package org.gflogger.perftest;

import static org.gflogger.helpers.OptionConverter.getIntProperty;
import static org.gflogger.helpers.OptionConverter.getStringProperty;

import org.gflogger.GFLoggerBuilder;
import org.gflogger.LogLevel;
import org.gflogger.LoggerService;
import org.gflogger.appender.AppenderFactory;
import org.gflogger.base.LoggerServiceImpl;




/**
 * LoggerExample
 *
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public class LoggerExample extends AbstractLoggerExample {

	@Override
	protected LoggerService createLoggerImpl() {
		final AppenderFactory[] factories = createAppenderFactories();
		final GFLoggerBuilder[] loggers =
				new GFLoggerBuilder[]{ new GFLoggerBuilder(LogLevel.INFO, "com.db", factories)};
		final int count = getIntProperty("gflogger.service.count", 1 << 10);
		final LoggerService impl = new LoggerServiceImpl(
				count,
				getIntProperty("gflogger.service.maxMessageSize", 1 << 8),
				loggers,
				factories);
		return impl;
	}

	@Override
	protected String fileAppenderFileName() {
		return getStringProperty("gflogger.filename", "./logs/gflogger.log");
	}

	public static void main(final String[] args) throws Throwable {
		if (args.length > 2) System.in.read();
		final LoggerExample loggerExample = new LoggerExample();

		loggerExample.parseArgs(args);
		loggerExample.runTest();
	}
}

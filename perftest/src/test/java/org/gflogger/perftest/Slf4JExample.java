package org.gflogger.perftest;

import org.gflogger.GFLoggerBuilder;
import org.gflogger.LogLevel;
import org.gflogger.LoggerService;
import org.gflogger.Slf4JLoggerServiceImpl;
import org.gflogger.appender.AppenderFactory;

import static org.gflogger.helpers.OptionConverter.getIntProperty;
import static org.gflogger.helpers.OptionConverter.getStringProperty;

/**
 * Log4JExample
 *
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public class Slf4JExample extends AbstractLoggerExample {

	@Override
	protected LoggerService createLoggerImpl() {
		final AppenderFactory[] factories = createAppenderFactories();
		final GFLoggerBuilder[] loggers =
				new GFLoggerBuilder[]{ new GFLoggerBuilder(LogLevel.INFO, "com.db", factories)};
		final int count = getIntProperty("org.gflogger.service.count", 1 << 10);
		final LoggerService impl = new Slf4JLoggerServiceImpl(
				count,
				getIntProperty("org.gflogger.service.maxMessageSize", 1 << 8),
				loggers,
				factories);
		return impl;
	}

	@Override
	protected String fileAppenderFileName() {
		return getStringProperty("org.gflogger.filename", "./logs/dgflogger.log");
	}

	public static void main(final String[] args) throws Throwable {
		if (args.length > 2) System.in.read();
		final Slf4JExample loggerExample = new Slf4JExample();

		loggerExample.parseArgs(args);
		loggerExample.runTest();
	}
}

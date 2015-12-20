package org.gflogger.perftest;

import org.gflogger.*;
import org.gflogger.appender.AppenderFactory;

import static org.gflogger.helpers.OptionConverter.getIntProperty;
import static org.gflogger.helpers.OptionConverter.getStringProperty;

/**
 * Log4JExample
 *
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public class Slf4JDExample extends AbstractLoggerExample {

	@Override
	protected LoggerService createLoggerImpl() {
		final AppenderFactory[] factories = createAppenderFactories();
		final GFLoggerBuilder[] loggers =
				new GFLoggerBuilder[]{ new GFLoggerBuilder(LogLevel.INFO, "com.db", factories)};
		final int count = getIntProperty("org.gflogger.service.count", 1 << 10);
		final LoggerService impl = new Slf4JDLoggerServiceImpl(
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
		final Slf4JDExample loggerExample = new Slf4JDExample();

		loggerExample.parseArgs(args);
		loggerExample.runTest();
	}
}

package org.gflogger.perftest;

import static org.gflogger.helpers.OptionConverter.*;

import org.gflogger.GFLogger;
import org.gflogger.GFLoggerImpl;
import org.gflogger.LogLevel;
import org.gflogger.LoggerService;
import org.gflogger.appender.AppenderFactory;
import org.gflogger.disruptor.DLoggerServiceImpl;



/**
 * LoggerExample
 *
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public class DLoggerExample extends AbstractLoggerExample {

	@Override
	protected LoggerService createLoggerImpl() {
		final AppenderFactory[] factories = createAppenderFactories();
		final GFLogger[] loggers =
				new GFLogger[]{ new GFLoggerImpl(LogLevel.INFO, "com.db", factories)};
		final LoggerService impl = new DLoggerServiceImpl(
			getIntProperty("gflogger.service.count", 1 << 10),
			getIntProperty("gflogger.service.maxMessageSize", 1 << 8),
			loggers,
			factories);
		return impl;
	}

	@Override
	protected String fileAppenderFileName() {
		return getStringProperty("gflogger.filename", "./logs/dgflogger.log");
	}

	public static void main(final String[] args) throws Throwable {
		if (args.length > 2) System.in.read();
		final DLoggerExample dLoggerExample = new DLoggerExample();

		dLoggerExample.parseArgs(args);

		dLoggerExample.runTest();
	}
}

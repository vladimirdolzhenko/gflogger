package org.gflogger.perftest;

import static org.gflogger.helpers.OptionConverter.*;

import org.gflogger.GFLogger;
import org.gflogger.GFLoggerImpl;
import org.gflogger.LogLevel;
import org.gflogger.LoggerService;
import org.gflogger.appender.AppenderFactory;
import org.gflogger.base.DefaultLoggerServiceImpl;




/**
 * LoggerExample
 *
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public class LoggerExample extends AbstractLoggerExample {

	@Override
	protected LoggerService createLoggerImpl() {
		final AppenderFactory[] factories = createAppenderFactories();
		final GFLogger[] loggers =
				new GFLogger[]{ new GFLoggerImpl(LogLevel.INFO, "com.db", factories)};
		final LoggerService impl =
			new DefaultLoggerServiceImpl(
				getIntProperty("gflogger.service.count", 1 << 10),
				getIntProperty("gflogger.service.maxMessageSize", 1 << 8),
				loggers,
				factories);
		return impl;
	}

	@Override
	protected String fileAppenderFileName() {
		return getStringProperty("gflogger.filename", "./logs/gflogger.log");
	}

	/*/
	@Override
	protected void logFinalMessage(int count, long t, long e) {
		final DefaultLoggerServiceImpl impl2 = (DefaultLoggerServiceImpl) service;
//		logMessage("__ park:" + impl2.park.get().get(), 0);
//		logMessage("__ miss:" + impl2.miss.get().get(), 0);
//		logMessage("__ acq:" + ((impl2.acq.get().get() / 1000) / 1e3) + " ms", 0);
		logMessage("__ commit:" + ((impl2.commit.get().get() / 1000) / 1e3) + " ms", 0);
		System.out.println("__ commit:" + ((impl2.commit.get().get() / 1000) / 1e3) + " ms / " + impl2.commitbytes.get().get() + " bytes");
		super.logFinalMessage(count, t, e);
	}
	/*/
	//*/

	public static void main(final String[] args) throws Throwable {
		final LoggerExample loggerExample = new LoggerExample();
		loggerExample.parseArgs(args);

		loggerExample.runTest();
	}
}

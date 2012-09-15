package org.gflogger.perftest;

import static org.gflogger.helpers.OptionConverter.*;


import org.apache.commons.logging.Log;
import org.gflogger.GFLogFactory;
import org.gflogger.GFLogger;
import org.gflogger.GFLoggerImpl;
import org.gflogger.LogLevel;
import org.gflogger.LoggerService;
import org.gflogger.PatternLayout;
import org.gflogger.appender.AppenderFactory;
import org.gflogger.appender.ConsoleAppenderFactory;
import org.gflogger.appender.FileAppenderFactory;
import org.gflogger.base.DefaultLoggerServiceImpl;

/**
 *
 * -Dorg.apache.commons.logging.Log=gflogger.jcl.LogImpl
 *
 * JCLGFLoggerExample
 *
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public class JCLGFLoggerExample extends AbstractExample {

	protected LoggerService service;
	private Log log;

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

	protected String fileAppenderFileName() {
		return getStringProperty("gflogger.filename", "./logs/jcl-gflogger.log");
	}

	@Override
	protected void initLogger() {
		service = createLoggerImpl();

		GFLogFactory.init(service);

		log = org.apache.commons.logging.LogFactory.getLog("com.db.fxpricing.Logger");
	}

	protected AppenderFactory[] createAppenderFactories(){
		final FileAppenderFactory fileAppender = new FileAppenderFactory();
		fileAppender.setLogLevel(LogLevel.INFO);
		fileAppender.setFileName(fileAppenderFileName());
		fileAppender.setAppend(false);
		fileAppender.setImmediateFlush(false);
		fileAppender.setLayout(new PatternLayout("%d{HH:mm:ss,SSS zzz} %p %m [%c{2}] [%t]%n"));

		final ConsoleAppenderFactory consoleAppender = new ConsoleAppenderFactory();
		consoleAppender.setLogLevel(LogLevel.INFO);
		consoleAppender.setLayout(new PatternLayout("%d{HH:mm:ss,SSS zzz} %p %m [%c{2}] [%t]%n"));

		return new AppenderFactory[]{fileAppender};
	}

	@Override
	protected void stop() {
		GFLogFactory.stop();
	}

	@Override
	protected void logDebugTestMessage(int i) {
		log.debug("test" + i);
	}

	@Override
	protected void logMessage(String msg, int j) {
		log.info(msg + j);
	}

	@Override
	protected void logFinalMessage(final int count, final long t, final long e) {
		log.info("final count: " + count + " time: " + ((e - t) / 1000 / 1e3) + " ms");
		System.out.println("final count: " + count + " time: " + ((int)((e-t)/1000)) / 1e3);
	}

	@Override
	protected void logTotalMessage(final long start) {
		log.info("total time:"+ (System.currentTimeMillis() - start) + " ms.");
	}

	public static void main(String[] args) throws Throwable {
		final JCLGFLoggerExample example = new JCLGFLoggerExample();
		example.parseArgs(args);

		example.runTest();
	}

}

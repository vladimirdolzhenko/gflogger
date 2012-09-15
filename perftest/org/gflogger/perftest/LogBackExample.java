package org.gflogger.perftest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;


/**
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public class LogBackExample extends AbstractExample {

	private Logger log;

	@Override
	protected void initLogger() {
		log = LoggerFactory.getLogger("com.db.fxpricing.Logger");
	}

	@Override
	protected void stop() {
		LoggerContext ctx = ((LoggerContext)LoggerFactory.getILoggerFactory());
		ctx.stop();
	}

	@Override
	protected void logDebugTestMessage(int i) {
		log.debug("test{}", i);
	}

	@Override
	protected void logMessage(String msg, int j) {
		log.info("{}{}", msg, j);
	}

	@Override
	protected void logFinalMessage(final int count, final long t, final long e) {
		log.info("final count: {} time: {} ms", count, ((e - t) / 1000 / 1e3));
		System.out.println("final count: " + count + " time: " + ((int)((e-t)/1000)) / 1e3);
	}

	@Override
	protected void logTotalMessage(final long start) {
		log.info("total time:{} ms",System.currentTimeMillis() - start);
	}


	public static void main(String[] args) throws Throwable {
		final LogBackExample example = new LogBackExample();
		example.parseArgs(args);

		example.runTest();
	}


}

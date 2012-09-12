package perftest;

import static gflogger.helpers.OptionConverter.*;

import gflogger.LoggerService;
import gflogger.disruptor.DLoggerServiceImpl;

/**
 * LoggerExample
 *
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public class DLoggerExample extends AbstractLoggerExample {

    @Override
    protected LoggerService createLoggerImpl() {
        final LoggerService impl = new DLoggerServiceImpl(
        	getIntProperty("gflogger.service.count", 1 << 10),
        	getIntProperty("gflogger.service.maxMessageSize", 1 << 8),
        	createAppenderFactories());
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

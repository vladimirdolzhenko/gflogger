package perftest;

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
        final LoggerService impl = new DLoggerServiceImpl(1 << 10, 1 << 8, createAppenderFactories());
        return impl;
    }
    
	@Override
	protected String fileAppenderFileName() {
		return "./logs/dgflogger.log";
	}
    
    public static void main(final String[] args) throws Throwable {
        final DLoggerExample dLoggerExample = new DLoggerExample();
        
        dLoggerExample.parseArgs(args);
        
        dLoggerExample.runTest();
    }
}

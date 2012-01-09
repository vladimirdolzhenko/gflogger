package perftest;

import gflogger.LoggerService;
import gflogger.base.DefaultLoggerServiceImpl;


/**
 * LoggerExample
 * 
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public class LoggerExample extends AbstractLoggerExample {
    
    @Override
    protected LoggerService createLoggerImpl() {
        final LoggerService impl = 
        	new DefaultLoggerServiceImpl(1 << 10, 1 << 8, createAppenderFactories());
        return impl;
    }
    
	@Override
	protected String fileAppenderFileName() {
		return "./logs/gflogger.log";
	}
    
    /*/
    @Override
    protected void logFinalMessage(long t, long e) {
        final DefaultLoggerServiceImpl impl2 = (DefaultLoggerServiceImpl) service;
        logMessage("__ park:" + impl2.park.get().get(), 0);
        logMessage("__ miss:" + impl2.miss.get().get(), 0);
        logMessage("__ acq:" + ((impl2.acq.get().get() / 1000) / 1e3) + " ms", 0);
        logMessage("__ commit:" + ((impl2.commit.get().get() / 1000) / 1e3) + " ms", 0);
        super.logFinalMessage(t, e);
    }
    /*/
    //*/

    public static void main(final String[] args) throws Throwable {
        final LoggerExample loggerExample = new LoggerExample();
        loggerExample.parseArgs(args);
        
        loggerExample.runTest();
    }
}

package perftest;

import gflogger.LoggerService;
import gflogger.log4j.Log4jLoggerServiceImpl;

/**
 * Log4JLoggerFacadeExample
 * 
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public class Log4JLoggerFacadeExample extends AbstractLoggerExample {
    
    @Override
    protected LoggerService createLoggerImpl() {
        final LoggerService impl = new Log4jLoggerServiceImpl();
        return impl;
    }
    
    @Override
    protected String fileAppenderFileName() {
        return null;
    }
    
    public static void main(final String[] args) throws Throwable {
        final Log4JLoggerFacadeExample loggerExample = new Log4JLoggerFacadeExample();
        loggerExample.parseArgs(args);
        
        loggerExample.runTest();
    }
}

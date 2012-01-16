package perftest;

import gflogger.LogFactory;
import gflogger.LogLevel;
import gflogger.LoggerService;
import gflogger.PatternLayout;
import gflogger.appender.AppenderFactory;
import gflogger.appender.ConsoleAppenderFactory;
import gflogger.appender.FileAppenderFactory;
import gflogger.base.DefaultLoggerServiceImpl;

import java.util.Collections;

import org.apache.commons.logging.Log;

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
        final LoggerService impl = 
        	new DefaultLoggerServiceImpl(1 << 10, 1 << 8, createAppenderFactories());
        return impl;
    }
    
	protected String fileAppenderFileName() {
		return "./logs/jcl-gflogger.log";
	}

    @Override
    protected void initLogger() {
        service = createLoggerImpl();

        LogFactory.init(Collections.singletonMap("com.db", service));

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
        LogFactory.stop();
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
    protected void logFinalMessage(final long t, final long e) {
        log.info("final: " + ((e - t) / 1000 / 1e3));
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

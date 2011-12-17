package perftest;

import gflogger.LogLevel;
import gflogger.LoggerService;
import gflogger.PatternLayout;
import gflogger.base.DefaultLoggerServiceImpl;
import gflogger.disruptor.DLoggerServiceImpl;
import gflogger.disruptor.appender.ConsoleAppender;
import gflogger.disruptor.appender.FileAppender;


/**
 * LoggerExample
 * 
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public class DLoggerExample extends AbstractLoggerExample {
    
    @Override
    protected LoggerService createLoggerImpl() {
        final FileAppender fileAppender = new FileAppender();
        fileAppender.setLogLevel(LogLevel.INFO);
        fileAppender.setFileName("./logs/dgflogger.log");
        fileAppender.setAppend(false);
        fileAppender.setImmediateFlush(false);
        fileAppender.setLayout(new PatternLayout("%d{HH:mm:ss,SSS zzz} %p %m [%c{2}] [%t]%n"));

        final ConsoleAppender consoleAppender = new ConsoleAppender();
        consoleAppender.setLogLevel(LogLevel.INFO);
        consoleAppender.setLayout(new PatternLayout("%d{HH:mm:ss,SSS zzz} %p %m [%c{2}] [%t]%n"));

        //final LoggerImpl impl = new LoggerImpl(1 << 10, 1 << 8, fileAppender);
        //final LoggerImpl impl = new LoggerImpl(1 << 2, 1 << 8, fileAppender, consoleAppender);
        final LoggerService impl = new DLoggerServiceImpl(1 << 10, 1 << 8, 
                 fileAppender
                //, consoleAppender
        );
        return impl;
    }
    
    @Override
    protected void logFinalMessage(long t, long e) {
        final DLoggerServiceImpl impl2 = (DLoggerServiceImpl) service;
//        logMessage("__ park:" + impl2.park.get().get(), 0);
//        logMessage("__ miss:" + impl2.miss.get().get(), 0);
        logMessage("__ acq:" + ((impl2.acq.get().get() / 1000) / 1e3) + " ms", 0);
        logMessage("__ commit:" + ((impl2.commit.get().get() / 1000) / 1e3) + " ms", 0);
        super.logFinalMessage(t, e);
    }

    public static void main(final String[] args) throws Throwable {
        final DLoggerExample dLoggerExample = new DLoggerExample();
        
        dLoggerExample.parseArgs(args);
        
        dLoggerExample.runTest();
    }
}

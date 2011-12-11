package perftest;

import gflogger.LogLevel;
import gflogger.LoggerService;
import gflogger.PatternLayout;
import gflogger.base.DefaultLoggerServiceImpl;
import gflogger.base.appender.ConsoleAppender;
import gflogger.base.appender.FileAppender;


/**
 * LoggerExample
 * 
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public class LoggerExample extends AbstractLoggerExample {
    
    @Override
    protected LoggerService createLoggerImpl() {
        final FileAppender fileAppender = new FileAppender();
        fileAppender.setLogLevel(LogLevel.INFO);
        fileAppender.setFileName("./logs/gflogger.log");
        fileAppender.setAppend(false);
        fileAppender.setImmediateFlush(false);
        fileAppender.setLayout(new PatternLayout("%d{HH:mm:ss,SSS zzz} %p %m [%c{2}] [%t]%n"));

        final ConsoleAppender consoleAppender = new ConsoleAppender();
        consoleAppender.setLogLevel(LogLevel.INFO);
        consoleAppender.setLayout(new PatternLayout("%d{HH:mm:ss,SSS zzz} %p %m [%c{2}] [%t]%n"));

        //final LoggerImpl impl = new LoggerImpl(1 << 10, 1 << 8, fileAppender);
        //final LoggerImpl impl = new LoggerImpl(1 << 2, 1 << 8, fileAppender, consoleAppender);
        final LoggerService impl = new DefaultLoggerServiceImpl(1 << 10, 1 << 8, fileAppender
        //final LoggerImpl impl = new DefaultLoggerImpl(8, 1 << 8, fileAppender
                //, consoleAppender
        );
        return impl;
    }
    
    @Override
    protected void logFinalMessage(long t, long e) {
        final DefaultLoggerServiceImpl impl2 = (DefaultLoggerServiceImpl) impl;
        logMessage("__ park:" + impl2.park.get().get(), 0);
        logMessage("__ miss:" + impl2.miss.get().get(), 0);
        logMessage("__ acq:" + ((impl2.acq.get().get() / 1000) / 1e3) + " ms", 0);
        logMessage("__ commit:" + ((impl2.commit.get().get() / 1000) / 1e3) + " ms", 0);
        super.logFinalMessage(t, e);
    }

    public static void main(final String[] args) throws Throwable {
        final LoggerExample loggerExample = new LoggerExample();
        loggerExample.parseArgs(args);
        
        loggerExample.runTest();
    }
}

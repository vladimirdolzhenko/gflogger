package perftest;

import gflogger.LogLevel;
import gflogger.LoggerImpl;
import gflogger.PatternLayout;
import gflogger.disruptor.DLoggerImpl;
import gflogger.disruptor.appender.ConsoleAppender;
import gflogger.disruptor.appender.FileAppender;


/**
 * LoggerExample
 * 
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public class DLoggerExample extends AbstractLoggerExample {
    
    @Override
    protected LoggerImpl createLoggerImpl() {
        final FileAppender fileAppender = new FileAppender();
        fileAppender.setLogLevel(LogLevel.INFO);
        fileAppender.setFileName("./logs/dgflogger.log");
        fileAppender.setAppend(false);
        fileAppender.setBufferedIO(false);
        fileAppender.setLayout(new PatternLayout("%d{HH:mm:ss,SSS zzz} %p %m [%c{2}] [%t]%n"));

        final ConsoleAppender consoleAppender = new ConsoleAppender();
        consoleAppender.setLogLevel(LogLevel.INFO);
        consoleAppender.setLayout(new PatternLayout("%d{HH:mm:ss,SSS zzz} %p %m [%c{2}] [%t]%n"));

        //final LoggerImpl impl = new LoggerImpl(1 << 10, 1 << 8, fileAppender);
        //final LoggerImpl impl = new LoggerImpl(1 << 2, 1 << 8, fileAppender, consoleAppender);
        final LoggerImpl impl = new DLoggerImpl(1 << 10, 1 << 8, 
                 fileAppender
                //, consoleAppender
        );
        return impl;
    }

    public static void main(final String[] args) throws Throwable {
        final DLoggerExample dLoggerExample = new DLoggerExample();
        
        dLoggerExample.parseArgs(args);
        
        dLoggerExample.runTest();
    }
}

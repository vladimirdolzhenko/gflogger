package perftest;

import gflogger.DefaultLoggerImpl;
import gflogger.LogLevel;
import gflogger.LoggerImpl;
import gflogger.PatternLayout;
import gflogger.appender.ConsoleAppender;
import gflogger.appender.FileAppender;


/**
 * LoggerExample
 * 
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public class LoggerExample extends AbstractLoggerExample {
    
    @Override
    protected LoggerImpl createLoggerImpl() {
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
        final LoggerImpl impl = new DefaultLoggerImpl(1 << 10, 1 << 8, fileAppender
                //, consoleAppender
        );
        return impl;
    }

    public static void main(final String[] args) throws Throwable {
        final LoggerExample loggerExample = new LoggerExample();
        loggerExample.parseArgs(args);
        
        loggerExample.runTest();
    }
}

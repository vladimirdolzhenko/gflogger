package gflogger;

/**
 * LoggerImpl
 * 
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public interface LoggerImpl {
    
    LogLevel getLevel();

    LogEntry log(final LogLevel level, final String name, final String className);
    
    void stop();
}

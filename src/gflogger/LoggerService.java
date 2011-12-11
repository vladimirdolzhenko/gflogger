package gflogger;

/**
 * LoggerService
 * 
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public interface LoggerService {
	
	LogLevel getLevel();

	LogEntry log(final LogLevel level, final String name, final String className);
	
	void entryFlushed(final LocalLogEntry localEntry);
	
	void stop();
}

package gflogger;

/**
 * Logger
 * 
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public interface Logger {

	boolean isDebugEnabled();

	LogEntry debug();

	boolean isInfoEnabled();

	LogEntry info();

	boolean isErrorEnabled();

	LogEntry error();

}

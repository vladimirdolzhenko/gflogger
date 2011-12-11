package gflogger;

import java.nio.CharBuffer;

/**
 * LogEntryItem
 * 
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public interface LogEntryItem {

	static final long startTime = System.currentTimeMillis();

	LogLevel getLogLevel();
	
	String getName();

	String getThreadName();

	String getClassName();

	long getTimestamp();

	CharBuffer getBuffer();
}

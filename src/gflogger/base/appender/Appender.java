package gflogger.base.appender;

import gflogger.LogEntryItem;
import gflogger.LogLevel;
import gflogger.base.RingBuffer;

public interface Appender<T extends LogEntryItem> {

	LogLevel getLogLevel();
	
	void start(final RingBuffer<T> ringBuffer);

	void entryFlushed(T entryItem);
	
	long getMaxReleased();

	void stop();
}

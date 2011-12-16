package gflogger.base;

import gflogger.LogEntryItem;
import gflogger.LogLevel;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;

/**
 * LogEntryItemImpl
 * 
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public final class LogEntryItemImpl implements LogEntryItem {

	private final CharBuffer buffer;
	
	private String categoryName;
	private LogLevel logLevel;
	private long timestamp;
	private String threadName;

	private volatile long id;

	public LogEntryItemImpl(final int sizeInBytes) {
		this(ByteBuffer.allocateDirect(sizeInBytes).asCharBuffer());
	}

	public LogEntryItemImpl(final CharBuffer buffer) {
		this.buffer = buffer;
	}
	
	public void setId(long id) {
		this.id = id;
	}
	
	public long getId() {
		return this.id;
	}

	@Override
	public LogLevel getLogLevel() {
		return logLevel;
	}

	public void setLogLevel(final LogLevel logLevel) {
		this.logLevel = logLevel;
	}

	@Override
	public String getCategoryName() {
		return categoryName;
	}

	@Override
	public String getThreadName() {
		return threadName;
	}

	@Override
	public long getTimestamp() {
		return timestamp;
	}

	@Override
	public CharBuffer getBuffer() {
		return buffer;
	}

	public void setCategoryName(String name) {
		this.categoryName = name;
	}
	
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	
	public void setThreadName(String threadName) {
		this.threadName = threadName;
	}

	@Override
	public String toString() {
		return "[" + 
		id + " , " + logLevel
		+ " pos:" + buffer.position() + " limit:" + buffer.limit() + " capacity:" + buffer.capacity() + "]";
	}

}

package gflogger.disruptor;

import gflogger.LogEntryItem;
import gflogger.LogLevel;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;

/**
 * LogEntryItem
 * 
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public final class DLogEntryItem implements LogEntryItem {

	private final CharBuffer buffer;
	
	private String categoryName;
	private LogLevel logLevel;
	private long timestamp;
	private String threadName;

	private long sequenceId;

	public DLogEntryItem(final int size) {
		this(ByteBuffer.allocateDirect(size).asCharBuffer());
	}

	public DLogEntryItem(final CharBuffer buffer) {
		this.buffer = buffer;
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

	public void setSequenceId(long sequenceId) {
		this.sequenceId = sequenceId;
	}
	
	public long getSequenceId() {
		return this.sequenceId;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	
	public void setCategoryName(String name) {
		this.categoryName = name;
	}
	
	public void setThreadName(String threadName) {
		this.threadName = threadName;
	}
	
	@Override
	public String toString() {
		return "[" 
			+ " pos:" + buffer.position() 
			+ " limit:" + buffer.limit() 
			+ " capacity:" + buffer.capacity() + "]";
	}

}

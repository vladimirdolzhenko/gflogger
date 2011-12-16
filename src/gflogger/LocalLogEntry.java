package gflogger;

import gflogger.formatter.BufferFormatter;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;


/**
 * LocalLogEntry
 * 
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public final class LocalLogEntry implements LogEntry {

	private final String threadName;
	private final CharBuffer buffer;
	private final LoggerService loggerService;

	private String categoryName;
	private LogLevel logLevel;
	
	public LocalLogEntry(final Thread owner, final int size, final LoggerService loggerService) {
		this(owner, ByteBuffer.allocateDirect(size).asCharBuffer(), loggerService);
	}

	public LocalLogEntry(final Thread owner, final CharBuffer buffer, final LoggerService loggerService) {
		/*
		 * It worth to cache thread categoryName at thread local variable cause
		 * thread.getName() creates new String(char[])
		 */
		this.threadName = owner.getName();
		this.buffer = buffer;
		this.loggerService = loggerService;
	}
	
	public LogLevel getLogLevel() {
		return logLevel;
	}

	public void setLogLevel(final LogLevel logLevel) {
		this.logLevel = logLevel;
	}

	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}
	
	public String getCategoryName() {
		return categoryName;
	}

	public String getThreadName() {
		return threadName;
	}

	public CharBuffer getBuffer() {
		return buffer;
	}


	@Override
	public LocalLogEntry append(final char c) {
		buffer.append(c);
		return this;
	}

	@Override
	public LocalLogEntry append(final CharSequence csq) {
		BufferFormatter.append(buffer, csq);
		return this;
	}

	@Override
	public LocalLogEntry append(final CharSequence csq, final int start, final int end) {
		BufferFormatter.append(buffer, csq, start, end);
		return this;
	}

	@Override
	public LogEntry append(final boolean b) {
		BufferFormatter.append(buffer, b);
		return this;
	}

	@Override
	public LogEntry append(final byte i) {
		BufferFormatter.append(buffer, i);
		return this;
	}

	@Override
	public LogEntry append(final short i) {
		BufferFormatter.append(buffer, i);
		return this;
	}

	@Override
	public LocalLogEntry append(final int i){
		BufferFormatter.append(buffer, i);
		return this;
	}

	@Override
	public LogEntry append(final long i) {
		BufferFormatter.append(buffer, i);
		return this;
	}

	@Override
	public LogEntry append(final double i, final int precision) {
		BufferFormatter.append(buffer, i, precision);
		return this;
	}
	
	@Override
	public LogEntry append(Object o) {
		if (o != null){
			buffer.append(o.toString());
		} else {
			buffer.put('n').put('u').put('l').put('l');
		}
		return this;
	}

	@Override
	public void commit() {
		buffer.flip();
		loggerService.entryFlushed(this);
	}

	@Override
	public String toString() {
		return "[local of " + threadName + 
			" " + logLevel + 
			" pos:" + buffer.position() + 
			" limit:" + buffer.limit() + 
			" capacity:" + buffer.capacity() + 
			"]";
	}

}

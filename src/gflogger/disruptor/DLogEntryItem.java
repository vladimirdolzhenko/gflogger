package gflogger.disruptor;

import gflogger.LogEntry;
import gflogger.LogLevel;
import gflogger.formatter.BufferFormatter;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;

/**
 * LogEntryItem
 * 
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public final class DLogEntryItem implements LogEntry {

    private final CharBuffer buffer;
    
    private final DLoggerImpl loggerImpl;

    private String name;
    private LogLevel logLevel;
    private long timestamp;
    private String threadName;
    private String className;

    private long sequenceId;

    public DLogEntryItem(final int size, final DLoggerImpl loggerImpl) {
        this(ByteBuffer.allocateDirect(size).asCharBuffer(), loggerImpl);
    }

    public DLogEntryItem(final CharBuffer buffer, final DLoggerImpl loggerImpl) {
        this.buffer = buffer;
        this.loggerImpl = loggerImpl;
    }
    
    @Override
    public LogLevel getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(final LogLevel logLevel) {
        this.logLevel = logLevel;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getThreadName() {
        return threadName;
    }

    @Override
    public String getClassName() {
        return className;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public CharBuffer getBuffer() {
        return buffer;
    }


    @Override
    public DLogEntryItem append(final char c) {
        buffer.append(c);
        return this;
    }

    @Override
    public DLogEntryItem append(final CharSequence csq) {
        BufferFormatter.append(buffer, csq);
        return this;
    }

    @Override
    public DLogEntryItem append(final CharSequence csq, final int start, final int end) {
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
    public DLogEntryItem append(final int i){
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
    
    public void setSequenceId(long sequenceId) {
        this.sequenceId = sequenceId;
    }
    
    public long getSequenceId() {
        return this.sequenceId;
    }

    @Override
    public void commit() {
        buffer.flip();
        loggerImpl.entryFlushed(this);
    }


    void acquire(final String name, final String className) {
        this.name = name;
        this.className = className;
        this.timestamp = System.currentTimeMillis();
        this.buffer.clear();
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

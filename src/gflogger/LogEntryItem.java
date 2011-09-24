package gflogger;

import gflogger.formatter.BufferFormatter;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * LogEntryItem
 * 
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public final class LogEntryItem implements LogEntry {

    // inter thread fence
    private final Object lock = new Object();
    private final CharBuffer buffer;
    
    // inter thread marker
    private final AtomicInteger counter;
    
    private final DefaultLoggerImpl loggerImpl;

    private String name;
    private LogLevel logLevel;
    private long timestamp;
    private String threadName;
    private String className;

    private LogEntryItem next;

    public LogEntryItem(final int size, final DefaultLoggerImpl loggerImpl) {
        this(ByteBuffer.allocateDirect(size).asCharBuffer(), loggerImpl);
    }

    public LogEntryItem(final CharBuffer buffer, final DefaultLoggerImpl loggerImpl) {
        this.buffer = buffer;
        this.loggerImpl = loggerImpl;
        this.counter = new AtomicInteger();
    }
    
    public void setNext(LogEntryItem next) {
        this.next = next;
    }

    public LogEntryItem getNext() {
        return next;
    }
    
    public boolean testCounterBit(int bit){
        return (counter.get() & (1 << bit)) != 0;
    }
    
    public int resetCounterBit(int bit){
        int mask = ~(1 << bit);
        // similar to getAndIncrement
        for(;;){
            final int value = counter.get();
            final int newValue = value & mask;
            if (counter.compareAndSet(value, newValue)) {
                return newValue;
            }
        }
    }

    public int getCounter() {
        return counter.get();
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
    public LogEntryItem append(final char c) {
        buffer.append(c);
        return this;
    }

    @Override
    public LogEntryItem append(final CharSequence csq) {
        BufferFormatter.append(buffer, csq);
        return this;
    }

    @Override
    public LogEntryItem append(final CharSequence csq, final int start, final int end) {
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
    public LogEntryItem append(final int i){
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
    public void commit() {
        buffer.flip();
        loggerImpl.entryFlushed(this);
    }

    boolean tryToAcquire(){
        // the 0th bit is reserved for logger
        // 1 - 32 for appenders
        return counter.compareAndSet(0, 1);
    }
    
    boolean tryToFlush(final int consumers){
        // mask like 11110
        // there are numberOfAppenders ones 
        int mask = ((1 << consumers ) - 1) << 1; 
        return counter.compareAndSet(1, mask);
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

    public Object getLock() {
        return lock;
    }
    
    @Override
    public String toString() {
        return "[" + counter.get()
        + " pos:" + buffer.position() + " limit:" + buffer.limit() + " capacity:" + buffer.capacity() + "]";
    }

}

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

    // inter thread id
    private volatile long id;

    // inter thread fence
    private final Object lock = new Object();
    private final CharBuffer buffer;
    private final AtomicInteger counter;
    private final LoggerImpl loggerImpl;

    private String name;
    private LogLevel logLevel;
    private long timestamp;
    private String threadName;
    private String className;

    private LogEntryItem next;

    public LogEntryItem(final int size, final LoggerImpl loggerImpl) {
        this(ByteBuffer.allocateDirect(size).asCharBuffer(), loggerImpl);
    }

    public LogEntryItem(final CharBuffer buffer, final LoggerImpl loggerImpl) {
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

    public void incCounter(){
        counter.incrementAndGet();
    }

    public int decCounter(){
        return counter.decrementAndGet();
    }

    public int getCounter() {
        return counter.get();
    }

    public long getId() {
        return id;
    }

    public LogLevel getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(final LogLevel logLevel) {
        this.logLevel = logLevel;
    }

    public String getName() {
        return name;
    }

    public String getThreadName() {
        return threadName;
    }

    public String getClassName() {
        return className;
    }

    public long getTimestamp() {
        return timestamp;
    }

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
        return counter.compareAndSet(0, -1);
    }
    
    boolean tryToFlush(final int numberOfAppenders){
        return counter.compareAndSet(-1, numberOfAppenders);
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

    public void setId(final long id) {
        this.id = id;
    }

    public Object getLock() {
        return lock;
    }

    @Override
    public String toString() {
        return "[#" + id + "@" + counter.get()
        + " pos:" + buffer.position() + " limit:" + buffer.limit() + " capacity:" + buffer.capacity() + "]";
    }

}

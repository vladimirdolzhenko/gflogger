package gflogger;

import gflogger.appender.Appender;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicLong;


/**
 * LoggerImpl ring buffer implementation of logger on top of off heap buffer
 * 
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
class DefaultLoggerImpl implements LoggerImpl {

    private final LogLevel level;
    private final Appender[] appenders;
    private final AtomicLong cursor;
    private final LogEntryItem[] entries;
    
    /*
     * It worth to cache thread name at thread local variable cause
     * thread.getName() creates new String(char[])
     */
    private final ThreadLocal<String> threadName = new ThreadLocal<String>(){
        @Override
        protected String initialValue() {
            return Thread.currentThread().getName();
        }
    };

    private final int mask;

    /**
     * @param count a number of items in the ring, could be rounded up to the next power of 2
     * @param bufferSize buffer size of each item in the ring 
     * @param appenders
     */
    public DefaultLoggerImpl(final int count, final int bufferSize, final Appender ... appenders) {
        if (appenders.length == 0){
            throw new IllegalArgumentException("Expected at least one appender");
        }
        // flag size restriction
        if (appenders.length > (Integer.SIZE - 1)){
            throw new IllegalArgumentException("Expected less than " + (Integer.SIZE - 1) + " appenders");
        }
        this.appenders = appenders;
        
        this.cursor = new AtomicLong();
        
        int c = count;
        if (Integer.bitCount(c) != 1){
            c = roundUpNextPower2(c);
        }

        this.entries = initEnties(c, bufferSize);
        // mask is like 000111111 
        this.mask = (entries.length - 1);
        this.level = initLogLevel(appenders);
        
        start(appenders);
    }
    
    private LogLevel initLogLevel(final Appender... appenders) {
        LogLevel level = LogLevel.ERROR;
        for (int i = 0; i < appenders.length; i++) {
            final LogLevel l = appenders[i].getLogLevel();
            level = level.compareTo(l) <= 0 ? level : l;
        }
        return level;
    }

    private LogEntryItem[] initEnties(int count, final int bufferSize) {
        final ByteBuffer buffer = ByteBuffer.allocateDirect(count * bufferSize);

        final LogEntryItem[] entries = new LogEntryItem[count];
        for (int i = 0; i < count; i++) {
            buffer.limit((i + 1) * bufferSize - 1);
            buffer.position(i * bufferSize);
            final ByteBuffer subBuffer = buffer.slice();
            entries[i] = new LogEntryItem(subBuffer.asCharBuffer(), this);
            if (i > 0){
                entries[i - 1].setNext(entries[i]);
            }
        }
        entries[count - 1].setNext(entries[0]);
        return entries;
    }

    private void start(final Appender... appenders) {
        for (int i = 0; i < appenders.length; i++) {
            appenders[i].setIndex(i + 1);
            appenders[i].start(entries[0]);
        }
    }

    @Override
    public LogEntry log(final LogLevel level, final String name, final String className){
        final int idx = (int) (cursor.getAndIncrement() & mask);
        final LogEntryItem entry = entries[idx];
        // each thread will be await for its preacquired (consecutive) entry
        for(int i = 0; ; i++){
            if (entry.tryToAcquire()){
                entry.acquire(name, className);
                entry.setLogLevel(level);
                entry.setThreadName(threadName.get());
                return entry;
            }

            /*/
            final Object lock = actualEntry.getLock();
            synchronized (lock) {
                if(actualEntry.getCounter() > 1){
                    try {
                        lock.wait(1L);
                    } catch (final InterruptedException e) {
                    }
                }
            }
            /*/
            if (i == 100){
                Thread.yield();
            }
            //*/
        }
    }

    void entryFlushed(final LogEntryItem entry){
        // inner entry counter (atomic integer) is a fence
        if (!entry.tryToFlush(appenders.length))
            throw new IllegalStateException("entry has counter " + entry.getCounter());
        for (int i = 0; i < appenders.length; i++) {
            appenders[i].entryFlushed(entry);
        }
    }

    @Override
    public LogLevel getLevel() {
        return level;
    }

    @Override
    public void stop(){
        for(int i = 0; i < appenders.length; i++){
            appenders[i].stop();
        }
    }
    
    private int roundUpNextPower2(int x) {
        // HD, Figure 3-3
        x = x - 1; 
        x = x | (x >> 1); 
        x = x | (x >> 2); 
        x = x | (x >> 4); 
        x = x | (x >> 8); 
        x = x | (x >>16); 
        return x + 1; 
    }

}

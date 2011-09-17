package gflogger;

import gflogger.appender.Appender;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicLong;


/**
 * LoggerImpl ring buffer implementation of logger on top of off heap buffer
 * 
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
class LoggerImpl {

    private final LogLevel level;

    private volatile LogEntryItem entry;
    
    public long q1, q2, q3, q4, q5, q6, q7; // cache line padding

    private final AtomicLong entryIdGenerator;

    private final Appender[] appenders;
    
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

    /**
     * @param count a number of items in the ring
     * @param bufferSize buffer size of each item in the ring 
     * @param appenders
     */
    public LoggerImpl(final int count, final int bufferSize, final Appender ... appenders) {
        this.appenders = appenders;

        final ByteBuffer buffer = ByteBuffer.allocateDirect(count * bufferSize);

        final LogEntryItem[] entries = new LogEntryItem[count];
        // limited to (2^64 - 1) calls
        // it is enough to cover case when the log entry enquires each ns during 292 years 
        entryIdGenerator = new AtomicLong(Long.MIN_VALUE + 1);
        for (int i = 0; i < count; i++) {
            buffer.limit((i + 1) * bufferSize - 1);
            buffer.position(i * bufferSize);
            final ByteBuffer subBuffer = buffer.slice();
            entries[i] = new LogEntryItem(subBuffer.asCharBuffer(), this);
            if (i > 0){
                entries[i - 1].setNext(entries[i]);
            }
            entries[i].setId(entryIdGenerator.getAndIncrement());
        }
        entries[count - 1].setNext(entries[0]);

        entry = entries[0];

        LogLevel level = LogLevel.ERROR;
        for (int i = 0; i < appenders.length; i++) {
            final LogLevel l = appenders[i].getLogLevel();
            level = level.compareTo(l) <= 0 ? level : l;
        }
        this.level = level;
        for (int i = 0; i < appenders.length; i++) {
            appenders[i].start(entries[0]);
        }
    }

    LogEntry log(final LogLevel level, final String name, final String className){
        for(;;){
            final LogEntryItem actualEntry = entry;
            
            // do not mind the gap !
            final long nextId = entryIdGenerator.getAndIncrement();
            
            final boolean acq = actualEntry.tryToAcquire();
            
            if (acq){
                entry = actualEntry.getNext();
                actualEntry.acquire(name, className);
                actualEntry.setLogLevel(level);
                actualEntry.setThreadName(threadName.get());
                // id is a fence
                actualEntry.setId(nextId);
                return actualEntry;
            }

            final Object lock = actualEntry.getLock();
            synchronized (lock) {
                if(actualEntry.getCounter() > 0){
                    try {
                        lock.wait();
                    } catch (final InterruptedException e) {
                    }
                }
            }
        }
    }

    void entryFlushed(final LogEntryItem entry){
        if (!entry.tryToFlush(appenders.length))
            throw new IllegalStateException();
        for (int i = 0; i < appenders.length; i++) {
            appenders[i].entryFlushed(entry);
        }
    }

    public LogLevel getLevel() {
        return level;
    }

    void stop(){
        for(int i = 0; i < appenders.length; i++){
            appenders[i].stop();
        }
    }

}

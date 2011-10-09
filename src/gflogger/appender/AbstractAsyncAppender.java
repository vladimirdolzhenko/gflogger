package gflogger.appender;

import gflogger.Layout;
import gflogger.LogEntry;
import gflogger.LogEntryItem;
import gflogger.LogLevel;
import gflogger.PatternLayout;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.concurrent.atomic.AtomicInteger;


public abstract class AbstractAsyncAppender implements Appender, Runnable {

    // inter thread counter
    protected final AtomicInteger changes;
    
    protected int index;

    protected final Object lock;
    // inner thread buffer
    protected final CharBuffer charBuffer;

    protected LogLevel logLevel = LogLevel.ERROR;
    protected Layout layout;
    protected boolean immediateFlush = false;
    protected int bufferedIOThreshold = 50;
    protected long awaitTimeout = 100;
    protected volatile boolean running = false;

    // inner thread current entry
    protected LogEntryItem entry;


    public AbstractAsyncAppender() {
        // 4M
        this(1 << 22);
    }

    public AbstractAsyncAppender(final int sizeOfBuffer) {
        charBuffer = ByteBuffer.allocateDirect(sizeOfBuffer).asCharBuffer();
        changes = new AtomicInteger();
        lock = new Object();
    }

    @Override
    public LogLevel getLogLevel() {
        return logLevel;
    }

    public synchronized void setLogLevel(final LogLevel logLevel) {
        this.logLevel = logLevel;
    }

    public void setLayout(final Layout layout) {
        this.layout = layout;
    }

    public void setImmediateFlush(final boolean immediateFlush) {
        this.immediateFlush = immediateFlush;
    }

    public void setBufferedIOThreshold(final int bufferedIOThreshold) {
        this.bufferedIOThreshold = bufferedIOThreshold;
    }

    public void setAwaitTimeout(final long awaitTimeout) {
        this.awaitTimeout = awaitTimeout;
    }

    @Override
    public void run() {
        System.out.println(Thread.currentThread().getName() + " is started.");
        int loopCounter = 0;
        do{
            // handle all available changes in a row
            int changesLocal = 0;
            while (changesLocal > 0 || ((changesLocal = changes.get()) > 0)){
                // handle if entry has not been processed yet. 
                while(entry.testCounterBit(index)){
                    // handle entry that has a log level equals or higher than required
                    final boolean hasProperLevel =
                        logLevel.compareTo(entry.getLogLevel()) >= 0;
                        if (hasProperLevel){
                            formatMessage();
                        }

                        // release entry anyway
                        releaseEntry();
                        changesLocal--;

                        if (hasProperLevel){
                            processCharBuffer();

                            if (immediateFlush){
                                flushCharBuffer();
                                loopCounter = 0;
                            }
                        }
                }
            }

            if (immediateFlush || loopCounter > bufferedIOThreshold){
                flushCharBuffer();
                loopCounter = 0;
            }

            synchronized (lock) {
                try {
                    lock.wait(awaitTimeout);
                    loopCounter++;
                } catch (final InterruptedException e) {
                    // nothing
                }
            }
       } while((running || changes.get() > 0) && !Thread.interrupted());
        workerIsAboutFinish();
        System.out.println(Thread.currentThread().getName() + " is finished.");
    }

    protected void processCharBuffer(){
        // empty
    }

    protected void flushCharBuffer(){
        // empty
    }

    protected void workerIsAboutFinish(){
        flushCharBuffer();
    }

    @Override
    public void entryFlushed(final LogEntry entryItem) {
        if (changes.getAndIncrement() == 0){
        // awaitTimeout (100 ms) later on async wake up
        // synchronized (lock) {
        //     lock.notifyAll();
        // }
        }
    }


    protected void formatMessage() {
        final CharBuffer buffer = entry.getBuffer();
        synchronized (buffer) {
            final int position = buffer.position();
            final int limit = buffer.limit();

            layout.format(charBuffer, entry);

            buffer.position(position);
            buffer.limit(limit);
        }
    }

    protected void releaseEntry() {
        final LogEntryItem oldEntry = entry;

        final int counter = oldEntry.resetCounterBit(index);

        entry = entry.getNext();
        final int andDecrement = changes.getAndDecrement();

        if (counter == 0){
            final Object entyLock = oldEntry.getLock();
            synchronized (entyLock) {
                entyLock.notifyAll();
            }
        }
    }

    protected abstract String name();

    @Override
    public void setIndex(int index) {
        // just fence
        synchronized(lock){
            this.index = index;
        }
    }

    @Override
    public void start(final LogEntry entryItem) {
        if (running)
            throw new IllegalStateException();
        
        running = true;

        // just fence
        synchronized(lock){
            if (layout == null){
                layout = new PatternLayout();
            }

            entry = (LogEntryItem) entryItem;
        }

        final Thread thread = new Thread(this, name() + "-logger");
        thread.start();
    }

    @Override
    public void stop(){
        if (!running)
            throw new IllegalStateException();
        System.out.println(name() + " stop ");
        running = false;
        synchronized (lock) {
            lock.notifyAll();
        }
    }
}

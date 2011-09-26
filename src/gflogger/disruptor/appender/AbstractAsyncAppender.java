package gflogger.disruptor.appender;

import gflogger.Layout;
import gflogger.LogLevel;
import gflogger.disruptor.DLogEntryItem;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;


public abstract class AbstractAsyncAppender implements DAppender {


    // inner thread buffer
    protected final CharBuffer charBuffer;

    protected LogLevel logLevel = LogLevel.ERROR;
    protected Layout layout;
    protected boolean autoFlush = true;
    protected int autoFlushThreshold = 50;
    protected long awaitTimeout = 100;

    public AbstractAsyncAppender() {
        // 4M
        this(1 << 22);
    }

    public AbstractAsyncAppender(final int sizeOfBuffer) {
        charBuffer = ByteBuffer.allocate(sizeOfBuffer).asCharBuffer();
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

    public void setAutoFlush(final boolean autoFlush) {
        this.autoFlush = autoFlush;
    }

    public void setAutoFlushThreshold(final int autoFlushThreshold) {
        this.autoFlushThreshold = autoFlushThreshold;
    }

    public void setAwaitTimeout(final long awaitTimeout) {
        this.awaitTimeout = awaitTimeout;
    }
    
    @Override
    public void onEvent(DLogEntryItem event, long sequence, boolean endOfBatch)
            throws Exception {
        //System.out.println(">" + event.getSequenceId() + " " + sequence + " " + endOfBatch);
        // handle entry that has a log level equals or higher than required
        final LogLevel entryLevel = event.getLogLevel();
        if (entryLevel == null) throw new IllegalStateException(); 
        final boolean hasProperLevel = logLevel.compareTo(entryLevel) >= 0;
        if (hasProperLevel) {
            formatMessage(event);
        }

        if (hasProperLevel) {
            processCharBuffer();

            if (autoFlush) {
                flushCharBuffer();
            }
        }
    }

    protected void processCharBuffer(){
        // empty
    }

    protected void flushCharBuffer(){
        // empty
    }


    protected void formatMessage(DLogEntryItem entry) {
        //System.out.println("formatMessage:" + entry.getSequenceId());
        final CharBuffer buffer = entry.getBuffer();
        synchronized (buffer) {
            final int position = buffer.position();
            final int limit = buffer.limit();

            layout.format(charBuffer, entry);

            buffer.position(position);
            buffer.limit(limit);
        }
        //System.out.println("formatedMessage:" + entry.getSequenceId());
    }


    protected abstract String name();
    
    @Override
    public void onStart() {
    }

    @Override
    public void onShutdown() {
        autoFlush = true;
        flushCharBuffer();
    }
}

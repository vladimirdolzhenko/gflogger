package gflogger;

import java.nio.CharBuffer;

/**
 * MockLogEntry
 * 
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public class MockLogEntry implements LogEntry {

    @Override
    public LogEntry append(final char c) {
        return this;
    }

    @Override
    public LogEntry append(final CharSequence csq) {
        return this;
    }

    @Override
    public LogEntry append(final CharSequence csq, final int start, final int end) {
        return this;
    }

    @Override
    public LogEntry append(final boolean b) {
        return this;
    }

    @Override
    public LogEntry append(final byte i) {
        return this;
    }

    @Override
    public LogEntry append(final short i) {
        return this;
    }

    @Override
    public LogEntry append(final int i) {
        return this;
    }

    @Override
    public LogEntry append(final long i) {
        return this;
    }

    @Override
    public LogEntry append(final double i, final int precision) {
        return this;
    }

    @Override
    public void commit() {
    }
    
    @Override
    public LogLevel getLogLevel() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getThreadName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getClassName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getTimestamp() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public CharBuffer getBuffer() {
        // TODO Auto-generated method stub
        return null;
    }
    
    
}

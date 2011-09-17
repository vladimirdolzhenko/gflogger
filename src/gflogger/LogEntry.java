package gflogger;

/**
 * LogEntry
 * 
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public interface LogEntry {

    static final long startTime = System.currentTimeMillis();

    /**
     * append a single char
     * @return a reference to this object.
     */
    LogEntry append(char c);

    LogEntry append(CharSequence csq);

    LogEntry append(CharSequence csq, int start, int end);

    LogEntry append(boolean b);

    LogEntry append(byte i);

    LogEntry append(short i);

    LogEntry append(int i);

    LogEntry append(long i);

    LogEntry append(double i, int precision);

    /**
     * commit an entry
     */
    void commit();

}

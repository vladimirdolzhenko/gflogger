package gflogger.appender;

import gflogger.LogEntry;
import gflogger.LogLevel;

public interface Appender {

    LogLevel getLogLevel();
    
    void setIndex(int index);

    void start(LogEntry entryItem);

    void entryFlushed(LogEntry entryItem);

    void stop();
}

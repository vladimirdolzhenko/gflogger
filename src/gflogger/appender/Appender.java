package gflogger.appender;

import gflogger.LogEntryItem;
import gflogger.LogLevel;

public interface Appender {

    LogLevel getLogLevel();
    
    void setIndex(int index);

    void start(LogEntryItem entryItem);

    void entryFlushed(LogEntryItem entryItem);

    void stop();
}

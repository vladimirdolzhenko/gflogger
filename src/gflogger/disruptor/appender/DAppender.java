package gflogger.disruptor.appender;

import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.LifecycleAware;

import gflogger.LogLevel;
import gflogger.disruptor.DLogEntryItem;

/**
 * DAppender
 * 
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public interface DAppender extends EventHandler<DLogEntryItem>, LifecycleAware {

    LogLevel getLogLevel();
    
}

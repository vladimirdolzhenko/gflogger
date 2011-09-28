package gflogger.disruptor;

import gflogger.LogEntry;
import gflogger.LogLevel;
import gflogger.LoggerImpl;
import gflogger.disruptor.appender.DAppender;

import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.lmax.disruptor.ClaimStrategy;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.ExceptionHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;


/**
 * LoggerImpl ring buffer implementation of logger on top of off heap buffer
 * 
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public class DLoggerImpl implements LoggerImpl {

    private final LogLevel level;

    private final DAppender[] appenders;
    
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

    private final Disruptor<DLogEntryItem> disruptor;

    private final RingBuffer<DLogEntryItem> ringBuffer;

    private final ExecutorService executorService;

    /**
     * @param count a number of items in the ring
     * @param bufferSize buffer size of each item in the ring 
     * @param appenders
     */
    public DLoggerImpl(final int count, final int bufferSize, final DAppender ... appenders) {
        if (appenders.length == 0){
            throw new IllegalArgumentException("Expected at least one appender");
        }
        if (appenders.length > (Integer.SIZE - 1)){
            throw new IllegalArgumentException("Expected less than " + (Integer.SIZE - 1) + " appenders");
        }
        this.appenders = appenders;
        
        int c = count;
        // quick check is count = 2^k ?
        if ((count & (count - 1)) != 0){
            c = roundUpNextPower2(c);
        }
        
        this.level = initLevel(appenders);

        final ByteBuffer buffer = ByteBuffer.allocate(c * bufferSize);
        
        executorService = Executors.newFixedThreadPool(appenders.length);
        disruptor = new Disruptor<DLogEntryItem>(new EventFactory<DLogEntryItem>() {
            int i = 0;
            @Override
            public DLogEntryItem newInstance() {
                buffer.limit((i + 1) * bufferSize - 1);
                buffer.position(i * bufferSize);
                i++;
                final ByteBuffer subBuffer = buffer.slice();
                return new DLogEntryItem(subBuffer.asCharBuffer(), DLoggerImpl.this);
            }
        }, c, 
        executorService, 
        ClaimStrategy.Option.MULTI_THREADED,
        WaitStrategy.Option.YIELDING);
        
        disruptor.handleExceptionsWith(new ExceptionHandler() {
            
            @Override
            public void handle(Exception ex, long sequence, Object event) {
                ex.printStackTrace();
            }
        });
        disruptor.handleEventsWith(appenders);
        
        
        ringBuffer = disruptor.start();
    }

    private LogLevel initLevel(final DAppender... appenders) {
        LogLevel level = LogLevel.ERROR;
        for (int i = 0; i < appenders.length; i++) {
            final LogLevel l = appenders[i].getLogLevel();
            level = level.compareTo(l) <= 0 ? level : l;
        }
        return level;
    }

    @Override
    public LogEntry log(final LogLevel level, final String name, final String className){
        long sequence = ringBuffer.next();
        final DLogEntryItem entryItem = ringBuffer.get(sequence);
        entryItem.setSequenceId(sequence);
        entryItem.acquire(name, className);
        entryItem.setLogLevel(level);
        entryItem.setThreadName(threadName.get());
        return entryItem;
    }

    void entryFlushed(final DLogEntryItem entry){
        final long sequenceId = entry.getSequenceId();
        ringBuffer.publish(sequenceId);
        //System.out.println(sequenceId);
    }

    @Override
    public LogLevel getLevel() {
        return level;
    }

    @Override
    public void stop(){
        disruptor.halt();
        executorService.shutdown();
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

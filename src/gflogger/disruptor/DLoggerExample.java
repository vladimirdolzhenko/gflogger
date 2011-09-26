package gflogger.disruptor;

import gflogger.LogEntry;
import gflogger.LogFactory;
import gflogger.LogLevel;
import gflogger.Logger;
import gflogger.LoggerImpl;
import gflogger.PatternLayout;
import gflogger.disruptor.appender.ConsoleAppender;
import gflogger.disruptor.appender.FileAppender;

import java.util.Collections;
import java.util.concurrent.CountDownLatch;

import com.google.monitoring.runtime.instrumentation.AllocationRecorder;
import com.google.monitoring.runtime.instrumentation.Sampler;


/**
 * LoggerExample
 * 
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public class DLoggerExample {

    public static void main(final String[] args) throws Exception {
        final int threadCount = args.length > 0 ? Integer.parseInt(args[0]) : 1;

        // 1024 items per 256 bytes each

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {

            @Override
            public void uncaughtException(final Thread t, final Throwable e) {
                e.printStackTrace();
            }
        });

        final FileAppender fileAppender = new FileAppender();
        fileAppender.setLogLevel(LogLevel.INFO);
        fileAppender.setFileName("./logs/dgflogger.log");
        fileAppender.setAppend(false);
        fileAppender.setAutoFlush(false);
        fileAppender.setLayout(new PatternLayout("%d{HH:mm:ss,SSS zzz} %p %m [%c{2}] [%t]%n"));

        final ConsoleAppender consoleAppender = new ConsoleAppender();
        consoleAppender.setLogLevel(LogLevel.INFO);
        consoleAppender.setLayout(new PatternLayout("%d{HH:mm:ss,SSS zzz} %p %m [%c{2}] [%t]%n"));

        //final LoggerImpl impl = new LoggerImpl(1 << 10, 1 << 8, fileAppender);
        //final LoggerImpl impl = new LoggerImpl(1 << 2, 1 << 8, fileAppender, consoleAppender);
        final LoggerImpl impl = new DLoggerImpl(1 << 10, 1 << 8, 
                 fileAppender
                //, consoleAppender
        );

        LogFactory.init(Collections.singletonMap("com.db", impl));

        final Logger logger = LogFactory.getLog("com.db.fxpricing.Logger");
        
        final ThreadLocal<StringBuilder> local = new ThreadLocal<StringBuilder>(){
            @Override
            public StringBuilder get() {
                return new StringBuilder(64);
            }
        };
        
        final ThreadLocal<String> threadName = new ThreadLocal<String>(){
            @Override
            protected String initialValue() {
                return Thread.currentThread().getName();
            }
        };
        
        //*/
        AllocationRecorder.addSampler(new Sampler() {
            
            @Override
            public void sampleAllocation(int count, String desc,
              Object newObj, long size) {
              final StringBuilder builder = local.get();
              builder.setLength(0);
              if (count != -1) {
                  builder.append("It's an array of ").
                    append(newObj.getClass().getComponentType().getName()).
                    append("[").append(count).append("]");
              } else {
                  if (newObj instanceof String){
                      builder.append("I just allocated the string '").append(newObj).
                          append('\'');
                    } else {
                        builder.append("I just allocated the object ").append(newObj).
                        append(" of type ").append(desc).append(" whose size is ").append(size);
                    }
              }
              builder.append('[').append(threadName.get()).append(']');
              System.out.println(builder);
            }
          });
        /*/
        //*/

        final long start = System.currentTimeMillis();
        final int n = args.length > 1 ? Integer.parseInt(args[1]) : 1 << 10;

        final CountDownLatch latch = new CountDownLatch(1);
        final CountDownLatch finalLatch = new CountDownLatch(threadCount);

        final Thread[] threads = new Thread[threadCount];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(new Runnable() {
                
                @Override
                public void run() {
                    try {
                        doSmth();
                    } catch (Throwable e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }

                public void doSmth() throws Throwable{
                    latch.await();
                    for(int j = 0; j < (n << 1); j++){
                        logger.info().append("warmup").append(j).commit();
                    }
                    
                    System.gc();
                    System.gc();
                    System.gc();
                    Thread.sleep(5000);
                    
                    System.out.println("--- warmed up ---");
                    System.out.println("--- warmed up ---");
                    System.out.println("--- warmed up ---");
                    
                    final long t = System.nanoTime();
                    long acq = 0;
                    long commit = 0;
                    long append = 0;
                    //System.out.println(Thread.currentThread().getName() + " is started.");
                    for(int j = 0; j < n; j++){
                        /*/
                        acq -= System.nanoTime();
                        /*/
                        //*/
                        final LogEntry entry = logger.info();
                        /*/
                        final long t0 = System.nanoTime();
                        acq += t0;
                        append -= t0;
                        /*/
                        //*/
                        entry.append("test").append(j);
                        /*/                        
                        final long t1 = System.nanoTime();
                        append += t1;
                        commit -= t1;
                        /*/
                        //*/
                        entry.commit();
                        /*/
                        commit += System.nanoTime();
                        /*/
                        //*/
                        //System.out.println("info:" + i);
                    }
                    final long e = System.nanoTime();
                    logger.info().append("final: ").append((e - t) / 1e6, 3).
                        append(" acq:").append(acq / 1e6, 3).
                        append(" append:").append(append / 1e6, 3).
                        append(" commit:").append(commit / 1e6, 3).
                        commit();
                    finalLatch.countDown();

                    //System.out.println(Thread.currentThread().getName() + " is finished.");
                }
            }, "thread-" + i);
            threads[i].start();
        }

        //*/
        for(int i = 0; i < 10; i++){
            logger.debug().append("test").append(i).commit();
        }

        latch.countDown();

        System.out.println("---");
        finalLatch.await();
        logger.info().append("total time:").append(System.currentTimeMillis() - start).append(" ms.").commit();
        Thread.sleep(5000);
        LogFactory.stop();
    }
}

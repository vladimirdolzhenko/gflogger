package org.gflogger.perftest;


import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import org.gflogger.helpers.LogLog;

import com.google.monitoring.runtime.instrumentation.AllocationRecorder;
import com.google.monitoring.runtime.instrumentation.Sampler;


/**
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public abstract class AbstractExample {

    protected boolean allocationEnabled;

    protected int threadCount = 1;

    protected int messageCount = 1 << 10;

    protected void parseArgs(final String[] args){
        threadCount = args.length > 0 ? Integer.parseInt(args[0]) : 1;
        messageCount = args.length > 1 ? Integer.parseInt(args[1]) : 1 << 10;
    }

    public void runTest() throws Throwable{
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {

            @Override
            public void uncaughtException(final Thread t, final Throwable e) {
                e.printStackTrace();
            }
        });

        synchronized (this) {
            initLogger();
        }

        final ThreadLocal<StringBuilder> local = new ThreadLocal<StringBuilder>(){
            @Override
            public StringBuilder get() {
                return new StringBuilder(1 << 6);
            }
        };

        final ThreadLocal<String> threadName = new ThreadLocal<String>(){
            @Override
            protected String initialValue() {
                return Thread.currentThread().getName();
            }
        };

        final AtomicBoolean objectCounting = new AtomicBoolean(false);
        //*/
        if (allocationEnabled)
        AllocationRecorder.addSampler(new Sampler() {

            @Override
            public void sampleAllocation(int count, String desc,
              Object newObj, long size) {
              if (!objectCounting.get()) return;
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
        final int n = messageCount;

        final CountDownLatch latch = new CountDownLatch(1);
        final CountDownLatch finalLatch = new CountDownLatch(threadCount);

        final Thread[] threads = new Thread[threadCount];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(new Runnable() {

                private String name;
                private String startUpMessage;
                private String warmedUpMessage;
                private String finishedMessage;

                @Override
                public void run() {
                    this.name = Thread.currentThread().getName();
                    this.startUpMessage = name + "--- start up ---";
                    this.warmedUpMessage = name + "--- warmed up ---";
                    this.finishedMessage = name + "--- finished ---";

                    System.out.println(startUpMessage);
                    try {
                        doSmth();
                    } catch (Throwable e) {
                    	LogLog.error("[" + name + "] exception: " + e.getMessage(), e);
                    }
                    System.out.println(finishedMessage);
                }

                public void doSmth() throws Throwable{
                    for(int k = 0; k < 5; k++){
                        for(int j = 0; j < 10000; j++){
                            logMessage("warm", j);
                        }
                    }

                    latch.await();
                    System.gc();
                    System.gc();
                    System.gc();
                    Thread.sleep(2000);

                    System.out.println(warmedUpMessage);
                    System.out.println(warmedUpMessage);
                    System.out.println(warmedUpMessage);

                    objectCounting.set(true);

                    final long t = System.nanoTime();
                    //System.out.println(Thread.currentThread().getName() + " is started.");
                    for(int j = 0; j < n; j++){
                        logMessage("test", j);
                    }
                    final long e = System.nanoTime();
                    logFinalMessage(n, t, e);
                    finalLatch.countDown();

                    //System.out.println(Thread.currentThread().getName() + " is finished.");
                }


            }, "thread-" + i);
            threads[i].start();
        }

        //*/
        for(int i = 0; i < 10; i++){
            logDebugTestMessage(i);
        }

        latch.countDown();

        System.out.println("---");
        finalLatch.await();
        objectCounting.set(false);
        logTotalMessage(start);
        System.out.println("--- stopping ---");
        System.out.println("--- stopping ---");
        Thread.sleep(500);
        System.out.println("--- stopping ---");
        stop();
        System.out.println("stopped.");

        //System.exit(0);
    }

    protected abstract void stop();

    protected abstract void initLogger();

    protected abstract void logDebugTestMessage(int i);

    protected abstract void logMessage(final String msg, int j) ;

    protected abstract void logFinalMessage(final int count, final long t, final long e);

    protected abstract void logTotalMessage(final long start);
}

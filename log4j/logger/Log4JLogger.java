package logger;

import java.util.concurrent.CountDownLatch;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.BasicConfigurator;

public class Log4JLogger {

    static {
        BasicConfigurator.configure();
    }
    
    private static final Log log = LogFactory.getLog("com.db.fxpricing.Logger");
    
    public static void main(String[] args) throws Throwable {
        final long start = System.currentTimeMillis();
        int threadCount = args.length > 0 ? Integer.parseInt(args[0]) : 1;
        
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                e.printStackTrace();
            }
        });
        
        final int n = args.length > 1 ? Integer.parseInt(args[1]) : (1 << 10);
        
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
                        e.printStackTrace();
                    }
                }
                
                public void doSmth() throws Throwable {
                    latch.await();
                    for(int j = 0; j < (n << 1); j++){
                        log.info("warmup" + j);
                    }
                    System.gc();
                    System.gc();
                    System.gc();
                    Thread.sleep(2000);
                    
                    System.out.println("--- warmed up ---");
                    System.out.println("--- warmed up ---");
                    System.out.println("--- warmed up ---");
                    final long s = System.nanoTime(); 
                    //System.out.println(Thread.currentThread().getName() + " is started.");
                    for(int j = 0; j < n; j++){
                        log.info("test" + j);
                        //System.out.println("info:" + i);
                    }
                    log.info("final: " + ((System.nanoTime() - s) / 1000) / 1e3);
                    finalLatch.countDown();
                    
                    //System.out.println(Thread.currentThread().getName() + " is finished.");
                }
            }, "thread-" + i);
            threads[i].start();
        }
        
        //*/
        for(int i = 0; i < 10; i++){
            log.debug("test" + i);
        }
        
        latch.countDown();
        finalLatch.await();
        log.info("total time:" + (System.currentTimeMillis() - start) + " ms.");
        Thread.sleep(5000);
        System.out.println("---");
    }
}

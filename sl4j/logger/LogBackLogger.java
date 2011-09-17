package logger;

import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class LogBackLogger {

    private static final Logger log = LoggerFactory.getLogger("com.db.fxpricing.Logger");
    
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
                        latch.await();
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        return;
                    }
                    for(int j = 0; j < (n << 1); j++){
                        log.info("warmup{}", j);
                    }
                    final long s = System.nanoTime(); 
                    //System.out.println(Thread.currentThread().getName() + " is started.");
                    for(int j = 0; j < n; j++){
                        log.info("test{}", j);
                        //System.out.println("info:" + i);
                    }
                    log.info("final: {}", ((System.nanoTime() - s) / 1000) / 1e3);
                    finalLatch.countDown();
                    
                    //System.out.println(Thread.currentThread().getName() + " is finished.");
                }
            }, "thread-" + i);
            threads[i].start();
        }
        
        //*/
        for(int i = 0; i < 10; i++){
            log.debug("test{}", i);
        }
        
        latch.countDown();
        finalLatch.await();
        log.info("total time: {} ms", System.currentTimeMillis() - start);
        Thread.sleep(5000);
        System.out.println("---");
    }
}

package perftest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public class LogBackExample extends AbstractExample {
    
    private Logger log;

    @Override
    protected void initLogger() {
        log = LoggerFactory.getLogger("com.db.fxpricing.Logger");
    }
    
    @Override
    protected void stop() {
        // TODO Auto-generated method stub
    }
    
    @Override
    protected void logDebugTestMessage(int i) {
        log.debug("test{}", i);
    }
    
    @Override
    protected void logTestMessage(int j) {
        log.info("test{}", j);
    }

    @Override
    protected void logWarmup(int j) {
        log.info("warmup{}", j);
    }
    
    @Override
    protected void logFinalMessage(final long t, final long e) {
        log.info("final: {}",(e - t) / 1000 / 1e3);
    }
    
    @Override
    protected void logTotalMessage(final long start) {
        log.info("total time:{} ms",System.currentTimeMillis() - start);
    }


    public static void main(String[] args) throws Throwable {
        final LogBackExample example = new LogBackExample();
        example.parseArgs(args);
        
        example.runTest();
    }
    
    
}

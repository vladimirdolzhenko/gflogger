package perftest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.BasicConfigurator;

/**
 * Log4JExample
 * 
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public class Log4JExample extends AbstractExample {
    
    private Log log;

    @Override
    protected void initLogger() {
        BasicConfigurator.configure();
        log = LogFactory.getLog("com.db.fxpricing.Logger");
    }
    
    @Override
    protected void stop() {
        // TODO Auto-generated method stub
    }
    
    @Override
    protected void logDebugTestMessage(int i) {
        log.debug("test" + i);
    }
    
    @Override
    protected void logTestMessage(int j) {
        log.info("test" + j);
    }

    @Override
    protected void logWarmup(int j) {
        log.info("warmup" + j);
    }
    
    @Override
    protected void logFinalMessage(final long t, final long e) {
        log.info("final: " + ((e - t) / 1000 / 1e3));
    }
    
    @Override
    protected void logTotalMessage(final long start) {
        log.info("total time:"+ (System.currentTimeMillis() - start) + " ms.");
    }


    public static void main(String[] args) throws Throwable {
        final Log4JExample example = new Log4JExample();
        example.parseArgs(args);
        
        example.runTest();
    }
    
    
}

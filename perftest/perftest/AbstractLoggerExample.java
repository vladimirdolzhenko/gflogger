package perftest;

import gflogger.LogFactory;
import gflogger.Logger;
import gflogger.LoggerImpl;

import java.util.Collections;


/**
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public abstract class AbstractLoggerExample extends AbstractExample {
    
    private Logger logger;
    
    @Override
    protected void initLogger() {
        final LoggerImpl impl = createLoggerImpl();

        LogFactory.init(Collections.singletonMap("com.db", impl));

        this.logger = LogFactory.getLog("com.db.fxpricing.Logger");
    }
    
    protected abstract LoggerImpl createLoggerImpl();
    
    @Override
    protected void stop() {
        LogFactory.stop();
    }
    
    @Override
    protected void logDebugTestMessage(int i) {
        logger.debug().append("test").append(i).commit();
    }
    
    @Override
    protected void logTestMessage(int j) {
        logger.info().append("test").append(j).commit();
    }

    @Override
    protected void logWarmup(int j) {
        logger.info().append("warmup").append(j).commit();
    }
    
    @Override
    protected void logFinalMessage(final long t, final long e) {
        logger.info().append("final: ").append((e - t) / 1e6, 3).commit();
    }
    
    @Override
    protected void logTotalMessage(final long start) {
        logger.info().append("total time:").append(System.currentTimeMillis() - start).append(" ms.").commit();
    }
}

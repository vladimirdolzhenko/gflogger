package perftest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.LogManager;

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
        LogManager.shutdown();
    }

    @Override
    protected void logDebugTestMessage(int i) {
        log.debug("test" + i);
    }

    @Override
    protected void logMessage(String msg, int j) {
        log.info(msg + j);
    }

    @Override
    protected void logFinalMessage(final int count, final long t, final long e) {
        log.info("final count: " + count + " time: " + ((e - t) / 1000 / 1e3) + " ms");
        System.out.println("final count: " + count + " time: " + ((int)((e-t)/1000)) / 1e3);
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

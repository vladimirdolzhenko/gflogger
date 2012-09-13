package org.gflogger.perftest;

import org.gflogger.GFLog;
import org.gflogger.GFLogFactory;
import org.gflogger.LogLevel;
import org.gflogger.LoggerService;
import org.gflogger.PatternLayout;
import org.gflogger.appender.AppenderFactory;
import org.gflogger.appender.FileAppenderFactory;


/**
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public abstract class AbstractLoggerExample extends AbstractExample {

    protected GFLog log;
    protected LoggerService service;

    @Override
    protected void initLogger() {
        service = createLoggerImpl();

        GFLogFactory.init("com.db", service);

        this.log = GFLogFactory.getLog("com.db.fxpricing.Logger");
    }

    protected AppenderFactory[] createAppenderFactories(){
    	final FileAppenderFactory fileAppender = new FileAppenderFactory();
        fileAppender.setLogLevel(LogLevel.INFO);
        fileAppender.setFileName(fileAppenderFileName());
        fileAppender.setAppend(false);
        fileAppender.setImmediateFlush(false);
        fileAppender.setLayout(new PatternLayout("%d{HH:mm:ss,SSS zzz} %p %m [%c{2}] [%t]%n"));

        return new AppenderFactory[]{fileAppender};
    }

    protected abstract String fileAppenderFileName();

	protected abstract LoggerService createLoggerImpl();

    @Override
    protected void stop() {
        GFLogFactory.stop();
    }

    @Override
    protected void logDebugTestMessage(int i) {
        log.debug().append("test").append(i).commit();
    }

    @Override
    protected void logMessage(String msg, int j) {
        log.info().append(msg).append(j).commit();
    }

    @Override
    protected void logFinalMessage(final int count, final long t, final long e) {
        log.info().append("final count: ").append(count).append(" time: ").append((e - t) / 1e6, 3).append(" ms").commit();
        System.out.println("final count: " + count + " time: " + ((int)((e-t)/1000)) / 1e3);
    }

    @Override
    protected void logTotalMessage(final long start) {
        log.info().append("total time:").append(System.currentTimeMillis() - start).append(" ms.").commit();
    }
}

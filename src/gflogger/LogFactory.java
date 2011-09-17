package gflogger;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * LogFactory
 * 
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public class LogFactory {

    private final Map<String, LoggerImpl> impls;
    private final Map<String, LoggerView> namedLogger;
    private final Map<Class, LoggerView> classedLogger;

    private LogFactory(){
        impls = new HashMap<String, LoggerImpl>();
        namedLogger = new HashMap<String, LoggerView>();
        classedLogger = new HashMap<Class, LoggerView>();
    }

    private Logger get(final String name){
        LoggerView logger = namedLogger.get(name);
        if (logger != null) return logger;
        synchronized (namedLogger) {
            logger = namedLogger.get(name);
            if (logger != null) return logger;

            String n = name;

            // look for name
            for(;;){
                final LoggerImpl impl = impls.get(n);
                if (impl != null){
                    logger = new LoggerView(impl, name);
                    namedLogger.put(name, logger);
                    return logger;
                }
                final int idx = n.lastIndexOf('.');
                if (idx < 0) {
                    break;
                }
                n = n.substring(0, idx);
            }


            // otherwise looking for dumpl root
            logger = new LoggerView(null, name);
            namedLogger.put(name, logger);
            return logger;
        }
    }

    private Logger get(final Class clazz){
        LoggerView logger = classedLogger.get(clazz);
        if (logger != null) return logger;
        synchronized (classedLogger) {
            logger = classedLogger.get(clazz);
            if (logger != null) return logger;

            String n = clazz.getName();

            // look for name
            for(;;){
                final LoggerImpl impl = impls.get(n);
                if (impl != null){
                    logger = new LoggerView(impl, clazz);
                    classedLogger.put(clazz, logger);
                    return logger;
                }
                final int idx = n.lastIndexOf('.');
                if (idx < 0) {
                    break;
                }
                n = n.substring(0, idx);
            }


            // otherwise looking for dumpl root
            logger = new LoggerView(null, clazz);
            classedLogger.put(clazz, logger);
            return logger;
        }
    }

    public static Logger getLog(final String name){
        return Helper.FACTORY.get(name);
    }

    public static Logger getLog(final Class clazz){
        return Helper.FACTORY.get(clazz);
    }

    public static void stop(){
        final Collection<LoggerImpl> values = Helper.FACTORY.impls.values();
        for (final LoggerImpl impl : values) {
            impl.stop();
        }
    }

    static void init(final Map<String, LoggerImpl> impls){
        Helper.FACTORY.impls.clear();
        if (impls != null){
            Helper.FACTORY.impls.putAll(impls);
        }
    }

    private static class Helper {
        final static LogFactory FACTORY = new LogFactory();
    }
}

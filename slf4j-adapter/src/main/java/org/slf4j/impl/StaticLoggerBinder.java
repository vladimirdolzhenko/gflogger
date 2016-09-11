package org.slf4j.impl;

import org.gflogger.config.xml.XmlLogFactoryConfigurator;
import org.gflogger.helpers.LogLog;
import org.gflogger.slf4j.Slf4jLoggerFactory;
import org.slf4j.ILoggerFactory;

/**
 * @author Denis Gburg
 */
public class StaticLoggerBinder {
    private static final StaticLoggerBinder SINGLETON = new StaticLoggerBinder();

    static {
        SINGLETON.init();
    }

    public static StaticLoggerBinder getSingleton() {
        return SINGLETON;
    }

    private void init(){
        try {
            XmlLogFactoryConfigurator.configure();
        } catch (Exception e) {
            LogLog.error("Failed to configure gflogger", e);
        }
    }

    public ILoggerFactory getLoggerFactory() {
        return new Slf4jLoggerFactory();
    }

    public String getLoggerFactoryClassStr() {
        return Slf4jLoggerFactory.class.getName();
    }
}

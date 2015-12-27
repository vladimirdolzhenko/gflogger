package org.gflogger.slf4j;

import org.gflogger.GFLogFactory;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;

/**
 * @author Denis Gburg
 */
public final class Slf4jLoggerFactory implements ILoggerFactory {

    @Override
    public Logger getLogger(String name) {
        return new Slf4jLoggerImpl(GFLogFactory.getLog(name));
    }
}

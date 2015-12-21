package org.gflogger.jcl;


import org.apache.commons.logging.Log;
import org.gflogger.GFLog;
import org.gflogger.GFLogFactory;

/**
 * LogImpl is java commons logging impl on the top of gflogger
 * 
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public class LogImpl implements Log {

	private final GFLog log;
	
	public LogImpl(final String categoryName) {
		this.log = GFLogFactory.getLog(categoryName);
	}

	@Override
	public boolean isDebugEnabled() {
		return log.isDebugEnabled();
	}

	@Override
	public boolean isErrorEnabled() {
		return log.isErrorEnabled();
	}

	@Override
	public boolean isFatalEnabled() {
		// there is no fatal log level
		return log.isErrorEnabled();
	}

	@Override
	public boolean isInfoEnabled() {
		return log.isInfoEnabled();
	}

	@Override
	public boolean isTraceEnabled() {
		// there is no trace log level
		return log.isDebugEnabled();
	}

	@Override
	public boolean isWarnEnabled() {
		// there is no warn log level
		return log.isErrorEnabled();
	}

	@Override
	public void trace(Object message) {
		// there is no trace log level
		log.debug().append(message).commit();
	}

	@Override
	public void trace(Object message, Throwable t) {
		// there is no trace log level
		log.debug().append(message).append(t).commit();
	}

	@Override
	public void debug(Object message) {
		log.debug().append(message).commit();
	}

	@Override
	public void debug(Object message, Throwable t) {
		log.debug().append(message).append(t).commit();

	}

	@Override
	public void info(Object message) {
		log.info().append(message).commit();
	}

	@Override
	public void info(Object message, Throwable t) {
		log.info().append(message).append(t).commit();
	}

	@Override
	public void warn(Object message) {
		// there is no warn log level
		log.error().append(message).commit();
	}

	@Override
	public void warn(Object message, Throwable t) {
		// there is no warn log level
		log.error().append(message).append(t).commit();
	}

	@Override
	public void error(Object message) {
		log.error().append(message).commit();
	}

	@Override
	public void error(Object message, Throwable t) {
		log.error().append(message).append(t).commit();
	}

	@Override
	public void fatal(Object message) {
		// there is no fatal log level
		log.error().append(message).commit();
	}

	@Override
	public void fatal(Object message, Throwable t) {
		// there is no fatal log level
		log.error().append(message).append(t).commit();
	}

}

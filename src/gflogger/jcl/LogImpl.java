package gflogger.jcl;

import gflogger.LogFactory;
import gflogger.Logger;

import org.apache.commons.logging.Log;

/**
 * 
 * 
 * 
 * LogImpl is java commons logging impl on the top of gflogger
 * 
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public class LogImpl implements Log {

	private final Logger log;
	
	public LogImpl(final String categoryName) {
		this.log = LogFactory.getLog(categoryName);
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
		// there is no fatal log level
		return log.isErrorEnabled();
	}

	@Override
	public void trace(Object message) {
		// TODO Auto-generated method stub
	}

	@Override
	public void trace(Object message, Throwable t) {
		// TODO Auto-generated method stub

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
		// TODO Auto-generated method stub

	}

	@Override
	public void warn(Object message, Throwable t) {
		// TODO Auto-generated method stub

	}

	@Override
	public void error(Object message) {
		// TODO Auto-generated method stub

	}

	@Override
	public void error(Object message, Throwable t) {
		// TODO Auto-generated method stub

	}

	@Override
	public void fatal(Object message) {
		// TODO Auto-generated method stub

	}

	@Override
	public void fatal(Object message, Throwable t) {
		// TODO Auto-generated method stub

	}

}

package org.gflogger.ring;

public final class AlertException extends RuntimeException {

	private static final long	serialVersionUID	= 1L;

	public static final AlertException ALERT_EXCEPTION = new AlertException();

	private AlertException() {
	}

	@Override
	public synchronized Throwable fillInStackTrace() {
		return this;
	}
}

package com.oms.logback;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.FileAppender;

import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by IntelliJ IDEA.
 * User: koticka
 * Date: 11.12.2009
 * Time: 0:08:31
 * To change this template use File | Settings | File Templates.
 */
public final class AsynchFileAppender<E> extends FileAppender<E> {

	private static final int MAX_PENDING_EVENTS = 1024;

	private static final ArrayBlockingQueue<AsynchEvent> logQ = new ArrayBlockingQueue<AsynchEvent>(MAX_PENDING_EVENTS);
	private static AsynchWorker<ILoggingEvent> worker = null;
	private static boolean workerStarted = false;

	private static final Object workerLock = new Object();

	@Override
	public void start() {
		synchronized (workerLock) {
			if (worker == null || !workerStarted) {
				if (worker != null) {
					worker.stopWorker();
				}
				worker = new AsynchWorker<ILoggingEvent>();
				worker.start();
				workerStarted = true;
			}
		}
		super.start();
	}

	@Override
	public void stop() {
		synchronized (workerLock) {
			if (worker != null) {
				worker.stopWorker();
				worker = null;
				workerStarted = false;
			}
			AsynchEvent<E> e;
			while ((e = logQ.poll())!=null) e.appender.subAppendImpl(e.event);
		}
		super.stop();
	}


	private void subAppendImpl(E event) {
		super.subAppend(event);
	}

	@Override
	protected void subAppend(E event) {
		((ILoggingEvent) event).prepareForDeferredProcessing();

		try {
			logQ.put(new AsynchEvent<E>(this, event));
		} catch (InterruptedException e) {
			e.printStackTrace();
			addError("Asynch enqueue fails for " + event, e);
		}
	}

	private static class AsynchEvent<E> {
		final AsynchFileAppender<E> appender;
		final E event;

		AsynchEvent(AsynchFileAppender<E> appender, E event) {
			this.appender = appender;
			this.event = event;
		}
	}

	private static class AsynchWorker<E> extends Thread {
		private volatile boolean running = true;

		AsynchWorker() {
			super("AsynchAppenderWorkerThread");
			setDaemon(true);
		}

		void stopWorker() {
			running = false;
		}

		@Override
		public void run() {
			AsynchEvent<E> e;
			while (running) {
				e = null;
				try {
					e = null;
					e = logQ.take();
					if (e != null) e.appender.subAppendImpl(e.event);
				} catch (InterruptedException t) {
					running = false;
				} catch (Throwable t) {
					t.printStackTrace();
					if (e != null) e.appender.addError("Asynch handler fails for " + e.event, t);
				}
			}
		}
	}
}

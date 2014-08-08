/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gflogger;

import java.util.concurrent.atomic.AtomicBoolean;

import org.gflogger.helpers.LogLog;


/**
 * AbstractEntryHandler
 *
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public abstract class AbstractEntryHandler<T extends AbstractLoggerServiceImpl> {

	protected final AtomicBoolean running = new AtomicBoolean();

	protected final T service;

	protected final Appender[] appenders;

	public AbstractEntryHandler(T service, Appender[] appenders) {
		this.service = service;
		this.appenders = appenders;
	}

	protected void workerIsAboutToFinish() {
		for (int i = 0; i < appenders.length; i++) {
			try {
				appenders[i].workerIsAboutToFinish();
			} catch (Throwable e){
				LogLog.error("unhanled exception at " + Thread.currentThread().getName() +  " in " + appenders[i].getClass().getName() + ".workerIsAboutToFinish: " + e.getMessage() , e);
			}
		}
	}

	protected final void flushBuffer() {
		flushBuffer(true);
	}

	protected void flushBuffer(boolean force) {
		for(int i = 0; i < appenders.length; i++){
			appenders[i].flush(force);
		}
	}

	protected final void process(LogEntryItemImpl entry) {
		//if (!running.get()) return;

		long mask = entry.getAppenderMask();
		int idx = 0;
		while(mask != 0L){
			if ((mask & 1L) != 0L){
				try {
					appenders[idx].process(entry);
				} catch (Throwable e){
					appenders[idx].onUncatchException(e);
				}
			}
			idx++;
			mask >>= 1;
		}
	}

	public void start() {
		if (running.getAndSet(true)) throw new IllegalStateException();

		for(int i = 0; i < appenders.length; i++){
			LogLog.debug("going to start appender " + appenders[i].getName());
			appenders[i].start();
		}
	}

	public void stop(){
		if (!running.getAndSet(false)) return;

		service.running = false;

		for(int i = 0; i < appenders.length; i++){
			LogLog.debug("going to stop appender " + appenders[i].getName());
			appenders[i].stop();
		}
	}

}

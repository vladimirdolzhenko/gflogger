/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *	  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gflogger.disruptor;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.gflogger.disruptor.appender.DAppender;
import org.gflogger.helpers.LogLog;

import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.LifecycleAware;

/**
 *
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public final class EntryHandler implements EventHandler<DLogEntryItem>, LifecycleAware {

	protected final AtomicBoolean running = new AtomicBoolean();

	protected final DAppender[] appenders;

	private final AtomicLong timer = new AtomicLong();

	public EntryHandler(DAppender[] appenders) {
		this.appenders = appenders;
	}

	protected final void flushBuffer() {
		for(int i = 0; i < appenders.length; i++){
			appenders[i].flush();
		}
	}

	@Override
	public void onStart() {
		if (running.getAndSet(true)) throw new IllegalStateException();

		for(int i = 0 ; i < appenders.length; i++){
			try {
				appenders[i].onStart();
			} catch (Throwable e){
				// TODO:
			}
		}
	}

	@Override
	public void onShutdown() {
		if (!running.getAndSet(false)) return;

		flushBuffer();

		for(int i = 0 ; i < appenders.length; i++){
			try {
				LogLog.debug("going to stop appender " + appenders[i].getName());
				appenders[i].onShutdown();
			} catch (Throwable e){
				// TODO:
			}
		}

		LogLog.debug("total appender time:" + (timer.get() / 1000) / 1e3 + " ms");
	}

	@Override
	public void onEvent(DLogEntryItem event, long sequence, boolean endOfBatch)
			throws Exception {
		if (!running.get()) return;

		final long start = System.nanoTime();

		long mask = event.getAppenderMask();
		int idx = 0;
		while(mask != 0L){
			if ((mask & 1L) != 0L){
				try {
					appenders[idx].onEvent(event, sequence, endOfBatch);
				} catch (Throwable e){
					// TODO:
				}
			}
			idx++;
			mask >>= 1;
		}

		final long end = System.nanoTime();

		timer.addAndGet(end - start);
	}
}

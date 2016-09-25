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

import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.LifecycleAware;

import org.gflogger.AbstractEntryHandler;
import org.gflogger.Appender;
import org.gflogger.LogEntryItemImpl;

/**
 *
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public final class EntryHandler extends AbstractEntryHandler<LoggerServiceImpl>
		implements EventHandler<LogEntryItemImpl>, LifecycleAware {

	public EntryHandler(LoggerServiceImpl service, Appender[] appenders) {
		super(service, appenders);
	}

	@Override
	public void onStart() {
		start();
	}

	@Override
	public void onShutdown() {
		stop();
	}

	@Override
	public void onEvent(LogEntryItemImpl event, long sequence, boolean endOfBatch)
			throws Exception {
		process(event);
	}
}

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
package org.gflogger.config.xml;


import java.util.ArrayList;
import java.util.List;

import org.gflogger.GFLogger;
import org.gflogger.GFLoggerImpl;
import org.gflogger.LogLevel;
import org.gflogger.appender.AppenderFactory;


/**
 *
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public class GFLoggerBuilder {

	private LogLevel logLevel = LogLevel.FATAL;
	private String name;
	private boolean additivity = false;
	private final List<AppenderFactory> appenderFactories =
		new ArrayList<AppenderFactory>();

	public LogLevel getLogLevel() {
		return logLevel;
	}

	public void setLogLevel(LogLevel logLevel) {
		this.logLevel = logLevel;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isAdditivity() {
		return additivity;
	}

	public void setAdditivity(boolean additivity) {
		this.additivity = additivity;
	}

	public void addAppenderFactory(final AppenderFactory appender){
		this.appenderFactories.add(appender);
	}

	public GFLogger build(){
		return new GFLoggerImpl(logLevel, name, additivity,
			appenderFactories.toArray(new AppenderFactory[appenderFactories.size()]));
	}
}

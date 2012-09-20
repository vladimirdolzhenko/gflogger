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

package org.gflogger.config.xml;

import org.gflogger.GFLoggerBuilder;
import org.gflogger.LoggerService;
import org.gflogger.ObjectFormatter;
import org.gflogger.appender.AppenderFactory;


/**
 *
 * @author Harald Wendel
 */
public interface LoggerServiceFactory {

	LoggerService createService();

	void setCount(int count);

	void setMaxMessageSize(int maxMessageSize);

	void addAppenderFactory(AppenderFactory factory);

	void addGFLoggerBuilder(GFLoggerBuilder logger);

	void addObjectFormatter(Class clazz, ObjectFormatter objectFormatter);
}
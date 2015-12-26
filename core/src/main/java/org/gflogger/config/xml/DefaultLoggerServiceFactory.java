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
import org.gflogger.appender.AppenderFactory;
import org.gflogger.base.LoggerServiceImpl;


/**
 *
 * @author Harald Wendel
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public class DefaultLoggerServiceFactory extends AbstractLoggerServiceFactory {

	@Override
	public LoggerService createService() {
		return new LoggerServiceImpl(
			count,
			maxMessageSize,
			objectFormatterFactory,
			loggersBuilders.toArray(new GFLoggerBuilder[loggersBuilders.size()]),
			appenderFactories.toArray(new AppenderFactory[appenderFactories.size()])
		);
	}

}
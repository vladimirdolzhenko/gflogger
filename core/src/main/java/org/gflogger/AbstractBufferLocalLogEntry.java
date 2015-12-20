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

import org.gflogger.formatter.BufferFormatter;

import java.nio.ByteBuffer;

import static org.gflogger.helpers.OptionConverter.getStringProperty;


/**
 * AbstractBufferLocalLogEntry
 *
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
abstract class AbstractBufferLocalLogEntry extends AbstractLocalLogEntry {

	protected final ByteBuffer byteBuffer;

	public AbstractBufferLocalLogEntry(final Thread owner,
			final ObjectFormatterFactory formatterFactory,
			final LoggerService loggerService,
			final ByteBuffer byteBuffer,
			final FormattingStrategy strategy) {
		this(owner, formatterFactory,
			loggerService, getStringProperty("gflogger.errorMessage", ">>TRNCTD>>"), byteBuffer,strategy);
	}

	public AbstractBufferLocalLogEntry(final Thread owner,
			final ObjectFormatterFactory formatterFactory,
			final LoggerService loggerService,
			final String logErrorsMessage,
			final ByteBuffer byteBuffer,
			final FormattingStrategy strategy) {
		super(owner, formatterFactory, loggerService, logErrorsMessage, strategy);
		this.byteBuffer = byteBuffer;

		// there is no reason to register in Cleaner as direct byte buffer registers in it by its own
		// Cleaner.create(this, new BufferPurger(this.byteBuffer));
	}

	protected static class BufferPurger implements Runnable {
		private final ByteBuffer buffer;

		public BufferPurger(ByteBuffer buffer) {
			this.buffer = buffer;
		}

		@Override
		public void run() {
			BufferFormatter.purge(buffer);
		}

	}
}

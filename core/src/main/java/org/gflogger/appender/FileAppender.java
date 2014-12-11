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

package org.gflogger.appender;


import org.gflogger.Layout;
import org.gflogger.LogLevel;
import org.gflogger.helpers.LogLog;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * FileAppender
 *
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public class FileAppender extends AbstractAsyncAppender {

	private static final int DEFAULT_BUFFER_SIZE = 1 << 20/*=1M*/;
	private static final String DUMMY_NAME = null;

	protected String fileName;
	protected FileChannel channel;

	protected boolean append = true;

	public FileAppender(final boolean multibyte,
	                    final LogLevel logLevel,
	                    final boolean enabled ) {
		this(DEFAULT_BUFFER_SIZE, multibyte, logLevel, enabled);
	}

	public FileAppender(final int bufferSize,
	                    final boolean multibyte,
	                    final LogLevel logLevel,
	                    final boolean enabled ) {
		super(DUMMY_NAME,bufferSize, multibyte, logLevel, enabled);
		immediateFlush = false;
	}

	public FileAppender(final Layout layout,
	                    final String filename,
	                    final boolean multibyte,
	                    final LogLevel logLevel,
	                    final boolean enabled ) {
		this(DEFAULT_BUFFER_SIZE, layout, filename, multibyte, logLevel, enabled);
	}

	public FileAppender(final int bufferSize,
	                    final Layout layout,
	                    final String filename,
	                    final boolean multibyte,
	                    final LogLevel logLevel,
	                    final boolean enabled ) {
		this(bufferSize, multibyte, logLevel, enabled);
		this.setLayout(layout);
		this.fileName = filename;
	}

	public synchronized void setCodepage(final String codepage) {
		//this.codepage = codepage;
		if (buffer instanceof CharBufferImpl){
			CharBufferImpl charBuffer = (CharBufferImpl)buffer;
			charBuffer.setCodepage(codepage);
		}
	}

	public synchronized void setFileName(final String fileName) {
		this.fileName = fileName;
	}

	public void setAppend(final boolean append) {
		this.append = append;
	}

	@Override
	public void flush(boolean force) {
		if (!(force || immediateFlush)) return;
		store("flushCharBuffer");
	}

	@Override
	protected void workerIsAboutToFinish() {
		store("workerIsAboutFinish");
		closeFile();
	}

	@Override
	public void start() {
		try {
			createFileChannel();
		} catch (final FileNotFoundException e) {
			throw new RuntimeException(e.getMessage(), e);
		}

		super.start();
	}

	protected void createFileChannel() throws FileNotFoundException {
		final File file = new File(fileName);
		final File folder = file.getParentFile();
		if(!folder.exists()){
			if(!folder.mkdirs()){
				throw new FileNotFoundException("Can't create folder " + folder.getAbsolutePath());
			}
		}
		final FileOutputStream fout = new FileOutputStream(file, append);
		channel = fout.getChannel();
	}

	protected void closeFile() {
		try {
			channel.force(true);
			channel.close();
		} catch (IOException e) {
			LogLog.error("[" + Thread.currentThread().getName() +
				"] exception at " + getName() + " - " + e.getMessage(), e);
		}
	}

	protected boolean store(final String cause) {
		final ByteBuffer byteBuffer = buffer.getBuffer();

		if (byteBuffer.position() == 0) return false;
		byteBuffer.flip();
		try {
			/*/
			final int limit = buffer.limit();
			final long start = System.nanoTime();
			channel.write(buffer);
			final long end = System.nanoTime();

			final String msg = "[" + Thread.currentThread().getName() + "] " + getName() +
				" " + cause + ":" + limit + " bytes stored in " +
				((end - start) / 1000 / 1e3) + " ms";
			LogLog.debug(msg);
			/*/
			channel.write(byteBuffer);
			//*/
		} catch (final IOException e) {
			LogLog.error("[" + Thread.currentThread().getName() +
				"] exception at " + getName() + " - " + e.getMessage(), e);
		} finally {
			byteBuffer.clear();
		}
		return true;
	}


	public String getName() {
		//forced to overwrite since fileName could be dynamic
		return "file:" + fileName;
	}
}

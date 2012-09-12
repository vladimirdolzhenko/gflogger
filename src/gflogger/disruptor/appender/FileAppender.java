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

package gflogger.disruptor.appender;

import gflogger.Layout;
import gflogger.helpers.LogLog;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;

/**
 * FileAppender
 *
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public class FileAppender extends AbstractAsyncAppender {

	protected CharsetEncoder encoder;

	protected String fileName;
	protected String codepage = "UTF-8";

	protected FileChannel channel;

	protected boolean append = true;

	protected int maxBytesPerChar;

	public FileAppender() {
		// 1M
		this(1 << 20);
	}

	public FileAppender(int bufferSize) {
		this(bufferSize, false);
	}

	public FileAppender(int bufferSize, boolean multibyte) {
		super(bufferSize, multibyte);
		immediateFlush = false;
	}

	public FileAppender(Layout layout, String filename) {
		this(1 << 20, layout, filename);
	}

	public FileAppender(int bufferSize, Layout layout, String filename) {
		this(bufferSize);
		this.layout = layout;
		this.fileName = filename;
	}

	public synchronized void setCodepage(final String codepage) {
		this.codepage = codepage;
	}

	public synchronized void setFileName(final String fileName) {
		this.fileName = fileName;
	}

	public void setAppend(final boolean append) {
		this.append = append;
	}

	@Override
	protected void processCharBuffer() {
		CoderResult result;
		charBuffer.flip();
		do{
			result = encoder.encode(charBuffer, byteBuffer, true);
			if (result.isOverflow()){
				store("result.isOverflow()");
			}
		} while(result.isOverflow());
		charBuffer.clear();
	}

	@Override
	protected void flushBuffer() {
		store("flushCharBuffer");
	}

	protected boolean store(final String cause) {
		if (byteBuffer.position() == 0) return false;
		byteBuffer.flip();
		try {
			/*/
			final int limit = byteBuffer.limit();
			final long start = System.nanoTime();
			channel.write(byteBuffer);
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

	@Override
	public String getName() {
		return "file:" + fileName;
	}

	protected void createFileChannel() throws FileNotFoundException {
		final FileOutputStream fout = new FileOutputStream(fileName, append);
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

	@Override
	public void onStart() {
		try {
			encoder = multibyte ? Charset.forName(codepage).newEncoder() : null;
			maxBytesPerChar = multibyte ? (int) Math.floor(encoder.maxBytesPerChar()) : 1;
			createFileChannel();
		} catch (final FileNotFoundException e) {
			throw new RuntimeException(e.getMessage(), e);
		}

		super.onStart();
	}

	@Override
	public void onShutdown() {
		store("onShutdown");
		closeFile();
	}
}

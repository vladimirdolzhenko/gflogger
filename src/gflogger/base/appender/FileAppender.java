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

package gflogger.base.appender;

import static gflogger.formatter.BufferFormatter.allocate;

import gflogger.Layout;
import gflogger.base.LogEntryItemImpl;
import gflogger.base.RingBuffer;
import gflogger.helpers.LogLog;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;


public class FileAppender extends AbstractAsyncAppender {

	protected final ByteBuffer buffer;

	protected String fileName;
	protected String codepage = "UTF-8";

	protected CharsetEncoder encoder;
	protected FileChannel channel;

	protected boolean append = true;

	protected int maxBytesPerChar;

	public FileAppender() {
		// 1M
		this(1 << 20);
	}
	
	public FileAppender(int bufferSize) {
		// unicode char is 4 bytes 
		super(bufferSize << 2);
		buffer = allocate(bufferSize);
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
		final int remaining = buffer.remaining();
		final int sizeOfBuffer = maxBytesPerChar * charBuffer.position();
		
		// store buffer if there it could be no enough space for message
		if (remaining < sizeOfBuffer){
			store("remaining < sizeOfBuffer");
		}

		CoderResult result;
		charBuffer.flip();
		do{
			result = encoder.encode(charBuffer, buffer, true);
			//*/
			if (result.isOverflow()){
				store("result.isOverflow()");
			}
			/*/
			store("force");
			//*/
		} while(result.isOverflow());
		charBuffer.clear();
	}

	@Override
	protected void flushCharBuffer() {
		store("flushCharBuffer");
	}

	@Override
	protected void workerIsAboutToFinish() {
		store("workerIsAboutFinish");
		closeFile();
	}

	@Override
	public void start(RingBuffer<LogEntryItemImpl> ringBuffer) {
		try {
			encoder = Charset.forName(codepage).newEncoder();
			createFileChannel();
		} catch (final FileNotFoundException e) {
			throw new RuntimeException(e.getMessage(), e);
		}

		maxBytesPerChar = (int) Math.floor(encoder.maxBytesPerChar());

		super.start(ringBuffer);
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
				"] exception at " + name() + " - " + e.getMessage(), e);
		}
	}

	protected boolean store(final String cause) {
		if (buffer.position() == 0) return false;
		buffer.flip();
		try {
//			final int limit = buffer.limit();
//			final long start = System.nanoTime();
			channel.write(buffer);
//			final long end = System.nanoTime();

			//LogLog.debug(cause + ":" + limit + " bytes stored in " + ((end - start) / 1000 / 1e3) + " ms");
		} catch (final IOException e) {
			LogLog.error("[" + Thread.currentThread().getName() +  
				"] exception at " + name() + " - " + e.getMessage(), e);
		} finally {
			buffer.clear();
		}
		return true;
	}

	@Override
	protected String name() {
		return "file";
	}
}

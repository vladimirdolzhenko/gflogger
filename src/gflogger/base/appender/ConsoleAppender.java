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

import java.io.PrintStream;

public class ConsoleAppender extends AbstractAsyncAppender implements Runnable {

	private final PrintStream out;
	
	public ConsoleAppender() {
		this(System.out);
	}
	
	public ConsoleAppender(final int sizeOfBuffer) {
		this(sizeOfBuffer, System.out);
	}
	
	public ConsoleAppender(final PrintStream out) {
		this.out = out;
	}
	
	public ConsoleAppender(final int sizeOfBuffer, final PrintStream out) {
		super(sizeOfBuffer);
		this.out = out;
	}
	
	@Override
	protected void processCharBuffer() {
		flushCharBuffer();
	}

	@Override
	protected void flushCharBuffer() {
		if (charBuffer.position() > 0){
			charBuffer.flip();
			//*/
			while(charBuffer.hasRemaining()){
				out.append(charBuffer.get());
			}
			//*/
			//*/
			out.flush();
			charBuffer.clear();
		}
	}

	@Override
	protected String name() {
		return "console";
	}
}

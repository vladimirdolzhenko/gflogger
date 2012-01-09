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

package gflogger.ring;

import java.util.concurrent.TimeUnit;

/**
 * Strategy employed for making {@link EventProcessor}s wait on a cursor {@link Sequence}.
 */
public interface WaitStrategy {

	/**
     * Signal those {@link EventProcessor}s waiting that the cursor has advanced.
     */
	void signallAll();

	/**
	 * Wait for the given sequence to be available
	 * 
	 * @param cursor
	 * @param seqNum
	 * @return
	 * @throws InterruptedException
	 */
	long waitFor(Sequence cursor, long seqNum) throws InterruptedException;

	/**
	 * Wait for the given sequence to be available with a timeout specified.
	 * 
	 * @param cursor
	 * @param seqNum
	 * @param timeout
	 * @param unit
	 * @return
	 * @throws InterruptedException
	 */
	long waitFor(Sequence cursor, long seqNum, long timeout, TimeUnit unit) throws InterruptedException;

}

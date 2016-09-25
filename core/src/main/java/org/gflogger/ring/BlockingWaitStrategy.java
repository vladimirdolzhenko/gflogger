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

package org.gflogger.ring;

import java.util.concurrent.TimeUnit;

/**
 * BlockingWaitStrategy
 */
public class BlockingWaitStrategy implements WaitStrategy {

	private final Object lock;
	
	private boolean signalled;
	private volatile int waiters;

	public BlockingWaitStrategy() {
		this.lock = new Object();
	}

	@Override
	public void signallAll() {
		if (waiters != 0) {
			synchronized (lock) {
				signalled = true;
				lock.notifyAll();
			}
		}
	}

	@Override
	public long waitFor(final Sequence cursor, final long seqNum)
	throws InterruptedException {
		long availableSequence;
		if ((availableSequence = cursor.get()) < seqNum) {
			synchronized (lock) {
				signalled = false;
				try {
					++waiters;
					while ((availableSequence = cursor.get()) < seqNum) {
						lock.wait();
					}
				} finally {
					--waiters;
				}
			}
		}
		return availableSequence;
	}

	@Override
	public long waitFor(
		final Sequence cursor,
		final long seqNum,
		final long timeout,
		final TimeUnit unit
	) throws InterruptedException {
		long availableSequence;
		if ((availableSequence = cursor.get()) < seqNum) {
			final long timeoutMs = unit.toMillis(timeout);
			final long startTime = System.currentTimeMillis() ;

			synchronized (lock) {
				signalled = false;
				try {
					++waiters;
					while ((availableSequence = cursor.get()) < seqNum) {
						lock.wait(timeoutMs);
						
						if (!signalled || (System.currentTimeMillis() - startTime) > timeoutMs) break;
					}
				} finally {
					--waiters;
				}
			}
		}
		return availableSequence;
	}

}

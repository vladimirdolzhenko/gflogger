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

import static org.gflogger.ring.AlertException.ALERT_EXCEPTION;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * RingBuffer in-place implementation of disruptor.
 *
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public final class RingBuffer<T extends Publishable> {

	public static final long INITIAL_CURSOR_VALUE = -1L;

	public static final int SPIN_TRIES = 1000;

	private final T[] entries;

	// provides quick mod, mask is like 000111111
	private final int mask;

	private final PaddedAtomicLong sequence = new PaddedAtomicLong(INITIAL_CURSOR_VALUE);

	private final ThreadLocal<MutableLong> minSequence = new ThreadLocal<MutableLong>(){
		@Override
		protected MutableLong initialValue() {
			return new MutableLong(INITIAL_CURSOR_VALUE);
		}
	};

	private final EntryProcessor[] entryProcessors;

	private final AtomicBoolean running = new AtomicBoolean();

	public RingBuffer(final T[] entries, final EntryProcessor ... entryProcessors) {
		// quick check is count = 2^k ?
		if ((entries.length & entries.length - 1) != 0)
			throw new IllegalArgumentException("number of entries should be power of 2");
		this.entries = entries;
		this.mask = entries.length - 1;

		this.entryProcessors = entryProcessors;
		for (int i = 0; i < entryProcessors.length; i++) {
			if (entryProcessors[i] instanceof RingBufferAware){
				RingBufferAware bufferAware = (RingBufferAware) entryProcessors[i];
				bufferAware.setRingBuffer(this);
			}
		}
		running.set(true);
	}

	private long getMinSeqNum() {
		long minimum = entryProcessors[0].getSequence();
		if (entryProcessors.length > 1){
			for (int i = 1; i < entryProcessors.length; i++) {
				minimum = Math.min(minimum, entryProcessors[i].getSequence());
			}
		}
		return minimum;
	}

	public long next() {
		final long nextSeqNum = sequence.incrementAndGet();
		claimSequence(nextSeqNum);
		return nextSeqNum;
	}

	private void claimSequence(final long seqNum) {
		final long wrapPoint = seqNum - entries.length;
		final MutableLong minSeq = minSequence.get();
		if (wrapPoint > minSeq.get()) {
			long minSeqNum;
			while (wrapPoint > (minSeqNum = getMinSeqNum())) {
				Thread.yield();
			}
			minSeq.set(minSeqNum);
		}
	}

	public void publish(final long sequence) {
		entries[(int) (sequence & mask)].setPublished(true);
		signallAll();
	}

	public long waitFor(final long seqNum) throws InterruptedException {
		final int idx = (int) (seqNum & mask);
		boolean published = entries[idx].isPublished();
		long availableSequence = published ? seqNum : seqNum - 1;
		if (!published){

			synchronized (lock) {
				signalled = false;
				try {
					++waiters;
					while (!(published = entries[idx].isPublished())) {
						if (!running.get()) throw ALERT_EXCEPTION;

						lock.wait();

						if (!signalled) break;
					}
				} finally {
					--waiters;
				}
			}
		}
		if (published){
			availableSequence = seqNum;
			for(long i = seqNum, e = seqNum + entries.length; i < e; i++){
				if (!entries[(int) (i & mask)].isPublished()){
					availableSequence = i - 1;
					break;
				}
			}
		}
		return availableSequence;
	}

	public long waitFor(final long seqNum, final long timeout, final TimeUnit unit) throws InterruptedException {
		final int idx = (int) (seqNum & mask);
		boolean published = entries[idx].isPublished();
		long availableSequence = published ? seqNum : seqNum - 1;
		if (!published){
			final long timeoutMs = unit.toMillis(timeout);
			final long startTime = System.currentTimeMillis() ;

			synchronized (lock) {
				signalled = false;
				try {
					++waiters;
					while (!(published = entries[idx].isPublished())) {
						if (!running.get()) throw ALERT_EXCEPTION;

						lock.wait(timeoutMs);

						if (!signalled || (System.currentTimeMillis() - startTime) > timeoutMs) break;
					}
				} finally {
					--waiters;
				}
			}
		}
		if (published){
			availableSequence = seqNum;
			for(long i = seqNum, e = seqNum + entries.length; i < e; i++){
				if (!entries[(int) (i & mask)].isPublished()){
					availableSequence = i - 1;
					break;
				}
			}
		}
		return availableSequence;
	}

	public T get(final long index){
		return entries[(int) (index & mask)];
	}

	public int size(){
		return entries.length;
	}

	public void stop(){
		if (running.getAndSet(false)){
			signallAll();
		}
	}

	private final Object lock  = new Object();

	private boolean signalled;
	private volatile int waiters;


	private void signallAll() {
		if (waiters != 0) {
			synchronized (lock) {
				signalled = true;
				lock.notifyAll();
			}
		}
	}
}

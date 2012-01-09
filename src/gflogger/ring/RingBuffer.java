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
 * RingBuffer in-place implementation of disruptor.
 * 
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public final class RingBuffer<T> {

	public static final long INITIAL_CURSOR_VALUE = -1L;
	
	public static final int SPIN_TRIES = 1000;

	private final T[] entries;

	// provides quick mod, mask is like 000111111
	private final int mask;

	private final WaitStrategy waitStrategy;

	private final Sequence cursor = new Sequence(INITIAL_CURSOR_VALUE);

	private final PaddedAtomicLong sequence = new PaddedAtomicLong(INITIAL_CURSOR_VALUE);

	private final PaddedAtomicLong minSequence = new PaddedAtomicLong(INITIAL_CURSOR_VALUE);

	private EntryProcessor[] entryProcessors;

	public RingBuffer(final WaitStrategy waitStrategy, final T ... entries) {
		// quick check is count = 2^k ?
		if ((entries.length & entries.length - 1) != 0)
			throw new IllegalArgumentException("number of entries should be power of 2");
		this.entries = entries;
		this.mask = entries.length - 1;
		this.waitStrategy = waitStrategy;
	}

	public WaitStrategy getWaitStrategy() {
		return waitStrategy;
	}

	public long getCursor() {
		return cursor.get();
	}

	public int getFreeEntries() {
		return entries.length - getUsedEntries();
	}

	public int getUsedEntries() {
		return (int)(getCursor() - minSeqNum(entryProcessors));
	}

	public long getMinSeqNum() {
		return minSeqNum(entryProcessors);
	}

	public void setEntryProcessors(final EntryProcessor... entryProcessors) {
		this.entryProcessors = entryProcessors;
		for (int i = 0; i < entryProcessors.length; i++) {
	        if (entryProcessors[i] instanceof RingBufferAware){
	        	RingBufferAware bufferAware = (RingBufferAware) entryProcessors[i];
	        	bufferAware.setRingBuffer(this);
	        }
        }
	}

	public long next() {
		final long nextSeqNum = sequence.incrementAndGet();
		claimSequence(nextSeqNum);
		return nextSeqNum;
	}

	private void claimSequence(final long seqNum) {
		final long wrapPoint = seqNum - entries.length;
		if (wrapPoint > minSequence.get()) {
			long minSeqNum;
			while (wrapPoint > (minSeqNum = minSeqNum(entryProcessors))) {
				Thread.yield();
			}
			minSequence.set(minSeqNum);
		}
	}

	private void serialisePublishing(final Sequence cursor, final long sequence) {
		final long expectedSequence = sequence - 1;
		int counter = SPIN_TRIES;
		while (expectedSequence != cursor.get()) {
			if (0 == --counter) {
				counter = SPIN_TRIES;
				Thread.yield();
			}
		}
	}

	public void publish(final long sequence) {
		serialisePublishing(cursor, sequence);
		cursor.set(sequence);
		waitStrategy.signallAll();
	}

	public long waitFor(final long seqNum) throws InterruptedException {
		return waitStrategy.waitFor(cursor, seqNum);
	}

	public long waitFor(final long seqNum, final long timeout, final TimeUnit unit) throws InterruptedException {
		return waitStrategy.waitFor(cursor, seqNum, timeout, unit);
	}

	private static long minSeqNum(final EntryProcessor[] processors) {
		long minimum = Long.MAX_VALUE;
		for (final EntryProcessor p : processors) {
			minimum = Math.min(minimum, p.getSequence());
		}
		return minimum;
	}


	public T get(final long index){
		return entries[(int) (index & mask)];
	}

	public int size(){
		return entries.length;
	}
}

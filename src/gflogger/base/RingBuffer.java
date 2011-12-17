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

package gflogger.base;

/**
 * RingBuffer
 * 
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public final class RingBuffer<T> {

	private final T[] array;
	
 // to provide quick mod - mask is like 000111111
	private final int mask;
	
	public RingBuffer(final T[] array) {
	 // quick check is count = 2^k ?
		if ((array.length & (array.length - 1)) != 0){
			throw new IllegalArgumentException("array length should be power of 2");
		}
		this.array = array;
		this.mask = (array.length - 1);
	}
	
	public T get(final long index){
		return array[(int) (index & mask)];
	}
	
	public int mask() {
		return this.mask;
	}
	
	public int size(){
		return array.length;
	}
}

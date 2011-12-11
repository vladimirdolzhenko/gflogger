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

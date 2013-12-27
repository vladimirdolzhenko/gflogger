package org.gflogger.base.ring;


/**
 * RingBufferTest
 *
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public class RingBufferTest {

//	@Test
//	public void testPublish() throws Exception {
//		RingBuffer<MutableLong> ringBuffer =
//			new RingBuffer<MutableLong>(new BlockingWaitStrategy(),
//				new MutableLong[]{
//					new MutableLong(-1),
//					new MutableLong(-1),
//				});
//
//		assertEquals(RingBuffer.INITIAL_CURSOR_VALUE, ringBuffer.getCursor());
//
//		final long next = ringBuffer.next();
//		final MutableLong mutableLong = ringBuffer.get(next);
//		mutableLong.set(0);
//
//		ringBuffer.publish(next);
//
//		assertEquals(RingBuffer.INITIAL_CURSOR_VALUE + 1, ringBuffer.getCursor());
//	}
//
//	@Test
//	public void testWaitForTimeout() throws Exception {
//		RingBuffer<MutableLong> ringBuffer =
//			new RingBuffer<MutableLong>(new BlockingWaitStrategy(),
//					new MutableLong[]{
//					new MutableLong(-1),
//					new MutableLong(-1),
//			});
//
//		final int timeout = 500;
//
//		final long time0 = System.currentTimeMillis();
//		assertEquals(RingBuffer.INITIAL_CURSOR_VALUE,
//			ringBuffer.waitFor(RingBuffer.INITIAL_CURSOR_VALUE + 1, timeout, TimeUnit.MILLISECONDS));
//		final long time1 = System.currentTimeMillis();
//
//		long diff = (time1 - time0);
//		diff += diff / 10;
//
//		assertTrue(diff > timeout);
//
//		final long next = ringBuffer.next();
//		final MutableLong mutableLong = ringBuffer.get(next);
//		mutableLong.set(0);
//
//		ringBuffer.publish(next);
//
//		assertEquals(RingBuffer.INITIAL_CURSOR_VALUE + 1, ringBuffer.getCursor());
//	}
//
//	@Test
//	public void testWaitForTimeout2() throws Exception {
//		final RingBuffer<MutableLong> ringBuffer =
//			new RingBuffer<MutableLong>(new BlockingWaitStrategy(),
//					new MutableLong[]{
//				new MutableLong(-1),
//				new MutableLong(-1),
//			});
//
//		final int timeout = 500;
//
//		final CountDownLatch latch1 = new CountDownLatch(1);
//
//		assertEquals(RingBuffer.INITIAL_CURSOR_VALUE, ringBuffer.getCursor());
//
//		new Thread(new Runnable() {
//
//			@Override
//			public void run() {
//				latch1.countDown();
//
//				final long next = ringBuffer.next();
//				final MutableLong mutableLong = ringBuffer.get(next);
//				mutableLong.set(0);
//
//				ringBuffer.publish(next);
//			}
//		}).start();
//
//		latch1.await();
//
//		final long time0 = System.currentTimeMillis();
//		assertEquals(RingBuffer.INITIAL_CURSOR_VALUE + 1,
//				ringBuffer.waitFor(RingBuffer.INITIAL_CURSOR_VALUE + 1, timeout, TimeUnit.MILLISECONDS));
//		final long time1 = System.currentTimeMillis();
//
//		long diff = (time1 - time0);
//		diff += diff / 10;
//
//		assertTrue(diff < timeout);
//	}
}

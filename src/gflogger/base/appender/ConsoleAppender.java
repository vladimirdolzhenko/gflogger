package gflogger.base.appender;

public class ConsoleAppender extends AbstractAsyncAppender implements Runnable {

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
				System.out.append(charBuffer.get());
			}
			//*/
			//*/
			System.out.flush();
			charBuffer.clear();
		}
	}

	@Override
	protected String name() {
		return "console";
	}
}

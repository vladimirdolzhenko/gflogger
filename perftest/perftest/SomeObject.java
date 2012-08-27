package perftest;

public class SomeObject {

	private final long value;

	public SomeObject(final long value){
		this.value = value;
	}

	public long getValue() {
		return value;
	}

	@Override
	public String toString() {
		return "[toString] value:" + value;
	}
}

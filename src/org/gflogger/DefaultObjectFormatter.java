package org.gflogger;

/**
 * DefaultObjectFormatter
 *
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public final class DefaultObjectFormatter implements ObjectFormatter<Object> {

	public static final ObjectFormatter<Object> DEFAULT_OBJECT_FORMATTER =
		new DefaultObjectFormatter();

	private DefaultObjectFormatter() {
	}

	@Override
	public void append(Object obj, LogEntry entry) {
		if (obj != null){
			entry.append(obj.toString());
		} else {
			entry.append('n').append('u').append('l').append('l');
		}
	}

	@Override
	public String toString() {
		return "DefaultObjectFormatter";
	}
}

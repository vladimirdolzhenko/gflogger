package perftest;

import gflogger.LogEntry;
import gflogger.ObjectFormatter;

public class SomeObjectFormatter implements ObjectFormatter<SomeObject> {

	@Override
	public void append(SomeObject obj, LogEntry entry) {
		entry.append("[formatter] value:").append(obj.getValue());
	}

}

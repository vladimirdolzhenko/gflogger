package org.gflogger.perftest;

import org.gflogger.LogEntry;
import org.gflogger.ObjectFormatter;

public class SomeObjectFormatter implements ObjectFormatter<SomeObject> {

	@Override
	public void append(SomeObject obj, LogEntry entry) {
		entry.append("[formatter] value:").append(obj.getValue());
	}

}

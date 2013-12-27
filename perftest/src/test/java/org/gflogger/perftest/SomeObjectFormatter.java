package org.gflogger.perftest;

import org.gflogger.GFLogEntry;
import org.gflogger.ObjectFormatter;

public class SomeObjectFormatter implements ObjectFormatter<SomeObject> {

	@Override
	public void append(SomeObject obj, GFLogEntry entry) {
		entry.append("[formatter] value:").append(obj.getValue());
	}

}

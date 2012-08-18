package gflogger.disruptor;

import gflogger.AbstractTestLoggerService;
import gflogger.LoggerService;
import gflogger.appender.AppenderFactory;

/**
 * TestDefaultLoggerServiceImpl
 *
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
public class TestDLoggerServiceImpl extends AbstractTestLoggerService {

	@Override
	protected LoggerService createLoggerService(int maxMessageSize, AppenderFactory... factories) {
	    return new DLoggerServiceImpl(4, maxMessageSize, factories);
	}

}

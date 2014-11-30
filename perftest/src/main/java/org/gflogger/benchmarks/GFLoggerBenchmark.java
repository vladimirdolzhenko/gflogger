package org.gflogger.benchmarks;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.gflogger.*;
import org.gflogger.appender.AbstractAppenderFactory;
import org.gflogger.appender.AppenderFactory;
import org.gflogger.disruptor.LoggerServiceImpl;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import static org.gflogger.helpers.OptionConverter.getIntProperty;

/**
 * Benchmark of GFLoggerBenchmark with dummy writer: just in-memory byte buffer which
 * is cleared on flush. So, no real IO involved, but apart from it, all other parts are measured
 *
 * @author Ruslan Cheremin
 */
@BenchmarkMode( { Mode.AverageTime } )
@OutputTimeUnit( TimeUnit.NANOSECONDS )
@State( Scope.Group )
public class GFLoggerBenchmark {
	public static final int LENGTH_POW = Integer.getInteger( "length-pow", 14 );
	public static final int LENGTH = 1 << LENGTH_POW;
	/**
	 * It's CELLS-1 arguments + 1 cell for header -- to be consistent with previous
	 * benchmarks
	 */
	public static final int CELLS_PER_RECORD = Integer.getInteger( "cells-per-record", 8 );//8longs = 1 cache line


	public static final int MAX_MESSAGE_SIZE = getIntProperty( "gflogger.service.maxMessageSize", 1 << 7 );

	public static final int PAYLOAD = Integer.getInteger( "writer-backoff", 20 );

	public static final AtomicInteger ID_GENERATOR = new AtomicInteger( 1 );

	static {
		System.out.printf( "len=%d, record=%d, payload=%d\n", LENGTH, CELLS_PER_RECORD, PAYLOAD );
	}


	public LoggerService gfloggerService;
	public GFLog gflog;

	@Setup
	public void setup() throws Exception {
		gfloggerService = createLoggerImpl();

		GFLogFactory.init( gfloggerService );

		this.gflog = GFLogFactory.getLog( "com.db.fxpricing.Logger" );
	}

	@TearDown
	public void tearDown() throws Exception {
		GFLogFactory.stop();
	}

	@Setup( Level.Iteration )
	public void setupIteration() {
		//TODO set thread affinity
	}

	/*=============================================================================*/
	@Benchmark
	@Group( "payload" )
	@GroupThreads( 3 )//to be consistent with others
	public void payloadAlone() {
		Blackhole.consumeCPU( PAYLOAD );
	}


	/*=============================================================================*/
	@Benchmark
	@Group( "logAppendingAndPayload" )
	@GroupThreads( 3 )
	public void writeLogAppending( final ThreadState ts ) {
		final GFLogEntry entry = gflog.info();

		entry.append( ' ' ).append( 5d );
		final int count = CELLS_PER_RECORD - 1;
		for( int i = 1; i < count; i++ ) {
			entry.append( ' ' ).append( ( long ) i );
		}
		entry.commit();

		Blackhole.consumeCPU( PAYLOAD );
	}

	/*=============================================================================*/
	@Benchmark
	@Group( "logFormattingAndPayload" )
	@GroupThreads( 3 )
	public void writeRawMessage( final ThreadState ts ) {
		final FormattedGFLogEntry entry = gflog.info( ts.message ).with( 5d );
		final int count = CELLS_PER_RECORD - 1;
		for( int i = 1; i < count; i++ ) {
			entry.with( ( long ) i );
		}

		Blackhole.consumeCPU( PAYLOAD );
	}

	@State( Scope.Thread )
	public static class ThreadState {
		public final int id = ID_GENERATOR.incrementAndGet();

		public final String message;

		public ThreadState() {
			final StringBuilder sb = new StringBuilder();
			sb.append( id );
			//actually, it's CELLS-1 arguments + 1 cell for header -- to be consistent
			//with previous benchmarks
			for( int i = 1; i < CELLS_PER_RECORD; i++ ) {
				sb.append( " %s" );
			}
			message = sb.toString();
		}
	}


	protected AppenderFactory[] createAppenderFactories() {
//		final FileAppenderFactory fileAppender = new FileAppenderFactory();
//		fileAppender.setLogLevel( LogLevel.INFO );
//		fileAppender.setFileName( "log.log" );
//		fileAppender.setAppend( false );
//		fileAppender.setImmediateFlush( false );
//		fileAppender.setLayout( new PatternLayout( "%d{HH:mm:ss,SSS zzz} %p %m [%c{2}] [%t]%n" ) );

		return new AppenderFactory[] { new AbstractAppenderFactory<Appender>(){
			@Override
			public Appender createAppender( final Class<? extends LoggerService> aClass ) {
				return new Appender() {
					@Override
					public boolean isMultibyte() {
						return false;
					}

					@Override
					public boolean isEnabled() {
						return true;
					}

					@Override
					public LogLevel getLogLevel() {
						return LogLevel.DEBUG;
					}

					@Override
					public String getName() {
						return "fake";
					}

					@Override
					public void flush() {
					}

					@Override
					public void flush( final boolean b ) {
					}

					@Override
					public void process( final LogEntryItem logEntryItem ) {
					}

					@Override
					public void start() {
					}

					@Override
					public void stop() {
					}

					@Override
					public void onUncatchException(Throwable e) {
					}
				};
			}

			@Override
			public LogLevel getLogLevel() {
				return LogLevel.DEBUG;
			}

		} };
	}

	protected LoggerService createLoggerImpl() {
		final AppenderFactory[] factories = createAppenderFactories();
		final GFLoggerBuilder[] loggers = {
				new GFLoggerBuilder(
						LogLevel.INFO,
						"com.db",
						factories
				)
		};

		final int count = LENGTH / MAX_MESSAGE_SIZE;
		final LoggerService impl = new LoggerServiceImpl(
				count,
				MAX_MESSAGE_SIZE,
				loggers,
				factories
		);
		return impl;
	}

	public static void main(String[] args) throws Throwable {
		Options opts = new OptionsBuilder()
				.include(".*")
				.warmupIterations(10)
				.measurementIterations(10)
				.jvmArgs("-server")
				.forks(1)
				.build();

		new Runner(opts).run();
	}
}
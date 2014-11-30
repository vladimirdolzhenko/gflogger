package org.gflogger.benchmarks;

import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

import org.gflogger.formatter.BufferFormatter;
import org.openjdk.jmh.annotations.*;

/**
 * @author ruslan
 *         created 29/11/14 at 01:52
 */
@BenchmarkMode( { Mode.AverageTime } )
@OutputTimeUnit( TimeUnit.NANOSECONDS )
@State( Scope.Benchmark )
public class FormattingBenchmark {

	private ByteBuffer buffer;

	@Param( {
			        "0",
			        "1.0",
			        "100.001",
			        "123456789.123456789",
			        "123456789123456789.123456789123456789",
			        "123456789123456789123456789.123456789123456789123456789" }
	)
	private double doubleValue = 123456789123456789123456789.123456789123456789123456789;

	private long longValue;
	private int intValue;

	@Setup( Level.Trial )
	public void setup() {
		//I need long formatting here as baseline for double -- I'd expect double
		// formatting be ~ twice as heavy as long one.
		longValue = ( long ) doubleValue;
		intValue = ( int ) doubleValue;
		buffer = ByteBuffer.allocate( 50 );
	}

	@TearDown( Level.Invocation )
	public void cleanup() {
		buffer.clear();
	}

	@Benchmark
	public void formatDoubleFull() {
		BufferFormatter.append( buffer, doubleValue );
	}

	@Benchmark
	public void formatDoubleWith3Digits() {
		BufferFormatter.append( buffer, doubleValue, 3 );
	}


	@Benchmark
	public void formatDoubleWith10Digits() {
		BufferFormatter.append( buffer, doubleValue, 10 );
	}

	@Benchmark
	public void formatDoubleWith16Digits() {
		BufferFormatter.append( buffer, doubleValue, 16 );
	}

	@Benchmark
	public void formatLong() {
		BufferFormatter.append( buffer, longValue );
	}

	@Benchmark
	public void formatInt() {
		BufferFormatter.append( buffer, intValue );
	}

//	@Benchmark
//	public String formatLongToString() {
//		return Long.toString( longValue );
//	}
//
//	@Benchmark
//	public String formatDoubleToString() {
//		return Double.toString( doubleValue );
//	}


}

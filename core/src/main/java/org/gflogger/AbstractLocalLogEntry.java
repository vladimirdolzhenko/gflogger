/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gflogger;

import org.gflogger.helpers.LogLog;

import java.util.Iterator;

import static org.gflogger.helpers.OptionConverter.getStringProperty;
import static org.gflogger.util.StackTraceUtils.*;

/**
 * AbstractLocalLogEntry
 *
 * @author Vladimir Dolzhenko, vladimir.dolzhenko@gmail.com
 */
abstract class AbstractLocalLogEntry implements LocalLogEntry {

	protected String threadName;
	protected final LoggerService loggerService;
	protected final ObjectFormatterFactory	formatterFactory;
	protected final String logErrorsMessage;
	protected final FormattingStrategy strategy;

	protected String categoryName;
	protected LogLevel logLevel;
	protected long appenderMask;

	protected boolean commited = true;
	protected Throwable error;

	protected String pattern;
	protected int pPos;

	AbstractLocalLogEntry(
		final ObjectFormatterFactory formatterFactory,
		final LoggerService loggerService,
		final String logErrorsMessage,
		final FormattingStrategy strategy
	){
		this.formatterFactory = formatterFactory;
		this.loggerService = loggerService;
		this.logErrorsMessage = logErrorsMessage;
		this.strategy = strategy;
	}

	public AbstractLocalLogEntry(
		final Thread owner,
		final ObjectFormatterFactory formatterFactory,
		final LoggerService loggerService,
		final FormattingStrategy strategy
	) {
		this(owner, formatterFactory,
			loggerService, getStringProperty("gflogger.errorMessage", ">>TRNCTD>>"), strategy);
	}

	public AbstractLocalLogEntry(
		final Thread owner,
		final ObjectFormatterFactory formatterFactory,
		final LoggerService loggerService,
		final String logErrorsMessage,
		final FormattingStrategy strategy
	) {
		this.strategy = strategy;
		/*
		 * It have to be cached thread name at thread local variable cause
		 * thread.getName() generates new String(char[])
		 */
		this.threadName = owner.getName();
		this.formatterFactory = formatterFactory;
		this.loggerService = loggerService;
		this.logErrorsMessage = logErrorsMessage != null
				&& logErrorsMessage.length() > 0 ? logErrorsMessage : null;
	}

	@Override
	public LogLevel getLogLevel() {
		return logLevel;
	}

	@Override
	public void setLogLevel(final LogLevel logLevel) {
		this.logLevel = logLevel;
	}

	@Override
	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}

	@Override
	public String getCategoryName() {
		return categoryName;
	}

	@Override
	public String getThreadName() {
		return threadName;
	}

	@Override
	public boolean isCommited() {
		return this.commited;
	}

	@Override
	public boolean isPatternEnd() {
		return pPos == pattern.length();
	}

	@Override
	public void setCommited(boolean commited) {
		this.commited = commited;
	}

	@Override
	public long getAppenderMask() {
		return appenderMask;
	}

	@Override
	public void setAppenderMask(long appenderMask) {
		this.appenderMask = appenderMask;
	}

	@Override
	public Throwable getError() {
		return this.error;
	}

	@Override
	public void setPattern(String pattern) {
		if (pattern == null){
			throw new IllegalArgumentException("expected not null pattern.");
		}
		this.pattern = pattern;
		this.pPos = 0;
		appendNextPatternChunk();
	}

	protected void appendNextPatternChunk(){
		final int len = pattern.length();
		for(; pPos < len; pPos++){
			final char ch = pattern.charAt(pPos);
			if(strategy.isEscape(pattern, pPos)){
				append(pattern.charAt(pPos + 1));
				pPos++;
			} else if(strategy.isPlaceholder(pattern, pPos)){
				break;
			} else {
				append(ch);
			}
		}
		if (this.pPos == len && strategy.autocommitEnabled()){
			commit();
		}
	}

	protected void checkIfCommitted(){
		if (commited){
			throw new IllegalStateException("Entry has been committed.");
		}
	}

	protected void checkPlaceholder(){
		checkIfCommitted();
		if (pattern == null){
			throw new IllegalStateException("Entry has been committed.");
		}
		if (pPos + 1 >= pattern.length()){
			throw new IllegalStateException("Illegal pattern '" + pattern + "' or position " + pPos);
		}
		final char ch1 = pattern.charAt(pPos);
		final char ch2 = pattern.charAt(pPos + 1);
        if (!strategy.isPlaceholder(pattern, pPos)) {
            throw new IllegalArgumentException("Illegal pattern placeholder '" + ch1 + "" + ch2 + " at " + pPos);
		}
		pPos += 2;
	}

	protected void checkAndCommit(){
		if (commited) return;
		if (pPos + 1 < pattern.length()){
			throw new IllegalStateException("The pattern has not been finished. More parameters are required.");
		}
		commit();
	}

	protected void error(String msg, Throwable e){
		this.error = e;
		// there is insufficient space in this buffer
		if (logErrorsMessage == null) {
			LogLog.error(msg + ":" + e.getMessage(), e);
		} else {
			moveAndAppendSilent(logErrorsMessage);
		}
	}

	protected abstract void moveAndAppendSilent(String message);

	@Override
	public GFLogEntry append(Loggable loggable) {
		checkIfCommitted();
		if (loggable != null){
			try {
				loggable.appendTo(this);
			} catch(Throwable e){
				error("append(Loggable loggable)", e);
			}
		} else {
			append('n').append('u').append('l').append('l');
		}
		return this;
	}

	@Override
	public <T> GFLogEntry append(T[] array, String separator) {
		checkIfCommitted();
		if (array == null){
			append('n').append('u').append('l').append('l');
		} else {
			try {
				append('[');
				ObjectFormatter formatter = null;
				for(int i = 0; i < array.length; i++){
					if (i > 0){
						append(separator);
					}
					final T obj = array[i];
					if (obj != null){
						if (formatter == null) {
							formatter = formatterFactory.getObjectFormatter(obj);
						}
						formatter.append(obj, this);
					} else {
						append('n').append('u').append('l').append('l');
					}
				}
				append(']');
			} catch (Throwable e){
				error("append(Object o)", e);
			}
		}
		return this;
	}

	@Override
	public <T> GFLogEntry append(Iterable<T> iterable, String separator) {
		checkIfCommitted();
		if (iterable == null){
			append('n').append('u').append('l').append('l');
		} else {
			try {
				append('[');
				ObjectFormatter formatter = null;
				for(final Iterator<T> it = iterable.iterator();it.hasNext();){
					final T obj = it.next();
					if (obj != null){
						if (formatter == null) {
							formatter = formatterFactory.getObjectFormatter(obj);
						}
						formatter.append(obj, this);
					} else {
						append('n').append('u').append('l').append('l');
					}
					if (it.hasNext()){
						append(separator);
					}
				}
				append(']');
			} catch (Throwable e){
				error("append(Object o)", e);
			}
		}
		return this;
	}

	@Override
	public GFLogEntry append(Throwable e) {
		checkIfCommitted();
		if (e != null){
			try {
				append(e.getClass().getName());
				String message = e.getLocalizedMessage();
				if (message != null){
					append(": ").append(message);
				}
				append('\n');
				final StackTraceElement[] trace = e.getStackTrace();
				for (int i = 0; i < trace.length; i++) {
					append("\tat ").append(trace[i].getClassName()).append('.').
						append(trace[i].getMethodName());
					append('(');
					if (trace[i].isNativeMethod()){
						append("native)");
					} else {
						final String fileName = trace[i].getFileName();
						final int lineNumber = trace[i].getLineNumber();
						if (fileName != null){
							append(fileName);
							if (lineNumber >= 0){
								append(':').append(lineNumber);
							}
							append(')');
							final Class clazz =
								loadClass(trace[i].getClassName());
							if (clazz != null){
								append('[').append(getCodeLocation(clazz));
								final String implVersion = getImplementationVersion(clazz);
								if (implVersion != null){
									append(':').append(implVersion);
								}
								append(']');
							}

						} else {
							append("unknown");
						}
					}
					append('\n');
				}
				Throwable cause = e.getCause();
				if (cause != null) {
					append("\n caused by: \n");
					append(cause);
				}
			} catch (Throwable t){
				// there is insufficient space in this buffer
				LogLog.error("append(Throwable e):" + t.getMessage(), t);
			}
		}
		return this;
	}

	@Override
	public GFLogEntry append(double v) {
		checkIfCommitted();
		append(v, 10);
//		long    dBits = Double.doubleToLongBits( v );
//        long    fractBits;
//        int     binExp;
//        int     nSignificantBits;
//        boolean     isNegative;
//        int         decExponent;
//        int         nDigits;
//        int         bigIntExp;
//        int         bigIntNBits;
//
//        final long   signMask = 0x8000000000000000L;
//        final long   expMask  = 0x7ff0000000000000L;
//        final long   fractMask= ~(signMask|expMask);
//        final int    expShift = 52;
//        final int    expBias  = 1023;
//        final long   fractHOB = ( 1L<<expShift ); // assumed High-Order bit
//        final long   expOne   = ((long)expBias)<<expShift; // exponent of 1.0
//
//        // discover and delete sign
//        if ( (dBits&signMask) != 0 ){
//            isNegative = true;
//            dBits ^= signMask;
//        } else {
//            isNegative = false;
//        }
//        // Begin to unpack
//        // Discover obvious special cases of NaN and Infinity.?
//        binExp = (int)( (dBits&expMask) >> expShift );
//        fractBits = dBits&fractMask;
//        if ( binExp == (int)(expMask>>expShift) ) {
//            if ( fractBits == 0L ){
//            	if (isNegative) append('-');
//                append("Infinity");
//            } else {
//                append("NaN");
//                isNegative = false; // NaN has no sign!
//            }
//            return this;
//        }
//        if (isNegative) append('-');
////        // Finish unpacking
////        // Normalize denormalized numbers.
////        // Insert assumed high-order bit for normalized numbers.
////        // Subtract exponent bias.
//        if ( binExp == 0 ){
//            if ( fractBits == 0L ){
//                // not a denorm, just a 0!
//                decExponent = 0;
//                return append('0');
//            }
//            while ( (fractBits&fractHOB) == 0L ){
//                fractBits <<= 1;
//                binExp -= 1;
//            }
//            nSignificantBits = expShift + binExp +1; // recall binExp is  - shift count.
//            binExp += 1;
//        } else {
//            fractBits |= fractHOB;
//            nSignificantBits = expShift+1;
//        }
//        binExp -= expBias;
//
//        /*
//         * This is the hard case. We are going to compute large positive
//         * integers B and S and integer decExp, s.t.
//         *      d = ( B / S ) * 10^decExp
//         *      1 <= B / S < 10
//         * Obvious choices are:
//         *      decExp = floor( log10(d) )
//         *      B      = d * 2^nTinyBits * 10^max( 0, -decExp )
//         *      S      = 10^max( 0, decExp) * 2^nTinyBits
//         * (noting that nTinyBits has already been forced to non-negative)
//         * I am also going to compute a large positive integer
//         *      M      = (1/2^nSignificantBits) * 2^nTinyBits * 10^max( 0, -decExp )
//         * i.e. M is (1/2) of the ULP of d, scaled like B.
//         * When we iterate through dividing B/S and picking off the
//         * quotient bits, we will know when to stop when the remainder
//         * is <= M.
//         *
//         * We keep track of powers of 2 and powers of 5.
//         */
//
//        /*
//         * Estimate decimal exponent. (If it is small-ish,
//         * we could double-check.)
//         *
//         * First, scale the mantissa bits such that 1 <= d2 < 2.
//         * We are then going to estimate
//         *          log10(d2) ~=~  (d2-1.5)/1.5 + log(1.5)
//         * and so we can estimate
//         *      log10(d) ~=~ log10(d2) + binExp * log10(2)
//         * take the floor and call it decExp.
//         * FIXME -- use more precise constants here. It costs no more.
//         */
//
//        double d2 = Double.longBitsToDouble(
//                expOne | ( fractBits &~ fractHOB ) );
//        int decExp = (int)Math.floor(
//                (d2-1.5D)*0.289529654D + 0.176091259 + binExp * 0.301029995663981 );

//
//        assert nDigits <= 19 : nDigits; // generous bound on size of nDigits
//        int i = 0;
//        if (isNegative) { i = 1; }
//        if (decExponent > 0 && decExponent < 8) {
//		    // print digits.digits.
//		    int charLength = Math.min(nDigits, decExponent);
//		    //System.arraycopy(digits, 0, result, i, charLength);
//		    i += charLength;
//		    if (charLength < decExponent) {
//		        charLength = decExponent-charLength;
//		        //System.arraycopy(zero, 0, result, i, charLength);
//		        for(int j = 0; j < charLength; j++)
//		        	append('0');
//		        i += charLength;
//		        append('.');
//		        append('0');
//		    } else {
//		    	append('.');
//		        if (charLength < nDigits) {
//		            int t = nDigits - charLength;
//		            //System.arraycopy(digits, charLength, result, i, t);
//		            i += t;
//		        } else {
//		        	append('0');
//		        }
//		    }
//		} else if (decExponent <=0 && decExponent > -3) {
//			append('0');
//			append('.');
//		    if (decExponent != 0) {
//		        //System.arraycopy(zero, 0, result, i, -decExponent);
//		    	for(int j = 0; j < -decExponent; j++)
//		        	append('0');
//		        i -= decExponent;
//		    }
//		    //System.arraycopy(digits, 0, result, i, nDigits);
//		    i += nDigits;
//		} else {
//		    result[i++] = digits[0];
//		    append('.');
//		    if (nDigits > 1) {
//		        System.arraycopy(digits, 1, result, i, nDigits-1);
//		        i += nDigits-1;
//		    } else {
//		        append('0');
//		    }
//		    append('E');
//		    int e;
//		    if (decExponent <= 0) {
//		        append('-');
//		        e = -decExponent+1;
//		    } else {
//		        e = decExponent-1;
//		    }
//		    // decExponent has 1, 2, or 3, digits
//		    if (e <= 9) {
//		    	append(BufferFormatter.DIGIT_ONES[e]);
//		    } else if (e <= 99) {
//		    	append(BufferFormatter.DIGIT_TENS[e]);
//		    	append(BufferFormatter.DIGIT_ONES[e]);
//		    } else {
//		    	append(BufferFormatter.DIGIT_ONES[e/100]);
//		        e %= 100;
//		        append(BufferFormatter.DIGIT_TENS[e]);
//		        append(BufferFormatter.DIGIT_ONES[e]);
//		    }
//		}
		return this;
	}

	@Override
	public GFLogEntry append(Object o) {
		checkIfCommitted();
		try {
			if (o != null){
				final ObjectFormatter formatter = formatterFactory.getObjectFormatter(o);
				formatter.append(o, this);
			} else {
				append('n').append('u').append('l').append('l');
			}
		} catch (Throwable e){
			error("append(Object o)", e);
		}
		return this;
	}

	@Override
	public void appendLast (final char c) {
		append(c);
		commit();
	}

	@Override
	public void appendLast (final CharSequence csq) {
		append(csq);
		commit();
	}

	@Override
	public void appendLast (final CharSequence csq, final int start, final int end) {
		append(csq, start, end);
		commit();
	}

	@Override
	public void appendLast (final boolean b) {
		append(b);
		commit();
	}

	@Override
	public void appendLast (final int i) {
		append(i);
		commit();
	}

	@Override
	public void appendLast (final long i) {
		append(i);
		commit();
	}

	@Override
	public void appendLast (final double i) {
		append(i);
		commit();
	}

	@Override
	public void appendLast (final double i, final int precision) {
		append(i, precision);
		commit();
	}

	@Override
	public <T> void appendLast(T[] array, String separator) {
		append(array, separator);
		commit();
	}

	@Override
	public <T> void appendLast(Iterable<T> iterable, String separator) {
		append(iterable, separator);
		commit();
	}

	@Override
	public void appendLast (Throwable e) {
		append(e);
		commit();
	}

	@Override
	public void appendLast (Loggable loggable) {
		append(loggable);
		commit();
	}

	@Override
	public void appendLast (Object o) {
		append(o);
		commit();
	}

	@Override
	public FormattedGFLogEntry with(char c){
		checkPlaceholder();
		append(c);
		appendNextPatternChunk();
		return this;
	}

	@Override
	public FormattedGFLogEntry with(CharSequence csq){
		checkPlaceholder();
		append(csq);
		appendNextPatternChunk();
		return this;
	}

	@Override
	public FormattedGFLogEntry with(CharSequence csq, int start, int end){
		checkPlaceholder();
		append(csq, start, end);
		appendNextPatternChunk();
		return this;
	}

	@Override
	public FormattedGFLogEntry with(boolean b){
		checkPlaceholder();
		append(b);
		appendNextPatternChunk();
		return this;
	}

	@Override
	public FormattedGFLogEntry with(int i){
		checkPlaceholder();
		append(i);
		appendNextPatternChunk();
		return this;
	}

	@Override
	public FormattedGFLogEntry with(long i){
		checkPlaceholder();
		append(i);
		appendNextPatternChunk();
		return this;
	}

	@Override
	public FormattedGFLogEntry with(double i){
		checkPlaceholder();
		append(i);
		appendNextPatternChunk();
		return this;
	}

	@Override
	public FormattedGFLogEntry with(double i, int precision){
		checkPlaceholder();
		append(i, precision);
		appendNextPatternChunk();
		return this;
	}

	@Override
	public <T> FormattedGFLogEntry with(T[] array, String separator) {
		checkPlaceholder();
		append(array, separator);
		appendNextPatternChunk();
		return this;
	}

	@Override
	public <T> FormattedGFLogEntry with(Iterable<T> iterable, String separator) {
		checkPlaceholder();
		append(iterable, separator);
		appendNextPatternChunk();
		return this;
	}

	@Override
	public FormattedGFLogEntry with(Throwable e){
		checkPlaceholder();
		append(e);
		appendNextPatternChunk();
		return this;
	}

	@Override
	public FormattedGFLogEntry with(Loggable loggable){
		checkPlaceholder();
		append(loggable);
		appendNextPatternChunk();
		return this;
	}

	@Override
	public FormattedGFLogEntry with(Object o){
		checkPlaceholder();
		append(o);
		appendNextPatternChunk();
		return this;
	}

	@Override
	public void withLast(char c){
		with(c);
		checkAndCommit();
	}

	@Override
	public void withLast(CharSequence csq){
		with(csq);
		checkAndCommit();
	}

	@Override
	public void withLast(CharSequence csq, int start, int end){
		with(csq, start, end);
		checkAndCommit();
	}

	@Override
	public void withLast(boolean b){
		with(b);
		checkAndCommit();
	}

	@Override
	public void  withLast(int i){
		with(i);
		checkAndCommit();
	}

	@Override
	public void  withLast(long i){
		with(i);
		checkAndCommit();
	}

	@Override
	public void withLast(double i){
		with(i);
		checkAndCommit();
	}

	@Override
	public void withLast(double i, int precision){
		with(i, precision);
		checkAndCommit();
	}

	@Override
	public <T> void withLast(T[] array, String separator) {
		with(array, separator);
		checkAndCommit();
	}

	@Override
	public <T> void withLast(Iterable<T> iterable, String separator) {
		with(iterable, separator);
		checkAndCommit();
	}

	@Override
	public void withLast(Throwable e){
		with(e);
		checkAndCommit();
	}

	@Override
	public void withLast(Loggable loggable){
		with(loggable);
		checkAndCommit();
	}

	@Override
	public void withLast(Object o){
		with(o);
		checkAndCommit();
	}

	@Override
	public void commit() {
		checkIfCommitted();
		commit0();
		loggerService.entryFlushed(this);
		commited = true;
		pattern = null;
		error = null;
	}

	protected abstract void commit0();
}

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *	  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gflogger.helpers;


import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.*;

import org.gflogger.Layout;
import org.gflogger.LogEntryItem;
import org.gflogger.formatter.BufferFormatter;
import org.gflogger.formatter.FastDateFormat;


// Contributors:   Nelson Minar <(nelson@monkey.org>
//				 Igor E. Poteryaev <jah@mail.ru>
//				 Reinhard Deschler <reinhard.deschler@web.de>

/**
 * Most of the work of the {@link org.apache.log4j.PatternLayout} class is
 * delegated to the PatternParser class.
 *
 * <p>
 * It is this class that parses conversion patterns and creates a chained list
 * of {@link OptionConverter OptionConverters}.
 *
 * @author <a href=mailto:"cakalijp@Maritz.com">James P. Cakalic</a>
 * @author Ceki G&uuml;lc&uuml;
 * @author Anders Kristensen
 * @since 0.8.2
 */
public class PatternParser {

	private static final char	ESCAPE_CHAR				= '%';

	private static final int	LITERAL_STATE			= 0;
	private static final int	CONVERTER_STATE			= 1;
	private static final int	DOT_STATE				= 3;
	private static final int	MIN_STATE				= 4;
	private static final int	MAX_STATE				= 5;

//	static final int		  FULL_LOCATION_CONVERTER   = 1000;
//	static final int		  METHOD_LOCATION_CONVERTER = 1001;
//	static final int		  CLASS_LOCATION_CONVERTER  = 1002;
//	static final int		  LINE_LOCATION_CONVERTER   = 1003;
//	static final int		  FILE_LOCATION_CONVERTER   = 1004;

	static final int			RELATIVE_TIME_CONVERTER	= 2000;
	static final int			THREAD_CONVERTER		= 2001;
	static final int			LEVEL_CONVERTER			= 2002;
//	static final int			NDC_CONVERTER			= 2003;
	static final int			MESSAGE_CONVERTER		= 2004;

	protected int				state;
	protected StringBuffer		currentLiteral			= new StringBuffer(32);
	protected int				patternLength;
	protected int				i;
	PatternConverter			head;
	PatternConverter			tail;
	protected FormattingInfo	formattingInfo			= new FormattingInfo();

	protected final String		pattern;
	protected final Locale		locale;
	protected final TimeZone	timeZone;

	public PatternParser(String pattern, Locale locale, TimeZone timeZone) {
		this.pattern = pattern;
		this.locale = locale;
		this.timeZone = timeZone;
		patternLength = pattern.length();
		state = LITERAL_STATE;
	}

	private void addToList(PatternConverter pc) {
		if (head == null) {
			head = tail = pc;
		} else {
			tail.next = pc;
			tail = pc;
		}
	}

	protected String extractOption() {
		if ((i < patternLength) && (pattern.charAt(i) == '{')) {
			int end = pattern.indexOf('}', i);
			if (end > i) {
				String r = pattern.substring(i + 1, end);
				i = end + 1;
				return r;
			}
		}
		return null;
	}

	/**
	 * The option is expected to be in decimal and positive. In case of error,
	 * zero is returned.
	 */
	protected int extractPrecisionOption() {
		String opt = extractOption();
		int r = 0;
		if (opt != null) {
			try {
				r = Integer.parseInt(opt);
				if (r <= 0) {
					LogLog.error("Precision option (" + opt + ") isn't a positive integer.");
					r = 0;
				}
			} catch (NumberFormatException e) {
				LogLog.error("Category option \"" + opt + "\" not a decimal integer.", e);
			}
		}
		return r;
	}

	public PatternConverter parse() {
		char c;
		i = 0;
		while (i < patternLength) {
			c = pattern.charAt(i++);
			switch (state) {
			case LITERAL_STATE:
				// In literal state, the last char is always a literal.
				if (i == patternLength) {
					currentLiteral.append(c);
					continue;
				}
				if (c == ESCAPE_CHAR) {
					// peek at the next char.
					switch (pattern.charAt(i)) {
					case ESCAPE_CHAR:
						currentLiteral.append(c);
						i++; // move pointer
						break;
					case 'n':
						currentLiteral.append(Layout.LINE_SEP);
						i++; // move pointer
						break;
					default:
						if (currentLiteral.length() != 0) {
							addToList(new LiteralPatternConverter(currentLiteral.toString()));
							LogLog.debug("Parsed LITERAL converter: \"" +currentLiteral+"\".");
						}
						currentLiteral.setLength(0);
						currentLiteral.append(c); // append %
						state = CONVERTER_STATE;
						formattingInfo.reset();
					}
				} else {
					currentLiteral.append(c);
				}
				break;
			case CONVERTER_STATE:
				currentLiteral.append(c);
				switch (c) {
				case '-':
					formattingInfo.leftAlign = true;
					break;
				case '.':
					state = DOT_STATE;
					break;
				default:
					if (c >= '0' && c <= '9') {
						formattingInfo.min = c - '0';
						state = MIN_STATE;
					} else
						finalizeConverter(c);
				} // switch
				break;
			case MIN_STATE:
				currentLiteral.append(c);
				if (c >= '0' && c <= '9')
					formattingInfo.min = formattingInfo.min * 10 + (c - '0');
				else if (c == '.')
					state = DOT_STATE;
				else {
					finalizeConverter(c);
				}
				break;
			case DOT_STATE:
				currentLiteral.append(c);
				if (c >= '0' && c <= '9') {
					formattingInfo.max = c - '0';
					state = MAX_STATE;
				} else {
				   LogLog.error("Error occured in position " + i
					   + ".\n Was expecting digit, instead got char \"" + c + "\".");
					state = LITERAL_STATE;
				}
				break;
			case MAX_STATE:
				currentLiteral.append(c);
				if (c >= '0' && c <= '9')
					formattingInfo.max = formattingInfo.max * 10 + (c - '0');
				else {
					finalizeConverter(c);
					state = LITERAL_STATE;
				}
				break;
			} // switch
		} // while
		if (currentLiteral.length() != 0) {
			addToList(new LiteralPatternConverter(currentLiteral.toString()));
			LogLog.debug("Parsed LITERAL converter: \""+currentLiteral+"\".");
		}
		return head;
	}

	protected void finalizeConverter(char c) {
		PatternConverter pc = null;
		switch (c) {
		case 'c':
			pc = new CategoryPatternConverter(formattingInfo, extractPrecisionOption());
			LogLog.debug("CATEGORY converter.");
			formattingInfo.dump();
			currentLiteral.setLength(0);
			break;
		case 'd':
			String dateFormatStr = "ISO8601";
			FastDateFormat df = null;
			String dOpt = extractOption();
			if (dOpt != null)
				dateFormatStr = dOpt;

//			if (dateFormatStr.equalsIgnoreCase(AbsoluteTimeDateFormat.ISO8601_DATE_FORMAT))
//				df = new ISO8601DateFormat();
//			else if (dateFormatStr.equalsIgnoreCase(AbsoluteTimeDateFormat.ABS_TIME_DATE_FORMAT))
//				df = new AbsoluteTimeDateFormat();
//			else if (dateFormatStr
//					.equalsIgnoreCase(AbsoluteTimeDateFormat.DATE_AND_TIME_DATE_FORMAT))
//				df = new DateTimeDateFormat();
//			else {
				try {
					df = FastDateFormat.getInstance(dateFormatStr, timeZone, locale);
				} catch (IllegalArgumentException e) {
					LogLog.error("Could not instantiate SimpleDateFormat with " + dateFormatStr, e);
					break;
				}
//			}
			pc = new DatePatternConverter(formattingInfo, df);
			LogLog.debug("DATE converter {"+dateFormatStr+"}.");
			formattingInfo.dump();
			currentLiteral.setLength(0);
			break;
//		case 'F':
//			pc = new LocationPatternConverter(formattingInfo, FILE_LOCATION_CONVERTER);
//			LogLog.debug("File name converter.");
//			formattingInfo.dump();
//			currentLiteral.setLength(0);
//			break;
//		case 'l':
//			pc = new LocationPatternConverter(formattingInfo, FULL_LOCATION_CONVERTER);
//			LogLog.debug("Location converter.");
//			formattingInfo.dump();
//			currentLiteral.setLength(0);
//			break;
//		case 'L':
//			pc = new LocationPatternConverter(formattingInfo, LINE_LOCATION_CONVERTER);
//			LogLog.debug("LINE NUMBER converter.");
//			formattingInfo.dump();
//			currentLiteral.setLength(0);
//			break;
		case 'm':
			pc = new BasicPatternConverter(formattingInfo, MESSAGE_CONVERTER);
			LogLog.debug("MESSAGE converter.");
			formattingInfo.dump();
			currentLiteral.setLength(0);
			break;
//		case 'M':
//			pc = new LocationPatternConverter(formattingInfo, METHOD_LOCATION_CONVERTER);
//			LogLog.debug("METHOD converter.");
//			formattingInfo.dump();
//			currentLiteral.setLength(0);
//			break;
		case 'p':
			pc = new BasicPatternConverter(formattingInfo, LEVEL_CONVERTER);
			LogLog.debug("LEVEL converter.");
			formattingInfo.dump();
			currentLiteral.setLength(0);
			break;
		case 'r':
			pc = new BasicPatternConverter(formattingInfo, RELATIVE_TIME_CONVERTER);
			LogLog.debug("RELATIVE time converter.");
			formattingInfo.dump();
			currentLiteral.setLength(0);
			break;
		case 't':
			pc = new BasicPatternConverter(formattingInfo, THREAD_CONVERTER);
			LogLog.debug("THREAD converter.");
			formattingInfo.dump();
			currentLiteral.setLength(0);
			break;
////		case 'u':
////			if (i < patternLength) {
////				char cNext = pattern.charAt(i);
////				if (cNext >= '0' && cNext <= '9') {
////					pc = new UserFieldPatternConverter(formattingInfo, cNext - '0');
////					LogLog.debug("USER converter [" + cNext + "].");
////					formattingInfo.dump();
////					currentLiteral.setLength(0);
////					i++;
////				} else
////					LogLog.error("Unexpected char" + cNext + " at position " + i);
////			}
////			break;
//		case 'x':
//			pc = new BasicPatternConverter(formattingInfo, NDC_CONVERTER);
//			LogLog.debug("NDC converter.");
//			currentLiteral.setLength(0);
//			break;
//		case 'X':
//			String xOpt = extractOption();
//			pc = new MDCPatternConverter(formattingInfo, xOpt);
//			currentLiteral.setLength(0);
//			break;
		default:
			LogLog.error("Unexpected char [" + c + "] at position " + i
					+ " in conversion patterrn.");
			pc = new LiteralPatternConverter(currentLiteral.toString());
			currentLiteral.setLength(0);
		}

		addConverter(pc);
	}

	protected void addConverter(PatternConverter pc) {
		currentLiteral.setLength(0);
		// Add the pattern converter to the list.
		addToList(pc);
		// Next pattern is assumed to be a literal.
		state = LITERAL_STATE;
		// Reset formatting info
		formattingInfo.reset();
	}

	// ---------------------------------------------------------------------
	// PatternConverters
	// ---------------------------------------------------------------------

	private static class BasicPatternConverter extends PatternConverter {
		int type;

		BasicPatternConverter(FormattingInfo formattingInfo, int type) {
			super(formattingInfo);
			this.type = type;
		}

		@Override
		public void format(ByteBuffer buffer, LogEntryItem item) {
			switch (type) {
			case RELATIVE_TIME_CONVERTER:
				BufferFormatter.append(buffer, item.getTimestamp() - LogEntryItem.startTime);
				break;
			case THREAD_CONVERTER:
//				if (item.isByteBufferBased()){
//					buffer.put(item.getThreadNameBuffer());
//				} else {
					BufferFormatter.append(buffer, item.getThreadName());
//				}
				 return;
			case LEVEL_CONVERTER:
				BufferFormatter.append(buffer, item.getLogLevel().name());
				return;
//			case NDC_CONVERTER:
//				return event.getNDC();
			case MESSAGE_CONVERTER: {
				buffer.put(item.getBuffer());
				return;
			}
			default:
			}
		}

		@Override
		public void format(CharBuffer buffer, LogEntryItem item) {
			switch (type) {
			case RELATIVE_TIME_CONVERTER:
				BufferFormatter.append(buffer, item.getTimestamp() - LogEntryItem.startTime);
				break;
			case THREAD_CONVERTER:
				BufferFormatter.append(buffer, item.getThreadName());
				return;
			case LEVEL_CONVERTER:
				BufferFormatter.append(buffer, item.getLogLevel().name());
				return;
//			case NDC_CONVERTER:
//				return event.getNDC();
			case MESSAGE_CONVERTER: {
				buffer.put(item.getCharBuffer());
				return;
			}
			default:
			}
		}

		@Override
		public int size(LogEntryItem item) {
			switch (type) {
			case RELATIVE_TIME_CONVERTER:
				return BufferFormatter.numberOfDigits(item.getTimestamp() - LogEntryItem.startTime);
			case THREAD_CONVERTER:
				return item.getThreadName().length();
			case LEVEL_CONVERTER:
				return item.getLogLevel().name().length();
//			case NDC_CONVERTER:
//				return event.getNDC();
			case MESSAGE_CONVERTER: {
				final ByteBuffer buffer = item.getBuffer();
				return buffer.position();
			}
			default:
			}
			return 0;
		}

		@Override
		public String toString() {
			final StringBuilder builder = new StringBuilder();
			switch (type) {
			case RELATIVE_TIME_CONVERTER:
				builder.append("%time");
				break;
			case THREAD_CONVERTER:
				builder.append("%thread");
				break;
			case LEVEL_CONVERTER:
				builder.append("%level");
				break;
			case MESSAGE_CONVERTER:
				builder.append("%msg");
				break;
			default:
				builder.append("%unkown");
				break;
			}
			if (next != null) builder.append(" ").append(next);
			return builder.toString();
		}
	}

	private static class LiteralPatternConverter extends PatternConverter {
		private String literal;

		LiteralPatternConverter(String value) {
			literal = value;
		}

		@Override
		public void format(ByteBuffer buffer, LogEntryItem logEntryItem) {
			BufferFormatter.append(buffer, literal);
		}

		@Override
		public void format(CharBuffer buffer, LogEntryItem logEntryItem) {
			BufferFormatter.append(buffer, literal);
		}

		@Override
		public int size(LogEntryItem entry) {
			return literal.length();
		}

		@Override
		public String toString() {
			final StringBuilder builder = new StringBuilder().append(literal);
			if (next != null) builder.append(" ").append(next);
			return builder.toString();
		}
	}

	private static class DatePatternConverter extends PatternConverter {
		private FastDateFormat df;

		DatePatternConverter(FormattingInfo formattingInfo, FastDateFormat df) {
			super(formattingInfo);
			this.df = df;
		}

		@Override
		public void format(ByteBuffer buffer, LogEntryItem logEntryItem) {
			df.format(logEntryItem.getTimestamp(), buffer);
		}

		@Override
		public void format(CharBuffer buffer, LogEntryItem logEntryItem) {
			df.format(logEntryItem.getTimestamp(), buffer);
		}

		@Override
		public int size(LogEntryItem entry) {
			return df.getMaxLengthEstimate();
		}

		@Override
		public String toString() {
			final StringBuilder builder = new StringBuilder().append("%date");
			if (next != null) builder.append(" ").append(next);
			return builder.toString();
		}
	}

//	private class LocationPatternConverter extends PatternConverter {
//		int type;
//
//		LocationPatternConverter(FormattingInfo formattingInfo, int type) {
//			super(formattingInfo);
//			this.type = type;
//		}
//
//		public String convert(LoggingEvent event) {
//			LocationInfo locationInfo = event.getLocationInformation();
//			switch (type) {
//			case FULL_LOCATION_CONVERTER:
//				return locationInfo.fullInfo;
//			case METHOD_LOCATION_CONVERTER:
//				return locationInfo.getMethodName();
//			case LINE_LOCATION_CONVERTER:
//				return locationInfo.getLineNumber();
//			case FILE_LOCATION_CONVERTER:
//				return locationInfo.getFileName();
//			default:
//				return null;
//			}
//		}
//	}

	private static abstract class NamedPatternConverter extends PatternConverter {
		int precision;

		NamedPatternConverter(FormattingInfo formattingInfo, int precision) {
			super(formattingInfo);
			this.precision = precision;
		}

		abstract String getFullyQualifiedName(LogEntryItem item);

		@Override
		public int size(LogEntryItem item) {
			String n = getFullyQualifiedName(item);
			if (precision <= 0) {
				return n.length();
			}
			int len = n.length();

			// We substract 1 from 'len' when assigning to 'end' to avoid
			// out of
			// bounds exception in return r.substring(end+1, len). This can
			// happen if
			// precision is 1 and the category name ends with a dot.
			int end = len - 1;
			for (int i = precision; i > 0 && end > 0; i--) {
				end = n.lastIndexOf('.', end - 1);
			}
			return (len - (end + 1));
		}

		@Override
		public void format(ByteBuffer buffer, LogEntryItem item) {
//			if (!item.isByteBufferBased()){
				String n = getFullyQualifiedName(item);
				if (precision <= 0) {
					BufferFormatter.append(buffer, n);
				} else {
					int len = n.length();

					// We substract 1 from 'len' when assigning to 'end' to avoid
					// out of
					// bounds exception in return r.substring(end+1, len). This can
					// happen if
					// precision is 1 and the category name ends with a dot.
					int end = len - 1;
					for (int i = precision; i > 0 && end > 0; i--) {
						end = n.lastIndexOf('.', end - 1);
					}
					BufferFormatter.append(buffer, n, end + 1, len);
				}
//			}
		}

		@Override
		public void format(CharBuffer buffer, LogEntryItem item) {
			String n = getFullyQualifiedName(item);
			if (precision <= 0) {
				BufferFormatter.append(buffer, n);
			} else {
				int len = n.length();

				// We substract 1 from 'len' when assigning to 'end' to avoid
				// out of
				// bounds exception in return r.substring(end+1, len). This can
				// happen if
				// precision is 1 and the category name ends with a dot.
				int end = len - 1;
				for (int i = precision; i > 0 && end > 0; i--) {
					end = n.lastIndexOf('.', end - 1);
				}
				BufferFormatter.append(buffer, n, end + 1, len);
			}
		}

	}

	private class CategoryPatternConverter extends NamedPatternConverter {

		CategoryPatternConverter(FormattingInfo formattingInfo, int precision) {
			super(formattingInfo, precision);
		}

		@Override
		String getFullyQualifiedName(LogEntryItem item) {
			return item.getCategoryName();
		}

		@Override
		public String toString() {
			final StringBuilder builder = new StringBuilder().append("%category");
			if (next != null) builder.append(" ").append(next);
			return builder.toString();
		}
	}
}

package org.gflogger;

/**
 * @author Denis Gburg
 */
public interface FormattingStrategy {

	boolean isPlaceholder(String pattern, int position);

	boolean isEscape(String pattern, int position);

	boolean autocommitEnabled();

}

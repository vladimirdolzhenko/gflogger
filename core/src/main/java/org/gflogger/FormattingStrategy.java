package org.gflogger;

public interface FormattingStrategy {
    boolean isPlaceholder(String pattern, int position);
    boolean isEscape(String pattern, int position);
}

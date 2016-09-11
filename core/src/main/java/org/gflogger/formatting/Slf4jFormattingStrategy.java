package org.gflogger.formatting;

import org.gflogger.FormattingStrategy;

/**
 * @author Denis Gburg
 */
public final class Slf4JFormattingStrategy implements FormattingStrategy {
    @Override
    public boolean isPlaceholder(String pattern, int position) {
        return pattern.length() > position + 1
            && pattern.charAt(position) == '{'
            && pattern.charAt(position + 1) == '}';
    }

    @Override
    public boolean isEscape(String pattern, int position) {
        return pattern.length() > position + 1
            && pattern.charAt(position) == '\\'
            && pattern.charAt(position + 1) == '{';
    }

    @Override
    public boolean autocommitEnabled() {
        return false;
    }

}

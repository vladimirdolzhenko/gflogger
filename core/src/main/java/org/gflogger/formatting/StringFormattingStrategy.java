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

package org.gflogger.formatting;

import org.gflogger.FormattingStrategy;

/**
 * @author Denis Gburg
 */
public final class StringFormattingStrategy implements FormattingStrategy {
	@Override
	public boolean isPlaceholder(String pattern, int position) {
		return pattern.length() > position + 1
			&& pattern.charAt(position) == '%'
			&& pattern.charAt(position + 1) == 's';
	}

	@Override
	public boolean isEscape(String pattern, int position) {
		return pattern.length() > position + 1
			&& pattern.charAt(position) == '%'
			&& pattern.charAt(position + 1) == '%';
	}

	@Override
	public boolean autocommitEnabled() {
		return true;
	}
}

package com.redhat.qute.settings;

import com.redhat.qute.utils.AntPathMatcher;

class ExcludedProperty {

	private final String pattern;
	private final AntPathMatcher matcher;

	public ExcludedProperty(String pattern, AntPathMatcher matcher) {
		this.pattern = pattern;
		this.matcher = matcher.isPattern(pattern) ? matcher : null;
	}

	/**
	 * Returns true if the given property name matches the pattern and false
	 * otherwise.
	 *
	 * @param propertyName the property name.
	 * @return true if the given property name matches the pattern and false
	 *         otherwise.
	 */
	public boolean match(String propertyName) {
		if (matcher != null) {
			// the excluded property is a pattern, use pattern matcher to check the match
			return matcher.match(pattern, propertyName);
		}
		// the excluded property is not a pattern, check if the property name is equal
		// to the pattern
		return pattern.equals(propertyName);
	}

}

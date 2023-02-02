/*******************************************************************************
* Copyright (c) 2021 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.utils;

import java.util.Arrays;
import java.util.Collection;

/**
 * String utilities.
 *
 */
public class StringUtils {

	public static final String[] EMPTY_STRING = new String[0];

	public static final String TRUE = "true";
	public static final String FALSE = "false";
	public static final Collection<String> TRUE_FALSE_ARRAY = Arrays.asList(TRUE, FALSE);

	private static final float MAX_DISTANCE_DIFF_RATIO = 0.4f;

	private StringUtils() {
	}

	public static boolean isEmpty(String value) {
		return value == null || value.isEmpty();
	}

	// Utilities class.

	/**
	 * Uses Levenshtein distance to determine similarity between strings
	 *
	 * @param reference the string being compared to
	 * @param current   the string compared
	 * @return true if the two strings are similar, false otherwise
	 */
	public static boolean isSimilar(String reference, String current) {
		int threshold = Math.round(MAX_DISTANCE_DIFF_RATIO * reference.length());
		LevenshteinDistance levenshteinDistance = new LevenshteinDistance(threshold);
		return levenshteinDistance.apply(reference, current) != -1;
	}

}

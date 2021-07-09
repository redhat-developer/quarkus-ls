/*******************************************************************************
* Copyright (c) 2021 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.microprofile.jdt.internal.quarkus.utils;

import java.util.List;
import java.util.Map;

public class YamlUtils {

	private YamlUtils() {}

	/**
	 * Returns the value extracted from the map as a String, or null if the value is
	 * not in the map
	 *
	 * @param segments the keys to use when searching for the value
	 * @param mapOrValue the map from a property key to another portion of the map, or the
	 * @return the value extracted from the map as a String, or null if the value is
	 *         not in the map
	 */
	public static String getValueRecursively(List<String> segments, Object mapOrValue) {
		if (mapOrValue == null) {
			return null;
		}
		if (segments.size() == 0) {
			return mapOrValue.toString();
		} else if (segments.size() > 0 && mapOrValue instanceof Map<?, ?>) {
			Map<String, Object> configMap = (Map<String, Object>) mapOrValue;
			Object configChild = configMap.get(segments.get(0));
			return getValueRecursively(segments.subList(1, segments.size()), configChild);
		} else {
			return null;
		}
	}

}

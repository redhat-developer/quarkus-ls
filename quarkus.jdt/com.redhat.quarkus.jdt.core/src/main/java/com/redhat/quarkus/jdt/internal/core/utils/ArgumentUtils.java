/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.quarkus.jdt.internal.core.utils;

import java.util.List;
import java.util.Map;

/**
 * Arguments utilities.
 * 
 * @author Angelo ZERR
 *
 */
public class ArgumentUtils {

	public static Map<String, Object> getFirst(List<Object> arguments) {
		return arguments.isEmpty() ? null : (Map<String, Object>) arguments.get(0);
	}

	public static String getString(Map<String, Object> obj, String key) {
		return (String) obj.get(key);
	}

	public static boolean getBoolean(Map<String, Object> obj, String key) {
		Object result = obj.get(key);
		return result != null && result instanceof Boolean && ((Boolean) result).booleanValue();
	}

	public static int getInt(Map<String, Object> obj, String key) {
		Object result = obj.get(key);
		return result != null && result instanceof Number ? ((Number) result).intValue() : 0;
	}
}

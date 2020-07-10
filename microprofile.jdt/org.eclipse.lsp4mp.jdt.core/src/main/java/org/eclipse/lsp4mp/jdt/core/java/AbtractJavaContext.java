/*******************************************************************************
* Copyright (c) 2020 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lsp4mp.jdt.core.java;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.lsp4mp.jdt.core.utils.IJDTUtils;

/**
 * Abstract class for Java context for a given compilation unit.
 * 
 * @author Angelo ZERR
 *
 */
public abstract class AbtractJavaContext {

	private final String uri;

	private final ITypeRoot typeRoot;

	private final IJDTUtils utils;

	private Map<String, Object> cache;

	public AbtractJavaContext(String uri, ITypeRoot typeRoot, IJDTUtils utils) {
		this.uri = uri;
		this.typeRoot = typeRoot;
		this.utils = utils;
	}

	public String getUri() {
		return uri;
	}

	public ITypeRoot getTypeRoot() {
		return typeRoot;
	}

	public IJavaProject getJavaProject() {
		return getTypeRoot().getJavaProject();
	}

	public IJDTUtils getUtils() {
		return utils;
	}
	

	/**
	 * Associates the specified value with the specified key in the cache.
	 * 
	 * @param key   the key.
	 * @param value the value.
	 */
	public void put(String key, Object value) {
		if (cache == null) {
			cache = new HashMap<>();
		}
		cache.put(key, value);
	}

	/**
	 * Returns the value to which the specified key is mapped, or {@code null} if
	 * this map contains no mapping for the key.
	 * 
	 * @param key the key.
	 * @return the value to which the specified key is mapped, or {@code null} if
	 *         this map contains no mapping for the key.
	 */
	public Object get(String key) {
		if (cache == null) {
			return null;
		}
		return cache.get(key);
	}

}

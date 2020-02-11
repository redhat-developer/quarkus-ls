/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.microprofile.jdt.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.IJavaProject;

import com.redhat.microprofile.commons.MicroProfilePropertiesScope;

/**
 * Class for base context.
 * 
 * @author Angelo ZERR
 *
 */
public class BaseContext {

	private final IJavaProject javaProject;
	private final List<MicroProfilePropertiesScope> scopes;
	private final Map<String, Object> cache;

	public BaseContext(IJavaProject javaProject, List<MicroProfilePropertiesScope> scopes) {
		this.javaProject = javaProject;
		this.scopes = scopes;
		cache = new HashMap<>();
	}

	/**
	 * Associates the specified value with the specified key in the cache.
	 * 
	 * @param key   the key.
	 * @param value the value.
	 */
	public void put(String key, Object value) {
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
		return cache.get(key);
	}

	/**
	 * Returns the java project.
	 * 
	 * @return the java project.
	 */
	public IJavaProject getJavaProject() {
		return javaProject;
	}

	/**
	 * Returns the scope of the search.
	 * 
	 * @return the scope of the search.
	 */
	public List<MicroProfilePropertiesScope> getScopes() {
		return scopes;
	}
}

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
import java.util.Map;

import org.eclipse.jdt.core.IJavaProject;

public class SearchContext {

	private final IJavaProject javaProject;

	private final Map<String, Object> cache;

	public SearchContext(IJavaProject javaProject) {
		this.javaProject = javaProject;
		cache = new HashMap<>();
	}

	public void put(String key, Object value) {
		cache.put(key, value);
	}

	public Object get(String key) {
		return cache.get(key);
	}

	public IJavaProject getJavaProject() {
		return javaProject;
	}

}

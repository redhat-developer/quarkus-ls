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

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IJavaProject;

import com.redhat.microprofile.commons.DocumentFormat;
import com.redhat.microprofile.jdt.core.utils.ConfigUtils;
import com.redhat.microprofile.jdt.core.utils.IJDTUtils;

public class SearchContext {

	private final IJavaProject javaProject;

	private final IFile file;

	private final IPropertiesCollector collector;

	private final IJDTUtils utils;

	private final DocumentFormat documentFormat;

	private final Map<String, Object> cache;

	public SearchContext(IJavaProject javaProject, IFile file, IPropertiesCollector collector, IJDTUtils utils,
			DocumentFormat documentFormat) {
		this.javaProject = javaProject;
		this.file = file;
		this.collector = collector;
		this.utils = utils;
		this.documentFormat = documentFormat;
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

	public IFile getFile() {
		return file;
	}

	public IPropertiesCollector getCollector() {
		return collector;
	}

	public IJDTUtils getUtils() {
		return utils;
	}

	public DocumentFormat getDocumentFormat() {
		return documentFormat;
	}

	/**
	 * Returns the value of the given property name declared in the file (ex :
	 * application.properties) where collection properties is done and null
	 * otherwise.
	 * 
	 * @param propertyName the property name.
	 * @return the value of the given property name declared in the file (ex :
	 *         application.properties) where collection properties is done and null
	 *         otherwise.
	 */
	public String getConfigProperty(String propertyName) {
		return ConfigUtils.getProperty(file, propertyName);
	}
}

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

import java.util.List;

import org.eclipse.jdt.core.IJavaProject;

import com.redhat.microprofile.commons.DocumentFormat;
import com.redhat.microprofile.commons.MicroProfilePropertiesScope;
import com.redhat.microprofile.jdt.core.utils.IJDTUtils;

/**
 * The search context used to collect properties.
 * 
 * @author Angelo ZERR
 *
 */
public class SearchContext extends BaseContext {
	private final IPropertiesCollector collector;
	private final IJDTUtils utils;
	private final DocumentFormat documentFormat;

	public SearchContext(IJavaProject javaProject, IPropertiesCollector collector, IJDTUtils utils,
			DocumentFormat documentFormat, List<MicroProfilePropertiesScope> scopes) {
		super(javaProject, scopes);
		this.collector = collector;
		this.utils = utils;
		this.documentFormat = documentFormat;
	}

	/**
	 * Returns the properties collector.
	 * 
	 * @return the properties collector
	 */
	public IPropertiesCollector getCollector() {
		return collector;
	}

	/**
	 * Returns the JDT utilities.
	 * 
	 * @return the JDT utilities.
	 */
	public IJDTUtils getUtils() {
		return utils;
	}

	/**
	 * Returns the document format to use for converting Javadoc.
	 * 
	 * @return the document format to use for converting Javadoc
	 */
	public DocumentFormat getDocumentFormat() {
		return documentFormat;
	}
}

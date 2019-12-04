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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchPattern;

/**
 * Properties provider API.
 * 
 * @author Angelo ZERR
 *
 */
public interface IPropertiesProvider {

	/**
	 * Begin the search.
	 * 
	 * @param context the search context
	 */
	default void begin(SearchContext context) {

	}

	/**
	 * End the search.
	 * 
	 * @param context the search context
	 */
	default void end(SearchContext context) {

	}

	/**
	 * Create the Java search pattern.
	 * 
	 * @return the Java search pattern.
	 */
	SearchPattern createSearchPattern();

	/**
	 * Contribute to the classpath to add extra JARs in classpath (ex : deployment
	 * JARs for Quarkus).
	 * 
	 * @param project
	 * @param excludeTestCode
	 * @param artifactResolver
	 * @param newClasspathEntries
	 * @throws JavaModelException
	 */
	default void contributeToClasspath(IJavaProject project, boolean excludeTestCode, ArtifactResolver artifactResolver,
			List<IClasspathEntry> newClasspathEntries) throws JavaModelException {

	}

	/**
	 * Collect properties.
	 * 
	 * @param match
	 * @param context
	 * @param collector
	 * @param monitor
	 */
	void collectProperties(SearchMatch match, SearchContext context, IPropertiesCollector collector,
			IProgressMonitor monitor);

}

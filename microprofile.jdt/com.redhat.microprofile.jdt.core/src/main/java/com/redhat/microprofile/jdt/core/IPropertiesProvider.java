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

import org.eclipse.core.runtime.IProgressMonitor;
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
	 * Begin the building scope.
	 * 
	 * @param context the search building scope
	 * @param monitor the progress monitor
	 */
	default void beginBuildingScope(BuildingScopeContext context, IProgressMonitor monitor) {

	}

	/**
	 * Contribute to the classpath to add extra JARs in classpath (ex : deployment
	 * JARs for Quarkus).
	 * 
	 * @param context the building scope context.
	 * @param monitor the progress monitor.
	 * @throws JavaModelException
	 */
	default void contributeToClasspath(BuildingScopeContext context, IProgressMonitor monitor)
			throws JavaModelException {

	}

	/**
	 * End the building scope.
	 * 
	 * @param context the building scope context.
	 * @param monitor the progress monitor.
	 */
	default void endBuildingScope(BuildingScopeContext context, IProgressMonitor monitor) {

	}

	/**
	 * Begin the search.
	 * 
	 * @param context the search context
	 * @param monitor the progress monitor
	 */
	default void beginSearch(SearchContext context, IProgressMonitor monitor) {

	}

	/**
	 * Create the Java search pattern.
	 * 
	 * @return the Java search pattern.
	 */
	SearchPattern createSearchPattern();

	/**
	 * Collect properties from the given Java search match.
	 * 
	 * @param match   the java search match.
	 * @param context the search context.
	 * @param monitor the progress monitor.
	 */
	void collectProperties(SearchMatch match, SearchContext context, IProgressMonitor monitor);

	/**
	 * End the search.
	 * 
	 * @param context the search context
	 * @param monitor the progress monitor
	 */
	default void endSearch(SearchContext context, IProgressMonitor monitor) {

	}

}

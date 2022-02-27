/*******************************************************************************
* Copyright (c) 2022 Red Hat Inc. and others.
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License v. 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
* which is available at https://www.apache.org/licenses/LICENSE-2.0.
*
* SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.jdt.template.datamodel;

import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.SearchPattern;

import com.redhat.qute.commons.datamodel.resolvers.NamespaceResolverInfo;

/**
 * Abstract class for data model provider.
 *
 * @author Angelo ZERR
 *
 */
public abstract class AbstractDataModelProvider implements IDataModelProvider {

	private NamespaceResolverInfo namespaceResolverInfo;

	@Override
	public void setNamespaceResolverInfo(NamespaceResolverInfo namespaceResolverInfo) {
		this.namespaceResolverInfo = namespaceResolverInfo;
	}

	/**
	 * Returns the namespace resolver information and null otherwise.
	 * 
	 * @return the namespace resolver information and null otherwise.
	 */
	public NamespaceResolverInfo getNamespaceResolverInfo() {
		return namespaceResolverInfo;
	}

	/**
	 * Returns the Java search pattern.
	 *
	 * @return the Java search pattern.
	 */
	protected abstract String[] getPatterns();

	@Override
	public SearchPattern createSearchPattern() {
		SearchPattern leftPattern = null;
		String[] patterns = getPatterns();

		if (patterns == null) {
			return null;
		}

		for (String pattern : patterns) {
			if (leftPattern == null) {
				leftPattern = createSearchPattern(pattern);
			} else {
				SearchPattern rightPattern = createSearchPattern(pattern);
				if (rightPattern != null) {
					leftPattern = SearchPattern.createOrPattern(leftPattern, rightPattern);
				}
			}
		}
		return leftPattern;
	}

	/**
	 * Create an instance of search pattern with the given <code>pattern</code>.
	 *
	 * @param pattern the search pattern
	 * @return an instance of search pattern with the given <code>pattern</code>.
	 */
	protected abstract SearchPattern createSearchPattern(String pattern);

	/**
	 * Create a search pattern for the given <code>annotationName</code> annotation
	 * name.
	 *
	 * @param annotationName the annotation name to search.
	 * @return a search pattern for the given <code>annotationName</code> annotation
	 *         name.
	 */
	protected static SearchPattern createAnnotationTypeReferenceSearchPattern(String annotationName) {
		return SearchPattern.createPattern(annotationName, IJavaSearchConstants.ANNOTATION_TYPE,
				IJavaSearchConstants.ANNOTATION_TYPE_REFERENCE, SearchPattern.R_EXACT_MATCH);
	}

	/**
	 * Create a search pattern for the given <code>className</code> class name.
	 *
	 * @param annotationName the class name to search.
	 * @return a search pattern for the given <code>className</code> class name.
	 */
	protected static SearchPattern createFieldDeclarationTypeReferenceSearchPattern(String className) {
		return SearchPattern.createPattern(className, IJavaSearchConstants.TYPE,
				IJavaSearchConstants.FIELD_DECLARATION_TYPE_REFERENCE, SearchPattern.R_EXACT_MATCH);
	}

	@Override
	public void endSearch(SearchContext context, IProgressMonitor monitor) {
		NamespaceResolverInfo info = getNamespaceResolverInfo();
		if (info != null) {
			// Register namespace information
			String namespacekey = info.getNamespaces().get(0);
			Map<String, NamespaceResolverInfo> infos = context.getDataModelProject().getNamespaceResolverInfos();
			infos.put(namespacekey, info);
		}
	}
}

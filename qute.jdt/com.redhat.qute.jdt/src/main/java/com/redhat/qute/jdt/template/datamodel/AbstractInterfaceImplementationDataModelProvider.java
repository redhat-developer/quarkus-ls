/*******************************************************************************
* Copyright (c) 2024 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.jdt.template.datamodel;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchPattern;

/**
 * Abstract class to collect Java types which implement some interfaces.
 *
 * @author Angelo ZERR
 *
 */
public abstract class AbstractInterfaceImplementationDataModelProvider extends AbstractDataModelProvider {

	private static final Logger LOGGER = Logger
			.getLogger(AbstractInterfaceImplementationDataModelProvider.class.getName());

	@Override
	protected String[] getPatterns() {
		return getInterfaceNames();
	}

	/**
	 * Returns the interface names to search.
	 *
	 * @return the interface names to search.
	 */
	protected abstract String[] getInterfaceNames();

	@Override
	protected SearchPattern createSearchPattern(String interfaceName) {
		return createInterfaceImplementationSearchPattern(interfaceName);
	}

	@Override
	public void collectDataModel(SearchMatch match, SearchContext context, IProgressMonitor monitor) {
		Object element = match.getElement();
		if (element instanceof IType type) {
			try {
				if (isApplicable(type)) {
					processType(type, context, monitor);
				}
			} catch (Exception e) {
				if (LOGGER.isLoggable(Level.SEVERE)) {
					LOGGER.log(Level.SEVERE,
							"Cannot collect Qute data model for the type '" + type.getElementName() + "'.", e);
				}
			}
		}
	}

	private boolean isApplicable(IType type) throws JavaModelException {
		String[] superInterfaceNames = type.getSuperInterfaceNames();
		if (superInterfaceNames == null || superInterfaceNames.length == 0) {
			return false;
		}
		for (String interfaceName : getInterfaceNames()) {
			for (String superInterfaceName : superInterfaceNames) {
				if (interfaceName.endsWith(superInterfaceName)) {
					return true;
				}
			}
		}
		return false;
	}

	protected abstract void processType(IType type, SearchContext context, IProgressMonitor monitor)
			throws JavaModelException;

}

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

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.SearchMatch;

/**
 * Abstract class for properties provider based on type declaration (class,
 * interface, annotation type, etc) search.
 * 
 * @author Angelo ZERR
 *
 */
public abstract class AbstractTypeDeclarationPropertiesProvider extends AbstractPropertiesProvider {

	private static final Logger LOGGER = Logger.getLogger(AbstractTypeDeclarationPropertiesProvider.class.getName());

	@Override
	protected String[] getPatterns() {
		return getTypeNames();
	}

	/**
	 * Returns the type names to search.
	 * 
	 * @return the type names to search.
	 */
	protected abstract String[] getTypeNames();

	@Override
	public void collectProperties(SearchMatch match, SearchContext context, IProgressMonitor monitor) {
		Object element = match.getElement();
		if (element instanceof IType) {
			IType type = (IType) element;
			String className = type.getFullyQualifiedName();
			String[] names = getTypeNames();
			for (String name : names) {
				if (name.equals(className)) {
					try {
						// Collect properties from the class name and stop the loop.
						processClass(type, className, context, monitor);
						break;
					} catch (Exception e) {
						if (LOGGER.isLoggable(Level.SEVERE)) {
							LOGGER.log(Level.SEVERE,
									"Cannot compute MicroProfile properties for the Java class '" + className + "'.",
									e);
						}
					}
				}
			}
		}
	}

	protected abstract void processClass(IType type, String className, SearchContext context, IProgressMonitor monitor)
			throws JavaModelException;
}

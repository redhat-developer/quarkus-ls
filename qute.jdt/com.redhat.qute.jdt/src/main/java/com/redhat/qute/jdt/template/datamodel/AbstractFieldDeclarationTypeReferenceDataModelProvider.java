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

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchPattern;

import com.redhat.qute.jdt.utils.JDTTypeUtils;

/**
 * Abstract class for data model provider based on field declaration type
 * (class, interface, annotation type, etc) search.
 *
 * @author Angelo ZERR
 *
 */
public abstract class AbstractFieldDeclarationTypeReferenceDataModelProvider extends AbstractDataModelProvider {

	private static final Logger LOGGER = Logger
			.getLogger(AbstractFieldDeclarationTypeReferenceDataModelProvider.class.getName());

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
	protected SearchPattern createSearchPattern(String className) {
		return createFieldDeclarationTypeReferenceSearchPattern(className);
	}

	@Override
	public void collectDataModel(SearchMatch match, SearchContext context, IProgressMonitor monitor) {
		Object element = match.getElement();
		if (element instanceof IField) {
			IField field = (IField) element;
			if (!isApplicable(field)) {
				return;
			}
			try {
				// Collect properties from the class name and stop the loop.
				processField(field, context, monitor);
			} catch (Exception e) {
				if (LOGGER.isLoggable(Level.SEVERE)) {
					LOGGER.log(
							Level.SEVERE, "Cannot collect Qute data model for the field '"
									+ field.getDeclaringType().getElementName() + "#" + field.getElementName() + "'.",
							e);
				}
			}
		}
	}

	private boolean isApplicable(IField field) {
		String fieldTypeName = JDTTypeUtils.getResolvedTypeName(field);
		if (fieldTypeName == null) {
			return false;
		}
		for (String typeName : getTypeNames()) {
			if (typeName.endsWith(fieldTypeName)) {
				return true;
			}
		}
		return false;
	}

	protected abstract void processField(IField field, SearchContext context, IProgressMonitor monitor)
			throws JavaModelException;
}

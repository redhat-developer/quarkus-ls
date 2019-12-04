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

import static com.redhat.microprofile.jdt.core.utils.AnnotationUtils.isMatchAnnotation;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IAnnotatable;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchPattern;

import com.redhat.microprofile.commons.metadata.ItemHint;
import com.redhat.microprofile.commons.metadata.ItemHint.ValueHint;
import com.redhat.microprofile.commons.metadata.ItemMetadata;

/**
 * Abstract class for properties provider.
 * 
 * @author Angelo ZERR
 *
 */
public abstract class AbstractPropertiesProvider implements IPropertiesProvider {

	private static final Logger LOGGER = Logger.getLogger(AbstractPropertiesProvider.class.getName());

	protected abstract String[] getAnnotationNames();

	public SearchPattern createSearchPattern() {
		SearchPattern leftPattern = null;
		String[] names = getAnnotationNames();
		for (String name : names) {
			if (leftPattern == null) {
				leftPattern = createAnnotationSearchPattern(name);
			} else {
				SearchPattern rightPattern = createAnnotationSearchPattern(name);
				if (rightPattern != null) {
					leftPattern = SearchPattern.createOrPattern(leftPattern, rightPattern);
				}
			}
		}
		return leftPattern;
	}

	private static SearchPattern createAnnotationSearchPattern(String annotationName) {
		return SearchPattern.createPattern(annotationName, IJavaSearchConstants.ANNOTATION_TYPE,
				IJavaSearchConstants.ANNOTATION_TYPE_REFERENCE, SearchPattern.R_EXACT_MATCH);
	}

	@Override
	public void collectProperties(SearchMatch match, SearchContext context, IPropertiesCollector collector,
			IProgressMonitor monitor) {
		Object element = match.getElement();
		if (element instanceof IAnnotatable && element instanceof IJavaElement) {
			IJavaElement javaElement = (IJavaElement) element;
			processAnnotation(javaElement, context, collector, monitor);
		}
	}

	protected void processAnnotation(IJavaElement javaElement, SearchContext context, IPropertiesCollector collector,
			IProgressMonitor monitor) {
		try {
			String[] names = getAnnotationNames();
			IAnnotation[] annotations = ((IAnnotatable) javaElement).getAnnotations();
			for (IAnnotation annotation : annotations) {
				for (String annotationName : names) {
					if (isMatchAnnotation(annotation, annotationName)) {
						processAnnotation(javaElement, annotation, annotationName, context, collector, monitor);
					}
				}
			}
		} catch (Exception e) {
			if (LOGGER.isLoggable(Level.SEVERE)) {
				LOGGER.log(Level.SEVERE, "Cannot compute MicroProfile properties for the Java element '"
						+ javaElement.getElementName() + "'.", e);
			}
		}
	}

	protected ItemMetadata addItemMetadata(IPropertiesCollector collector, String name, String type, String description,
			String sourceType, String sourceField, String sourceMethod, String defaultValue, String extensionName,
			boolean binary, int phase) {
		return collector.addItemMetadata(name, type, description, sourceType, sourceField, sourceMethod, defaultValue,
				extensionName, binary, phase);
	}

	protected ItemMetadata addItemMetadata(IPropertiesCollector collector, String name, String type, String description,
			String sourceType, String sourceField, String sourceMethod, String defaultValue, String extensionName,
			boolean binary) {
		return addItemMetadata(collector, name, type, description, sourceType, sourceField, sourceMethod, defaultValue,
				extensionName, binary, 0);
	}

	protected String updateHint(IPropertiesCollector collector, IType fieldClass) throws JavaModelException {
		if (fieldClass == null) {
			return null;
		}
		if (fieldClass.isEnum()) {
			// Register Enumeration in "hints" section
			String hint = fieldClass.getFullyQualifiedName();
			if (!collector.hasItemHint(hint)) {
				ItemHint itemHint = collector.getItemHint(hint);
				itemHint.setSourceType(hint);
				if (!fieldClass.isBinary()) {
					itemHint.setSource(Boolean.TRUE);
				}
				IJavaElement[] children = fieldClass.getChildren();
				for (IJavaElement c : children) {
					if (c.getElementType() == IJavaElement.FIELD && ((IField) c).isEnumConstant()) {
						String enumName = ((IField) c).getElementName();
						// TODO: extract Javadoc
						String description = null;
						ValueHint value = new ValueHint();
						value.setValue(enumName);
						itemHint.getValues().add(value);
					}
				}
			}
			return hint;
		}
		return null;
	}

	protected abstract void processAnnotation(IJavaElement javaElement, IAnnotation annotation, String annotationName,
			SearchContext context, IPropertiesCollector collector, IProgressMonitor monitor) throws JavaModelException;

}

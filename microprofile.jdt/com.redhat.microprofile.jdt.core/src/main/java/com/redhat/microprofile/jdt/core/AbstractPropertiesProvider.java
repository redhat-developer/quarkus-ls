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

import static com.redhat.microprofile.jdt.core.utils.JDTTypeUtils.findType;
import static com.redhat.microprofile.jdt.core.utils.JDTTypeUtils.getOptionalTypeParameter;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
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

	/**
	 * Returns the Java search pattern.
	 * 
	 * @return the Java search pattern.
	 */
	protected abstract String[] getPatterns();

	/**
	 * Return an instance of search pattern.
	 */
	public SearchPattern createSearchPattern() {
		SearchPattern leftPattern = null;
		String[] patterns = getPatterns();
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
	protected static SearchPattern createAnnotationTypeDeclarationSearchPattern(String annotationName) {
		return SearchPattern.createPattern(annotationName, IJavaSearchConstants.ANNOTATION_TYPE,
				IJavaSearchConstants.DECLARATIONS, SearchPattern.R_EXACT_MATCH);
	}

	/**
	 * Add item metadata.
	 * 
	 * @param collector     the properties collector.
	 * @param name          the property name.
	 * @param type          the type of the property.
	 * @param description   the description of the property.
	 * @param sourceType    the source type (class or interface) of the property.
	 * @param sourceField   the source field (field name) and null otherwise.
	 * @param sourceMethod  the source method (signature method) and null otherwise.
	 * @param defaultValue  the default vaue and null otherwise.
	 * @param extensionName the extension name and null otherwise.
	 * @param binary        true if the property comes from a JAR and false
	 *                      otherwise.
	 * @param phase         teh Quarkus config phase.
	 * @return the item metadata.
	 */
	protected ItemMetadata addItemMetadata(IPropertiesCollector collector, String name, String type, String description,
			String sourceType, String sourceField, String sourceMethod, String defaultValue, String extensionName,
			boolean binary, int phase) {
		return collector.addItemMetadata(name, type, description, sourceType, sourceField, sourceMethod, defaultValue,
				extensionName, binary, phase);
	}

	/**
	 * Add item metadata.
	 * 
	 * @param collector     the properties collector.
	 * @param name          the property name.
	 * @param type          the type of the property.
	 * @param description   the description of the property.
	 * @param sourceType    the source type (class or interface) of the property.
	 * @param sourceField   the source field (field name) and null otherwise.
	 * @param sourceMethod  the source method (signature method) and null otherwise.
	 * @param defaultValue  the default vaue and null otherwise.
	 * @param extensionName the extension name and null otherwise.
	 * @param binary        true if the property comes from a JAR and false
	 *                      otherwise.
	 * @return the item metadata.
	 */
	protected ItemMetadata addItemMetadata(IPropertiesCollector collector, String name, String type, String description,
			String sourceType, String sourceField, String sourceMethod, String defaultValue, String extensionName,
			boolean binary) {
		return addItemMetadata(collector, name, type, description, sourceType, sourceField, sourceMethod, defaultValue,
				extensionName, binary, 0);
	}

	/**
	 * Get or create the update hint from the given type.
	 * 
	 * @param collector
	 * @param type        the JDT type and null otherwise.
	 * @param typeName    the type name which is the string of the JDT type.
	 * @param javaProject the java project where the JDT type belong to.
	 * @return the hint name.
	 * @throws JavaModelException
	 */
	protected String updateHint(IPropertiesCollector collector, IType type, String typeName, IJavaProject javaProject)
			throws JavaModelException {
		// type name is the string of the JDT type (which could be null if type is not
		// retrieved)
		String enclosedType = typeName;
		if (type == null) {
			// JDT type is null, in some case it's because type is optional (ex :
			// java.util.Optional<MyType>)
			// try to extract the enclosed type from the optional type (to get 'MyType' )
			enclosedType = getOptionalTypeParameter(typeName);
			if (enclosedType != null) {
				type = findType(javaProject, enclosedType);
			}
			if (type == null) {
				return null;
			}
		}
		if (type.isEnum()) {
			// Register Enumeration in "hints" section
			String hint = enclosedType;
			if (!collector.hasItemHint(hint)) {
				ItemHint itemHint = collector.getItemHint(hint);
				itemHint.setSourceType(hint);
				if (!type.isBinary()) {
					itemHint.setSource(Boolean.TRUE);
				}
				IJavaElement[] children = type.getChildren();
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

}

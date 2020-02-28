/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.microprofile.jdt.internal.config.properties;

import static com.redhat.microprofile.jdt.core.utils.AnnotationUtils.getAnnotationMemberValue;
import static com.redhat.microprofile.jdt.core.utils.JDTTypeUtils.findType;
import static com.redhat.microprofile.jdt.core.utils.JDTTypeUtils.getEnclosedType;
import static com.redhat.microprofile.jdt.core.utils.JDTTypeUtils.getPropertyType;
import static com.redhat.microprofile.jdt.core.utils.JDTTypeUtils.getResolvedTypeName;
import static com.redhat.microprofile.jdt.core.utils.JDTTypeUtils.getSourceField;
import static com.redhat.microprofile.jdt.core.utils.JDTTypeUtils.getSourceType;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

import com.redhat.microprofile.jdt.core.AbstractAnnotationTypeReferencePropertiesProvider;
import com.redhat.microprofile.jdt.core.IPropertiesCollector;
import com.redhat.microprofile.jdt.core.MicroProfileConfigConstants;
import com.redhat.microprofile.jdt.core.SearchContext;

/**
 * Properties provider to collect MicroProfile properties from the Java fields
 * annotated with "org.eclipse.microprofile.config.inject.ConfigProperty"
 * annotation.
 * 
 * @author Angelo ZERR
 *
 */
public class MicroProfileConfigPropertyProvider extends AbstractAnnotationTypeReferencePropertiesProvider {

	private static final String[] ANNOTATION_NAMES = { MicroProfileConfigConstants.CONFIG_PROPERTY_ANNOTATION };

	@Override
	protected String[] getAnnotationNames() {
		return ANNOTATION_NAMES;
	}

	@Override
	protected void processAnnotation(IJavaElement javaElement, IAnnotation configPropertyAnnotation,
			String annotationName, SearchContext context, IProgressMonitor monitor) throws JavaModelException {
		if (javaElement.getElementType() == IJavaElement.FIELD) {
			IPropertiesCollector collector = context.getCollector();
			String name = getAnnotationMemberValue(configPropertyAnnotation,
					MicroProfileConfigConstants.CONFIG_PROPERTY_ANNOTATION_NAME);
			if (name != null && !name.isEmpty()) {
				IField field = (IField) javaElement;
				String fieldTypeName = getResolvedTypeName(field);
				IType fieldClass = findType(field.getJavaProject(), fieldTypeName);

				String type = getPropertyType(fieldClass, fieldTypeName);
				String description = null;
				String sourceType = getSourceType(field);
				String sourceField = getSourceField(field);
				String defaultValue = getAnnotationMemberValue(configPropertyAnnotation,
						MicroProfileConfigConstants.CONFIG_PROPERTY_ANNOTATION_DEFAULT_VALUE);
				String extensionName = null;

				// Enumerations
				IType enclosedType = getEnclosedType(fieldClass, type, field.getJavaProject());
				super.updateHint(collector, enclosedType);

				addItemMetadata(collector, name, type, description, sourceType, sourceField, null, defaultValue,
						extensionName, field.isBinary());
			}
		}
	}

}

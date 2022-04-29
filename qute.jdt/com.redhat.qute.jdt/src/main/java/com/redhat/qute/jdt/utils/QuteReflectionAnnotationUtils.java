/*******************************************************************************
* Copyright (c) 2022 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.jdt.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

import com.redhat.qute.commons.RegisterForReflectionAnnotation;
import com.redhat.qute.commons.ResolvedJavaTypeInfo;
import com.redhat.qute.commons.TemplateDataAnnotation;
import com.redhat.qute.jdt.internal.QuteJavaConstants;

/**
 * Utilities for collecting @TemplateData and @RegisterForReflection.
 * 
 * @author Angelo ZERR
 *
 */
public class QuteReflectionAnnotationUtils {

	private static final Logger LOGGER = Logger.getLogger(QuteReflectionAnnotationUtils.class.getName());
	private static final String TRUE_VALUE = "true";
	private static final String FALSE_VALUE = "false";

	/**
	 * Collect
	 * 
	 * @param resolvedType
	 * @param type
	 * @throws JavaModelException
	 */
	public static void collectAnnotations(ResolvedJavaTypeInfo resolvedType, IType type) throws JavaModelException {
		List<TemplateDataAnnotation> templateDataAnnotations = null;
		RegisterForReflectionAnnotation registerForReflectionAnnotation = null;
		IAnnotation[] annotations = type.getAnnotations();
		for (IAnnotation annotation : annotations) {
			if (AnnotationUtils.isMatchAnnotation(annotation, QuteJavaConstants.TEMPLATE_DATA_ANNOTATION)) {
				// @TemplateData
				if (templateDataAnnotations == null) {
					templateDataAnnotations = new ArrayList<>();
				}
				templateDataAnnotations.add(createTemplateData(annotation));
			} else if (AnnotationUtils.isMatchAnnotation(annotation,
					QuteJavaConstants.REGISTER_FOR_REFLECTION_ANNOTATION)) {
				// @RegisterForReflection
				registerForReflectionAnnotation = createRegisterForReflection(annotation);
			}
		}
		resolvedType.setTemplateDataAnnotations(templateDataAnnotations);
		resolvedType.setRegisterForReflectionAnnotation(registerForReflectionAnnotation);
	}

	private static TemplateDataAnnotation createTemplateData(IAnnotation templateDataAnnotation) {
		TemplateDataAnnotation templateData = new TemplateDataAnnotation();
		try {
			// @TemplateData/ignoreSuperclasses
			String ignoreSuperclasses = AnnotationUtils.getAnnotationMemberValue(templateDataAnnotation,
					QuteJavaConstants.TEMPLATE_DATA_ANNOTATION_IGNORE_SUPER_CLASSES);
			if (TRUE_VALUE.equals(ignoreSuperclasses)) {
				templateData.setIgnoreSuperclasses(Boolean.TRUE);
			}
		} catch (JavaModelException e) {
			LOGGER.log(Level.SEVERE,
					"Error while getting member values of '" + templateDataAnnotation.getElementName() + "'.", e);
		}
		return templateData;
	}

	private static RegisterForReflectionAnnotation createRegisterForReflection(
			IAnnotation registerForReflectionAnnotation) {
		RegisterForReflectionAnnotation registerForReflection = new RegisterForReflectionAnnotation();
		try {
			// @RegisterForReflection/methods
			String methods = AnnotationUtils.getAnnotationMemberValue(registerForReflectionAnnotation,
					QuteJavaConstants.REGISTER_FOR_REFLECTION_ANNOTATION_METHODS);
			if (FALSE_VALUE.equals(methods)) {
				registerForReflection.setMethods(Boolean.FALSE);
			}
			// @RegisterForReflection/fields
			String fields = AnnotationUtils.getAnnotationMemberValue(registerForReflectionAnnotation,
					QuteJavaConstants.REGISTER_FOR_REFLECTION_ANNOTATION_FIELDS);
			if (FALSE_VALUE.equals(fields)) {
				registerForReflection.setFields(Boolean.FALSE);
			}
		} catch (JavaModelException e) {
			LOGGER.log(Level.SEVERE,
					"Error while getting member values of '" + registerForReflectionAnnotation.getElementName() + "'.",
					e);
		}
		return registerForReflection;
	}
}

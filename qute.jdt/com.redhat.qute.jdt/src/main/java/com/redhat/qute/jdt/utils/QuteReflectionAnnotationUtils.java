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

import static com.redhat.qute.jdt.internal.QuteJavaConstants.REGISTER_FOR_REFLECTION_ANNOTATION;
import static com.redhat.qute.jdt.internal.QuteJavaConstants.REGISTER_FOR_REFLECTION_ANNOTATION_FIELDS;
import static com.redhat.qute.jdt.internal.QuteJavaConstants.REGISTER_FOR_REFLECTION_ANNOTATION_METHODS;
import static com.redhat.qute.jdt.internal.QuteJavaConstants.REGISTER_FOR_REFLECTION_ANNOTATION_TARGETS;
import static com.redhat.qute.jdt.internal.QuteJavaConstants.TEMPLATE_DATA_ANNOTATION;
import static com.redhat.qute.jdt.internal.QuteJavaConstants.TEMPLATE_DATA_ANNOTATION_IGNORE;
import static com.redhat.qute.jdt.internal.QuteJavaConstants.TEMPLATE_DATA_ANNOTATION_IGNORE_SUPER_CLASSES;
import static com.redhat.qute.jdt.internal.QuteJavaConstants.TEMPLATE_DATA_ANNOTATION_PROPERTIES;
import static com.redhat.qute.jdt.internal.QuteJavaConstants.TEMPLATE_DATA_ANNOTATION_TARGET;
import static com.redhat.qute.jdt.utils.AnnotationUtils.getValueAsArray;
import static com.redhat.qute.jdt.utils.AnnotationUtils.getValueAsBoolean;
import static com.redhat.qute.jdt.utils.AnnotationUtils.getValueAsString;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;

import com.redhat.qute.commons.ResolvedJavaTypeInfo;
import com.redhat.qute.commons.annotations.RegisterForReflectionAnnotation;
import com.redhat.qute.commons.annotations.TemplateDataAnnotation;
import com.redhat.qute.jdt.internal.resolver.ITypeResolver;

/**
 * Utilities for collecting @TemplateData and @RegisterForReflection.
 * 
 * @author Angelo ZERR
 *
 * @see <a href=
 *      "https://quarkus.io/guides/qute-reference#template_data">@TemplateData</a>
 * @see <a href=
 *      "https://quarkus.io/guides/writing-native-applications-tips#registerForReflection">Using
 *      the @RegisterForReflection annotation</a>
 */
public class QuteReflectionAnnotationUtils {

	private static final Logger LOGGER = Logger.getLogger(QuteReflectionAnnotationUtils.class.getName());

	/**
	 * Collect @TemplateData and @RegisterForReflection annotations from the given
	 * Java type.
	 * 
	 * @param resolvedType the Java type to update.
	 * @param type         the JDT Java type.
	 * @param typeResolver the Java type resolver.
	 * @throws JavaModelException
	 */
	public static void collectAnnotations(ResolvedJavaTypeInfo resolvedType, IType type, ITypeResolver typeResolver)
			throws JavaModelException {
		List<TemplateDataAnnotation> templateDataAnnotations = null;
		RegisterForReflectionAnnotation registerForReflectionAnnotation = null;
		IAnnotation[] annotations = type.getAnnotations();
		for (IAnnotation annotation : annotations) {
			if (AnnotationUtils.isMatchAnnotation(annotation, TEMPLATE_DATA_ANNOTATION)) {
				// @TemplateData
				if (templateDataAnnotations == null) {
					templateDataAnnotations = new ArrayList<>();
				}
				templateDataAnnotations.add(createTemplateData(annotation, typeResolver));
			} else if (AnnotationUtils.isMatchAnnotation(annotation, REGISTER_FOR_REFLECTION_ANNOTATION)) {
				// @RegisterForReflection
				registerForReflectionAnnotation = createRegisterForReflection(annotation, typeResolver);
			}
		}
		resolvedType.setTemplateDataAnnotations(templateDataAnnotations);
		resolvedType.setRegisterForReflectionAnnotation(registerForReflectionAnnotation);
	}

	private static TemplateDataAnnotation createTemplateData(IAnnotation templateDataAnnotation,
			ITypeResolver typeResolver) {
		TemplateDataAnnotation templateData = new TemplateDataAnnotation();
		try {
			// Loop for attributes of the @TemplateData annotation
			for (IMemberValuePair pair : templateDataAnnotation.getMemberValuePairs()) {
				switch (pair.getMemberName()) {

				// @TemplateData/ignoreSuperclasses
				case TEMPLATE_DATA_ANNOTATION_IGNORE_SUPER_CLASSES: {
					// @TemplateData(ignoreSuperclasses = true)
					// public class Item
					Boolean ignoreSuperclasses = getValueAsBoolean(pair);
					if (Boolean.TRUE.equals(ignoreSuperclasses)) {
						templateData.setIgnoreSuperclasses(ignoreSuperclasses);
					}
					break;
				}

				// @TemplateData/target
				case TEMPLATE_DATA_ANNOTATION_TARGET: {
					// @TemplateData(target = BigDecimal.class)
					// public class Item
					String target = getValueAsString(pair);
					if (target != null) {
						// here target is equals to "BigDecimal", we must resolve it to have
						// "java.math.BigDecimal"
						target = resolveTarget(target, typeResolver);
						templateData.setTarget(target);
					}
					break;
				}

				// @TemplateData/ignore
				case TEMPLATE_DATA_ANNOTATION_IGNORE: {
					List<String> ignore = null;
					Object[] values = getValueAsArray(pair);
					if (values != null && values.length > 0) {
						ignore = new ArrayList<>(values.length);
						for (int i = 0; i < values.length; i++) {
							String ignoreItem = values[i] != null ? values[i].toString() : null;
							if (ignoreItem != null) {
								ignore.add(ignoreItem);
							}
						}
					}
					templateData.setIgnore(ignore);
					break;
				}

				// @TemplateData/properties
				case TEMPLATE_DATA_ANNOTATION_PROPERTIES: {
					// @TemplateData(properties = true)
					// public class Item
					Boolean properties = getValueAsBoolean(pair);
					if (Boolean.TRUE.equals(properties)) {
						templateData.setProperties(properties);
					}
					break;
				}

				}
			}
		} catch (JavaModelException e) {
			LOGGER.log(Level.SEVERE,
					"Error while getting member values of '" + templateDataAnnotation.getElementName() + "'.", e);
		}
		return templateData;
	}

	private static RegisterForReflectionAnnotation createRegisterForReflection(
			IAnnotation registerForReflectionAnnotation, ITypeResolver typeResolver) {
		RegisterForReflectionAnnotation registerForReflection = new RegisterForReflectionAnnotation();
		try {

			// Loop for attributes of the @RegisterForReflection annotation
			for (IMemberValuePair pair : registerForReflectionAnnotation.getMemberValuePairs()) {
				switch (pair.getMemberName()) {

				// @RegisterForReflection/methods
				case REGISTER_FOR_REFLECTION_ANNOTATION_METHODS: {
					// @RegisterForReflection(methods = false)
					// public class Item
					Boolean methods = getValueAsBoolean(pair);
					if (Boolean.FALSE.equals(methods)) {
						registerForReflection.setMethods(methods);
					}
					break;
				}

				// @RegisterForReflection/fields
				case REGISTER_FOR_REFLECTION_ANNOTATION_FIELDS: {
					// @RegisterForReflection(fields = false)
					// public class Item
					Boolean fields = getValueAsBoolean(pair);
					if (Boolean.FALSE.equals(fields)) {
						registerForReflection.setFields(fields);
					}
					break;
				}

				// @RegisterForReflection/targets
				case REGISTER_FOR_REFLECTION_ANNOTATION_TARGETS: {
					// @RegisterForReflection(targets = {BigDecimal.class, String.class})
					// public class Item
					List<String> targets = null;
					if (pair.getValue() != null && pair.getValue() instanceof Object[]) {
						Object[] values = getValueAsArray(pair);
						if (values != null && values.length > 0) {
							targets = new ArrayList<>(values.length);
							for (int i = 0; i < values.length; i++) {
								String target = values[i] != null ? values[i].toString() : null;
								if (target != null) {
									// here target is equals to "BigDecimal", we must resolve it to have
									// "java.math.BigDecimal"
									target = resolveTarget(target, typeResolver);
									targets.add(target);
								}
							}
						}
					}
					registerForReflection.setTargets(targets);
					break;
				}

				}

			}
		} catch (JavaModelException e) {
			LOGGER.log(Level.SEVERE,
					"Error while getting member values of '" + registerForReflectionAnnotation.getElementName() + "'.",
					e);
		}
		return registerForReflection;
	}

	private static String resolveTarget(String target, ITypeResolver typeResolver) {
		//
		return typeResolver.resolveTypeSignature(Signature.C_UNRESOLVED + target + Signature.C_NAME_END, null);
	}
}

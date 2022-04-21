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
package com.redhat.qute.jdt.internal.template.datamodel;

import static com.redhat.qute.jdt.internal.QuteJavaConstants.TEMPLATE_DATA_ANNOTATION;
import static com.redhat.qute.jdt.internal.QuteJavaConstants.TEMPLATE_ENUM_ANNOTATION;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IAnnotatable;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

import com.redhat.qute.commons.datamodel.resolvers.ValueResolverInfo;
import com.redhat.qute.jdt.QuteSupportForTemplate;
import com.redhat.qute.jdt.internal.resolver.ITypeResolver;
import com.redhat.qute.jdt.template.datamodel.AbstractAnnotationTypeReferenceDataModelProvider;
import com.redhat.qute.jdt.template.datamodel.SearchContext;
import com.redhat.qute.jdt.utils.AnnotationUtils;

/**
 * @TemplateEnum annotation support.
 *
 *
 *               <code>
 * &#64;TemplateEnum
 * public enum Status {
 * 		ON,
 * 		OFF
 * }
 * </code>
 *
 * @see https://quarkus.io/guides/qute-reference#convenient-annotation-for-enums
 *
 */
public class TemplateEnumAnnotationSupport extends AbstractAnnotationTypeReferenceDataModelProvider {

	private static final Logger LOGGER = Logger.getLogger(TemplateEnumAnnotationSupport.class.getName());

	private static final String[] ANNOTATION_NAMES = {
		TEMPLATE_ENUM_ANNOTATION
	};

	@Override
	protected String[] getAnnotationNames() {
		return ANNOTATION_NAMES;
	}

	@Override
	protected void processAnnotation(IJavaElement javaElement, IAnnotation annotation, String annotationName,
		SearchContext context, IProgressMonitor monitor) throws JavaModelException {
		if (javaElement.getElementType() != IJavaElement.TYPE) {
			return;
		}
		IType type = (IType) javaElement;
		if (!type.isEnum()) {
			// @TemplateEnum declared on a non-enum class is ignored.
			return;
		}
		IAnnotation templateEnum = AnnotationUtils.getAnnotation((IAnnotatable) javaElement, TEMPLATE_ENUM_ANNOTATION);
		if (templateEnum == null) {
			return;
		}
		// Check if type is annotated with @TemplateData
		IAnnotation templateData = AnnotationUtils.getAnnotation((IAnnotatable) javaElement, TEMPLATE_DATA_ANNOTATION);
		if (templateData != null) {
			// Also if an enum also declares the @TemplateData annotation then the
			// @TemplateEnum annotation is ignored.
			return;
		}
		collectResolversForTemplateEnum(type, context.getDataModelProject().getValueResolvers(), monitor);
	}

	private static void collectResolversForTemplateEnum(IType type, List<ValueResolverInfo> resolvers,
		IProgressMonitor monitor) {
		try {
			ITypeResolver typeResolver = QuteSupportForTemplate.createTypeResolver(type);
			IField[] fields = type.getFields();
			for (IField field : fields) {
				collectResolversForTemplateEnum(field, resolvers, typeResolver);
			}
		} catch (JavaModelException e) {
			LOGGER.log(Level.SEVERE, "Error while getting methods of '" + type.getElementName() + "'.", e);
		}
	}

	private static void collectResolversForTemplateEnum(IField field, List<ValueResolverInfo> resolvers,
		ITypeResolver typeResolver) {
		ValueResolverInfo resolver = new ValueResolverInfo();
		resolver.setSourceType(field.getDeclaringType().getFullyQualifiedName());
		resolver.setSignature(typeResolver.resolveFieldSignature(field));
		// This annotation is functionally equivalent to @TemplateData(namespace =
		// TemplateData.SIMPLENAME),
		// i.e. a namespace resolver is automatically generated for the target enum and
		// the simple name of the target enum is used as the namespace.
		resolver.setNamespace(field.getDeclaringType().getElementName());
		if (!resolvers.contains(resolver)) {
			resolvers.add(resolver);
		}
	}
}

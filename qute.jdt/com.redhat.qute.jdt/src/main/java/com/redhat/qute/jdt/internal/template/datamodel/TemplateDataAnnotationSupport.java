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
import static com.redhat.qute.jdt.internal.QuteJavaConstants.TEMPLATE_DATA_ANNOTATION_NAMESPACE;

import java.lang.reflect.Modifier;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IAnnotatable;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

import com.redhat.qute.commons.datamodel.resolvers.ValueResolverInfo;
import com.redhat.qute.jdt.QuteSupportForTemplate;
import com.redhat.qute.jdt.internal.resolver.ITypeResolver;
import com.redhat.qute.jdt.template.datamodel.AbstractAnnotationTypeReferenceDataModelProvider;
import com.redhat.qute.jdt.template.datamodel.SearchContext;
import com.redhat.qute.jdt.utils.AnnotationUtils;

/**
 * @TemplateData annotation support.
 *
 *               <code>
 * &#64;TemplateData
 * class Item {
 *
 * 		public final BigDecimal price;
 *
 * 		public Item(BigDecimal price) {
 * 			this.price = price;
 * 		}
 *
 * 		public BigDecimal getDiscountedPrice() {
 * 			return price.multiply(new BigDecimal("0.9"));
 * 		}
 * }
 * </code>
 *
 *
 * @see https://quarkus.io/guides/qute-reference#template_data
 *
 */
public class TemplateDataAnnotationSupport extends AbstractAnnotationTypeReferenceDataModelProvider {

	private static final Logger LOGGER = Logger.getLogger(TemplateDataAnnotationSupport.class.getName());

	private static final String[] ANNOTATION_NAMES = {
			TEMPLATE_DATA_ANNOTATION

	};

	@Override
	protected String[] getAnnotationNames() {
		return ANNOTATION_NAMES;
	}

	@Override
	protected void processAnnotation(IJavaElement javaElement, IAnnotation annotation, String annotationName,
			SearchContext context, IProgressMonitor monitor) throws JavaModelException {
		if (!(javaElement instanceof IAnnotatable)) {
			return;
		}
		IAnnotation templateData = AnnotationUtils.getAnnotation((IAnnotatable) javaElement, TEMPLATE_DATA_ANNOTATION);
		if (templateData == null) {
			return;
		}
		if (javaElement instanceof IType) {
			IType type = (IType) javaElement;
			collectResolversForTemplateData(type, templateData, context.getDataModelProject().getValueResolvers(),
					monitor);
		}
	}

	private static void collectResolversForTemplateData(IType type, IAnnotation templateData,
			List<ValueResolverInfo> resolvers, IProgressMonitor monitor) {
		try {
			ITypeResolver typeResolver = QuteSupportForTemplate.createTypeResolver(type);
			IField[] fields = type.getFields();
			for (IField field : fields) {
				if (Modifier.isPublic(field.getFlags()) && Modifier.isStatic(field.getFlags())) {
					collectResolversForTemplateData(field, templateData, resolvers, typeResolver);
				}
			}
			IMethod[] methods = type.getMethods();
			for (IMethod method : methods) {
				if (Modifier.isPublic(method.getFlags()) && Modifier.isStatic(method.getFlags())) {
					collectResolversForTemplateData(method, templateData, resolvers, typeResolver);
				}
			}
		} catch (JavaModelException e) {
			LOGGER.log(Level.SEVERE, "Error while getting methods of '" + type.getElementName() + "'.", e);
		}
	}

	private static void collectResolversForTemplateData(IField field, IAnnotation templateData,
			List<ValueResolverInfo> resolvers, ITypeResolver typeResolver) {
		ValueResolverInfo resolver = new ValueResolverInfo();
		resolver.setSourceType(field.getDeclaringType().getFullyQualifiedName());
		resolver.setSignature(typeResolver.resolveFieldSignature(field));
		try {
			String namespace = AnnotationUtils.getAnnotationMemberValue(templateData,
					TEMPLATE_DATA_ANNOTATION_NAMESPACE);
			resolver.setNamespace(StringUtils.isNotEmpty(namespace) ? namespace
					: field.getDeclaringType().getFullyQualifiedName().replace('.', '_'));
		} catch (JavaModelException e) {
			LOGGER.log(Level.SEVERE, "Error while getting annotation member value of '" + field.getElementName() + "'.",
					e);
		}
		if (!resolvers.contains(resolver)) {
			resolvers.add(resolver);
		}
	}

	private static void collectResolversForTemplateData(IMethod method, IAnnotation templateData,
			List<ValueResolverInfo> resolvers, ITypeResolver typeResolver) {
		ValueResolverInfo resolver = new ValueResolverInfo();
		resolver.setSourceType(method.getDeclaringType().getFullyQualifiedName());
		resolver.setSignature(typeResolver.resolveMethodSignature(method));
		try {
			String namespace = AnnotationUtils.getAnnotationMemberValue(templateData,
					TEMPLATE_DATA_ANNOTATION_NAMESPACE);
			resolver.setNamespace(StringUtils.isNotEmpty(namespace) ? namespace
					: method.getDeclaringType().getFullyQualifiedName().replace('.', '_'));
		} catch (JavaModelException e) {
			LOGGER.log(Level.SEVERE,
					"Error while getting annotation member value of '" + method.getElementName() + "'.", e);
		}
		if (!resolvers.contains(resolver)) {
			resolvers.add(resolver);
		}
	}
}

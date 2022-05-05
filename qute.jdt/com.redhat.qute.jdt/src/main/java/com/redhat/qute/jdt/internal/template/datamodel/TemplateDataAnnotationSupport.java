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
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
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

	private static final String[] ANNOTATION_NAMES = { TEMPLATE_DATA_ANNOTATION };

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
		// Check if the given Java type defines a @TemplateData/namespace

		// Loop for @TemplateData annotations:
		// Ex :
		// @TemplateData
		// @TemplateData(namespace = "foo")
		// public class Item
		String namespace = null;
		boolean hasTemplateData = false;
		for (IAnnotation typeAnnotation : type.getAnnotations()) {
			if (AnnotationUtils.isMatchAnnotation(typeAnnotation, TEMPLATE_DATA_ANNOTATION)) {
				hasTemplateData = true;
				namespace = AnnotationUtils.getAnnotationMemberValue(typeAnnotation,
						TEMPLATE_DATA_ANNOTATION_NAMESPACE);
				if (StringUtils.isNotEmpty(namespace)) {
					break;
				}
			}
		}
		if (!hasTemplateData) {
			return;
		}

		ITypeResolver typeResolver = QuteSupportForTemplate.createTypeResolver(type);

		// Loop for static fields
		IField[] fields = type.getFields();
		for (IField field : fields) {
			collectResolversForTemplateData(field, namespace, context.getDataModelProject().getValueResolvers(),
					typeResolver, monitor);
		}

		// Loop for static methods
		IMethod[] methods = type.getMethods();
		for (IMethod method : methods) {
			collectResolversForTemplateData(method, namespace, context.getDataModelProject().getValueResolvers(),
					typeResolver, monitor);
		}
	}

	private void collectResolversForTemplateData(IMember member, String namespace, List<ValueResolverInfo> resolvers,
			ITypeResolver typeResolver, IProgressMonitor monitor) {
		try {
			if (Modifier.isPublic(member.getFlags()) && Modifier.isStatic(member.getFlags())) {
				// The field, method is public and static
				String sourceType = member.getDeclaringType().getFullyQualifiedName();
				ValueResolverInfo resolver = new ValueResolverInfo();
				resolver.setSourceType(sourceType);
				resolver.setSignature(getSignature(member, typeResolver));
				resolver.setNamespace(StringUtils.isNotEmpty(namespace) ? namespace : sourceType.replace('.', '_'));
				if (!resolvers.contains(resolver)) {
					resolvers.add(resolver);
				}
			}
		} catch (JavaModelException e) {
			LOGGER.log(Level.SEVERE,
					"Error while getting annotation member value of '" + member.getElementName() + "'.", e);
		}
	}

	private static String getSignature(IMember javaMember, ITypeResolver typeResolver) {
		switch (javaMember.getElementType()) {
		case IJavaElement.FIELD:
			return typeResolver.resolveFieldSignature((IField) javaMember);
		case IJavaElement.METHOD:
			return typeResolver.resolveMethodSignature((IMethod) javaMember);
		}
		return null;
	}

}

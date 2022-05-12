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

import static com.redhat.qute.jdt.internal.QuteJavaConstants.TEMPLATE_EXTENSION_ANNOTATION;
import static com.redhat.qute.jdt.internal.QuteJavaConstants.TEMPLATE_EXTENSION_ANNOTATION_MATCH_NAME;
import static com.redhat.qute.jdt.internal.QuteJavaConstants.TEMPLATE_EXTENSION_ANNOTATION_NAMESPACE;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IAnnotatable;
import org.eclipse.jdt.core.IAnnotation;
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
import com.redhat.qute.jdt.utils.JDTTypeUtils;

/**
 * @TemplateExtension annotation support.
 *
 *                    <code>
 * class Item {
 *
 * 		public final BigDecimal price;
 *
 * 		public Item(BigDecimal price) {
 * 			this.price = price;
 * 		}
 * }
 *
 * &#64;TemplateExtension
 * class MyExtensions {
 * 		static BigDecimal discountedPrice(Item item) {
 * 			return item.getPrice().multiply(new BigDecimal("0.9"));
 * 		}
 * }
 * </code>
 *
 *
 * @author Angelo ZERR
 *
 * @see https://quarkus.io/guides/qute-reference#template_extension_methods
 *
 */
public class TemplateExtensionAnnotationSupport extends AbstractAnnotationTypeReferenceDataModelProvider {

	private static final Logger LOGGER = Logger.getLogger(TemplateExtensionAnnotationSupport.class.getName());

	private static final String[] ANNOTATION_NAMES = {
		TEMPLATE_EXTENSION_ANNOTATION
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
		IAnnotation templateExtension = AnnotationUtils.getAnnotation((IAnnotatable) javaElement,
			TEMPLATE_EXTENSION_ANNOTATION);
		if (templateExtension == null) {
			return;
		}
		if (javaElement instanceof IType) {
			IType type = (IType) javaElement;
			collectResolversForTemplateExtension(type, templateExtension,
				context.getDataModelProject().getValueResolvers(), monitor);
		} else if (javaElement instanceof IMethod) {
			IMethod method = (IMethod) javaElement;
			collectResolversForTemplateExtension(method, templateExtension,
				context.getDataModelProject().getValueResolvers(), monitor);
		}
	}

	private static void collectResolversForTemplateExtension(IType type, IAnnotation templateExtension,
		List<ValueResolverInfo> resolvers, IProgressMonitor monitor) {
		try {
			ITypeResolver typeResolver = QuteSupportForTemplate.createTypeResolver(type);
			IMethod[] methods = type.getMethods();
			for (IMethod method : methods) {
				if (isTemplateExtensionMethod(method)) {
					IAnnotation methodTemplateExtension = AnnotationUtils.getAnnotation(method,
						TEMPLATE_EXTENSION_ANNOTATION);
					collectResolversForTemplateExtension(method,
						methodTemplateExtension != null ? methodTemplateExtension : templateExtension, resolvers,
						typeResolver);
				}
			}
		} catch (JavaModelException e) {
			LOGGER.log(Level.SEVERE, "Error while getting methods of '" + type.getElementName() + "'.", e);
		}
	}

	/**
	 * Returns true if the given method <code>method</code> is a template extension
	 * method and false otherwise.
	 *
	 * A template extension method:
	 *
	 * <ul>
	 * <li>must not be private</li>
	 * <li>must be static,</li>
	 * <li>must not return void.</li>
	 * </ul>
	 *
	 * @param method the method to check.
	 * @return true if the given method <code>method</code> is a template extension
	 *         method and false otherwise.
	 */
	private static boolean isTemplateExtensionMethod(IMethod method) {
		try {
			return !method.isConstructor() /* && Flags.isPublic(method.getFlags()) */
				&& !JDTTypeUtils.isVoidReturnType(method);
		} catch (JavaModelException e) {
			LOGGER.log(Level.SEVERE, "Error while getting method information of '" + method.getElementName() + "'.", e);
			return false;
		}
	}

	public static void collectResolversForTemplateExtension(IMethod method, IAnnotation templateExtension,
		List<ValueResolverInfo> resolvers, IProgressMonitor monitor) {
		if (isTemplateExtensionMethod(method)) {
			ITypeResolver typeResolver = QuteSupportForTemplate.createTypeResolver(method);
			collectResolversForTemplateExtension(method, templateExtension, resolvers, typeResolver);
		}
	}

	private static void collectResolversForTemplateExtension(IMethod method, IAnnotation templateExtension,
		List<ValueResolverInfo> resolvers, ITypeResolver typeResolver) {
		ValueResolverInfo resolver = new ValueResolverInfo();
		resolver.setSourceType(method.getDeclaringType().getFullyQualifiedName());
		resolver.setSignature(typeResolver.resolveMethodSignature(method));
		try {
			String namespace = AnnotationUtils.getAnnotationMemberValue(templateExtension,
				TEMPLATE_EXTENSION_ANNOTATION_NAMESPACE);
			String matchName = AnnotationUtils.getAnnotationMemberValue(templateExtension,
				TEMPLATE_EXTENSION_ANNOTATION_MATCH_NAME);
			resolver.setNamespace(namespace);
			if (StringUtils.isNotEmpty(matchName)) {
				resolver.setMatchName(matchName);
			}
		} catch (JavaModelException e) {
			LOGGER.log(Level.SEVERE,
				"Error while getting annotation member value of '" + method.getElementName() + "'.", e);
		}
		if (!resolvers.contains(resolver)) {
			resolvers.add(resolver);
		}
	}
}

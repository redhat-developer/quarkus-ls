/*******************************************************************************
* Copyright (c) 2021 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.jdt.internal.template;

import static com.redhat.qute.jdt.internal.QuteJavaConstants.TEMPLATE_EXTENSION_ANNOTATION_NAMESPACE;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

import com.redhat.qute.commons.ValueResolver;
import com.redhat.qute.jdt.QuteSupportForTemplate;
import com.redhat.qute.jdt.internal.resolver.ITypeResolver;
import com.redhat.qute.jdt.utils.AnnotationUtils;

/**
 * Template extension support.
 * 
 * @author Angelo ZERR
 * 
 * @see https://quarkus.io/guides/qute-reference#template_extension_methods
 *
 */
class TemplateExtensionSupport {

	private static final Logger LOGGER = Logger.getLogger(TemplateExtensionSupport.class.getName());

	public static void collectResolversForTemplateExtension(IType type, IAnnotation templateExtension,
			List<ValueResolver> resolvers, IProgressMonitor monitor) {
		try {
			ITypeResolver typeResolver = QuteSupportForTemplate.createTypeResolver(type);
			IMethod[] methods = type.getMethods();
			for (IMethod method : methods) {
				if (isTemplateExtensionMethod(method)) {
					collectResolversForTemplateExtension(method, templateExtension, resolvers, typeResolver);
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
					&& !"void".equals(method.getReturnType());
		} catch (JavaModelException e) {
			LOGGER.log(Level.SEVERE, "Error while getting method information of '" + method.getElementName() + "'.", e);
			return false;
		}
	}

	public static void collectResolversForTemplateExtension(IMethod method, IAnnotation templateExtension,
			List<ValueResolver> resolvers, IProgressMonitor monitor) {
		if (isTemplateExtensionMethod(method)) {
			ITypeResolver typeResolver = QuteSupportForTemplate.createTypeResolver(method);
			collectResolversForTemplateExtension(method, templateExtension, resolvers, typeResolver);
		}
	}

	private static void collectResolversForTemplateExtension(IMethod method, IAnnotation templateExtension,
			List<ValueResolver> resolvers, ITypeResolver typeResolver) {
		ValueResolver resolver = new ValueResolver();
		resolver.setSourceType(method.getDeclaringType().getFullyQualifiedName());
		resolver.setSignature(typeResolver.resolveMethodSignature(method));
		try {
			resolver.setNamespace(AnnotationUtils.getAnnotationMemberValue(templateExtension,
					TEMPLATE_EXTENSION_ANNOTATION_NAMESPACE));
		} catch (JavaModelException e) {
			LOGGER.log(Level.SEVERE, "Error while getting annotatim member value of '" + method.getElementName() + "'.",
					e);
		}
		resolvers.add(resolver);
	}
}

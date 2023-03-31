/*******************************************************************************
* Copyright (c) 2023 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0git che
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.jdt.internal.template.datamodel;

import static com.redhat.qute.jdt.internal.QuteJavaConstants.MESSAGE_ANNOTATION;
import static com.redhat.qute.jdt.internal.QuteJavaConstants.MESSAGE_BUNDLE_ANNOTATION;
import static com.redhat.qute.jdt.internal.QuteJavaConstants.MESSAGE_BUNDLE_ANNOTATION_LOCALE;
import static com.redhat.qute.jdt.internal.QuteJavaConstants.VALUE_ANNOTATION_NAME;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IAnnotatable;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

import com.redhat.qute.commons.datamodel.resolvers.MessageResolverData;
import com.redhat.qute.commons.datamodel.resolvers.ValueResolverInfo;
import com.redhat.qute.commons.datamodel.resolvers.ValueResolverKind;
import com.redhat.qute.jdt.QuteSupportForTemplate;
import com.redhat.qute.jdt.internal.resolver.ITypeResolver;
import com.redhat.qute.jdt.template.datamodel.AbstractAnnotationTypeReferenceDataModelProvider;
import com.redhat.qute.jdt.template.datamodel.SearchContext;
import com.redhat.qute.jdt.utils.AnnotationUtils;

/**
 * Type-safe Message Bundles support.
 * 
 * @author Angelo ZERR
 * 
 * @see https://quarkus.io/guides/qute-reference#type-safe-message-bundles
 */
public class TypeSafeMessageBundlesSupport extends AbstractAnnotationTypeReferenceDataModelProvider {

	private static final String DEFAULT_MESSAGE_NAMESPACE = "msg";

	private static final Logger LOGGER = Logger.getLogger(TemplateGlobalAnnotationSupport.class.getName());

	private static final String[] ANNOTATION_NAMES = { MESSAGE_ANNOTATION };

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
		if (annotation == null) {
			return;
		}
		ITypeResolver typeResolver = QuteSupportForTemplate.createTypeResolver((IMember) javaElement);
		if (javaElement.getElementType() == IJavaElement.METHOD) {
			IMethod method = (IMethod) javaElement;
			collectResolversForMessage(method, annotation, context.getDataModelProject().getValueResolvers(),
					typeResolver, monitor);
		}
	}

	private void collectResolversForMessage(IMethod method, IAnnotation messageAnnotation,
			List<ValueResolverInfo> resolvers, ITypeResolver typeResolver, IProgressMonitor monitor) {

		// @MessageBundle
		// public interface AppMessages {
		IAnnotation messageBundleAnnotation = getMessageBundleAnnotation(method.getTypeRoot().findPrimaryType());
		String sourceType = method.getDeclaringType().getFullyQualifiedName();
		ValueResolverInfo resolver = new ValueResolverInfo();
		String namespace = getNamespaceMessage(messageBundleAnnotation);
		resolver.setNamespace(namespace);
		resolver.setSourceType(sourceType);
		resolver.setSignature(typeResolver.resolveSignature(method));
		resolver.setKind(ValueResolverKind.Message);

		// data message
		String locale = getLocaleMessage(messageBundleAnnotation);
		String messageContent = getMessageContent(messageAnnotation);
		if (locale != null || messageContent != null) {
			MessageResolverData data = new MessageResolverData();
			data.setLocale(locale);
			data.setMessage(messageContent);
			resolver.setData(data);
		}

		if (!resolvers.contains(resolver)) {
			resolvers.add(resolver);
		}

	}

	private static IAnnotation getMessageBundleAnnotation(IType type) {
		try {
			return AnnotationUtils.getAnnotation((IAnnotatable) type, MESSAGE_BUNDLE_ANNOTATION);
		} catch (JavaModelException e) {
			LOGGER.log(Level.SEVERE, "Error while getting @MessageBundle annotation value.", e);
			return null;
		}
	}

	private static String getNamespaceMessage(IAnnotation messageBundleAnnotation) {
		String namespace = null;
		try {
			if (messageBundleAnnotation != null) {
				namespace = AnnotationUtils.getAnnotationMemberValue(messageBundleAnnotation, VALUE_ANNOTATION_NAME);
			}
		} catch (JavaModelException e) {
			LOGGER.log(Level.SEVERE, "Error while getting @MessageBundle#value annotation value.", e);
			return null;
		}
		return StringUtils.isEmpty(namespace) ? DEFAULT_MESSAGE_NAMESPACE : namespace;
	}

	private static String getLocaleMessage(IAnnotation messageBundleAnnotation) {
		try {
			if (messageBundleAnnotation != null) {
				return AnnotationUtils.getAnnotationMemberValue(messageBundleAnnotation,
						MESSAGE_BUNDLE_ANNOTATION_LOCALE);
			}
		} catch (JavaModelException e) {
			LOGGER.log(Level.SEVERE, "Error while getting @MessageBundle#locale annotation value.", e);
			return null;
		}
		return null;
	}

	private static String getMessageContent(IAnnotation messageAnnotation) {
		try {
			return AnnotationUtils.getAnnotationMemberValue(messageAnnotation, VALUE_ANNOTATION_NAME);
		} catch (JavaModelException e) {
			LOGGER.log(Level.SEVERE, "Error while getting @Message#value annotation value.", e);
			return null;
		}
	}
}

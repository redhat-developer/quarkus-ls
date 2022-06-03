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
package com.redhat.qute.jdt.internal.extensions.quarkus;

import static com.redhat.qute.jdt.internal.QuteJavaConstants.JAVAX_INJECT_NAMED_ANNOTATION;
import static com.redhat.qute.jdt.internal.QuteJavaConstants.VALUE_ANNOTATION_NAME;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IAnnotatable;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

import com.redhat.qute.commons.datamodel.resolvers.ValueResolverInfo;
import com.redhat.qute.jdt.QuteSupportForTemplate;
import com.redhat.qute.jdt.internal.resolver.ITypeResolver;
import com.redhat.qute.jdt.template.datamodel.AbstractAnnotationTypeReferenceDataModelProvider;
import com.redhat.qute.jdt.template.datamodel.SearchContext;
import com.redhat.qute.jdt.utils.AnnotationUtils;
import com.redhat.qute.jdt.utils.CDIUtils;

/**
 * Injecting Beans Directly In Templates support.
 * 
 * @author Angelo ZERR
 * 
 * @see https://quarkus.io/guides/qute-reference#injecting-beans-directly-in-templates
 *
 */
public class InjectNamespaceResolverSupport extends AbstractAnnotationTypeReferenceDataModelProvider {

	private static final Logger LOGGER = Logger.getLogger(InjectNamespaceResolverSupport.class.getName());

	private static final String INJECT_NAMESPACE = "inject";

	private static final String[] ANNOTATION_NAMES = { JAVAX_INJECT_NAMED_ANNOTATION };

	@Override
	protected String[] getAnnotationNames() {
		return ANNOTATION_NAMES;
	}

	@Override
	protected void processAnnotation(IJavaElement javaElement, IAnnotation annotation, String annotationName,
			SearchContext context, IProgressMonitor monitor) throws JavaModelException {
		switch (javaElement.getElementType()) {

		case IJavaElement.TYPE: {
			// @Named("flash")
			// public class Flash;
			// becomes --> inject:flash

			IType type = (IType) javaElement;
			String named = getNamed(type);
			collectResolversForInject(type, named, context.getDataModelProject().getValueResolvers());
			break;
		}
		case IJavaElement.FIELD:
		case IJavaElement.METHOD: {
			// @Named
			// private String foo;
			// becomes --> inject:foo

			// @Named("user")
			// private String getUser() {...
			// becomes --> inject:user

			IMember javaMember = (IMember) javaElement;
			String named = getNamed(javaMember);
			ITypeResolver typeResolver = QuteSupportForTemplate.createTypeResolver(javaMember);
			collectResolversForInject(javaMember, named, context.getDataModelProject().getValueResolvers(),
					typeResolver);
			break;
		}
		}
	}

	private static String getNamed(IJavaElement javaElement) {
		String named = getAnnotationNamedValue(javaElement);
		return CDIUtils.getSimpleName(javaElement, named);
	}

	private static String getAnnotationNamedValue(IJavaElement javaElement) {
		try {
			IAnnotation namedAnnotation = AnnotationUtils.getAnnotation((IAnnotatable) javaElement,
					JAVAX_INJECT_NAMED_ANNOTATION);
			if (namedAnnotation != null) {
				return AnnotationUtils.getAnnotationMemberValue(namedAnnotation, VALUE_ANNOTATION_NAME);
			}
		} catch (JavaModelException e) {
			LOGGER.log(Level.SEVERE, "Error while getting @Named annotation value.", e);
			return null;
		}
		return null;
	}

	private static void collectResolversForInject(IType type, String named, List<ValueResolverInfo> resolvers) {
		ValueResolverInfo resolver = new ValueResolverInfo();
		resolver.setNamed(named);
		resolver.setSourceType(type.getFullyQualifiedName());
		resolver.setSignature(type.getFullyQualifiedName());
		resolver.setNamespace(INJECT_NAMESPACE);
		resolvers.add(resolver);
	}

	private static void collectResolversForInject(IMember javaMember, String named, List<ValueResolverInfo> resolvers,
			ITypeResolver typeResolver) {
		ValueResolverInfo resolver = new ValueResolverInfo();
		resolver.setNamed(named);
		resolver.setSourceType(javaMember.getDeclaringType().getFullyQualifiedName());
		resolver.setSignature(typeResolver.resolveSignature(javaMember));
		resolver.setNamespace(INJECT_NAMESPACE);
		resolvers.add(resolver);
	}

}

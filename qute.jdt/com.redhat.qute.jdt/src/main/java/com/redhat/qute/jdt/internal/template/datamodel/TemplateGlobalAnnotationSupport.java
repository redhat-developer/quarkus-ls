/*******************************************************************************
* Copyright (c) 2022 Red Hat Inc. and others.
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

import static com.redhat.qute.jdt.internal.QuteJavaConstants.TEMPLATE_GLOBAL_ANNOTATION;
import static com.redhat.qute.jdt.internal.QuteJavaConstants.TEMPLATE_GLOBAL_ANNOTATION_NAME;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IAnnotatable;
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
import com.redhat.qute.jdt.utils.JDTTypeUtils;

/**
 * @TemplateGlobal annotation support.
 *
 *                 <code>
 * &#64;TemplateGlobal
 * public class Globals {
 *
 * 		static int age = 40;
 *
 * 		static Color[] myColors() {
 * 			return new Color[] { Color.RED, Color.BLUE };
 * 		}
 *
 * 		&#64;TemplateGlobal(name = "currentUser")
 * 		static String user() {
 * 			return "Mia";
 * 		}
 * }
 * </code>
 *
 *
 * @see https://quarkus.io/guides/qute-reference#global_variables
 *
 */
public class TemplateGlobalAnnotationSupport extends AbstractAnnotationTypeReferenceDataModelProvider {

	private static final Logger LOGGER = Logger.getLogger(TemplateGlobalAnnotationSupport.class.getName());

	private static final String[] ANNOTATION_NAMES = { TEMPLATE_GLOBAL_ANNOTATION };

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
		if (javaElement.getElementType() == IJavaElement.TYPE) {
			IType type = (IType) javaElement;
			collectResolversForTemplateGlobal(type, annotation, context.getDataModelProject().getValueResolvers(),
					typeResolver, monitor);
		} else if (javaElement.getElementType() == IJavaElement.FIELD
				|| javaElement.getElementType() == IJavaElement.METHOD) {
			IMember member = (IMember) javaElement;
			collectResolversForTemplateGlobal(member, annotation, context.getDataModelProject().getValueResolvers(),
					typeResolver, monitor);
		}
	}

	private void collectResolversForTemplateGlobal(IType type, IAnnotation templateGlobal,
			List<ValueResolverInfo> resolvers, ITypeResolver typeResolver, IProgressMonitor monitor) {
		try {
			IField[] fields = type.getFields();
			for (IField field : fields) {
				if (!AnnotationUtils.hasAnnotation(field, TEMPLATE_GLOBAL_ANNOTATION)
						&& isTemplateGlobalMember(field)) {
					collectResolversForTemplateGlobal(field, templateGlobal, resolvers, typeResolver, monitor);
				}
			}
			IMethod[] methods = type.getMethods();
			for (IMethod method : methods) {
				if (!AnnotationUtils.hasAnnotation(method, TEMPLATE_GLOBAL_ANNOTATION)
						&& isTemplateGlobalMember(method)) {
					collectResolversForTemplateGlobal(method, templateGlobal, resolvers, typeResolver, monitor);
				}
			}
		} catch (JavaModelException e) {
			LOGGER.log(Level.SEVERE, "Error while getting methods of '" + type.getElementName() + "'.", e);
		}
	}

	private void collectResolversForTemplateGlobal(IMember member, IAnnotation templateGlobal,
			List<ValueResolverInfo> resolvers, ITypeResolver typeResolver, IProgressMonitor monitor) {
		if (isTemplateGlobalMember(member)) {
			String sourceType = member.getDeclaringType().getFullyQualifiedName();
			ValueResolverInfo resolver = new ValueResolverInfo();
			resolver.setSourceType(sourceType);
			resolver.setSignature(typeResolver.resolveSignature(member));
			// Constant value for {@link #name()} indicating that the field/method name
			// should be used
			try {
				resolver.setNamed(
						AnnotationUtils.getAnnotationMemberValue(templateGlobal, TEMPLATE_GLOBAL_ANNOTATION_NAME));
			} catch (JavaModelException e) {
				LOGGER.log(Level.SEVERE, "Error while getting annotation member value of 'name'.", e);
			}
			resolver.setGlobalVariable(true);
			if (!resolvers.contains(resolver)) {
				resolvers.add(resolver);
			}
		}
	}

	/**
	 * Returns true if the given member is supported by @TemplateGlobal and false
	 * otherwise.
	 *
	 * A global variable method:
	 *
	 * <ul>
	 * <li>must not be private</li>
	 * <li>must be static,</li>
	 * <li>must not accept any parameter,</li>
	 * <li>must not return {@code void},</li>
	 * </ul>
	 *
	 * A global variable field:
	 *
	 * <ul>
	 * <li>must not be private</li>
	 * <li>must be static,</li>
	 * </ul>
	 *
	 * @param member the member to check.
	 * @return true if the given member <code>member</code> is a template global
	 *         member and false otherwise.
	 */
	private static boolean isTemplateGlobalMember(IMember member) {
		try {
			// every non-void non-private static method that declares no parameters and
			// every non-private static field is considered a global variable
			if (!JDTTypeUtils.isPrivateMember(member) && JDTTypeUtils.isStaticMember(member)) {
				if (member.getElementType() == IJavaElement.FIELD) {
					return true;
				} else if (member.getElementType() == IJavaElement.METHOD) {
					IMethod method = (IMethod) member;
					return method.getNumberOfParameters() == 0 && !JDTTypeUtils.isVoidReturnType(method);
				}
			}
			return false;
		} catch (JavaModelException e) {
			LOGGER.log(Level.SEVERE, "Error while getting method information of '" + member.getElementName() + "'.", e);
			return false;
		}
	}
}

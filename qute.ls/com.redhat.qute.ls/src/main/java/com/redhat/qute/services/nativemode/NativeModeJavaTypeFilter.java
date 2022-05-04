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
package com.redhat.qute.services.nativemode;

import java.util.List;
import java.util.Set;

import com.redhat.qute.commons.JavaElementKind;
import com.redhat.qute.commons.JavaMemberInfo;
import com.redhat.qute.commons.JavaMethodInfo;
import com.redhat.qute.commons.ResolvedJavaTypeInfo;
import com.redhat.qute.commons.annotations.TemplateDataAnnotation;
import com.redhat.qute.project.QuteProject;

/**
 * Java type filter in native mode.
 * 
 * <p>
 * In native mode Java reflection is forbidden, it means that by default fields
 * and methods for a Java type are not accessible.
 * </p>
 * 
 * <p>
 * To allow fields, methods for a given Java Type, the Java Type must be
 * annotated with:
 * 
 * <ul>
 * <li>@TemplateData which generates value resolvers.</li>
 * <li>@RegisterForReflection</li>
 * </ul>
 * </p>
 * 
 * 
 * @author Angelo ZERR
 *
 */
public class NativeModeJavaTypeFilter implements JavaTypeFilter {

	private final QuteProject project;

	public NativeModeJavaTypeFilter(QuteProject project) {
		this.project = project;
	}

	@Override
	public JavaTypeAccessibiltyRule getJavaTypeAccessibility(ResolvedJavaTypeInfo javaType,
			Set<String> templateParameterDeclarationJavaTypes) {
		if (templateParameterDeclarationJavaTypes.contains(javaType.getName())) {
			return JavaTypeAccessibiltyRule.ALLOWED_WITHOUT_RESTRICTION;
		}
		return project.getJavaTypeAccessibiltyInNativeMode(javaType.getName());
	}

	@Override
	public JavaMemberAccessibility getJavaMemberAccessibility(JavaMemberInfo member, JavaTypeAccessibiltyRule rule) {
		// @TemplateData(ignore = {"price", "name"})
		JavaMemberAccessibility templateDataAccessibility = getJavaMemberAccessibilityForTemplateData(member, rule);
		if (templateDataAccessibility == JavaMemberAccessibility.ALLOWED) {
			return JavaMemberAccessibility.ALLOWED;
		}
		// @RegisterForReflection
		JavaMemberAccessibility registerForReflectionAccessibility = getJavaMemberAccessibilityForRegisterForReflection(
				member, rule);
		if (registerForReflectionAccessibility == JavaMemberAccessibility.ALLOWED) {
			return JavaMemberAccessibility.ALLOWED;
		}

		if (templateDataAccessibility == null) {
			if (registerForReflectionAccessibility == null) {
				return JavaMemberAccessibility.ALLOWED;
			}
			return registerForReflectionAccessibility;
		}
		return templateDataAccessibility;
	}

	private static JavaMemberAccessibility getJavaMemberAccessibilityForTemplateData(JavaMemberInfo member,
			JavaTypeAccessibiltyRule rule) {
		// @TemplateData/ignore
		String pattern = rule.getIgnorePattern(member.getName());
		if (pattern != null) {
			return JavaMemberAccessibility.createIgnoreAccess(pattern);
		}
		// @TemplateData/properties
		if (rule.isProperties() && !isProperty(member)) {
			return JavaMemberAccessibility.FORBIDDEN_BY_TEMPLATE_DATA_PROPERTIES;
		}
		if (rule.hasTemplateDataAnnotation()) {
			// The Java type of the member is annotated with @TemplateData which
			// - matches the ignore rule
			// - matches the properties rule
			return JavaMemberAccessibility.ALLOWED;
		}
		return null;
	}

	private static boolean isProperty(JavaMemberInfo member) {
		switch (member.getJavaElementKind()) {
		case FIELD:
			return true;
		case METHOD:
			return ((JavaMethodInfo) member).getParameters().isEmpty();
		default:
			return false;
		}
	}

	private static JavaMemberAccessibility getJavaMemberAccessibilityForRegisterForReflection(JavaMemberInfo member,
			JavaTypeAccessibiltyRule rule) {
		// @RegisterForReflection(fields = false)
		if (!rule.isFields() && member.getJavaElementKind() == JavaElementKind.FIELD) {
			return JavaMemberAccessibility.FORBIDDEN_BY_REGISTER_FOR_REFLECTION_FIELDS;
		}
		// @RegisterForReflection(methods = false)
		if (!rule.isMethods() && member.getJavaElementKind() == JavaElementKind.METHOD) {
			return JavaMemberAccessibility.FORBIDDEN_BY_REGISTER_FOR_REFLECTION_METHODS;
		}
		if (rule.hasRegisterForReflectionAnnotation()) {
			// The Java type of the member is annotated with @RegisterForReflection
			return JavaMemberAccessibility.ALLOWED;
		}
		return null;
	}

	@Override
	public boolean isIgnoreSuperclasses(ResolvedJavaTypeInfo baseType, JavaTypeAccessibiltyRule rule) {
		if (isIgnoreSuperclasses(baseType)) {
			return true;
		}
		return rule.isIgnoreSuperClasses();
	}

	private boolean isIgnoreSuperclasses(ResolvedJavaTypeInfo targetType) {
		// Here we are in native images context, the Java Type must be annotated with
		// @TempateData#ignoreSuperclasses :

		// @TemplateData(ignoreSuperclasses = true)
		// public class Book extends PanacheEntity
		List<TemplateDataAnnotation> annotations = targetType.getTemplateDataAnnotations();
		if (annotations != null) {
			if (annotations.stream() //
					.anyMatch(TemplateDataAnnotation::isIgnoreSuperclasses)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isSuperClassAllowed(JavaMemberInfo member, ResolvedJavaTypeInfo baseType,
			JavaTypeAccessibiltyRule rule) {
		ResolvedJavaTypeInfo memberType = (ResolvedJavaTypeInfo) member.getJavaTypeInfo();
		if (memberType.equals(baseType)) {
			return true;
		}
		return !isIgnoreSuperclasses(memberType, rule);
	}

	public boolean isInNativeMode() {
		return true;
	}

}

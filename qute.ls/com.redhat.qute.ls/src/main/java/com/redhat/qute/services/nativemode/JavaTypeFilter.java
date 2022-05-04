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

import java.util.Set;

import com.redhat.qute.commons.JavaMemberInfo;
import com.redhat.qute.commons.ResolvedJavaTypeInfo;

/**
 * Java type, method field filter used to filters them according the native mode
 * and the annotations @TemplateData and @RegisterForReflection.
 * 
 * @author Angelo ZERR
 *
 */
public interface JavaTypeFilter {

	public enum JavaMemberAccessibilityKind {
		ALLOWED, //
		FORBIDDEN_BY_TEMPLATE_DATA_IGNORE, //
		FORBIDDEN_BY_TEMPLATE_DATA_PROPERTIES, //
		FORBIDDEN_BY_REGISTER_FOR_REFLECTION_FIELDS, //
		FORBIDDEN_BY_REGISTER_FOR_REFLECTION_METHODS;
	};

	public class JavaMemberAccessibility {

		public static final JavaMemberAccessibility ALLOWED = new JavaMemberAccessibility(
				JavaMemberAccessibilityKind.ALLOWED);

		public static final JavaMemberAccessibility FORBIDDEN_BY_TEMPLATE_DATA_PROPERTIES = new JavaMemberAccessibility(
				JavaMemberAccessibilityKind.FORBIDDEN_BY_TEMPLATE_DATA_PROPERTIES);

		public static final JavaMemberAccessibility FORBIDDEN_BY_REGISTER_FOR_REFLECTION_FIELDS = new JavaMemberAccessibility(
				JavaMemberAccessibilityKind.FORBIDDEN_BY_REGISTER_FOR_REFLECTION_FIELDS);

		public static final JavaMemberAccessibility FORBIDDEN_BY_REGISTER_FOR_REFLECTION_METHODS = new JavaMemberAccessibility(
				JavaMemberAccessibilityKind.FORBIDDEN_BY_REGISTER_FOR_REFLECTION_METHODS);

		private final JavaMemberAccessibilityKind kind;

		private final String ignore;

		public JavaMemberAccessibility(JavaMemberAccessibilityKind kind) {
			this(kind, null);
		}

		public JavaMemberAccessibility(JavaMemberAccessibilityKind kind, String ignore) {
			this.kind = kind;
			this.ignore = ignore;
		}

		public JavaMemberAccessibilityKind getKind() {
			return kind;
		}

		public String getIgnore() {
			return ignore;
		}

		public static JavaMemberAccessibility createIgnoreAccess(String ignore) {
			return new JavaMemberAccessibility(JavaMemberAccessibilityKind.FORBIDDEN_BY_TEMPLATE_DATA_IGNORE, ignore);
		}

	};

	/**
	 * Returns the accessibility for the given Java type and null otherwise.
	 * 
	 * @param javaType the Java type.
	 * @param templateParameterDeclarationJavaTypes 
	 * 
	 * @return the accessibility for the given Java type and null otherwise.
	 */
	JavaTypeAccessibiltyRule getJavaTypeAccessibility(ResolvedJavaTypeInfo javaType, Set<String> templateParameterDeclarationJavaTypes);

	/**
	 * Returns the accessibility for the given Java member.
	 * 
	 * @param member the Java method, field.
	 * @param rule   the Java type accessibility rule.
	 * 
	 * @return the accessibility for the given Java member.
	 */
	JavaMemberAccessibility getJavaMemberAccessibility(JavaMemberInfo member, JavaTypeAccessibiltyRule rule);

	/**
	 * Returns true if super classes are allowed and false otherwise.
	 * 
	 * @param member   the Java member.
	 * @param baseType the Java base type.
	 * @param rule     the Java type accessibility rule.
	 * 
	 * @return true if super classes are allowed and false otherwise.
	 */
	boolean isSuperClassAllowed(JavaMemberInfo member, ResolvedJavaTypeInfo baseType, JavaTypeAccessibiltyRule rule);

	/**
	 * Returns true if super classes must be ignored while searching fields method
	 * of the given Java base type and false otherwise.
	 * 
	 * @param baseType the Java base type.
	 * @param rule     the Java type accessibility rule.
	 * 
	 * @return true if super classes must be ignored while searching fields method
	 *         of the given Java base type and false otherwise.
	 */
	boolean isIgnoreSuperclasses(ResolvedJavaTypeInfo baseType, JavaTypeAccessibiltyRule rule);

	/**
	 * Returns true if the filter is in native mode and false otherwise.
	 * 
	 * @return true if the filter is in native mode and false otherwise.
	 */
	boolean isInNativeMode();

}

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

import com.redhat.qute.commons.JavaFieldInfo;
import com.redhat.qute.commons.JavaMemberInfo;
import com.redhat.qute.commons.JavaMethodInfo;
import com.redhat.qute.commons.RegisterForReflectionAnnotation;
import com.redhat.qute.commons.ResolvedJavaTypeInfo;
import com.redhat.qute.commons.TemplateDataAnnotation;
import com.redhat.qute.settings.QuteNativeSettings;

/**
 * Java type filter in native mode.
 * 
 * @author Angelo ZERR
 *
 */
public class JavaTypeFilterInNativeMode implements JavaTypeFilter {

	private final QuteNativeSettings nativeSettings;

	private final ResolvedJavaTypeInfo rootJavaType;

	JavaTypeFilterInNativeMode(ResolvedJavaTypeInfo rootJavaType, QuteNativeSettings nativeSettings) {
		this.nativeSettings = nativeSettings;
		this.rootJavaType = rootJavaType;
	}

	@Override
	public boolean isJavaTypeAllowed(ResolvedJavaTypeInfo javaType) {
		// Native images mode : the java reflection is supported only if the Java type
		// is
		// annotated with:
		// - @TemplateData
		// - @RegisterForReflection
		if (NativeModeUtils.canSupportJavaReflectionInNativeMode(javaType)) {
			return true;
		}
		if (javaType != rootJavaType) {
			// TODO : target

		}
		return false;
	}

	public JavaMemberdAccess getJavaMemberAccess(JavaFieldInfo field) {
		RegisterForReflectionAnnotation annotation = rootJavaType.getRegisterForReflectionAnnotation();
		if (annotation != null) {
			if (!annotation.isFields()) {
				// @RegisterForReflection(fields = false)
				return JavaMemberdAccess.FORBIDDEN_BY_REGISTER_FOR_REFLECTION_FIELDS;
			}
		}
		return JavaMemberdAccess.ALLOWED;
	}

	@Override
	public JavaMemberdAccess getJavaMemberAccess(JavaMemberInfo member) {
		switch (member.getJavaElementKind()) {
		case FIELD:
			return getJavaFieldAccess((JavaFieldInfo) member);
		case METHOD:
			return getJavaMethodAccess((JavaMethodInfo) member);
		default:
			return JavaMemberdAccess.ALLOWED;
		}
	}

	private JavaMemberdAccess getJavaFieldAccess(JavaFieldInfo field) {
		RegisterForReflectionAnnotation annotation = rootJavaType.getRegisterForReflectionAnnotation();
		if (annotation != null) {
			if (!annotation.isFields()) {
				// @RegisterForReflection(fields = false)
				return JavaMemberdAccess.FORBIDDEN_BY_REGISTER_FOR_REFLECTION_FIELDS;
			}
		}
		return JavaMemberdAccess.ALLOWED;
	}

	private JavaMemberdAccess getJavaMethodAccess(JavaMethodInfo method) {
		RegisterForReflectionAnnotation annotation = rootJavaType.getRegisterForReflectionAnnotation();
		if (annotation != null) {
			if (!annotation.isMethods()) {
				// @RegisterForReflection(methods = false)
				return JavaMemberdAccess.FORBIDDEN_BY_REGISTER_FOR_REFLECTION_METHODS;
			}
		}
		return JavaMemberdAccess.ALLOWED;
	}

	/**
	 * Returns true if super classes must be ignored while searching fields method
	 * of the given Java base type and false otherwise.
	 * 
	 * @return true if super classes must be ignored while searching fields method
	 *         of the given Java base type and false otherwise.
	 */
	public boolean isIgnoreSuperclasses() {
		// Here we are in native images context, the Java Type must be annotated with
		// @TempateData#ignoreSuperclasses :

		// @TemplateData(ignoreSuperclasses = true)
		// public class Book extends PanacheEntity
		List<TemplateDataAnnotation> annotations = rootJavaType.getTemplateDataAnnotations();
		if (annotations != null) {
			return annotations.stream() //
					.anyMatch(TemplateDataAnnotation::isIgnoreSuperclasses);
		}
		return false;
	}

	public boolean isInNativeMode() {
		return nativeSettings.isEnabled();
	}

	public QuteNativeSettings getNativeSettings() {
		return nativeSettings;
	}

	@Override
	public boolean isSuperClassAllowed(JavaMemberInfo member) {
		if (isIgnoreSuperclasses()) {
			return rootJavaType.equals(member.getJavaTypeInfo());
		}
		return true;
	}
}

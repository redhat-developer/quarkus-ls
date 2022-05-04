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
 * Java type filter in NO native mode.
 * 
 * <p>
 * in this mode Java reflection is allowed and all fields / methods are allowed.
 * </p>
 * 
 * @author Angelo ZERR
 *
 */
public class ReflectionJavaTypeFilter implements JavaTypeFilter {

	public static final JavaTypeFilter INSTANCE = new ReflectionJavaTypeFilter();

	@Override
	public JavaTypeAccessibiltyRule getJavaTypeAccessibility(ResolvedJavaTypeInfo javaType,
			Set<String> templateParameterDeclarationJavaTypes) {
		return JavaTypeAccessibiltyRule.ALLOWED_WITHOUT_RESTRICTION;
	}

	@Override
	public JavaMemberAccessibility getJavaMemberAccessibility(JavaMemberInfo member, JavaTypeAccessibiltyRule rule) {
		return JavaMemberAccessibility.ALLOWED;
	}

	@Override
	public boolean isIgnoreSuperclasses(ResolvedJavaTypeInfo baseType, JavaTypeAccessibiltyRule rule) {
		// In NO native images mode, Java reflection is allowed, in other words super
		// classes must not be ignored.
		return false;
	}

	@Override
	public boolean isSuperClassAllowed(JavaMemberInfo member, ResolvedJavaTypeInfo baseType,
			JavaTypeAccessibiltyRule rule) {
		return true;
	}

	@Override
	public boolean isInNativeMode() {
		return false;
	}
}

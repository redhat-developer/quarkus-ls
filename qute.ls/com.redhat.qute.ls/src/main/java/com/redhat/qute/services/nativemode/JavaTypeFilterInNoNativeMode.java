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

import com.redhat.qute.commons.JavaMemberInfo;
import com.redhat.qute.commons.ResolvedJavaTypeInfo;
import com.redhat.qute.settings.QuteNativeSettings;

/**
 * Java type filter in NO native mode (everything are allowed).
 * 
 * @author Angelo ZERR
 *
 */
public class JavaTypeFilterInNoNativeMode implements JavaTypeFilter {

	public static final JavaTypeFilter INSTANCE = new JavaTypeFilterInNoNativeMode();

	@Override
	public boolean isJavaTypeAllowed(ResolvedJavaTypeInfo member) {
		return true;
	}

	@Override
	public JavaMemberdAccess getJavaMemberAccess(JavaMemberInfo member) {
		return JavaMemberdAccess.ALLOWED;
	}

	@Override
	public boolean isIgnoreSuperclasses() {
		// In NO native images mode, Java reflection is allowed, in other words super
		// classes must not be ignored.
		return false;
	}

	@Override
	public boolean isInNativeMode() {
		return false;
	}

	@Override
	public QuteNativeSettings getNativeSettings() {
		return null;
	}

	@Override
	public boolean isSuperClassAllowed(JavaMemberInfo javaMember) {
		return true;
	}

}

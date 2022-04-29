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
 * Java type, method field filter used to filters them according the native mode
 * and the annotations @TemplateData and @RegisterForReflection.
 * 
 * @author Angelo ZERR
 *
 */
public interface JavaTypeFilter {

	public enum JavaMemberdAccess {
		ALLOWED, //
		FORBIDDEN_BY_TEMPLATE_DATA_IGNORE, //
		FORBIDDEN_BY_REGISTER_FOR_REFLECTION_FIELDS, //
		FORBIDDEN_BY_REGISTER_FOR_REFLECTION_METHODS;
	};

	/**
	 * Returns true if the Java root type is allowed and false otherwise.
	 * 
	 * @param javaType the Java type.
	 * 
	 * @return true if the Java root type is allowed and false otherwise.
	 */
	boolean isJavaTypeAllowed(ResolvedJavaTypeInfo javaType);

	/**
	 * Returns the access of the given member.
	 * 
	 * @param member the Java method, field.
	 * 
	 * @return the access of the given member.
	 */
	JavaMemberdAccess getJavaMemberAccess(JavaMemberInfo member);

	/**
	 * Returns true if super classes is allowed and false otherwise.
	 * 
	 * @param member the Java member.
	 * 
	 * @return true if super classes is allowed and false otherwise.
	 */
	boolean isSuperClassAllowed(JavaMemberInfo member);

	/**
	 * Returns true if super classes must be ignored while searching fields method
	 * of the given Java base type and false otherwise.
	 * 
	 * @return true if super classes must be ignored while searching fields method
	 *         of the given Java base type and false otherwise.
	 */
	boolean isIgnoreSuperclasses();

	/**
	 * Returns true if it is in native mode and false otherwise.
	 * 
	 * @return true if it is in native mode and false otherwise.
	 */
	boolean isInNativeMode();

	/**
	 * Returns the native settings and null otherwise.
	 * 
	 * @return the native settings and null otherwise.
	 */
	QuteNativeSettings getNativeSettings();

}

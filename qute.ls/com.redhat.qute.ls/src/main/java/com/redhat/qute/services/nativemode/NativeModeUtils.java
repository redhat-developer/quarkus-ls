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

import com.redhat.qute.commons.ResolvedJavaTypeInfo;
import com.redhat.qute.settings.QuteNativeSettings;

/**
 * Utilities for native mode.
 * 
 * @author Angelo ZERR
 *
 */
public class NativeModeUtils {

	/**
	 * Returns the java type filter according the given root java type and the
	 * native mode.
	 * 
	 * @param rootJavaType         the Java root type.
	 * @param nativeImagesSettings the native images settings.
	 * 
	 * @return the java type filter according the given root java type and the
	 *         native mode.
	 */
	public static JavaTypeFilter getJavaTypeFilter(ResolvedJavaTypeInfo rootJavaType,
			QuteNativeSettings nativeImagesSettings) {
		if (nativeImagesSettings != null && nativeImagesSettings.isEnabled()) {
			return new JavaTypeFilterInNativeMode(rootJavaType, nativeImagesSettings);
		}
		return JavaTypeFilterInNoNativeMode.INSTANCE;
	}

	/**
	 * Returns true if the Java reflection can be supported in native images mode
	 * and false otherwise.
	 * 
	 * @param baseType the Java type.
	 * 
	 * @return true if the Java reflection can be supported in native images mode
	 *         and false otherwise.
	 */
	public static boolean canSupportJavaReflectionInNativeMode(ResolvedJavaTypeInfo baseType) {
		if (baseType == null) {
			return false;
		}
		if (baseType.getTemplateDataAnnotations() != null && !baseType.getTemplateDataAnnotations().isEmpty()) {
			// The Java type have some @TemplateData annotation
			return true;
		}
		if (baseType.getRegisterForReflectionAnnotation() != null) {
			// The Java type is annotated with @RegisterForReflection
			return true;
		}
		return false;
	}

}

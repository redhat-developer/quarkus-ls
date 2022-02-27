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
package com.redhat.qute.jdt.utils;

import java.beans.Introspector;
import java.util.function.Supplier;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;

/**
 * CDI utilities.
 * 
 * @author Angelo ZERR
 * 
 * @see https://github.com/jbosstools/jbosstools-javaee/blob/8fc233ad8d90dbf44fc2f5475e45393fb9f9f4b1/cdi/plugins/org.jboss.tools.cdi.seam.solder.core/src/org/jboss/tools/cdi/seam/solder/core/CDISeamSolderCoreExtension.java#L261
 */
public class CDIUtils {

	private CDIUtils() {
	}

	public static String getSimpleName(IJavaElement javaElement, String annotationNamedValue) {
		return getSimpleName(javaElement.getElementName(), annotationNamedValue, javaElement.getElementType(), () -> {
			return BeanUtil.isGetter((IMethod) javaElement);
		});
	}

	public static String getSimpleName(String javaElementName, String annotationNamedValue, int javaElementType) {
		return getSimpleName(javaElementName, annotationNamedValue, javaElementType, () -> false);
	}

	public static String getSimpleName(String javaElementName, String annotationNamedValue, int javaElementType,
			Supplier<Boolean> isGetterMethod) {
		if (StringUtils.isNotEmpty(annotationNamedValue)) {
			// A @Named is defined with value. Ex:
			// @Named("flash")
			// private Flash fieldFlash;
			// --> returns 'flash'
			return annotationNamedValue;
		}
		switch (javaElementType) {
		case IJavaElement.TYPE:
			// MyClass --> myClass
			return Introspector.decapitalize(javaElementName);
		case IJavaElement.FIELD:
			return javaElementName;
		case IJavaElement.METHOD:
			if (isGetterMethod.get()) {
				return BeanUtil.getPropertyName(javaElementName);
			}
			return javaElementName;
		}
		return javaElementName;
	}
}

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

import static com.redhat.qute.jdt.internal.QuteJavaConstants.JAKARTA_DECORATOR_ANNOTATION;
import static com.redhat.qute.jdt.internal.QuteJavaConstants.JAKARTA_INJECT_VETOED_ANNOTATION;
import static com.redhat.qute.jdt.internal.QuteJavaConstants.JAVAX_DECORATOR_ANNOTATION;
import static com.redhat.qute.jdt.internal.QuteJavaConstants.JAVAX_INJECT_VETOED_ANNOTATION;

import java.beans.Introspector;
import java.util.function.Supplier;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IAnnotatable;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;

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

	public static boolean isValidBean(IJavaElement javaElement) {
		try {
			IType type = ((IType) javaElement);
			return (type.getDeclaringType() == null
					&& !Flags.isAbstract(type.getFlags())
					&& !AnnotationUtils.hasAnnotation((IAnnotatable) javaElement, JAVAX_DECORATOR_ANNOTATION, JAKARTA_DECORATOR_ANNOTATION)
					&& !AnnotationUtils.hasAnnotation((IAnnotatable) javaElement, JAVAX_INJECT_VETOED_ANNOTATION, JAKARTA_INJECT_VETOED_ANNOTATION)
					&& type.isClass() 
					// In Quarkus context, all arguments are injected
					// See https://github.com/redhat-developer/vscode-quarkus/issues/708
					/* && hasNoArgConstructor(type)*/);
		} catch (Exception e) {
			return false;
		}
	}

	private static boolean hasNoArgConstructor(IType type) {
		try {
			boolean hasNoArgConstructor = true;
			for (IMethod method : type.getMethods()) {
				if (method.isConstructor()) {
					int paramCount = method.getNumberOfParameters();
					if (paramCount > 0) {
						hasNoArgConstructor = false;
					} else if (paramCount == 0) {
						return true;
					}
				}
			}
			return hasNoArgConstructor;
		} catch (Exception e) {
			return true;
		}
	}
}

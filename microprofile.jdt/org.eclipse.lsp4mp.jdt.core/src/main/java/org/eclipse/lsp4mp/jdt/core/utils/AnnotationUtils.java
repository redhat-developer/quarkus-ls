/*******************************************************************************
* Copyright (c) 2020 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lsp4mp.jdt.core.utils;

import org.eclipse.jdt.core.IAnnotatable;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.jdt.core.JavaModelException;

/**
 * Java annotations utilities.
 * 
 * @author Angelo ZERR
 *
 */
public class AnnotationUtils {

	public static boolean hasAnnotation(IAnnotatable annotatable, String annotationName) throws JavaModelException {
		return getAnnotation(annotatable, annotationName) != null;
	}

	/**
	 * Returns the annotation from the given <code>annotatable</code> element with
	 * the given name <code>annotationName</code> and null otherwise.
	 * 
	 * @param annotatable    the class, field which can be annotated.
	 * @param annotationName the annotation name
	 * @return the annotation from the given <code>annotatable</code> element with
	 *         the given name <code>annotationName</code> and null otherwise.
	 * @throws JavaModelException
	 */
	public static IAnnotation getAnnotation(IAnnotatable annotatable, String annotationName) throws JavaModelException {
		if (annotatable == null) {
			return null;
		}
		IAnnotation[] annotations = annotatable.getAnnotations();
		for (IAnnotation annotation : annotations) {
			if (isMatchAnnotation(annotation, annotationName)) {
				return annotation;
			}
		}
		return null;
	}

	/**
	 * Returns true if the given annotation match the given annotation name and
	 * false otherwise.
	 * 
	 * @param annotation     the annotation.
	 * @param annotationName the annotation name.
	 * @return true if the given annotation match the given annotation name and
	 *         false otherwise.
	 */
	public static boolean isMatchAnnotation(IAnnotation annotation, String annotationName) {
		return annotationName.endsWith(annotation.getElementName());
	}

	/**
	 * Returns the value of the given member name of the given annotation.
	 * 
	 * @param annotation the annotation.
	 * @param memberName the member name.
	 * @return the value of the given member name of the given annotation.
	 * @throws JavaModelException
	 */
	public static String getAnnotationMemberValue(IAnnotation annotation, String memberName) throws JavaModelException {
		for (IMemberValuePair pair : annotation.getMemberValuePairs()) {
			if (memberName.equals(pair.getMemberName())) {
				return pair.getValue() != null ? pair.getValue().toString() : null;
			}
		}
		return null;
	}

}

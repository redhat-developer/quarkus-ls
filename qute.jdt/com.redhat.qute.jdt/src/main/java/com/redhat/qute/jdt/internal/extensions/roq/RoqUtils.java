/*******************************************************************************
* Copyright (c) 2026 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.jdt.internal.extensions.roq;

import org.eclipse.jdt.core.IJavaProject;

import com.redhat.qute.jdt.utils.JDTTypeUtils;

/**
 * Roq project utilities.
 */
public class RoqUtils {

	public static boolean isRoqProject(IJavaProject javaProject) {
		return JDTTypeUtils.findType(javaProject, RoqJavaConstants.SITE_CLASS) != null;
	}
}

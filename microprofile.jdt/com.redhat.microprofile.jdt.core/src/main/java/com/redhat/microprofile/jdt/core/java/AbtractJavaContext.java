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
package com.redhat.microprofile.jdt.core.java;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ITypeRoot;

import com.redhat.microprofile.jdt.core.utils.IJDTUtils;

/**
 * Abstract class for Java context for a given compilation unit.
 * 
 * @author Angelo ZERR
 *
 */
public abstract class AbtractJavaContext {

	private final String uri;

	private final ITypeRoot typeRoot;

	private final IJDTUtils utils;

	public AbtractJavaContext(String uri, ITypeRoot typeRoot, IJDTUtils utils) {
		this.uri = uri;
		this.typeRoot = typeRoot;
		this.utils = utils;
	}

	public String getUri() {
		return uri;
	}

	public ITypeRoot getTypeRoot() {
		return typeRoot;
	}

	public IJavaProject getJavaProject() {
		return getTypeRoot().getJavaProject();
	}

	public IJDTUtils getUtils() {
		return utils;
	}
}

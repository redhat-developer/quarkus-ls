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
package com.redhat.qute.jdt.internal.java;

import org.eclipse.jdt.core.ITypeRoot;

import com.redhat.qute.jdt.utils.IJDTUtils;
import com.redhat.qute.jdt.utils.JDTQuteProjectUtils;

/**
 * Base context to collect template links of a given Java file.
 */
public class QuteTemplateLinkCollectorContext {

	private final ITypeRoot typeRoot;
	private final IJDTUtils utils;

	private String relativeResourcesFolder;

	public QuteTemplateLinkCollectorContext(ITypeRoot typeRoot, IJDTUtils utils) {
		this.typeRoot = typeRoot;
		this.utils = utils;
	}

	public ITypeRoot getTypeRoot() {
		return typeRoot;
	}

	public IJDTUtils getUtils() {
		return utils;
	}

	public String getRelativeResourcesFolder() {
		if (relativeResourcesFolder == null) {
			this.relativeResourcesFolder = JDTQuteProjectUtils.getRelativeResourcesFolder(typeRoot.getJavaProject());
		}
		return relativeResourcesFolder;
	}

}

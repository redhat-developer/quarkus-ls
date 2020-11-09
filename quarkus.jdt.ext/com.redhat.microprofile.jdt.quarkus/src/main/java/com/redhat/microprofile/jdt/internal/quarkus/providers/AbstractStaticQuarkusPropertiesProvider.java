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
package com.redhat.microprofile.jdt.internal.quarkus.providers;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.lsp4mp.jdt.core.AbstractStaticPropertiesProvider;
import org.eclipse.lsp4mp.jdt.core.SearchContext;

import com.redhat.microprofile.jdt.quarkus.JDTQuarkusUtils;

/**
 * Abstract class for static Quarkus properties.
 * 
 * @author Angelo ZERR
 *
 */
public abstract class AbstractStaticQuarkusPropertiesProvider extends AbstractStaticPropertiesProvider {

	private static final String PLUGIN_ID = "com.redhat.microprofile.jdt.quarkus";

	public AbstractStaticQuarkusPropertiesProvider(String path) {
		super(PLUGIN_ID, path);
	}

	@Override
	protected boolean isAdaptedFor(SearchContext context, IProgressMonitor monitor) {
		IJavaProject javaProject = context.getJavaProject();
		return JDTQuarkusUtils.isQuarkusProject(javaProject);
	}

}

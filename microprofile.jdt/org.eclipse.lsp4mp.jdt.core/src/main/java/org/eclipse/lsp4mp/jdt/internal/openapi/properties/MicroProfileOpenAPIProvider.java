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
package org.eclipse.lsp4mp.jdt.internal.openapi.properties;

import static org.eclipse.lsp4mp.jdt.internal.openapi.MicroProfileOpenAPIConstants.OPEN_API_CONFIG;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.lsp4mp.jdt.core.AbstractStaticPropertiesProvider;
import org.eclipse.lsp4mp.jdt.core.MicroProfileCorePlugin;
import org.eclipse.lsp4mp.jdt.core.SearchContext;
import org.eclipse.lsp4mp.jdt.core.utils.JDTTypeUtils;

/**
 * Properties provider that provides static MicroProfile OpenAPI properties
 * 
 * @author David Kwon
 * 
 * @see https://github.com/eclipse/microprofile-open-api/blob/master/spec/src/main/asciidoc/microprofile-openapi-spec.adoc#311-core-configurations
 *
 */
public class MicroProfileOpenAPIProvider extends AbstractStaticPropertiesProvider {

	public MicroProfileOpenAPIProvider() {
		super(MicroProfileCorePlugin.PLUGIN_ID, "/static-properties/mp-openapi-metadata.json");
	}

	@Override
	protected boolean isAdaptedFor(SearchContext context, IProgressMonitor monitor) {
		IJavaProject javaProject = context.getJavaProject();
		return (JDTTypeUtils.findType(javaProject, OPEN_API_CONFIG) != null);
	}
}

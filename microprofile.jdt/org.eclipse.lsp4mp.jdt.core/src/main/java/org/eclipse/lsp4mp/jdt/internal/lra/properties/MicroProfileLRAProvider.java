/*******************************************************************************
* Copyright (c) 2020 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lsp4mp.jdt.internal.lra.properties;

import static org.eclipse.lsp4mp.jdt.internal.lra.MicroProfileLRAConstants.LRA_ANNOTATION;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.lsp4mp.jdt.core.AbstractStaticPropertiesProvider;
import org.eclipse.lsp4mp.jdt.core.MicroProfileCorePlugin;
import org.eclipse.lsp4mp.jdt.core.SearchContext;
import org.eclipse.lsp4mp.jdt.core.utils.JDTTypeUtils;

/**
 * Properties provider that provides static MicroProfile LRA properties
 * 
 * @author David Kwon
 * 
 * @see https://github.com/eclipse/microprofile-lra/blob/2d7b24b4bcb755eadb19c74dadd504cc41b0c094/spec/src/main/asciidoc/microprofile-lra-spec.adoc#322-configuration-parameters
 *
 */
public class MicroProfileLRAProvider extends AbstractStaticPropertiesProvider {
	
	public MicroProfileLRAProvider() {
		super(MicroProfileCorePlugin.PLUGIN_ID, "/static-properties/mp-lra-metadata.json");
	}

	@Override
	protected boolean isAdaptedFor(SearchContext context, IProgressMonitor monitor) {
		IJavaProject javaProject = context.getJavaProject();
		return (JDTTypeUtils.findType(javaProject, LRA_ANNOTATION) != null);
	}
}

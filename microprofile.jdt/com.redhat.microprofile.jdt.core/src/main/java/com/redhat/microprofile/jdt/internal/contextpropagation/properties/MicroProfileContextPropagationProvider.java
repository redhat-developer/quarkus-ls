/*******************************************************************************
* Copyright (c) 2020 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/

package com.redhat.microprofile.jdt.internal.contextpropagation.properties;

import static com.redhat.microprofile.jdt.internal.contextpropagation.MicroProfileContextPropagationConstants.CONTEXT_PROPAGATION_ANNOTATION;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;

import com.redhat.microprofile.jdt.core.AbstractStaticPropertiesProvider;
import com.redhat.microprofile.jdt.core.MicroProfileCorePlugin;
import com.redhat.microprofile.jdt.core.SearchContext;
import com.redhat.microprofile.jdt.core.utils.JDTTypeUtils;

/**
 * Properties provider that provides static MicroProfile Context Propagation
 * properties
 * 
 * @author Ryan Zegray
 * 
 * @see https://github.com/eclipse/microprofile-context-propagation/blob/0980f2d63a3a231c3e9d28939b14bd3c5ee7639f/spec/src/main/asciidoc/mpconfig.asciidoc
 *
 */
public class MicroProfileContextPropagationProvider extends AbstractStaticPropertiesProvider {
	public MicroProfileContextPropagationProvider() {
		super(MicroProfileCorePlugin.PLUGIN_ID, "/static-properties/mp-context-propagation-metadata.json");
	}

	@Override
	protected boolean isAdaptedFor(SearchContext context, IProgressMonitor monitor) {
		// Check if MicroProfile context propagation exists in classpath
		IJavaProject javaProject = context.getJavaProject();
		return (JDTTypeUtils.findType(javaProject, CONTEXT_PROPAGATION_ANNOTATION) != null);
	}
}
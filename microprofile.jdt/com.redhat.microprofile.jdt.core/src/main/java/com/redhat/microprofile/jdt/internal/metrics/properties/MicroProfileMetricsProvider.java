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
package com.redhat.microprofile.jdt.internal.metrics.properties;

import static com.redhat.microprofile.jdt.internal.metrics.MicroProfileMetricsConstants.METRIC_ID;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;

import com.redhat.microprofile.jdt.core.AbstractStaticPropertiesProvider;
import com.redhat.microprofile.jdt.core.MicroProfileCorePlugin;
import com.redhat.microprofile.jdt.core.SearchContext;
import com.redhat.microprofile.jdt.core.utils.JDTTypeUtils;

/**
 * Properties provider that provides static MicroProfile Metrics properties
 * 
 * @author David Kwon
 * 
 * @see https://github.com/eclipse/microprofile-metrics/blob/dc94b84ec90dd4a0bc983336e4273247a4e415cc/spec/src/main/asciidoc/architecture.adoc
 *
 */
public class MicroProfileMetricsProvider extends AbstractStaticPropertiesProvider {
	
	public MicroProfileMetricsProvider() {
		super(MicroProfileCorePlugin.PLUGIN_ID, "/static-properties/mp-metrics-metadata.json");
	}

	@Override
	protected boolean isAdaptedFor(SearchContext context, IProgressMonitor monitor) {
		IJavaProject javaProject = context.getJavaProject();
		return (JDTTypeUtils.findType(javaProject, METRIC_ID) != null);
	}
}

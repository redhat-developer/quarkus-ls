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

import static com.redhat.microprofile.jdt.internal.metrics.MicroProfileMetricsConstants.APPLICATION_NAME_VARIABLE;
import static com.redhat.microprofile.jdt.internal.metrics.MicroProfileMetricsConstants.GLOBAL_TAGS_VARIABLE;
import static com.redhat.microprofile.jdt.internal.metrics.MicroProfileMetricsConstants.METRIC_ID;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;

import com.redhat.microprofile.jdt.core.AbstractStaticPropertiesProvider;
import com.redhat.microprofile.jdt.core.IPropertiesCollector;
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

	@Override
	protected boolean isAdaptedFor(SearchContext context, IProgressMonitor monitor) {
		IJavaProject javaProject = context.getJavaProject();
		return (JDTTypeUtils.findType(javaProject, METRIC_ID) != null);
	}

	@Override
	protected void collectStaticProperties(SearchContext context, IProgressMonitor monitor) {
		IPropertiesCollector collector = context.getCollector();
		String docs = "List of tag values.\r\n"
				+ "Tag values set through `mp.metrics.tags` MUST escape equal symbols `=` and commas `,` with a backslash `\\`.";
		super.addItemMetadata(collector, GLOBAL_TAGS_VARIABLE, "java.util.Optional<java.lang.String>", docs, null, null,
				null, null, null, false);

		docs = "The app name.";
		super.addItemMetadata(collector, APPLICATION_NAME_VARIABLE, "java.util.Optional<java.lang.String>", docs, null,
				null, null, null, null, false);
	}

}

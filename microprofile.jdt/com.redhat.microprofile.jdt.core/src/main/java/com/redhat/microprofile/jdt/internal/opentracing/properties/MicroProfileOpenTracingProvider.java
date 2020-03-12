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
package com.redhat.microprofile.jdt.internal.opentracing.properties;

import static com.redhat.microprofile.jdt.internal.opentracing.MicroProfileOpenTracingConstants.OPERATION_NAME_PROVIDER;
import static com.redhat.microprofile.jdt.internal.opentracing.MicroProfileOpenTracingConstants.SKIP_PATTERN;
import static com.redhat.microprofile.jdt.internal.opentracing.MicroProfileOpenTracingConstants.TRACED_ANNOTATION;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;

import com.redhat.microprofile.commons.metadata.ItemHint;
import com.redhat.microprofile.commons.metadata.ItemHint.ValueHint;
import com.redhat.microprofile.jdt.core.AbstractStaticPropertiesProvider;
import com.redhat.microprofile.jdt.core.IPropertiesCollector;
import com.redhat.microprofile.jdt.core.SearchContext;
import com.redhat.microprofile.jdt.core.utils.JDTTypeUtils;

/**
 * Properties provider that provides static MicroProfile Metrics properties
 * 
 * @author David Kwon
 * 
 * @see https://github.com/eclipse/microprofile-opentracing/blob/master/spec/src/main/asciidoc/configuration.asciidoc
 *
 */
public class MicroProfileOpenTracingProvider extends AbstractStaticPropertiesProvider {

	@Override
	protected boolean isAdaptedFor(SearchContext context, IProgressMonitor monitor) {
		IJavaProject javaProject = context.getJavaProject();
		return (JDTTypeUtils.findType(javaProject, TRACED_ANNOTATION) != null);
	}

	@Override
	protected void collectStaticProperties(SearchContext context, IProgressMonitor monitor) {
		IPropertiesCollector collector = context.getCollector();
		String docs = "Specifies a skip pattern to avoid tracing of selected REST endpoints.";
		super.addItemMetadata(collector, SKIP_PATTERN, "java.util.Optional<java.util.regex.Pattern>", docs, null, null,
				null, null, null, false);

		docs = "Specifies operation name provider for server spans. Possible values are `http-path` and `class-method`.";
		super.addItemMetadata(collector, OPERATION_NAME_PROVIDER, "\"http-path\" or \"class-method\"", docs, null,
				null, null, "class-method", null, false);
		
		ItemHint itemHint = collector.getItemHint(OPERATION_NAME_PROVIDER);
		addHint(itemHint, "class-method", "The provider for the default operation name.");
		addHint(itemHint, "http-path",
				"The operation name has the following form `<HTTP method>:<@Path value of endpoint’s class>/<@Path value of endpoint’s method>`. "
				+ "For example if the class is annotated with `@Path(\"service\")` and method `@Path(\"endpoint/{id: \\\\d+}\")` "
				+ "then the operation name is `GET:/service/endpoint/{id: \\\\d+}`.");
	}
	
	private void addHint(ItemHint itemHint, String value, String description) {
		ValueHint hint = new ValueHint();
		hint.setValue(value);
		hint.setDescription(description);
		itemHint.getValues().add(hint);
	}
}
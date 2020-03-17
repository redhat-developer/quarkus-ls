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
package com.redhat.microprofile.jdt.internal.openapi.properties;

import static com.redhat.microprofile.jdt.internal.openapi.MicroProfileOpenAPIConstants.OPEN_API_CONFIG;
import static com.redhat.microprofile.jdt.internal.openapi.MicroProfileOpenAPIConstants.MODEL_READER;
import static com.redhat.microprofile.jdt.internal.openapi.MicroProfileOpenAPIConstants.FILTER;
import static com.redhat.microprofile.jdt.internal.openapi.MicroProfileOpenAPIConstants.SCAN_DISABLE;
import static com.redhat.microprofile.jdt.internal.openapi.MicroProfileOpenAPIConstants.SCAN_PACKAGES;
import static com.redhat.microprofile.jdt.internal.openapi.MicroProfileOpenAPIConstants.SCAN_CLASSES;
import static com.redhat.microprofile.jdt.internal.openapi.MicroProfileOpenAPIConstants.SCAN_EXCLUDE_PACKAGES;
import static com.redhat.microprofile.jdt.internal.openapi.MicroProfileOpenAPIConstants.SCAN_EXCLUDE_CLASSES;
import static com.redhat.microprofile.jdt.internal.openapi.MicroProfileOpenAPIConstants.SERVERS;
import static com.redhat.microprofile.jdt.internal.openapi.MicroProfileOpenAPIConstants.SERVERS_PATH;
import static com.redhat.microprofile.jdt.internal.openapi.MicroProfileOpenAPIConstants.SERVERS_OPERATION;
import static com.redhat.microprofile.jdt.internal.openapi.MicroProfileOpenAPIConstants.SCHEMA;


import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;

import com.redhat.microprofile.jdt.core.AbstractStaticPropertiesProvider;
import com.redhat.microprofile.jdt.core.IPropertiesCollector;
import com.redhat.microprofile.jdt.core.SearchContext;
import com.redhat.microprofile.jdt.core.utils.JDTTypeUtils;

/**
 * Properties provider that provides static MicroProfile OpenAPI properties
 * 
 * @author David Kwon
 * 
 * @see https://github.com/eclipse/microprofile-open-api/blob/master/spec/src/main/asciidoc/microprofile-openapi-spec.adoc#311-core-configurations
 *
 */
public class MicroProfileOpenAPIProvider extends AbstractStaticPropertiesProvider {

	@Override
	protected boolean isAdaptedFor(SearchContext context, IProgressMonitor monitor) {
		IJavaProject javaProject = context.getJavaProject();
		return (JDTTypeUtils.findType(javaProject, OPEN_API_CONFIG) != null);
	}

	@Override
	protected void collectStaticProperties(SearchContext context, IProgressMonitor monitor) {
		IPropertiesCollector collector = context.getCollector();
		String docs = "Configuration property to specify the fully qualified name of the OASModelReader implementation.";
		super.addItemMetadata(collector, MODEL_READER, "java.lang.String", docs, null, null,
				null, null, null, false);

		docs = "Configuration property to specify the fully qualified name of the OASFilter implementation.";
		super.addItemMetadata(collector, FILTER, "java.lang.String", docs, null,
				null, null, null, null, false);
		
		docs = "Configuration property to disable annotation scanning. Default value is `false`.";
		super.addItemMetadata(collector, SCAN_DISABLE, "boolean", docs, null,
				null, null, "false", null, false);
		
		docs = "Configuration property to specify the list of packages to scan.\r\n"
				+ "For example, `mp.openapi.scan.packages=com.xyz.PackageA,com.xyz.PackageB`.";
		super.addItemMetadata(collector, SCAN_PACKAGES, "java.lang.String", docs, null,
				null, null, null, null, false);
		
		docs = "Configuration property to specify the list of classes to scan.\r\n"
				+ "For example, `mp.openapi.scan.classes=com.xyz.MyClassA,com.xyz.MyClassB`.";
		super.addItemMetadata(collector, SCAN_CLASSES, "java.lang.String", docs, null,
				null, null, null, null, false);
		
		docs = "Configuration property to specify the list of packages to exclude from scans.\r\n"
				+ "For example, `mp.openapi.scan.exclude.packages=com.xyz.PackageC,com.xyz.PackageD`.";
		super.addItemMetadata(collector, SCAN_EXCLUDE_PACKAGES, "java.lang.String", docs, null,
				null, null, null, null, false);
		
		docs = "Configuration property to specify the list of classes to exclude from scans.\r\n"
				+ "For example, `mp.openapi.scan.exclude.classes=com.xyz.MyClassC,com.xyz.MyClassD`.";
		super.addItemMetadata(collector, SCAN_EXCLUDE_CLASSES, "java.lang.String", docs, null,
				null, null, null, null, false);
		
		docs = "Configuration property to specify the list of global servers that provide connectivity information.\r\n"
				+ "For example, `mp.openapi.servers=https://xyz.com/v1,https://abc.com/v1`.";
		super.addItemMetadata(collector, SERVERS, "java.lang.String", docs, null,
				null, null, null, null, false);
		
		docs = "Prefix of the configuration property to specify an alternative list of servers to service all operations in a path."
				+ "For example, `mp.openapi.servers.path./airlines/bookings/{id}=https://xyz.io/v1`.";
		super.addItemMetadata(collector, SERVERS_PATH, "java.lang.String", docs, null,
				null, null, null, null, false);
		
		docs = "Prefix of the configuration property to specify an alternative list of servers to service an operation."
				+ "Operations that want to specify an alternative list of servers must define an `operationId`, a unique string used to identify the operation."
				+ "For example, `mp.openapi.servers.operation.getBooking=https://abc.io/v1`.";
		super.addItemMetadata(collector, SERVERS_OPERATION, "java.lang.String", docs, null,
				null, null, null, null, false);
		
		docs = "Prefix of the configuration property to specify a schema for a specific class, in JSON format."
				+ "The remainder of the property key must be the fully-qualified class name."
				+ "The value must be a valid OpenAPI schema object, specified in the JSON format."
				+ "The use of this property is functionally equivalent to the use of the `@Schema` annotation"
				+ "on a Java class, but may be used in cases where the application developer does not have access to the source code of a class.";
		super.addItemMetadata(collector, SCHEMA, "java.lang.String", docs, null,
				null, null, null, null, false);
	}

}

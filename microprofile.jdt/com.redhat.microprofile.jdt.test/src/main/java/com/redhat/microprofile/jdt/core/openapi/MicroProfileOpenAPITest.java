/*******************************************************************************
* Copyright (c) 2020 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.microprofile.jdt.core.openapi;

import static com.redhat.microprofile.jdt.internal.core.MicroProfileAssert.assertProperties;
import static com.redhat.microprofile.jdt.internal.core.MicroProfileAssert.assertPropertiesDuplicate;
import static com.redhat.microprofile.jdt.internal.core.MicroProfileAssert.p;

import org.junit.Test;

import com.redhat.microprofile.commons.MicroProfileProjectInfo;
import com.redhat.microprofile.commons.MicroProfilePropertiesScope;
import com.redhat.microprofile.jdt.core.BasePropertiesManagerTest;

/**
 * Test the availability of the MicroProfile Open API properties
 * 
 * @author David Kwon
 *
 */
public class MicroProfileOpenAPITest extends BasePropertiesManagerTest {

	@Test
	public void microprofileOpenAPI() throws Exception {

		MicroProfileProjectInfo infoFromClasspath = getMicroProfileProjectInfoFromMavenProject(
				MavenProjectName.microprofile_openapi, MicroProfilePropertiesScope.SOURCES_AND_DEPENDENCIES);

		assertProperties(infoFromClasspath,

				p("microprofile-openapi-api", "mp.openapi.model.reader", "java.lang.String", 
						"Configuration property to specify the fully qualified name of the OASModelReader implementation.",
						true, null, null, null, 0, null),

				p("microprofile-openapi-api", "mp.openapi.filter", "java.lang.String",
						"Configuration property to specify the fully qualified name of the OASFilter implementation.",
						true, null, null, null, 0, null),
				
				p("microprofile-openapi-api", "mp.openapi.scan.disable", "boolean",
						"Configuration property to disable annotation scanning. Default value is `false`.",
						true, null, null, null, 0, "false"),
				
				p("microprofile-openapi-api", "mp.openapi.scan.packages", "java.lang.String",
						"Configuration property to specify the list of packages to scan.\r\n"
						+ "For example, `mp.openapi.scan.packages=com.xyz.PackageA,com.xyz.PackageB`.",
						true, null, null, null, 0, null),
				
				p("microprofile-openapi-api", "mp.openapi.scan.classes", "java.lang.String",
						"Configuration property to specify the list of classes to scan.\r\n"
						+ "For example, `mp.openapi.scan.classes=com.xyz.MyClassA,com.xyz.MyClassB`.",
						true, null, null, null, 0, null),
				
				p("microprofile-openapi-api", "mp.openapi.scan.exclude.packages", "java.lang.String",
						"Configuration property to specify the list of packages to exclude from scans.\r\n"
						+ "For example, `mp.openapi.scan.exclude.packages=com.xyz.PackageC,com.xyz.PackageD`.",
						true, null, null, null, 0, null),
				
				p("microprofile-openapi-api", "mp.openapi.scan.exclude.classes", "java.lang.String",
						"Configuration property to specify the list of classes to exclude from scans.\r\n"
						+ "For example, `mp.openapi.scan.exclude.classes=com.xyz.MyClassC,com.xyz.MyClassD`.",
						true, null, null, null, 0, null),
				
				p("microprofile-openapi-api", "mp.openapi.servers", "java.lang.String",
						"Configuration property to specify the list of global servers that provide connectivity information.\r\n"
						+ "For example, `mp.openapi.servers=https://xyz.com/v1,https://abc.com/v1`.",
						true, null, null, null, 0, null),
				
				p("microprofile-openapi-api", "mp.openapi.servers.path.{*}", "java.lang.String",
						"Prefix of the configuration property to specify an alternative list of servers to service all operations in a path."
						+ "For example, `mp.openapi.servers.path./airlines/bookings/{id}=https://xyz.io/v1`.",
						true, null, null, null, 0, null),
				
				p("microprofile-openapi-api", "mp.openapi.servers.operation.{*}", "java.lang.String",
						"Prefix of the configuration property to specify an alternative list of servers to service an operation."
						+ "Operations that want to specify an alternative list of servers must define an `operationId`, a unique string used to identify the operation."
						+ "For example, `mp.openapi.servers.operation.getBooking=https://abc.io/v1`.",
						true, null, null, null, 0, null),
				
				p("microprofile-openapi-api", "mp.openapi.schema.{*}", "java.lang.String",
						"Prefix of the configuration property to specify a schema for a specific class, in JSON format."
								+ "The remainder of the property key must be the fully-qualified class name."
								+ "The value must be a valid OpenAPI schema object, specified in the JSON format."
								+ "The use of this property is functionally equivalent to the use of the `@Schema` annotation"
								+ "on a Java class, but may be used in cases where the application developer does not have access to the source code of a class.",
						true, null, null, null, 0, null)

		);

		assertPropertiesDuplicate(infoFromClasspath);
	}

}

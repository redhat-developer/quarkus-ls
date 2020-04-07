/*******************************************************************************
* Copyright (c) 2020 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.microprofile.jdt.core.opentracing;

import static com.redhat.microprofile.jdt.internal.core.MicroProfileAssert.assertHints;
import static com.redhat.microprofile.jdt.internal.core.MicroProfileAssert.assertHintsDuplicate;
import static com.redhat.microprofile.jdt.internal.core.MicroProfileAssert.assertProperties;
import static com.redhat.microprofile.jdt.internal.core.MicroProfileAssert.assertPropertiesDuplicate;
import static com.redhat.microprofile.jdt.internal.core.MicroProfileAssert.h;
import static com.redhat.microprofile.jdt.internal.core.MicroProfileAssert.p;
import static com.redhat.microprofile.jdt.internal.core.MicroProfileAssert.vh;

import org.junit.Test;

import com.redhat.microprofile.commons.MicroProfileProjectInfo;
import com.redhat.microprofile.commons.MicroProfilePropertiesScope;
import com.redhat.microprofile.jdt.core.BasePropertiesManagerTest;

/**
 * Test the availability of the MicroProfile OpenTracing properties
 * 
 * @author David Kwon
 *
 */
public class MicroProfileOpenTracingTest extends BasePropertiesManagerTest {

	@Test
	public void microprofileOpenTracing() throws Exception {

		MicroProfileProjectInfo infoFromClasspath = getMicroProfileProjectInfoFromMavenProject(
				MavenProjectName.microprofile_opentracing, MicroProfilePropertiesScope.SOURCES_AND_DEPENDENCIES);

		assertProperties(infoFromClasspath,

				p("microprofile-opentracing-api", "mp.opentracing.server.skip-pattern", "java.util.regex.Pattern",
						"Specifies a skip pattern to avoid tracing of selected REST endpoints.",
						true, null, null, null, 0, null),

				p("microprofile-opentracing-api", "mp.opentracing.server.operation-name-provider", "\"http-path\" or \"class-method\"",
						"Specifies operation name provider for server spans. Possible values are `http-path` and `class-method`.",
						true, null, null, null, 0, "class-method")

		);

		assertPropertiesDuplicate(infoFromClasspath);
		
		assertHints(infoFromClasspath, h("mp.opentracing.server.operation-name-provider", null, false, "mp.opentracing.server.operation-name-provider", //
				vh("class-method", "The provider for the default operation name.", null), //
				vh("http-path", "The operation name has the following form `<HTTP method>:<@Path value of endpoint’s class>/<@Path value of endpoint’s method>`. "
						+ "For example if the class is annotated with `@Path(\"service\")` and method `@Path(\"endpoint/{id: \\\\d+}\")` "
						+ "then the operation name is `GET:/service/endpoint/{id: \\\\d+}`.", null)) //
		);
		
		

		assertHintsDuplicate(infoFromClasspath);
	}

}

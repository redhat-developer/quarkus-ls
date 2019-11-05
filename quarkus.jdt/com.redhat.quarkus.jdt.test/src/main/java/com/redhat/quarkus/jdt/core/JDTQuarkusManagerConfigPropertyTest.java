/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.quarkus.jdt.core;

import static com.redhat.quarkus.jdt.internal.core.QuarkusAssert.assertProperties;
import static com.redhat.quarkus.jdt.internal.core.QuarkusAssert.p;

import org.junit.Test;

import com.redhat.quarkus.commons.QuarkusProjectInfo;
import com.redhat.quarkus.commons.QuarkusPropertiesScope;

/**
 * Test collect Quarkus properties from @ConfigProperty
 * 
 * @author Angelo ZERR
 *
 */
public class JDTQuarkusManagerConfigPropertyTest extends BaseJDTQuarkusManagerTest {

	@Test
	public void applicationConfigurationFromClasspath() throws Exception {

		//
		QuarkusProjectInfo infoFromClasspath = getQuarkusProjectInfoFromMavenProject("application-configuration",
				QuarkusPropertiesScope.classpath);

		assertProperties(infoFromClasspath, 61 /* properties from JAR */ + 3 /* properties from Java sources */,
				// GreetingResource
				// @ConfigProperty(name = "greeting.message")
				// String message;
				p(null, "greeting.message", "java.lang.String", null, "/application-configuration/src/main/java",
						"org.acme.config.GreetingResource#message", 0, null),

				// @ConfigProperty(name = "greeting.suffix" , defaultValue="!")
				// String suffix;
				p(null, "greeting.suffix", "java.lang.String", null, "/application-configuration/src/main/java",
						"org.acme.config.GreetingResource#suffix", 0, "!"),

				// @ConfigProperty(name = "greeting.name")
				// Optional<String> name;
				p(null, "greeting.name", "java.util.Optional", null, "/application-configuration/src/main/java",
						"org.acme.config.GreetingResource#name", 0, null));
	}

	@Test
	public void applicationConfigurationFromJavaSources() throws Exception {

		QuarkusProjectInfo infoFromJavaSources = getQuarkusProjectInfoFromMavenProject("application-configuration",
				QuarkusPropertiesScope.sources);

		assertProperties(infoFromJavaSources, 3 /* properties from Java sources */,
				// GreetingResource
				// @ConfigProperty(name = "greeting.message")
				// String message;
				p(null, "greeting.message", "java.lang.String", null, "/application-configuration/src/main/java",
						"org.acme.config.GreetingResource#message", 0, null),

				// @ConfigProperty(name = "greeting.suffix" , defaultValue="!")
				// String suffix;
				p(null, "greeting.suffix", "java.lang.String", null, "/application-configuration/src/main/java",
						"org.acme.config.GreetingResource#suffix", 0, "!"),

				// @ConfigProperty(name = "greeting.name")
				// Optional<String> name;
				p(null, "greeting.name", "java.util.Optional", null, "/application-configuration/src/main/java",
						"org.acme.config.GreetingResource#name", 0, null));

	}
}

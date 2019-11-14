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

import static com.redhat.quarkus.commons.ExtendedConfigDescriptionBuildItem.CONFIG_PHASE_BUILD_TIME;
import static com.redhat.quarkus.jdt.internal.core.QuarkusAssert.assertProperties;
import static com.redhat.quarkus.jdt.internal.core.QuarkusAssert.assertPropertiesDuplicate;
import static com.redhat.quarkus.jdt.internal.core.QuarkusAssert.p;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

import com.redhat.quarkus.commons.QuarkusProjectInfo;
import com.redhat.quarkus.commons.QuarkusPropertiesScope;
import com.redhat.quarkus.jdt.internal.core.utils.DependencyUtil;

/**
 * Test collect Quarkus properties from @ConfigProperty
 * 
 * @author Angelo ZERR
 *
 */
public class JDTQuarkusManagerConfigPropertyTest extends BaseJDTQuarkusManagerTest {

	@Test
	public void configQuickstartFromClasspath() throws Exception {

		//
		QuarkusProjectInfo infoFromClasspath = getQuarkusProjectInfoFromMavenProject("config-quickstart",
				QuarkusPropertiesScope.classpath);

		File f = DependencyUtil.getArtifact("io.quarkus", "quarkus-core-deployment", "1.0.0.CR1", null);
		Assert.assertNotNull("Test existing of quarkus-core-deployment*.jar", f);

		String expectedDeploymentJar = f.getAbsolutePath();
		assertProperties(infoFromClasspath, 185 /* properties from JAR */ + 3 /* properties from Java sources */,

				// io.quarkus.deployment.ApplicationConfig
				p("quarkus-core", "quarkus.application.name", "java.lang.String",
						"The name of the application.\nIf not set, defaults to the name of the project.",
						expectedDeploymentJar, "io.quarkus.deployment.ApplicationConfig#name", CONFIG_PHASE_BUILD_TIME,
						null),

				p("quarkus-core", "quarkus.application.version", "java.lang.String",
						"The version of the application.\nIf not set, defaults to the version of the project",
						expectedDeploymentJar, "io.quarkus.deployment.ApplicationConfig#version",
						CONFIG_PHASE_BUILD_TIME, null),

				// GreetingResource
				// @ConfigProperty(name = "greeting.message")
				// String message;
				p(null, "greeting.message", "java.lang.String", null, "/config-quickstart/src/main/java",
						"org.acme.config.GreetingResource#message", 0, null),

				// @ConfigProperty(name = "greeting.suffix" , defaultValue="!")
				// String suffix;
				p(null, "greeting.suffix", "java.lang.String", null, "/config-quickstart/src/main/java",
						"org.acme.config.GreetingResource#suffix", 0, "!"),

				// @ConfigProperty(name = "greeting.name")
				// Optional<String> name;
				p(null, "greeting.name", "java.util.Optional", null, "/config-quickstart/src/main/java",
						"org.acme.config.GreetingResource#name", 0, null));

		assertPropertiesDuplicate(infoFromClasspath);
	}

	@Test
	public void configQuickstartFromJavaSources() throws Exception {

		QuarkusProjectInfo infoFromJavaSources = getQuarkusProjectInfoFromMavenProject("config-quickstart",
				QuarkusPropertiesScope.sources);

		assertProperties(infoFromJavaSources, 3 /* properties from Java sources */,
				// GreetingResource
				// @ConfigProperty(name = "greeting.message")
				// String message;
				p(null, "greeting.message", "java.lang.String", null, "/config-quickstart/src/main/java",
						"org.acme.config.GreetingResource#message", 0, null),

				// @ConfigProperty(name = "greeting.suffix" , defaultValue="!")
				// String suffix;
				p(null, "greeting.suffix", "java.lang.String", null, "/config-quickstart/src/main/java",
						"org.acme.config.GreetingResource#suffix", 0, "!"),

				// @ConfigProperty(name = "greeting.name")
				// Optional<String> name;
				p(null, "greeting.name", "java.util.Optional", null, "/config-quickstart/src/main/java",
						"org.acme.config.GreetingResource#name", 0, null));

	}
}

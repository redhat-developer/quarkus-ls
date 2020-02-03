/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.microprofile.jdt.core;

import static com.redhat.microprofile.commons.metadata.ItemMetadata.CONFIG_PHASE_BUILD_TIME;
import static com.redhat.microprofile.jdt.internal.core.MicroProfileAssert.assertProperties;
import static com.redhat.microprofile.jdt.internal.core.MicroProfileAssert.assertPropertiesDuplicate;
import static com.redhat.microprofile.jdt.internal.core.MicroProfileAssert.p;

import java.io.File;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.Assert;
import org.junit.Test;

import com.redhat.microprofile.commons.MicroProfileProjectInfo;
import com.redhat.microprofile.commons.MicroProfilePropertiesScope;
import com.redhat.microprofile.jdt.internal.core.utils.DependencyUtil;

/**
 * Test collection of Quarkus properties from @ConfigProperty
 * 
 * @author Angelo ZERR
 *
 */
public class MicroProfileConfigPropertyTest extends BasePropertiesManagerTest {

	@Test
	public void configQuickstartFromClasspath() throws Exception {

		//
		MicroProfileProjectInfo infoFromClasspath = getMicroProfileProjectInfoFromMavenProject(
				MavenProjectName.config_quickstart, MicroProfilePropertiesScope.SOURCES_AND_DEPENDENCIES);

		File f = DependencyUtil.getArtifact("io.quarkus", "quarkus-core-deployment", "1.0.0.CR1", null,
				new NullProgressMonitor());
		Assert.assertNotNull("Test existing of quarkus-core-deployment*.jar", f);

		assertProperties(infoFromClasspath, 185 /* properties from JAR */ + //
				3 /* properties from Java sources with ConfigProperty */ + //
				2 /* properties from Java sources with ConfigRoot */,

				// io.quarkus.deployment.ApplicationConfig
				p("quarkus-core", "quarkus.application.name", "java.lang.String",
						"The name of the application.\nIf not set, defaults to the name of the project.", true,
						"io.quarkus.deployment.ApplicationConfig", "name", null, CONFIG_PHASE_BUILD_TIME, null),

				p("quarkus-core", "quarkus.application.version", "java.lang.String",
						"The version of the application.\nIf not set, defaults to the version of the project", true,
						"io.quarkus.deployment.ApplicationConfig", "version", null, CONFIG_PHASE_BUILD_TIME, null),

				// GreetingResource
				// @ConfigProperty(name = "greeting.message")
				// String message;
				p(null, "greeting.message", "java.lang.String", null, false, "org.acme.config.GreetingResource",
						"message", null, 0, null),

				// @ConfigProperty(name = "greeting.suffix" , defaultValue="!")
				// String suffix;
				p(null, "greeting.suffix", "java.lang.String", null, false, "org.acme.config.GreetingResource",
						"suffix", null, 0, "!"),

				// @ConfigProperty(name = "greeting.name")
				// Optional<String> name;
				p(null, "greeting.name", "java.util.Optional", null, false, "org.acme.config.GreetingResource", "name",
						null, 0, null),

				// @ConfigRoot / CustomExtensionConfig / property1
				p(null, "quarkus.custom-extension.property1", "java.lang.String", null, false,
						"org.acme.config.CustomExtensionConfig", "property1", null, CONFIG_PHASE_BUILD_TIME, null),

				// @ConfigRoot / CustomExtensionConfig / property2
				p(null, "quarkus.custom-extension.property2", "java.lang.Integer", null, false,
						"org.acme.config.CustomExtensionConfig", "property2", null, CONFIG_PHASE_BUILD_TIME, null));

		assertPropertiesDuplicate(infoFromClasspath);
	}

	@Test
	public void configQuickstartFromJavaSources() throws Exception {

		MicroProfileProjectInfo infoFromJavaSources = getMicroProfileProjectInfoFromMavenProject(
				MavenProjectName.config_quickstart, MicroProfilePropertiesScope.ONLY_SOURCES);

		assertProperties(infoFromJavaSources, 3 /* properties from Java sources with ConfigProperty */ + //
				2 /* properties from Java sources with ConfigRoot */,

				// GreetingResource
				// @ConfigProperty(name = "greeting.message")
				// String message;
				p(null, "greeting.message", "java.lang.String", null, false, "org.acme.config.GreetingResource",
						"message", null, 0, null),

				// @ConfigProperty(name = "greeting.suffix" , defaultValue="!")
				// String suffix;
				p(null, "greeting.suffix", "java.lang.String", null, false, "org.acme.config.GreetingResource",
						"suffix", null, 0, "!"),

				// @ConfigProperty(name = "greeting.name")
				// Optional<String> name;
				p(null, "greeting.name", "java.util.Optional", null, false, "org.acme.config.GreetingResource", "name",
						null, 0, null),

				// @ConfigRoot / CustomExtensionConfig / property1
				p(null, "quarkus.custom-extension.property1", "java.lang.String", null, false,
						"org.acme.config.CustomExtensionConfig", "property1", null, CONFIG_PHASE_BUILD_TIME, null),

				// @ConfigRoot / CustomExtensionConfig / property2
				p(null, "quarkus.custom-extension.property2", "java.lang.Integer", null, false,
						"org.acme.config.CustomExtensionConfig", "property2", null, CONFIG_PHASE_BUILD_TIME, null));

		assertPropertiesDuplicate(infoFromJavaSources);
	}
}

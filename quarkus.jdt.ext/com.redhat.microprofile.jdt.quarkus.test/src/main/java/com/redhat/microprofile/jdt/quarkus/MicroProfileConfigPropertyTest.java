/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.microprofile.jdt.quarkus;

import static org.eclipse.lsp4mp.commons.metadata.ItemMetadata.CONFIG_PHASE_BUILD_AND_RUN_TIME_FIXED;
import static org.eclipse.lsp4mp.commons.metadata.ItemMetadata.CONFIG_PHASE_BUILD_TIME;
import static org.eclipse.lsp4mp.jdt.core.MicroProfileAssert.assertProperties;
import static org.eclipse.lsp4mp.jdt.core.MicroProfileAssert.assertPropertiesDuplicate;
import static org.eclipse.lsp4mp.jdt.core.MicroProfileAssert.p;

import org.eclipse.lsp4mp.commons.MicroProfileProjectInfo;
import org.eclipse.lsp4mp.commons.MicroProfilePropertiesScope;
import org.eclipse.lsp4mp.jdt.core.BasePropertiesManagerTest;
import org.junit.Test;

/**
 * Test collection of Quarkus properties from @ConfigProperty
 * 
 * @author Angelo ZERR
 *
 */
public class MicroProfileConfigPropertyTest extends BasePropertiesManagerTest {

	@Test
	public void configQuickstartFromClasspath() throws Exception {

		MicroProfileProjectInfo infoFromClasspath = getMicroProfileProjectInfoFromMavenProject(
				MicroProfileMavenProjectName.config_quickstart, MicroProfilePropertiesScope.SOURCES_AND_DEPENDENCIES);

		assertProperties(infoFromClasspath, 257 /* properties from JAR */ + //
				9 /* properties from Java sources with ConfigProperty */ + //
				2 /* properties from Java sources with ConfigRoot */ + //
				7 /* static properties from microprofile-context-propagation-api */ + //
				1 /* static property from mp-config-metadata */,

				// io.quarkus.deployment.ApplicationConfig
				p("quarkus-core", "quarkus.application.name", "java.util.Optional<java.lang.String>",
						"The name of the application.\nIf not set, defaults to the name of the project (except for tests where it is not set at all).",
						true, "io.quarkus.runtime.ApplicationConfig", "name", null,
						CONFIG_PHASE_BUILD_AND_RUN_TIME_FIXED, null),

				p("quarkus-core", "quarkus.application.version", "java.util.Optional<java.lang.String>",
						"The version of the application.\nIf not set, defaults to the version of the project (except for tests where it is not set at all).",
						true, "io.quarkus.runtime.ApplicationConfig", "version", null,
						CONFIG_PHASE_BUILD_AND_RUN_TIME_FIXED, null),

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

				// GreetingConstructorResource(
				// @ConfigProperty(name = "greeting.constructor.message") String message,
				// @ConfigProperty(name = "greeting.constructor.suffix" , defaultValue="!")
				// String suffix,
				// @ConfigProperty(name = "greeting.constructor.name") Optional<String> name)
				p(null, "greeting.constructor.message", "java.lang.String", null, false,
						"org.acme.config.GreetingConstructorResource", null,
						"GreetingConstructorResource(QString;QString;QOptional<QString;>;)V", 0, null),

				p(null, "greeting.constructor.suffix", "java.lang.String", null, false,
						"org.acme.config.GreetingConstructorResource", null,
						"GreetingConstructorResource(QString;QString;QOptional<QString;>;)V", 0, "!"),

				p(null, "greeting.constructor.name", "java.util.Optional", null, false,
						"org.acme.config.GreetingConstructorResource", null,
						"GreetingConstructorResource(QString;QString;QOptional<QString;>;)V", 0, null),

				// setMessage(@ConfigProperty(name = "greeting.method.message") String message)
				p(null, "greeting.method.message", "java.lang.String", null, false,
						"org.acme.config.GreetingMethodResource", null, "setMessage(QString;)V", 0, null),

				// setSuffix(@ConfigProperty(name = "greeting.method.suffix" , defaultValue="!")
				// String suffix)
				p(null, "greeting.method.suffix", "java.lang.String", null, false,
						"org.acme.config.GreetingMethodResource", null, "setSuffix(QString;)V", 0, "!"),

				// setName(@ConfigProperty(name = "greeting.method.name") Optional<String> name)
				p(null, "greeting.method.name", "java.util.Optional", null, false,
						"org.acme.config.GreetingMethodResource", null, "setName(QOptional<QString;>;)V", 0, null),

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
				MicroProfileMavenProjectName.config_quickstart, MicroProfilePropertiesScope.ONLY_SOURCES);

		assertProperties(infoFromJavaSources, 9 /* properties from Java sources with ConfigProperty */ + //
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

				// GreetingConstructorResource(
				// @ConfigProperty(name = "greeting.constructor.message") String message,
				// @ConfigProperty(name = "greeting.constructor.suffix" , defaultValue="!")
				// String suffix,
				// @ConfigProperty(name = "greeting.constructor.name") Optional<String> name)
				p(null, "greeting.constructor.message", "java.lang.String", null, false,
						"org.acme.config.GreetingConstructorResource", null,
						"GreetingConstructorResource(QString;QString;QOptional<QString;>;)V", 0, null),

				p(null, "greeting.constructor.suffix", "java.lang.String", null, false,
						"org.acme.config.GreetingConstructorResource", null,
						"GreetingConstructorResource(QString;QString;QOptional<QString;>;)V", 0, "!"),

				p(null, "greeting.constructor.name", "java.util.Optional", null, false,
						"org.acme.config.GreetingConstructorResource", null,
						"GreetingConstructorResource(QString;QString;QOptional<QString;>;)V", 0, null),

				// setMessage(@ConfigProperty(name = "greeting.method.message") String message)
				p(null, "greeting.method.message", "java.lang.String", null, false,
						"org.acme.config.GreetingMethodResource", null, "setMessage(QString;)V", 0, null),

				// setSuffix(@ConfigProperty(name = "greeting.method.suffix" , defaultValue="!")
				// String suffix)
				p(null, "greeting.method.suffix", "java.lang.String", null, false,
						"org.acme.config.GreetingMethodResource", null, "setSuffix(QString;)V", 0, "!"),

				// setName(@ConfigProperty(name = "greeting.method.name") Optional<String> name)
				p(null, "greeting.method.name", "java.util.Optional", null, false,
						"org.acme.config.GreetingMethodResource", null, "setName(QOptional<QString;>;)V", 0, null),

				// @ConfigRoot / CustomExtensionConfig / property1
				p(null, "quarkus.custom-extension.property1", "java.lang.String", null, false,
						"org.acme.config.CustomExtensionConfig", "property1", null, CONFIG_PHASE_BUILD_TIME, null),

				// @ConfigRoot / CustomExtensionConfig / property2
				p(null, "quarkus.custom-extension.property2", "java.lang.Integer", null, false,
						"org.acme.config.CustomExtensionConfig", "property2", null, CONFIG_PHASE_BUILD_TIME, null));

		assertPropertiesDuplicate(infoFromJavaSources);
	}
}

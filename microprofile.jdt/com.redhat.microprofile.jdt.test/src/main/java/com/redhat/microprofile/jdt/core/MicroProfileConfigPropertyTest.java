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

import static com.redhat.microprofile.commons.metadata.ItemMetadata.CONFIG_PHASE_BUILD_AND_RUN_TIME_FIXED;
import static com.redhat.microprofile.commons.metadata.ItemMetadata.CONFIG_PHASE_BUILD_TIME;
import static com.redhat.microprofile.jdt.internal.core.MicroProfileAssert.assertProperties;
import static com.redhat.microprofile.jdt.internal.core.MicroProfileAssert.assertPropertiesDuplicate;
import static com.redhat.microprofile.jdt.internal.core.MicroProfileAssert.p;

import org.junit.Test;

import com.redhat.microprofile.commons.MicroProfileProjectInfo;
import com.redhat.microprofile.commons.MicroProfilePropertiesScope;

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
				MavenProjectName.config_quickstart, MicroProfilePropertiesScope.SOURCES_AND_DEPENDENCIES);

		assertProperties(infoFromClasspath,
				9 /* properties from Java sources with ConfigProperty */ + //
				7 /* static properties from microprofile-context-propagation-api */,

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
						"org.acme.config.GreetingMethodResource", null, "setName(QOptional<QString;>;)V", 0, null));

		assertPropertiesDuplicate(infoFromClasspath);
	}

	@Test
	public void configQuickstartFromJavaSources() throws Exception {

		MicroProfileProjectInfo infoFromJavaSources = getMicroProfileProjectInfoFromMavenProject(
				MavenProjectName.config_quickstart, MicroProfilePropertiesScope.ONLY_SOURCES);

		assertProperties(infoFromJavaSources, 9 /* properties from Java sources with ConfigProperty */,

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
						"org.acme.config.GreetingMethodResource", null, "setName(QOptional<QString;>;)V", 0, null));

		assertPropertiesDuplicate(infoFromJavaSources);
	}
}

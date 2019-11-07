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
import static com.redhat.quarkus.jdt.internal.core.QuarkusAssert.assertPropertiesDuplicate;
import static com.redhat.quarkus.jdt.internal.core.QuarkusAssert.p;

import org.junit.Assert;
import org.junit.Test;

import com.redhat.quarkus.commons.QuarkusProjectInfo;
import com.redhat.quarkus.commons.QuarkusPropertiesScope;

/**
 * Test collect Quarkus properties from @ConfigProperties
 * 
 * @author Angelo ZERR
 *
 */
public class JDTQuarkusManagerConfigPropertiesTest extends BaseJDTQuarkusManagerTest {

	private static final int EXPECTED_PROPERTIES_FROM_GREETING_INTERFACE = 3;
	private static final int EXPECTED_PROPERTIES_FROM_PUBLIC_FIELDS = 2;
	private static final int EXPECTED_PROPERTIES_FROM_GETTER = 3;
	private static final int EXPECTED_PROPERTIES_FROM_NO_PREFIX = 2;
	private static final int EXPECTED_PROPERTIES_FROM_STACK_OVERFLOW = 2;

	private final int EXPECTED_PROPERTIES = EXPECTED_PROPERTIES_FROM_GREETING_INTERFACE
			+ EXPECTED_PROPERTIES_FROM_PUBLIC_FIELDS + EXPECTED_PROPERTIES_FROM_GETTER
			+ EXPECTED_PROPERTIES_FROM_NO_PREFIX + EXPECTED_PROPERTIES_FROM_STACK_OVERFLOW;

	@Test
	public void configProperties() throws Exception {

		QuarkusProjectInfo infoFromJavaSources = getQuarkusProjectInfoFromMavenProject(
				MavenProjectName.config_properties, QuarkusPropertiesScope.sources);

		int nbProperties = 0;

		// Test with interface IGreetingConfiguration bound with @ConfigProperties
		nbProperties += EXPECTED_PROPERTIES_FROM_GREETING_INTERFACE;
		assertProperties(infoFromJavaSources,

				// @ConfigProperties(prefix = "greetingInterface")
				// IGreetingResource
				// @ConfigProperty(name = "message")
				// String message();
				p(null, "greetingInterface.message", "java.lang.String", null, "/config-properties/src/main/java",
						"org.acme.config.IGreetingConfiguration#message()QString;", 0, null),

				// @ConfigProperty(defaultValue="!")
				// String getSuffix();
				p(null, "greetingInterface.suffix", "java.lang.String", null, "/config-properties/src/main/java",
						"org.acme.config.IGreetingConfiguration#getSuffix()QString;", 0, "!"),

				// Optional<String> getName();
				p(null, "greetingInterface.name", "java.util.Optional", null, "/config-properties/src/main/java",
						"org.acme.config.IGreetingConfiguration#getName()QOptional<QString;>;", 0, null));

		// Test with class GreetingPublicFieldsConfiguration bound with
		// @ConfigProperties
		nbProperties += EXPECTED_PROPERTIES_FROM_PUBLIC_FIELDS;
		assertProperties(infoFromJavaSources,

				// @ConfigProperties(prefix = "greetingPublicFields")
				// GreetingPublicFieldsConfiguration {

				// public String message;
				p(null, "greetingPublicFields.message", "java.lang.String", null, "/config-properties/src/main/java",
						"org.acme.config.GreetingPublicFieldsConfiguration#message", 0, null),

				// public HiddenConfig hidden;
				p(null, "greetingPublicFields.hidden.recipients", "java.util.List", null,
						"/config-properties/src/main/java",
						"org.acme.config.GreetingPublicFieldsConfiguration$HiddenConfig#recipients", 0, null));

		// Test with class GreetingGetterConfiguration bound with
		// @ConfigProperties
		nbProperties += EXPECTED_PROPERTIES_FROM_GETTER;
		assertProperties(infoFromJavaSources,

				// @ConfigProperties(prefix = "greetingGetter")
				// GreetingGetterConfiguration {

				// private String message;
				p(null, "greetingGetter.message", "java.lang.String", null, "/config-properties/src/main/java",
						"org.acme.config.GreetingGetterConfiguration#message", 0, null),

				// private String suffix;
				p(null, "greetingGetter.suffix", "java.lang.String", null, "/config-properties/src/main/java",
						"org.acme.config.GreetingGetterConfiguration#suffix", 0, null),

				// public Optional<String> getName();
				p(null, "greetingGetter.name", "java.util.Optional", null, "/config-properties/src/main/java",
						"org.acme.config.GreetingGetterConfiguration#name", 0, null));

		// Test with class GreetingNoPrefixConfiguration bound with
		// @ConfigProperties
		nbProperties += EXPECTED_PROPERTIES_FROM_NO_PREFIX;
		assertProperties(infoFromJavaSources,

				// @ConfigProperties
				// GreetingNoPrefixConfiguration {

				// public String message;
				p(null, "greeting-no-prefix.message", "java.lang.String", null, "/config-properties/src/main/java",
						"org.acme.config.GreetingNoPrefixConfiguration#message", 0, null),

				// public HiddenConfig hidden;
				p(null, "greeting-no-prefix.hidden.recipients", "java.util.List", null,
						"/config-properties/src/main/java",
						"org.acme.config.GreetingNoPrefixConfiguration$HiddenConfig#recipients", 0, null));

		// Test with class GreetingStackOverflowConfiguration bound with
		// @ConfigProperties
		nbProperties += EXPECTED_PROPERTIES_FROM_STACK_OVERFLOW;
		assertProperties(infoFromJavaSources,

				// @ConfigProperties(prefix = "greetingStackOverflow")
				// GreetingStackOverflowConfiguration

				// public String message;
				p(null, "greetingStackOverflow.message", "java.lang.String", null, "/config-properties/src/main/java",
						"org.acme.config.GreetingStackOverflowConfiguration#message", 0, null),

				// public HiddenConfig hidden;
				p(null, "greetingStackOverflow.hidden.recipients", "java.util.List", null,
						"/config-properties/src/main/java",
						"org.acme.config.GreetingStackOverflowConfiguration$HiddenConfig#recipients", 0, null));

		assertPropertiesDuplicate(infoFromJavaSources);
		Assert.assertEquals("Expected Quarkus properties count", EXPECTED_PROPERTIES, nbProperties);
	}
}

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

import static com.redhat.microprofile.jdt.internal.core.MicroProfileAssert.assertProperties;
import static com.redhat.microprofile.jdt.internal.core.MicroProfileAssert.assertPropertiesDuplicate;
import static com.redhat.microprofile.jdt.internal.core.MicroProfileAssert.p;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.junit.Assert;
import org.junit.Test;

import com.redhat.microprofile.commons.ClasspathKind;
import com.redhat.microprofile.commons.DocumentFormat;
import com.redhat.microprofile.commons.MicroProfileProjectInfo;
import com.redhat.microprofile.commons.MicroProfilePropertiesScope;
import com.redhat.microprofile.jdt.core.project.JDTMicroProfileProject;

import com.redhat.microprofile.jdt.core.BasePropertiesManagerTest;
import com.redhat.microprofile.jdt.core.PropertiesManager;

/**
 * Test collection of Quarkus properties from @ConfigProperties
 * 
 * @author Angelo ZERR
 *
 */
public class QuarkusConfigPropertiesTest extends BasePropertiesManagerTest {

	private static final int EXPECTED_PROPERTIES_FROM_VERBATIM_NAMING_STRATEGY = 2;
	private static final int EXPECTED_PROPERTIES_FROM_GREETING_INTERFACE = 3;
	private static final int EXPECTED_PROPERTIES_FROM_PUBLIC_FIELDS = 2;
	private static final int EXPECTED_PROPERTIES_FROM_GETTER = 3;
	private static final int EXPECTED_PROPERTIES_FROM_NO_PREFIX = 2;
	private static final int EXPECTED_PROPERTIES_FROM_STACK_OVERFLOW = 2;

	private final int EXPECTED_PROPERTIES = EXPECTED_PROPERTIES_FROM_VERBATIM_NAMING_STRATEGY
			+ EXPECTED_PROPERTIES_FROM_GREETING_INTERFACE + EXPECTED_PROPERTIES_FROM_PUBLIC_FIELDS
			+ EXPECTED_PROPERTIES_FROM_GETTER + EXPECTED_PROPERTIES_FROM_NO_PREFIX
			+ EXPECTED_PROPERTIES_FROM_STACK_OVERFLOW;

	@Test
	public void configPropertiesNoDefaultNamingStrategy() throws Exception {
		IJavaProject javaProject = loadMavenProject(MavenProjectName.config_properties);
		// no quarkus.arc.config-properties-default-naming-strategy
		saveFile(JDTMicroProfileProject.APPLICATION_PROPERTIES_FILE, "", javaProject);

		MicroProfileProjectInfo infoFromJavaSources = PropertiesManager.getInstance().getMicroProfileProjectInfo(
				javaProject, MicroProfilePropertiesScope.SOURCES_AND_DEPENDENCIES, ClasspathKind.SRC,
				JDT_UTILS, DocumentFormat.Markdown, new NullProgressMonitor());

		int nbProperties = 0;

		// Test with class GreetingVerbatimNamingStrategyConfiguration bound with
		// @ConfigProperties
		nbProperties += EXPECTED_PROPERTIES_FROM_VERBATIM_NAMING_STRATEGY;
		assertProperties(infoFromJavaSources,

				// @ConfigProperties(prefix = "greetingVerbatim", namingStrategy =
				// NamingStrategy.VERBATIM)
				// GreetingVerbatimNamingStrategyConfiguration {

				// public String message;
				p(null, "greetingVerbatim.message", "java.lang.String", null, false,
						"org.acme.config.GreetingVerbatimNamingStrategyConfiguration", "message", null, 0, null),

				// public HiddenConfig hiddenConfig;
				p(null, "greetingVerbatim.hiddenConfig.recipients", "java.util.List", null, false,
						"org.acme.config.GreetingVerbatimNamingStrategyConfiguration.HiddenConfig", "recipients", null,
						0, null));

		// Test with interface IGreetingConfiguration bound with @ConfigProperties
		nbProperties += EXPECTED_PROPERTIES_FROM_GREETING_INTERFACE;
		assertProperties(infoFromJavaSources,

				// @ConfigProperties(prefix = "greetingInterface")
				// IGreetingConfiguration
				// @ConfigProperty(name = "message")
				// String message();
				p(null, "greetingInterface.message", "java.lang.String", null, false,
						"org.acme.config.IGreetingConfiguration", null, "message()QString;", 0, null),

				// @ConfigProperty(defaultValue="!")
				// String getSuffix();
				p(null, "greetingInterface.suffix", "java.lang.String", null, false,
						"org.acme.config.IGreetingConfiguration", null, "getSuffix()QString;", 0, "!"),

				// Optional<String> getName();
				p(null, "greetingInterface.name", "java.util.Optional", null, false,
						"org.acme.config.IGreetingConfiguration", null, "getName()QOptional<QString;>;", 0, null));

		// Test with class GreetingPublicFieldsConfiguration bound with
		// @ConfigProperties
		nbProperties += EXPECTED_PROPERTIES_FROM_PUBLIC_FIELDS;
		assertProperties(infoFromJavaSources,

				// @ConfigProperties(prefix = "greetingPublicFields")
				// GreetingPublicFieldsConfiguration {

				// public String message;
				p(null, "greetingPublicFields.message", "java.lang.String", null, false,
						"org.acme.config.GreetingPublicFieldsConfiguration", "message", null, 0, null),

				// public HiddenConfig hiddenConfig;
				p(null, "greetingPublicFields.hidden-config.recipients", "java.util.List", null, false,
						"org.acme.config.GreetingPublicFieldsConfiguration.HiddenConfig", "recipients", null, 0, null));

		// Test with class GreetingGetterConfiguration bound with
		// @ConfigProperties
		nbProperties += EXPECTED_PROPERTIES_FROM_GETTER;
		assertProperties(infoFromJavaSources,

				// @ConfigProperties(prefix = "greetingGetter")
				// GreetingGetterConfiguration {

				// private String message;
				p(null, "greetingGetter.message", "java.lang.String", null, false,
						"org.acme.config.GreetingGetterConfiguration", "message", null, 0, null),

				// private String suffix;
				p(null, "greetingGetter.suffix", "java.lang.String", null, false,
						"org.acme.config.GreetingGetterConfiguration", "suffix", null, 0, null),

				// public Optional<String> getName();
				p(null, "greetingGetter.name", "java.util.Optional", null, false,
						"org.acme.config.GreetingGetterConfiguration", "name", null, 0, null));

		// Test with class GreetingNoPrefixConfiguration bound with
		// @ConfigProperties
		nbProperties += EXPECTED_PROPERTIES_FROM_NO_PREFIX;
		assertProperties(infoFromJavaSources,

				// @ConfigProperties
				// GreetingNoPrefixConfiguration {

				// public String message;
				p(null, "greeting-no-prefix.message", "java.lang.String", null, false,
						"org.acme.config.GreetingNoPrefixConfiguration", "message", null, 0, null),

				// public HiddenConfig hidden;
				p(null, "greeting-no-prefix.hidden.recipients", "java.util.List", null, false,
						"org.acme.config.GreetingNoPrefixConfiguration.HiddenConfig", "recipients", null, 0, null));

		// Test with class GreetingStackOverflowConfiguration bound with
		// @ConfigProperties
		nbProperties += EXPECTED_PROPERTIES_FROM_STACK_OVERFLOW;
		assertProperties(infoFromJavaSources,

				// @ConfigProperties(prefix = "greetingStackOverflow")
				// GreetingStackOverflowConfiguration

				// public String message;
				p(null, "greetingStackOverflow.message", "java.lang.String", null, false,
						"org.acme.config.GreetingStackOverflowConfiguration", "message", null, 0, null),

				// public HiddenConfig hidden;
				p(null, "greetingStackOverflow.hidden.recipients", "java.util.List", null, false,
						"org.acme.config.GreetingStackOverflowConfiguration.HiddenConfig", "recipients", null, 0,
						null));

		assertPropertiesDuplicate(infoFromJavaSources);
		Assert.assertEquals("Expected Quarkus properties count", EXPECTED_PROPERTIES, nbProperties);
	}

	@Test
	public void configPropertiesVerbatimDefaultNamingStrategy() throws Exception {
		IJavaProject javaProject = loadMavenProject(MavenProjectName.config_properties);
		// quarkus.arc.config-properties-default-naming-strategy = verbatim
		saveFile(JDTMicroProfileProject.APPLICATION_PROPERTIES_FILE,
				"quarkus.arc.config-properties-default-naming-strategy = verbatim", javaProject);

		MicroProfileProjectInfo infoFromJavaSources = PropertiesManager.getInstance().getMicroProfileProjectInfo(
				javaProject, MicroProfilePropertiesScope.SOURCES_AND_DEPENDENCIES, ClasspathKind.SRC,
				JDT_UTILS, DocumentFormat.Markdown, new NullProgressMonitor());

		int nbProperties = 0;

		// Test with class GreetingVerbatimNamingStrategyConfiguration bound with
		// @ConfigProperties
		nbProperties += EXPECTED_PROPERTIES_FROM_VERBATIM_NAMING_STRATEGY;
		assertProperties(infoFromJavaSources,

				// @ConfigProperties(prefix = "greetingVerbatim", namingStrategy =
				// NamingStrategy.VERBATIM)
				// GreetingVerbatimNamingStrategyConfiguration {

				// public String message;
				p(null, "greetingVerbatim.message", "java.lang.String", null, false,
						"org.acme.config.GreetingVerbatimNamingStrategyConfiguration", "message", null, 0, null),

				// public HiddenConfig hiddenConfig;
				p(null, "greetingVerbatim.hiddenConfig.recipients", "java.util.List", null, false,
						"org.acme.config.GreetingVerbatimNamingStrategyConfiguration.HiddenConfig", "recipients", null,
						0, null));

		// Test with interface IGreetingConfiguration bound with @ConfigProperties
		nbProperties += EXPECTED_PROPERTIES_FROM_GREETING_INTERFACE;
		assertProperties(infoFromJavaSources,

				// @ConfigProperties(prefix = "greetingInterface")
				// IGreetingConfiguration
				// @ConfigProperty(name = "message")
				// String message();
				p(null, "greetingInterface.message", "java.lang.String", null, false,
						"org.acme.config.IGreetingConfiguration", null, "message()QString;", 0, null),

				// @ConfigProperty(defaultValue="!")
				// String getSuffix();
				p(null, "greetingInterface.suffix", "java.lang.String", null, false,
						"org.acme.config.IGreetingConfiguration", null, "getSuffix()QString;", 0, "!"),

				// Optional<String> getName();
				p(null, "greetingInterface.name", "java.util.Optional", null, false,
						"org.acme.config.IGreetingConfiguration", null, "getName()QOptional<QString;>;", 0, null));

		// Test with class GreetingPublicFieldsConfiguration bound with
		// @ConfigProperties
		nbProperties += EXPECTED_PROPERTIES_FROM_PUBLIC_FIELDS;
		assertProperties(infoFromJavaSources,

				// @ConfigProperties(prefix = "greetingPublicFields")
				// GreetingPublicFieldsConfiguration {

				// public String message;
				p(null, "greetingPublicFields.message", "java.lang.String", null, false,
						"org.acme.config.GreetingPublicFieldsConfiguration", "message", null, 0, null),

				// public HiddenConfig hiddenConfig;
				p(null, "greetingPublicFields.hiddenConfig.recipients", "java.util.List", null, false,
						"org.acme.config.GreetingPublicFieldsConfiguration.HiddenConfig", "recipients", null, 0, null));

		// Test with class GreetingGetterConfiguration bound with
		// @ConfigProperties
		nbProperties += EXPECTED_PROPERTIES_FROM_GETTER;
		assertProperties(infoFromJavaSources,

				// @ConfigProperties(prefix = "greetingGetter")
				// GreetingGetterConfiguration {

				// private String message;
				p(null, "greetingGetter.message", "java.lang.String", null, false,
						"org.acme.config.GreetingGetterConfiguration", "message", null, 0, null),

				// private String suffix;
				p(null, "greetingGetter.suffix", "java.lang.String", null, false,
						"org.acme.config.GreetingGetterConfiguration", "suffix", null, 0, null),

				// public Optional<String> getName();
				p(null, "greetingGetter.name", "java.util.Optional", null, false,
						"org.acme.config.GreetingGetterConfiguration", "name", null, 0, null));

		// Test with class GreetingNoPrefixConfiguration bound with
		// @ConfigProperties
		nbProperties += EXPECTED_PROPERTIES_FROM_NO_PREFIX;
		assertProperties(infoFromJavaSources,

				// @ConfigProperties
				// GreetingNoPrefixConfiguration {

				// public String message;
				p(null, "greeting-no-prefix.message", "java.lang.String", null, false,
						"org.acme.config.GreetingNoPrefixConfiguration", "message", null, 0, null),

				// public HiddenConfig hidden;
				p(null, "greeting-no-prefix.hidden.recipients", "java.util.List", null, false,
						"org.acme.config.GreetingNoPrefixConfiguration.HiddenConfig", "recipients", null, 0, null));

		// Test with class GreetingStackOverflowConfiguration bound with
		// @ConfigProperties
		nbProperties += EXPECTED_PROPERTIES_FROM_STACK_OVERFLOW;
		assertProperties(infoFromJavaSources,

				// @ConfigProperties(prefix = "greetingStackOverflow")
				// GreetingStackOverflowConfiguration

				// public String message;
				p(null, "greetingStackOverflow.message", "java.lang.String", null, false,
						"org.acme.config.GreetingStackOverflowConfiguration", "message", null, 0, null),

				// public HiddenConfig hidden;
				p(null, "greetingStackOverflow.hidden.recipients", "java.util.List", null, false,
						"org.acme.config.GreetingStackOverflowConfiguration.HiddenConfig", "recipients", null, 0,
						null));

		assertPropertiesDuplicate(infoFromJavaSources);
		Assert.assertEquals("Expected Quarkus properties count", EXPECTED_PROPERTIES, nbProperties);
	}

}
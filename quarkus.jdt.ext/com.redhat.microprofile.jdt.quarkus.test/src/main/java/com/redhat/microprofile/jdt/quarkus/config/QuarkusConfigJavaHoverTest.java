/*******************************************************************************
* Copyright (c) 2021 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.microprofile.jdt.quarkus.config;

import static org.eclipse.lsp4mp.jdt.core.MicroProfileForJavaAssert.assertJavaHover;
import static org.eclipse.lsp4mp.jdt.core.MicroProfileForJavaAssert.fixURI;
import static org.eclipse.lsp4mp.jdt.core.MicroProfileForJavaAssert.h;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4mp.jdt.core.BasePropertiesManagerTest;
import org.eclipse.lsp4mp.jdt.internal.core.providers.MicroProfileConfigSourceProvider;
import org.junit.After;
import org.junit.Test;

import com.redhat.microprofile.jdt.internal.quarkus.providers.QuarkusConfigSourceProvider;

public class QuarkusConfigJavaHoverTest extends BasePropertiesManagerTest {

	private static IJavaProject javaProject;

	@After
	public void cleanup() throws Exception {
		deleteFile(QuarkusConfigSourceProvider.APPLICATION_YAML_FILE, javaProject);
		deleteFile(QuarkusConfigSourceProvider.APPLICATION_YML_FILE, javaProject);
		deleteFile(QuarkusConfigSourceProvider.APPLICATION_PROPERTIES_FILE, javaProject);
		deleteFile("application-foo.properties", javaProject);
	}

	@Test
	public void configPropertyNameRespectsPrecendence() throws Exception {

		javaProject = loadMavenProject(MicroProfileMavenProjectName.config_quickstart);
		IProject project = javaProject.getProject();
		IFile javaFile = project.getFile(new Path("src/main/java/org/acme/config/GreetingConstructorResource.java"));
		String javaFileUri = fixURI(javaFile.getLocation().toFile().toURI());
		IFile propertiesFile = project.getFile(new Path("src/main/resources/application.properties"));
		String propertiesFileUri = fixURI(propertiesFile.getLocation().toFile().toURI());

		// microprofile-config.properties exists
		saveFile(MicroProfileConfigSourceProvider.MICROPROFILE_CONFIG_PROPERTIES_FILE,
				"greeting.constructor.message = hello 1", javaProject);
		assertJavaHover(new Position(23, 48), javaFileUri, JDT_UTILS,
				h("`greeting.constructor.message = hello 1` *in* META-INF/microprofile-config.properties", 23, 36, 64));

		// microprofile-config.properties and application.properties exist
		saveFile(QuarkusConfigSourceProvider.APPLICATION_PROPERTIES_FILE, "greeting.constructor.message = hello 2",
				javaProject);
		assertJavaHover(new Position(23, 48), javaFileUri, JDT_UTILS,
				h("`greeting.constructor.message = hello 2` *in* [application.properties](" + propertiesFileUri + ")",
						23, 36, 64));

		// microprofile-config.properties, application.properties, and application.yaml
		// exist
		saveFile(QuarkusConfigSourceProvider.APPLICATION_YAML_FILE, //
				"greeting:\n" + //
						"  constructor:\n" + //
						"    message: hello 3", //
				javaProject);
		assertJavaHover(new Position(23, 48), javaFileUri, JDT_UTILS,
				h("`greeting.constructor.message = hello 3` *in* application.yaml", 23, 36, 64));

	}

	@Test
	public void configPropertyNameYaml() throws Exception {

		javaProject = loadMavenProject(MicroProfileMavenProjectName.config_hover);
		IProject project = javaProject.getProject();
		IFile javaFile = project.getFile(new Path("src/main/java/org/acme/config/GreetingResource.java"));
		String javaFileUri = fixURI(javaFile.getLocation().toFile().toURI());
		IFile yamlFile = project.getFile(new Path("src/main/resources/application.yaml"));
		String yamlFileUri = fixURI(yamlFile.getLocation().toFile().toURI());
		IFile propertiesFile = project.getFile(new Path("src/main/resources/application.properties"));
		String propertiesFileUri = fixURI(propertiesFile.getLocation().toFile().toURI());

		saveFile(QuarkusConfigSourceProvider.APPLICATION_YAML_FILE, //
				"greeting:\n" + //
						"  message: message from yaml\n" + //
						"  number: 2001",
				javaProject);

		saveFile(QuarkusConfigSourceProvider.APPLICATION_PROPERTIES_FILE, //
				"greeting.message = hello\r\n" + //
						"greeting.name = quarkus\r\n" + //
						"greeting.number = 100",
				javaProject);

		// Position(14, 40) is the character after the | symbol:
		// @ConfigProperty(name = "greeting.mes|sage")
		assertJavaHover(new Position(14, 40), javaFileUri, JDT_UTILS,
				h("`greeting.message = message from yaml` *in* [application.yaml](" + yamlFileUri + ")", 14, 28, 44));

		// Position(26, 33) is the character after the | symbol:
		// @ConfigProperty(name = "greet|ing.number", defaultValue="0")
		assertJavaHover(new Position(26, 33), javaFileUri, JDT_UTILS,
				h("`greeting.number = 2001` *in* [application.yaml](" + yamlFileUri + ")", 26, 28, 43));

		saveFile(QuarkusConfigSourceProvider.APPLICATION_YAML_FILE, //
				"greeting:\n" + //
						"  message: message from yaml",
				javaProject);
		// fallback to application.properties
		assertJavaHover(new Position(26, 33), javaFileUri, JDT_UTILS,
				h("`greeting.number = 100` *in* [application.properties](" + propertiesFileUri + ")", 26, 28, 43));
	}

	@Test
	public void configPropertyNameYml() throws Exception {

		javaProject = loadMavenProject(MicroProfileMavenProjectName.config_hover);
		IProject project = javaProject.getProject();
		IFile javaFile = project.getFile(new Path("src/main/java/org/acme/config/GreetingResource.java"));
		String javaFileUri = fixURI(javaFile.getLocation().toFile().toURI());
		IFile ymlFile = project.getFile(new Path("src/main/resources/application.yml"));
		String ymlFileUri = fixURI(ymlFile.getLocation().toFile().toURI());
		IFile propertiesFile = project.getFile(new Path("src/main/resources/application.properties"));
		String propertiesFileUri = fixURI(propertiesFile.getLocation().toFile().toURI());

		saveFile(QuarkusConfigSourceProvider.APPLICATION_YML_FILE, //
				"greeting:\n" + //
						"  message: message from yml\n" + //
						"  number: 2001",
				javaProject);

		saveFile(QuarkusConfigSourceProvider.APPLICATION_PROPERTIES_FILE, //
				"greeting.message = hello\r\n" + //
						"greeting.name = quarkus\r\n" + //
						"greeting.number = 100",
				javaProject);

		// Position(14, 40) is the character after the | symbol:
		// @ConfigProperty(name = "greeting.mes|sage")
		assertJavaHover(new Position(14, 40), javaFileUri, JDT_UTILS,
				h("`greeting.message = message from yml` *in* [application.yml](" + ymlFileUri + ")", 14, 28, 44));

		// Position(26, 33) is the character after the | symbol:
		// @ConfigProperty(name = "greet|ing.number", defaultValue="0")
		assertJavaHover(new Position(26, 33), javaFileUri, JDT_UTILS,
				h("`greeting.number = 2001` *in* [application.yml](" + ymlFileUri + ")", 26, 28, 43));

		saveFile(QuarkusConfigSourceProvider.APPLICATION_YML_FILE, //
				"greeting:\n" + //
						"  message: message from yml",
				javaProject);
		// fallback to application.properties
		assertJavaHover(new Position(26, 33), javaFileUri, JDT_UTILS,
				h("`greeting.number = 100` *in* [application.properties](" + propertiesFileUri + ")", 26, 28, 43));
	}

	@Test
	public void perProfileConfigPropertyFile() throws Exception {

		javaProject = loadMavenProject(MicroProfileMavenProjectName.config_hover);
		IProject project = javaProject.getProject();
		IFile javaFile = project.getFile(new Path("src/main/java/org/acme/config/GreetingResource.java"));
		String javaFileUri = fixURI(javaFile.getLocation().toFile().toURI());
		IFile propertiesFile = project.getFile(new Path("src/main/resources/application-foo.properties"));
		String propertiesFileUri = fixURI(propertiesFile.getLocation().toFile().toURI());

		saveFile("application-foo.properties", "greeting.message = hello from foo profile\n", javaProject);
		assertJavaHover(new Position(14, 29), javaFileUri, JDT_UTILS,
				h("`%foo.greeting.message = hello from foo profile` *in* [application-foo.properties]("
						+ propertiesFileUri + ")  \n`greeting.message` is not set", 14, 28, 44));

	}

}

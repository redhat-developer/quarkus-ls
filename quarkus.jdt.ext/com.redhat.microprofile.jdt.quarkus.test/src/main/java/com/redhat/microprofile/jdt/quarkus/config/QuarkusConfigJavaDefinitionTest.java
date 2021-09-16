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

import static org.eclipse.lsp4mp.jdt.core.MicroProfileForJavaAssert.assertJavaDefinitions;
import static org.eclipse.lsp4mp.jdt.core.MicroProfileForJavaAssert.def;
import static org.eclipse.lsp4mp.jdt.core.MicroProfileForJavaAssert.fixURI;
import static org.eclipse.lsp4mp.jdt.core.MicroProfileForJavaAssert.p;
import static org.eclipse.lsp4mp.jdt.core.MicroProfileForJavaAssert.r;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.lsp4mp.jdt.core.BasePropertiesManagerTest;
import org.junit.After;
import org.junit.Test;

import com.redhat.microprofile.jdt.internal.quarkus.providers.QuarkusConfigSourceProvider;

public class QuarkusConfigJavaDefinitionTest extends BasePropertiesManagerTest {

	private static IJavaProject javaProject;

	@After
	public void cleanup() throws Exception {
		deleteFile(QuarkusConfigSourceProvider.APPLICATION_YAML_FILE, javaProject);
		deleteFile(QuarkusConfigSourceProvider.APPLICATION_YML_FILE, javaProject);
		deleteFile(QuarkusConfigSourceProvider.APPLICATION_PROPERTIES_FILE, javaProject);
	}

	@Test
	public void configPropertyNameDefinitionYml() throws Exception {

		javaProject = loadMavenProject(MicroProfileMavenProjectName.config_hover);
		IProject project = javaProject.getProject();
		IFile javaFile = project.getFile(new Path("src/main/java/org/acme/config/GreetingResource.java"));
		String javaFileUri = fixURI(javaFile.getLocation().toFile().toURI());
		IFile applicationYmlFile = project.getFile(new Path("src/main/resources/application.yml"));
		String applicationYmlFileUri = fixURI(applicationYmlFile.getLocation().toFile().toURI());

		saveFile(QuarkusConfigSourceProvider.APPLICATION_YML_FILE, //
				"greeting:\n" + //
						"  message: hello\n" + //
						"  name: quarkus\n" + //
						"  number: 100\n",
				javaProject);
		// Position(14, 40) is the character after the | symbol:
		// @ConfigProperty(name = "greeting.mes|sage")
		assertJavaDefinitions(p(14, 40), javaFileUri, JDT_UTILS, //
				def(r(14, 28, 44), applicationYmlFileUri, "greeting.message"));

	}

}

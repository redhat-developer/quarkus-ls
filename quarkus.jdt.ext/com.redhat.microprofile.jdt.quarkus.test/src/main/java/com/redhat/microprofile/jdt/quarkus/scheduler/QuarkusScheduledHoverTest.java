/*******************************************************************************
* Copyright (c) 2021 Red Hat Inc. and others.
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License v. 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
* which is available at https://www.apache.org/licenses/LICENSE-2.0.
*
* SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.microprofile.jdt.quarkus.scheduler;

import static org.eclipse.lsp4mp.jdt.core.MicroProfileForJavaAssert.assertJavaHover;
import static org.eclipse.lsp4mp.jdt.core.MicroProfileForJavaAssert.fixURI;
import static org.eclipse.lsp4mp.jdt.core.MicroProfileForJavaAssert.h;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4mp.jdt.core.BasePropertiesManagerTest;
import org.junit.Test;

import com.redhat.microprofile.jdt.internal.quarkus.providers.QuarkusConfigSourceProvider;
import com.redhat.microprofile.jdt.quarkus.QuarkusMavenProjectName;

/**
 * Quarkus Scheduled annotation property test for hover in Java file.
 */
public class QuarkusScheduledHoverTest extends BasePropertiesManagerTest {

	private static IJavaProject javaProject;

	@Test
	public void configFirstPropertyNameHover() throws Exception {

		javaProject = loadMavenProject(QuarkusMavenProjectName.scheduler_quickstart);
		IProject project = javaProject.getProject();
		IFile javaFile = project.getFile(new Path("src/main/java/org/acme/scheduler/CounterBean.java"));
		String javaFileUri = fixURI(javaFile.getLocation().toFile().toURI());
		IFile propertiesFile = project.getFile(new Path("src/main/resources/application.properties"));
		String propertiesFileUri = fixURI(propertiesFile.getLocation().toFile().toURI());

		saveFile(QuarkusConfigSourceProvider.APPLICATION_PROPERTIES_FILE, "cron.expr=*/5 * * * * ?\r\n", javaProject);

		// Position(29, 25) is the character after the | symbol:
		// @Scheduled(cron = "{c|ron.expr}", every = "{every.expr}")
		assertJavaHover(new Position(29, 25), javaFileUri, JDT_UTILS,
				h("`cron.expr = */5 * * * * ?` *in* [application.properties](" + propertiesFileUri + ")", 29, 23, 34));
	}

	@Test
	public void configSecondPropertyNameHover() throws Exception {

		javaProject = loadMavenProject(QuarkusMavenProjectName.scheduler_quickstart);
		IProject project = javaProject.getProject();
		IFile javaFile = project.getFile(new Path("src/main/java/org/acme/scheduler/CounterBean.java"));
		String javaFileUri = fixURI(javaFile.getLocation().toFile().toURI());
		IFile propertiesFile = project.getFile(new Path("src/main/resources/application.properties"));
		String propertiesFileUri = fixURI(propertiesFile.getLocation().toFile().toURI());

		saveFile(QuarkusConfigSourceProvider.APPLICATION_PROPERTIES_FILE, "every.expr=*/5 * * * * ?\r\n", javaProject);

		// Position(29, 48) is the character after the | symbol:
		// @Scheduled(cron = "{cron.expr}", every = "{e|very.expr}")
		assertJavaHover(new Position(29, 48), javaFileUri, JDT_UTILS,
				h("`every.expr = */5 * * * * ?` *in* [application.properties](" + propertiesFileUri + ")", 29, 46, 58));
	}

}

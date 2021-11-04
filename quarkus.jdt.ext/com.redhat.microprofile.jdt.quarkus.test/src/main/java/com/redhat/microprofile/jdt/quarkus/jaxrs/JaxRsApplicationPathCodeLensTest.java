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
package com.redhat.microprofile.jdt.quarkus.jaxrs;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4mp.commons.MicroProfileJavaCodeLensParams;
import org.eclipse.lsp4mp.jdt.core.BasePropertiesManagerTest;
import org.eclipse.lsp4mp.jdt.core.PropertiesManagerForJava;
import org.eclipse.lsp4mp.jdt.core.utils.IJDTUtils;
import org.junit.Assert;
import org.junit.Test;

import com.redhat.microprofile.jdt.internal.quarkus.providers.QuarkusConfigSourceProvider;

/**
 * JAX-RS URL Codelens test for Java file with quarkus.http.root-path
 * and @ApplicationPath annotation.
 *
 */
public class JaxRsApplicationPathCodeLensTest extends BasePropertiesManagerTest {

	@Test
	public void urlCodeLensProperties() throws Exception {
		IJavaProject javaProject = loadMavenProject(MicroProfileMavenProjectName.microprofile_applicationpath);
		IJDTUtils utils = JDT_UTILS;

		MicroProfileJavaCodeLensParams params = new MicroProfileJavaCodeLensParams();
		params.setCheckServerAvailable(false);
		IFile javaFile = javaProject.getProject()
				.getFile(new Path("src/main/java/org/acme/ApplicationPathResource.java"));
		params.setUri(javaFile.getLocation().toFile().toURI().toString());
		params.setUrlCodeLensEnabled(true);

		saveFile(QuarkusConfigSourceProvider.APPLICATION_PROPERTIES_FILE, "quarkus.http.root-path=/root", javaProject);

		// Default port
		assertCodeLense(8080, params, utils, "/root/api/path");

		saveFile(QuarkusConfigSourceProvider.APPLICATION_PROPERTIES_FILE, "", javaProject);

		assertCodeLense(8080, params, utils, "/api/path");
	}

	private static void assertCodeLense(int port, MicroProfileJavaCodeLensParams params, IJDTUtils utils,
			String testPath) throws JavaModelException {
		List<? extends CodeLens> lenses = PropertiesManagerForJava.getInstance().codeLens(params, utils,
				new NullProgressMonitor());

		CodeLens lenseForEndpoint = lenses.get(0);
		Assert.assertNotNull(lenseForEndpoint.getCommand());
		Assert.assertEquals("http://localhost:" + port + testPath, lenseForEndpoint.getCommand().getTitle());
	}

}

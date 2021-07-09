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

import java.io.IOException;
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

public class JavaCodeLensQuarkusRestClientTest extends BasePropertiesManagerTest {

	@Test
	public void urlCodeLensYaml() throws Exception {
		IJavaProject javaProject = loadMavenProject(MicroProfileMavenProjectName.rest_client_quickstart);
		IJDTUtils utils = JDT_UTILS;

		// Initialize file
		initConfigFile(javaProject);

		MicroProfileJavaCodeLensParams params = new MicroProfileJavaCodeLensParams();
		IFile javaFile = javaProject.getProject()
				.getFile(new Path("src/main/java/org/acme/restclient/CountriesService.java"));
		params.setUri(javaFile.getLocation().toFile().toURI().toString());
		params.setUrlCodeLensEnabled(true);

		// No configuration of base url
		List<? extends CodeLens> lenses = PropertiesManagerForJava.getInstance().codeLens(params, utils,
				new NullProgressMonitor());
		Assert.assertEquals(0, lenses.size());

		// /mp-rest/url
		saveFile(QuarkusConfigSourceProvider.APPLICATION_YAML_FILE, "org:\r\n" + //
				"  acme:\r\n" + //
				"    restclient:\r\n" + //
				"      CountriesService/mp-rest/url: https://restcountries.url/rest", javaProject);
		assertCodeLenses("https://restcountries.url/rest", params, utils);

		// /mp-rest/uri
		saveFile(QuarkusConfigSourceProvider.APPLICATION_YAML_FILE, //
				"org:\r\n" + //
						"  acme:\r\n" + //
						"    restclient:\r\n" + //
						"      CountriesService/mp-rest/url: https://restcountries.url/rest\r\n" + //
						"      CountriesService/mp-rest/uri: https://restcountries.uri/rest\r\n" + //
						"", //
				javaProject);
		assertCodeLenses("https://restcountries.uri/rest", params, utils);
	}

	private static void initConfigFile(IJavaProject javaProject) throws JavaModelException, IOException {
		saveFile(QuarkusConfigSourceProvider.APPLICATION_YAML_FILE, "", javaProject);
		saveFile(QuarkusConfigSourceProvider.APPLICATION_PROPERTIES_FILE, "", javaProject);
	}

	private static void assertCodeLenses(String baseURL, MicroProfileJavaCodeLensParams params, IJDTUtils utils)
			throws JavaModelException {
		List<? extends CodeLens> lenses = PropertiesManagerForJava.getInstance().codeLens(params, utils,
				new NullProgressMonitor());
		Assert.assertEquals(2, lenses.size());

		// @GET
		// @Path("/name/{name}")
		// Set<Country> getByName(@PathParam String name);
		CodeLens lensForGet = lenses.get(0);
		Assert.assertNotNull(lensForGet.getCommand());
		Assert.assertEquals(baseURL + "/v2/name/{name}", lensForGet.getCommand().getTitle());

		// @GET
		// @Path("/name/{name}")
		// CompletionStage<Set<Country>> getByNameAsync(@PathParam String name);
		CodeLens lensForGetSingle = lenses.get(1);
		Assert.assertNotNull(lensForGetSingle.getCommand());
		Assert.assertEquals(baseURL + "/v2/name/{name}", lensForGetSingle.getCommand().getTitle());
	}

}

/*******************************************************************************
* Copyright (c) 2020 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.microprofile.jdt.core.jaxrs;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.lsp4j.CodeLens;
import org.junit.Assert;
import org.junit.Test;

import com.redhat.microprofile.commons.MicroProfileJavaCodeLensParams;
import com.redhat.microprofile.jdt.core.BasePropertiesManagerTest;
import com.redhat.microprofile.jdt.core.PropertiesManagerForJava;
import com.redhat.microprofile.jdt.core.project.JDTMicroProfileProject;
import com.redhat.microprofile.jdt.core.utils.IJDTUtils;

/**
 * JAX-RS URL Codelens test for Java file.
 * 
 * @author Angelo ZERR
 *
 */
public class JaxRsCodeLensTest extends BasePropertiesManagerTest {

	@Test
	public void urlCodeLensProperties() throws Exception {
		IJavaProject javaProject = loadMavenProject(MavenProjectName.hibernate_orm_resteasy);
		IJDTUtils utils = JDT_UTILS;

		MicroProfileJavaCodeLensParams params = new MicroProfileJavaCodeLensParams();
		params.setCheckServerAvailable(false);
		IFile javaFile = javaProject.getProject()
				.getFile(new Path("src/main/java/org/acme/hibernate/orm/FruitResource.java"));
		params.setUri(javaFile.getLocation().toFile().toURI().toString());
		params.setUrlCodeLensEnabled(true);

		// Default port
		assertCodeLenses(8080, params, utils);
	}

	@Test
	public void urlCodeLensYaml() throws Exception {
		IJavaProject javaProject = loadMavenProject(MavenProjectName.hibernate_orm_resteasy_yaml);
		IJDTUtils utils = JDT_UTILS;

		MicroProfileJavaCodeLensParams params = new MicroProfileJavaCodeLensParams();
		params.setCheckServerAvailable(false);
		IFile javaFile = javaProject.getProject()
				.getFile(new Path("src/main/java/org/acme/hibernate/orm/FruitResource.java"));
		params.setUri(javaFile.getLocation().toFile().toURI().toString());
		params.setUrlCodeLensEnabled(true);

		// Default port
		assertCodeLenses(8080, params, utils);
	}

	private static void assertCodeLenses(int port, MicroProfileJavaCodeLensParams params, IJDTUtils utils)
			throws JavaModelException {
		List<? extends CodeLens> lenses = PropertiesManagerForJava.getInstance().codeLens(params, utils,
				new NullProgressMonitor());
		Assert.assertEquals(2, lenses.size());

		// @GET
		// public Fruit[] get() {
		CodeLens lensForGet = lenses.get(0);
		Assert.assertNotNull(lensForGet.getCommand());
		Assert.assertEquals("http://localhost:" + port + "/fruits", lensForGet.getCommand().getTitle());

		// @GET
		// @Path("{id}")
		// public Fruit getSingle(@PathParam Integer id) {
		CodeLens lensForGetSingle = lenses.get(1);
		Assert.assertNotNull(lensForGetSingle.getCommand());
		Assert.assertEquals("http://localhost:" + port + "/fruits/{id}", lensForGetSingle.getCommand().getTitle());
	}

}

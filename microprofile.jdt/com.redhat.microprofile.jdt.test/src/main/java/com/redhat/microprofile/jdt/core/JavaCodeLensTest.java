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

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.lsp4j.CodeLens;
import org.junit.Assert;
import org.junit.Test;

import com.redhat.microprofile.commons.MicroProfileJavaCodeLensParams;
import com.redhat.microprofile.jdt.core.utils.IJDTUtils;
import com.redhat.microprofile.jdt.internal.core.ls.JDTUtilsLSImpl;
import com.redhat.microprofile.jdt.internal.core.project.JDTMicroProfileProject;

/**
 * JDT Quarkus manager test for Java file.
 * 
 * @author Angelo ZERR
 *
 */
public class JavaCodeLensTest extends BasePropertiesManagerTest {

	@Test
	public void urlCodeLens() throws Exception {
		IJavaProject javaProject = loadMavenProject(MavenProjectName.hibernate_orm_resteasy);
		IJDTUtils utils = JDTUtilsLSImpl.getInstance();

		MicroProfileJavaCodeLensParams params = new MicroProfileJavaCodeLensParams();
		params.setCheckServerAvailable(false);
		IFile javaFile = javaProject.getProject()
				.getFile(new Path("src/main/java/org/acme/hibernate/orm/FruitResource.java"));
		params.setUri(javaFile.getLocation().toFile().toURI().toString());
		params.setUrlCodeLensEnabled(true);

		// Default port
		assertCodeLenses(8080, params, utils);

		// META-INF/microprofile-config.properties : 8081
		saveFile(JDTMicroProfileProject.MICROPROFILE_CONFIG_PROPERTIES_FILE, "quarkus.http.port = 8081", javaProject);
		assertCodeLenses(8081, params, utils);

		// application.properties : 8082 -> it overrides 8081 coming from the
		// META-INF/microprofile-config.properties
		saveFile(JDTMicroProfileProject.APPLICATION_PROPERTIES_FILE, "quarkus.http.port = 8082", javaProject);
		assertCodeLenses(8082, params, utils);

		// application.properties : 8083
		// META-INF/microprofile-config.properties
		saveFile(JDTMicroProfileProject.APPLICATION_PROPERTIES_FILE, "quarkus.http.port = 8083", javaProject);
		assertCodeLenses(8083, params, utils);

		// remove quarkus.http.port from application.properties
		saveFile(JDTMicroProfileProject.APPLICATION_PROPERTIES_FILE, "", javaProject);
		assertCodeLenses(8081, params, utils); // here port is 8081 coming from META-INF/microprofile-config.properties
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

	private static void saveFile(String configFileName, String content, IJavaProject javaProject)
			throws JavaModelException, IOException {
		IPath output = javaProject.getOutputLocation();
		File file = javaProject.getProject().getLocation().append(output.removeFirstSegments(1)).append(configFileName)
				.toFile();
		updateFile(file, content);
	}

}

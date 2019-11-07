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

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.lsp4j.CodeLens;
import org.junit.Assert;
import org.junit.Test;

import com.redhat.quarkus.commons.QuarkusJavaCodeLensParams;
import com.redhat.quarkus.jdt.internal.core.ls.JDTUtilsLSImpl;

/**
 * JDT Quarkus manager test for Java file.
 * 
 * @author Angelo ZERR
 *
 */
public class JDTQuarkusManagerForJavaTest extends BaseJDTQuarkusManagerTest {

	@Test
	public void urlCodeLens() throws Exception {
		IJavaProject javaProject = loadMavenProject(MavenProjectName.hibernate_orm_resteasy);
		IJDTUtils utils = JDTUtilsLSImpl.getInstance();

		QuarkusJavaCodeLensParams params = new QuarkusJavaCodeLensParams();
		params.setCheckServerAvailable(false);
		IFile javaFile = javaProject.getProject()
				.getFile(new Path("src/main/java/org/acme/hibernate/orm/FruitResource.java"));
		params.setUri(javaFile.getLocation().toFile().toURI().toString());
		params.setUrlCodeLensEnabled(true);

		List<? extends CodeLens> lenses = JDTQuarkusManagerForJava.getInstance().codeLens(params, utils,
				new NullProgressMonitor());
		Assert.assertEquals(2, lenses.size());

		// @GET
		// public Fruit[] get() {
		CodeLens lensForGet = lenses.get(0);
		Assert.assertNotNull(lensForGet.getCommand());
		Assert.assertEquals("http://localhost:8080/fruits", lensForGet.getCommand().getTitle());

		// @GET
		// @Path("{id}")
		// public Fruit getSingle(@PathParam Integer id) {
		CodeLens lensForGetSingle = lenses.get(1);
		Assert.assertNotNull(lensForGetSingle.getCommand());
		Assert.assertEquals("http://localhost:8080/fruits/{id}", lensForGetSingle.getCommand().getTitle());
	}
}

/*******************************************************************************
* Copyright (c) 2023 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.microprofile.jdt.quarkus.renarde;

import static org.eclipse.lsp4mp.jdt.core.MicroProfileForJavaAssert.assertCodeLens;
import static org.eclipse.lsp4mp.jdt.core.MicroProfileForJavaAssert.assertWorkspaceSymbols;
import static org.eclipse.lsp4mp.jdt.core.MicroProfileForJavaAssert.cl;
import static org.eclipse.lsp4mp.jdt.core.MicroProfileForJavaAssert.r;
import static org.eclipse.lsp4mp.jdt.core.MicroProfileForJavaAssert.si;
import static org.junit.Assert.assertNotNull;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.lsp4mp.commons.MicroProfileJavaCodeLensParams;
import org.eclipse.lsp4mp.jdt.core.BasePropertiesManagerTest;
import org.junit.Test;

/**
 * Tests for the CodeLens features introduced by
 * {@link com.redhat.microprofile.jdt.internal.quarkus.renarde.java.RenardeJaxRsInfoProvider}.
 */
public class RenardeJaxRsTest extends BasePropertiesManagerTest {

	private static String quarkus_renarde_todo = "quarkus-renarde-todo";

	@Test
	public void codeLens() throws Exception {
		IJavaProject javaProject = loadMavenProject(quarkus_renarde_todo);

		assertNotNull(javaProject);

		MicroProfileJavaCodeLensParams params = new MicroProfileJavaCodeLensParams();
		params.setCheckServerAvailable(false);
		IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/rest/Application.java"));
		params.setUri(javaFile.getLocation().toFile().toURI().toString());
		params.setUrlCodeLensEnabled(true);

		assertCodeLens(params, JDT_UTILS, //
				cl("http://localhost:8080/", "", r(20, 14, 14)), //
				cl("http://localhost:8080/about", "", r(25, 19, 19)), //
				cl("http://localhost:8080/Application/test", "", r(30, 9, 9)), //
				cl("http://localhost:8080/Application/endpoint", "", r(34, 18, 26)));
	}

	@Test
	public void absolutePathCodeLens() throws Exception {
		IJavaProject javaProject = loadMavenProject(quarkus_renarde_todo);

		assertNotNull(javaProject);

		MicroProfileJavaCodeLensParams params = new MicroProfileJavaCodeLensParams();
		params.setCheckServerAvailable(false);
		IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/rest/Game.java"));
		params.setUri(javaFile.getLocation().toFile().toURI().toString());
		params.setUrlCodeLensEnabled(true);

		assertCodeLens(params, JDT_UTILS, //
				cl("http://localhost:8080/play/id", "", r(9, 16, 16)),
				cl("http://localhost:8080/play/start", "", r(13, 18, 23)));
	}
	
	@Test
	public void workspaceSymbols() throws Exception {
		IJavaProject javaProject = loadMavenProject(quarkus_renarde_todo);

		assertNotNull(javaProject);

		assertWorkspaceSymbols(javaProject, JDT_UTILS, //
				si("@/: GET", r(20, 28, 33)), //
				si("@/Application/endpoint: GET", r(34, 18, 26)), //
				si("@/Application/test: POST", r(30, 16, 20)), //
				si("@/Login/complete: POST", r(174, 20, 28)), //
				si("@/Login/confirm: GET", r(138, 28, 35)), //
				si("@/Login/login: GET", r(57, 28, 33)), //
				si("@/Login/logoutFirst: GET", r(153, 28, 39)), //
				si("@/Login/manualLogin: POST", r(73, 20, 31)), //
				si("@/Login/register: POST", r(118, 28, 36)), //
				si("@/Login/welcome: GET", r(65, 28, 35)), //
				si("@/Todos/add: POST", r(59, 16, 19)), //
				si("@/Todos/delete: POST", r(35, 16, 22)), //
				si("@/Todos/done: POST", r(46, 16, 20)), //
				si("@/Todos/index: GET", r(29, 28, 33)), //
				si("@/about: GET", r(25, 28, 33)),
				si("@/play/id: GET", r(9, 18, 26)),
				si("@/play/start: GET", r(13, 18, 23)));
	}

}

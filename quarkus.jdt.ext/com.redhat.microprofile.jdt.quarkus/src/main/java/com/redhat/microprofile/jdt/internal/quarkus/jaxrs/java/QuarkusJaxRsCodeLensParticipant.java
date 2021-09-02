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
package com.redhat.microprofile.jdt.internal.quarkus.jaxrs.java;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4mp.jdt.core.java.codelens.IJavaCodeLensParticipant;
import org.eclipse.lsp4mp.jdt.core.java.codelens.JavaCodeLensContext;
import org.eclipse.lsp4mp.jdt.core.jaxrs.JaxRsContext;
import org.eclipse.lsp4mp.jdt.core.project.JDTMicroProfileProject;
import org.eclipse.lsp4mp.jdt.core.project.JDTMicroProfileProjectManager;

/**
 *
 * Quarkus JAX-RS CodeLens participant used to update the server port declared
 * with "quarkus.http.port" property.
 *
 * @author Angelo ZERR 
 *
 */
public class QuarkusJaxRsCodeLensParticipant implements IJavaCodeLensParticipant {

	private static final String QUARKUS_DEV_HTTP_PORT = "%dev.quarkus.http.port";
	private static final String QUARKUS_HTTP_PORT = "quarkus.http.port";
	private static final String QUARKUS_DEV_HTTP_ROOT_PATH = "%dev.quarkus.http.root-path";
	private static final String QUARKUS_HTTP_ROOT_PATH = "quarkus.http.root-path";

	@Override
	public void beginCodeLens(JavaCodeLensContext context, IProgressMonitor monitor) throws CoreException {
		// Update the JAX-RS server port from the declared quarkus property
		// "quarkus.http.port"
		IJavaProject javaProject = context.getJavaProject();
		JDTMicroProfileProject mpProject = JDTMicroProfileProjectManager.getInstance()
				.getJDTMicroProfileProject(javaProject);
		
		// Retrieve server port from application.properties
		int serverPort = mpProject.getPropertyAsInteger(QUARKUS_HTTP_PORT, JaxRsContext.DEFAULT_PORT);
		int devServerPort = mpProject.getPropertyAsInteger(QUARKUS_DEV_HTTP_PORT, serverPort);
		JaxRsContext.getJaxRsContext(context).setServerPort(devServerPort);
		
		// Retrieve HTTP root path from application.properties
		String httpRootPath = mpProject.getProperty(QUARKUS_HTTP_ROOT_PATH);
		String devHttpRootPath = mpProject.getProperty(QUARKUS_DEV_HTTP_ROOT_PATH, httpRootPath);
		JaxRsContext.getJaxRsContext(context).setRootPath(devHttpRootPath);
	}

	@Override
	public List<CodeLens> collectCodeLens(JavaCodeLensContext context, IProgressMonitor monitor) throws CoreException {
		// Do nothing
		return null;
	}
}

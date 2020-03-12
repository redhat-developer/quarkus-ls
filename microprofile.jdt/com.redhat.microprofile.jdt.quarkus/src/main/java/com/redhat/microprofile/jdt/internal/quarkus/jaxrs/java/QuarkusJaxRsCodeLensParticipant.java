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

import com.redhat.microprofile.jdt.core.java.codelens.IJavaCodeLensParticipant;
import com.redhat.microprofile.jdt.core.java.codelens.JavaCodeLensContext;
import com.redhat.microprofile.jdt.core.jaxrs.JaxRsContext;
import com.redhat.microprofile.jdt.core.project.JDTMicroProfileProject;
import com.redhat.microprofile.jdt.core.project.JDTMicroProfileProjectManager;

/**
 *
 * Quarkus JAX-RS CodeLens participant used to update the server port declared
 * with "quarkus.http.port" property.
 * 
 * @author Angelo ZERR
 * 
 */
public class QuarkusJaxRsCodeLensParticipant implements IJavaCodeLensParticipant {

	private static final String QUARKUS_HTTP_PORT = "quarkus.http.port";

	@Override
	public void beginCodeLens(JavaCodeLensContext context, IProgressMonitor monitor) throws CoreException {
		// Update the JAX-RS server port from the declared quarkus property
		// "quarkus.http.port"
		IJavaProject javaProject = context.getJavaProject();
		JDTMicroProfileProject mpProject = JDTMicroProfileProjectManager.getInstance()
				.getJDTMicroProfileProject(javaProject);
		int serverPort = mpProject.getPropertyAsInteger(QUARKUS_HTTP_PORT, JaxRsContext.DEFAULT_PORT);
		JaxRsContext.getJaxRsContext(context).setServerPort(serverPort);
	}

	@Override
	public List<CodeLens> collectCodeLens(JavaCodeLensContext context, IProgressMonitor monitor) throws CoreException {
		// Do nothing
		return null;
	}
}

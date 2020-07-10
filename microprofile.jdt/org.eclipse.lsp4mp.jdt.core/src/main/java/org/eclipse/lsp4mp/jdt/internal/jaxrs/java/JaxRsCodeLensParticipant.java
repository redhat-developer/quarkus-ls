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
package org.eclipse.lsp4mp.jdt.internal.jaxrs.java;

import static org.eclipse.lsp4mp.jdt.core.jaxrs.JaxRsUtils.createURLCodeLens;
import static org.eclipse.lsp4mp.jdt.core.jaxrs.JaxRsUtils.getJaxRsPathValue;
import static org.eclipse.lsp4mp.jdt.core.jaxrs.JaxRsUtils.isJaxRsRequestMethod;
import static org.eclipse.lsp4mp.jdt.core.utils.JDTTypeUtils.overlaps;
import static org.eclipse.lsp4mp.jdt.internal.jaxrs.JaxRsConstants.JAVAX_WS_RS_PATH_ANNOTATION;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4mp.commons.MicroProfileJavaCodeLensParams;
import org.eclipse.lsp4mp.jdt.core.java.codelens.IJavaCodeLensParticipant;
import org.eclipse.lsp4mp.jdt.core.java.codelens.JavaCodeLensContext;
import org.eclipse.lsp4mp.jdt.core.jaxrs.JaxRsContext;
import org.eclipse.lsp4mp.jdt.core.utils.IJDTUtils;
import org.eclipse.lsp4mp.jdt.core.utils.JDTTypeUtils;

/**
 *
 * JAX-RS CodeLens participant
 * 
 * @author Angelo ZERR
 * 
 */
public class JaxRsCodeLensParticipant implements IJavaCodeLensParticipant {

	private static final String LOCALHOST = "localhost";

	private static final int PING_TIMEOUT = 2000;

	@Override
	public boolean isAdaptedForCodeLens(JavaCodeLensContext context, IProgressMonitor monitor) throws CoreException {
		MicroProfileJavaCodeLensParams params = context.getParams();
		if (!params.isUrlCodeLensEnabled()) {
			return false;
		}
		// Collection of URL codeLens is done only if JAX-RS is on the classpath
		IJavaProject javaProject = context.getJavaProject();
		return JDTTypeUtils.findType(javaProject, JAVAX_WS_RS_PATH_ANNOTATION) != null;
	}

	@Override
	public List<CodeLens> collectCodeLens(JavaCodeLensContext context, IProgressMonitor monitor) throws CoreException {
		ITypeRoot typeRoot = context.getTypeRoot();
		IJavaElement[] elements = typeRoot.getChildren();
		int serverPort = JaxRsContext.getJaxRsContext(context).getServerPort();
		IJDTUtils utils = context.getUtils();
		MicroProfileJavaCodeLensParams params = context.getParams();
		params.setLocalServerPort(serverPort);
		List<CodeLens> lenses = new ArrayList<>();
		collectURLCodeLenses(elements, null, lenses, params, utils, monitor);
		return lenses;
	}

	private static void collectURLCodeLenses(IJavaElement[] elements, String rootPath, Collection<CodeLens> lenses,
			MicroProfileJavaCodeLensParams params, IJDTUtils utils, IProgressMonitor monitor)
			throws JavaModelException {
		for (IJavaElement element : elements) {
			if (monitor.isCanceled()) {
				return;
			}
			if (element.getElementType() == IJavaElement.TYPE) {
				IType type = (IType) element;
				// Get value of JAX-RS @Path annotation from the class
				String pathValue = getJaxRsPathValue(type);
				if (pathValue != null) {
					// Class is annotated with @Path
					// Display code lens only if local server is available.
					if (!params.isCheckServerAvailable()
							|| isServerAvailable(LOCALHOST, params.getLocalServerPort(), PING_TIMEOUT)) {
						// Loop for each method annotated with @Path to generate URL code lens per
						// method.
						collectURLCodeLenses(type.getChildren(), pathValue, lenses, params, utils, monitor);
					}
				}
				continue;
			} else if (element.getElementType() == IJavaElement.METHOD) {
				if (utils.isHiddenGeneratedElement(element)) {
					continue;
				}
				// ignore element if method range overlaps the type range, happens for generated
				// bytecode, i.e. with lombok
				IJavaElement parentType = element.getAncestor(IJavaElement.TYPE);
				if (parentType != null && overlaps(((ISourceReference) parentType).getNameRange(),
						((ISourceReference) element).getNameRange())) {
					continue;
				}
			} else {// neither a type nor a method, we bail
				continue;
			}

			// Here java element is a method
			if (rootPath != null) {
				IMethod method = (IMethod) element;
				// A JAX-RS method is a public method annotated with @GET @POST, @DELETE, @PUT
				// JAX-RS
				// annotation
				if (isJaxRsRequestMethod(method) && Flags.isPublic(method.getFlags())) {
					String baseURL = params.getLocalBaseURL();
					String openURICommandId = params.getOpenURICommand();
					CodeLens lens = createURLCodeLens(baseURL, rootPath, openURICommandId, (IMethod) element, utils);
					if (lens != null) {
						lenses.add(lens);
					}
				}
			}
		}
	}

	private static boolean isServerAvailable(String host, int port, int timeout) {
		try (Socket socket = new Socket()) {
			socket.connect(new InetSocketAddress(host, port), timeout);
			return true;
		} catch (IOException e) {
			return false;
		}
	}
}

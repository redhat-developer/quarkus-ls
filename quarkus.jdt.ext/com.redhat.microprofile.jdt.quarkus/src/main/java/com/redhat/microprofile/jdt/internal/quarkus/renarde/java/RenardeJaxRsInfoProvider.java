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
package com.redhat.microprofile.jdt.internal.quarkus.renarde.java;

import static org.eclipse.lsp4mp.jdt.core.utils.AnnotationUtils.hasAnnotation;
import static org.eclipse.lsp4mp.jdt.core.utils.JDTTypeUtils.overlaps;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.lsp4mp.jdt.core.jaxrs.HttpMethod;
import org.eclipse.lsp4mp.jdt.core.jaxrs.IJaxRsInfoProvider;
import org.eclipse.lsp4mp.jdt.core.jaxrs.JaxRsConstants;
import org.eclipse.lsp4mp.jdt.core.jaxrs.JaxRsContext;
import org.eclipse.lsp4mp.jdt.core.jaxrs.JaxRsMethodInfo;
import org.eclipse.lsp4mp.jdt.core.jaxrs.JaxRsUtils;
import org.eclipse.lsp4mp.jdt.core.utils.IJDTUtils;

/**
 * Use custom logic for all JAX-RS features in classes that extends Renarde's
 * <code>Controller</code> class.
 */
public class RenardeJaxRsInfoProvider implements IJaxRsInfoProvider {

	private static final Logger LOGGER = Logger.getLogger(RenardeJaxRsInfoProvider.class.getName());

	@Override
	public boolean canProvideJaxRsMethodInfoForClass(ITypeRoot typeRoot, IProgressMonitor monitor) {
		return RenardeUtils.isControllerClass(typeRoot.getJavaProject(), typeRoot, monitor);
	}

	@Override
	public Set<ITypeRoot> getAllJaxRsClasses(IJavaProject javaProject, IProgressMonitor monitor) {
		return RenardeUtils.getAllControllerClasses(javaProject, monitor);
	}

	@Override
	public List<JaxRsMethodInfo> getJaxRsMethodInfo(ITypeRoot typeRoot, JaxRsContext jaxrsContext, IJDTUtils utils,
			IProgressMonitor monitor) {
		try {
			IType type = typeRoot.findPrimaryType();
			String pathSegment = JaxRsUtils.getJaxRsPathValue(type);
			String typeSegment = type.getElementName();

			List<JaxRsMethodInfo> methodInfos = new ArrayList<>();
			for (IMethod method : type.getMethods()) {

				if (utils.isHiddenGeneratedElement(method)) {
					continue;
				}
				// ignore element if method range overlaps the type range,
				// happens for generated
				// bytecode, i.e. with lombok
				if (overlaps(((ISourceReference) type).getNameRange(), ((ISourceReference) method).getNameRange())) {
					continue;
				}

				if (Flags.isPublic(method.getFlags())) {

					String methodSegment = JaxRsUtils.getJaxRsPathValue(method);
					if (methodSegment == null) {
						methodSegment = method.getElementName();
					}
					String path;
					if (pathSegment == null) {
						path = methodSegment.startsWith("/") ? methodSegment : JaxRsUtils.buildURL(typeSegment, methodSegment);
					} else {
						path = JaxRsUtils.buildURL(pathSegment, methodSegment);
					}
					
					String url = JaxRsUtils.buildURL(jaxrsContext.getLocalBaseURL(), path);

					JaxRsMethodInfo methodInfo = createMethodInfo(method, url);
					if (methodInfo != null) {
						methodInfos.add(methodInfo);
					}
				}
			}
			return methodInfos;
		} catch (JavaModelException e) {
			LOGGER.log(Level.SEVERE, "error while collecting JAX-RS methods for Renarde", e);
			return Collections.emptyList();
		}
	}

	private static JaxRsMethodInfo createMethodInfo(IMethod method, String url) throws JavaModelException {

		IResource resource = method.getResource();
		if (resource == null) {
			return null;
		}
		String documentUri = resource.getLocationURI().toString();

		HttpMethod httpMethod = HttpMethod.GET;
		for (String methodAnnotationFQN : JaxRsConstants.HTTP_METHOD_ANNOTATIONS) {
			if (hasAnnotation(method, methodAnnotationFQN)) {
				httpMethod = JaxRsUtils.getHttpMethodForAnnotation(methodAnnotationFQN);
				break;
			}
		}

		return new JaxRsMethodInfo(url, httpMethod, method, documentUri);
	}

}

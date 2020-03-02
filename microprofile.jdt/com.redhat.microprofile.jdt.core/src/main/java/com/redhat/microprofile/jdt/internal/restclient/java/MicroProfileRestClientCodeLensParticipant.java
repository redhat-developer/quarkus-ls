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
package com.redhat.microprofile.jdt.internal.restclient.java;

import static com.redhat.microprofile.jdt.core.jaxrs.JaxRsUtils.createURLCodeLens;
import static com.redhat.microprofile.jdt.core.jaxrs.JaxRsUtils.getJaxRsPathValue;
import static com.redhat.microprofile.jdt.core.jaxrs.JaxRsUtils.isJaxRsRequestMethod;
import static com.redhat.microprofile.jdt.core.utils.AnnotationUtils.getAnnotation;
import static com.redhat.microprofile.jdt.core.utils.AnnotationUtils.getAnnotationMemberValue;
import static com.redhat.microprofile.jdt.core.utils.JDTTypeUtils.overlaps;
import static com.redhat.microprofile.jdt.internal.restclient.MicroProfileRestClientConstants.REGISTER_REST_CLIENT_ANNOTATION;
import static com.redhat.microprofile.jdt.internal.restclient.MicroProfileRestClientConstants.REGISTER_REST_CLIENT_ANNOTATION_BASE_URI;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.lsp4j.CodeLens;

import com.redhat.microprofile.commons.MicroProfileJavaCodeLensParams;
import com.redhat.microprofile.jdt.core.java.IJavaCodeLensParticipant;
import com.redhat.microprofile.jdt.core.java.JavaCodeLensContext;
import com.redhat.microprofile.jdt.core.project.JDTMicroProfileProject;
import com.redhat.microprofile.jdt.core.project.JDTMicroProfileProjectManager;
import com.redhat.microprofile.jdt.core.utils.IJDTUtils;
import com.redhat.microprofile.jdt.core.utils.JDTTypeUtils;

/**
 *
 * MicroProfile RestClient CodeLens participant
 * 
 * @author Angelo ZERR
 * 
 */
public class MicroProfileRestClientCodeLensParticipant implements IJavaCodeLensParticipant {

	@Override
	public boolean isAdaptedForCodeLens(JavaCodeLensContext context, IProgressMonitor monitor) throws CoreException {
		MicroProfileJavaCodeLensParams params = context.getParams();
		if (!params.isUrlCodeLensEnabled()) {
			return false;
		}
		// Collection of URL codeLens is done only if @ResgisterRestClient annotation is
		// on the classpath
		IJavaProject javaProject = context.getJavaProject();
		return JDTTypeUtils.findType(javaProject, REGISTER_REST_CLIENT_ANNOTATION) != null;
	}

	@Override
	public List<CodeLens> collectCodeLens(JavaCodeLensContext context, IProgressMonitor monitor) throws CoreException {
		ITypeRoot typeRoot = context.getTypeRoot();
		IJavaElement[] elements = typeRoot.getChildren();
		IJDTUtils utils = context.getUtils();
		MicroProfileJavaCodeLensParams params = context.getParams();
		List<CodeLens> lenses = new ArrayList<>();
		JDTMicroProfileProject mpProject = JDTMicroProfileProjectManager.getInstance()
				.getJDTMicroProfileProject(context.getJavaProject());
		collectURLCodeLenses(elements, null, null, mpProject, lenses, params, utils, monitor);
		return lenses;
	}

	private static void collectURLCodeLenses(IJavaElement[] elements, String baseURL, String rootPath,
			JDTMicroProfileProject mpProject, Collection<CodeLens> lenses, MicroProfileJavaCodeLensParams params,
			IJDTUtils utils, IProgressMonitor monitor) throws JavaModelException {
		for (IJavaElement element : elements) {
			if (monitor.isCanceled()) {
				return;
			}
			if (element.getElementType() == IJavaElement.TYPE) {
				IType type = (IType) element;
				String url = getBaseURL(type, mpProject);
				if (url != null) {
					// Get value of JAX-RS @Path annotation from the class
					String pathValue = getJaxRsPathValue(type);
					collectURLCodeLenses(type.getChildren(), url, pathValue, mpProject, lenses, params, utils, monitor);
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
			if (baseURL != null) {
				IMethod method = (IMethod) element;
				// A JAX-RS method is a public method annotated with @GET @POST, @DELETE, @PUT
				// JAX-RS
				// annotation
				if (isJaxRsRequestMethod(method)) {
					String openURICommandId = params.getOpenURICommand();
					CodeLens lens = createURLCodeLens(baseURL, rootPath, openURICommandId, (IMethod) element, utils);
					if (lens != null) {
						lenses.add(lens);
					}
				}
			}
		}
	}

	/**
	 * Returns the base URL for the given class type and null otherwise.
	 * 
	 * @param type      the class type.
	 * @param mpProject the MicroProfile project
	 * @return the base URL for the given class type and null otherwise.
	 * @throws JavaModelException
	 */
	private static String getBaseURL(IType type, JDTMicroProfileProject mpProject) throws JavaModelException {
		IAnnotation registerRestClientAnnotation = getAnnotation(type, REGISTER_REST_CLIENT_ANNOTATION);
		if (registerRestClientAnnotation == null) {
			return null;
		}
		// Search base url from the configured property $class/mp-rest/uri
		String baseURIFromConfig = getBaseURIFromConfig(type, mpProject);
		if (baseURIFromConfig != null) {
			return baseURIFromConfig;
		}
		// Search base url from the configured property $class/mp-rest/url
		String baseURLFromConfig = getBaseURLFromConfig(type, mpProject);
		if (baseURLFromConfig != null) {
			return baseURLFromConfig;
		}
		// Search base url from the @RegisterRestClient/baseUri
		String baseURIFromAnnotation = getAnnotationMemberValue(registerRestClientAnnotation,
				REGISTER_REST_CLIENT_ANNOTATION_BASE_URI);
		return baseURIFromAnnotation;
	}

	private static String getBaseURIFromConfig(IType type, JDTMicroProfileProject mpProject) {
		String property = new StringBuilder(type.getFullyQualifiedName()).append("/mp-rest/uri").toString();
		return mpProject.getProperty(property, null);
	}

	private static String getBaseURLFromConfig(IType type, JDTMicroProfileProject mpProject) {
		String property = new StringBuilder(type.getFullyQualifiedName()).append("/mp-rest/url").toString();
		return mpProject.getProperty(property, null);
	}
}

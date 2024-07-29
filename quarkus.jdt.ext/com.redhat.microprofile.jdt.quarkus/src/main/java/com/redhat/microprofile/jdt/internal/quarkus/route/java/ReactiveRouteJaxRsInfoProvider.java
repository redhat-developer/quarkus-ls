/*******************************************************************************
* Copyright (c) 2024 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.microprofile.jdt.internal.quarkus.route.java;

import static com.redhat.microprofile.jdt.internal.quarkus.route.java.ReactiveRouteConstants.ROUTE_FQN;
import static com.redhat.microprofile.jdt.internal.quarkus.route.java.ReactiveRouteUtils.getRouteHttpMethodName;
import static com.redhat.microprofile.jdt.internal.quarkus.route.java.ReactiveRouteUtils.isReactiveRoute;
import static org.eclipse.lsp4mp.jdt.core.utils.JDTTypeUtils.overlaps;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.lsp4mp.jdt.core.jaxrs.HttpMethod;
import org.eclipse.lsp4mp.jdt.core.jaxrs.IJaxRsInfoProvider;
import org.eclipse.lsp4mp.jdt.core.jaxrs.JaxRsContext;
import org.eclipse.lsp4mp.jdt.core.jaxrs.JaxRsMethodInfo;
import org.eclipse.lsp4mp.jdt.core.jaxrs.JaxRsUtils;
import org.eclipse.lsp4mp.jdt.core.utils.IJDTUtils;
import org.eclipse.lsp4mp.jdt.core.utils.JDTTypeUtils;

/**
 * Use custom logic for all JAX-RS features for Reactive @Route.
 *
 * @see <a href=
 *      "https://quarkus.io/guides/reactive-routes#declaring-reactive-routes">https://quarkus.io/guides/reactive-routes#declaring-reactive-routes</a>
 */
public class ReactiveRouteJaxRsInfoProvider implements IJaxRsInfoProvider {

	private static final Logger LOGGER = Logger.getLogger(ReactiveRouteJaxRsInfoProvider.class.getName());

	@Override
	public boolean canProvideJaxRsMethodInfoForClass(ITypeRoot typeRoot, IProgressMonitor monitor) {
		return JDTTypeUtils.findType(typeRoot.getJavaProject(), ROUTE_FQN) != null;
	}

	@Override
	public Set<ITypeRoot> getAllJaxRsClasses(IJavaProject javaProject, IProgressMonitor monitor) {
		// TODO: implement support workspace symbols
		return Collections.emptySet();
	}

	@Override
	public List<JaxRsMethodInfo> getJaxRsMethodInfo(ITypeRoot typeRoot, JaxRsContext jaxrsContext, IJDTUtils utils,
			IProgressMonitor monitor) {
		try {
			IType type = typeRoot.findPrimaryType();
			if (type == null) {
				return Collections.emptyList();
			}
			// See https://quarkus.io/guides/reactive-routes#routebase
			// Try to get the @RouteBase declared in the Java type

			IAnnotation routeBaseAnnotation = ReactiveRouteUtils.getRouteBaseAnnotation(type);
			String pathSegment = routeBaseAnnotation != null ? ReactiveRouteUtils.getRouteBasePath(routeBaseAnnotation)
					: null;

			List<JaxRsMethodInfo> methodInfos = new ArrayList<>();
			for (IMethod method : type.getMethods()) {

				if (method.isConstructor() || utils.isHiddenGeneratedElement(method)) {
					continue;
				}
				// ignore element if method range overlaps the type range,
				// happens for generated
				// bytecode, i.e. with lombok
				if (overlaps(((ISourceReference) type).getNameRange(), ((ISourceReference) method).getNameRange())) {
					continue;
				}

				if (!isReactiveRoute(method)) {
					continue;
				}
				// Here method is annotated with @Route

				// Method can have several @Route
				// @Route(path = "/first")
				// @Route(path = "/second")
				// public void route(RoutingContext rc) {
				// // ...
				List<IAnnotation> routeAnnotations = ReactiveRouteUtils.getRouteAnnotations(method);

				// Loop for @Route annotation
				for (IAnnotation routeAnnotation : routeAnnotations) {
					// @Route(path = "/first")
					String methodSegment = ReactiveRouteUtils.getRoutePath(routeAnnotation);
					if (methodSegment == null) {
						// @Route(methods = Route.HttpMethod.GET)
						// void hello(RoutingContext rc)
						// Here the segment is the method name
						methodSegment = method.getElementName();
					}
					String path;
					if (pathSegment == null) {
						path = methodSegment;
					} else {
						path = JaxRsUtils.buildURL(pathSegment, methodSegment);
					}
					String url = JaxRsUtils.buildURL(jaxrsContext.getLocalBaseURL(), path);

					JaxRsMethodInfo methodInfo = createMethodInfo(method, routeAnnotation, url);
					if (methodInfo != null) {
						methodInfos.add(methodInfo);
					}
				}
			}
			return methodInfos;
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Error while collecting JAX-RS methods for Reactive @Route", e);
			return Collections.emptyList();
		}
	}

	private IType findFirstClass(IType typeRoot) throws JavaModelException {
		for (IJavaElement element : typeRoot.getChildren()) {
			if (element instanceof IType) {
				return (IType) element;
			}
		}
		return null;
	}

	private static JaxRsMethodInfo createMethodInfo(IMethod method, IAnnotation routeAnnotation, String url)
			throws JavaModelException {

		IResource resource = method.getResource();
		if (resource == null) {
			return null;
		}
		String documentUri = resource.getLocationURI().toString();

		String httpMethodName = getRouteHttpMethodName(routeAnnotation);
		HttpMethod httpMethod = ReactiveRouteUtils.getHttpMethodForAnnotation(httpMethodName);
		return new JaxRsMethodInfo(url, httpMethod, method, documentUri);
	}

}
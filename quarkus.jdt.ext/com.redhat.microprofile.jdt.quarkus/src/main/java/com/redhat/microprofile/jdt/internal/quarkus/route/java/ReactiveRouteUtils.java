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

import static com.redhat.microprofile.jdt.internal.quarkus.route.java.ReactiveRouteConstants.ROUTE_BASE_FQN;
import static com.redhat.microprofile.jdt.internal.quarkus.route.java.ReactiveRouteConstants.ROUTE_BASE_PATH;
import static com.redhat.microprofile.jdt.internal.quarkus.route.java.ReactiveRouteConstants.ROUTE_FQN;
import static com.redhat.microprofile.jdt.internal.quarkus.route.java.ReactiveRouteConstants.ROUTE_METHODS;
import static com.redhat.microprofile.jdt.internal.quarkus.route.java.ReactiveRouteConstants.ROUTE_PATH;
import static org.eclipse.lsp4mp.jdt.core.utils.AnnotationUtils.getAnnotationMemberValue;
import static org.eclipse.lsp4mp.jdt.core.utils.AnnotationUtils.getFirstAnnotation;
import static org.eclipse.lsp4mp.jdt.core.utils.AnnotationUtils.isMatchAnnotation;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IAnnotatable;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.lsp4mp.jdt.core.jaxrs.HttpMethod;
import org.eclipse.lsp4mp.jdt.core.utils.AnnotationUtils;

/**
 * Reactive @Route utilities.
 *
 * @see <a href=
 *      "https://quarkus.io/guides/reactive-routes#declaring-reactive-routes">https://quarkus.io/guides/reactive-routes#declaring-reactive-routes</a>
 */
public class ReactiveRouteUtils {

	private ReactiveRouteUtils() {

	}

	/**
	 * Returns true if the given method is a Reactive Route and false otherwise.
	 *
	 * @param method the method to check.
	 * @return true if the given method is a Reactive Route and false otherwise.
	 */
	public static boolean isReactiveRoute(IMethod method) {
		try {
			if (!method.isConstructor() && AnnotationUtils.hasAnyAnnotation(method, ReactiveRouteConstants.ROUTE_FQN)) {
				// The method is annotated with @Route
				// A route method must be a non-private non-static method of a CDI bean.
				// See https://quarkus.io/guides/reactive-routes#reactive-route-methods
				return !Flags.isPrivate(method.getFlags()) && !Flags.isStatic(method.getFlags());
			}
		} catch (JavaModelException e) {
			return false;
		}
		return false;
	}

	/**
	 * Return the @RouteBase annotation from the given annotatable element and null
	 * otherwise.
	 *
	 * @param annotatable the annotatable element.
	 * @return the @RouteBase annotation from the given annotatable element and null
	 *         otherwise.
	 * @throws JavaModelException
	 */
	public static IAnnotation getRouteBaseAnnotation(IAnnotatable annotatable) throws JavaModelException {
		return getFirstAnnotation(annotatable, ROUTE_BASE_FQN);
	}

	/**
	 * Returns the value of @RouteBase path attribute and null otherwise..
	 *
	 * @param routeBaseAnnotation the @Route annotation.
	 * @return the value of @RouteBase path attribute and null otherwise.
	 * @throws JavaModelException
	 */
	public static String getRouteBasePath(IAnnotation routeBaseAnnotation) throws JavaModelException {
		return getAnnotationMemberValue(routeBaseAnnotation, ROUTE_BASE_PATH);
	}

	/**
	 * Return the list of @Route annotation from the given annotatable element.
	 *
	 * @param annotatable the annotatable element.
	 * @return the list of @Route annotation from the given annotatable element.
	 * @throws JavaModelException
	 */
	public static List<IAnnotation> getRouteAnnotations(IAnnotatable annotatable) throws JavaModelException {
		return getAllAnnotations(annotatable, ROUTE_FQN);
	}

	/**
	 * Returns the value of @Route path attribute and null otherwise..
	 *
	 * @param routeAnnotation the @Route annotation.
	 * @return the value of @Route path attribute and null otherwise.
	 * @throws JavaModelException
	 */
	public static String getRoutePath(IAnnotation routeAnnotation) throws JavaModelException {
		return getAnnotationMemberValue(routeAnnotation, ROUTE_PATH);
	}

	public static String getRouteHttpMethodName(IAnnotation routeAnnotation) throws JavaModelException {
		// ex: @Route(methods = Route.HttpMethod.GET) 
		String value = getAnnotationMemberValue(routeAnnotation, ROUTE_METHODS);
		if (value != null) {
			int index = value.lastIndexOf('.');
			if (index != -1) {
				// ex : Route.HttpMethod.GET
				return value.substring(index);
			}
		}
		return value;
	}

	/**
	 * Returns an HttpMethod given the FQN of a Reactive @Route/methods annotation,
	 * nor null if the FQN doesn't match any HttpMethod.
	 *
	 * @param httpMethodName the Http method name of the annotation to convert into
	 *                       a HttpMethod
	 * @return an HttpMethod given the FQN of a Reactive @Route/methods *
	 *         annotation, nor null if the FQN doesn't match any HttpMethod.
	 */
	public static HttpMethod getHttpMethodForAnnotation(String httpMethodName) {
		if (httpMethodName != null) {
			try {
				return HttpMethod.valueOf(httpMethodName);
			} catch (Exception e) {
				// Do nothing
			}
		}
		return HttpMethod.GET;
	}

	public static List<IAnnotation> getAllAnnotations(IAnnotatable annotatable, String... annotationNames)
			throws JavaModelException {
		List<IAnnotation> all = new ArrayList<>();
		collectAnnotations(annotatable.getAnnotations(), all, annotationNames);
		return all;
	}

	private static void collectAnnotations(IAnnotation[] annotations, List<IAnnotation> all,
			String... annotationNames) {
		if (annotations == null || annotations.length == 0 || annotationNames == null || annotationNames.length == 0) {
			return;
		}
		for (IAnnotation annotation : annotations) {
			for (String annotationName : annotationNames) {
				if (isMatchAnnotation(annotation, annotationName)) {
					all.add(annotation);
				}
			}
		}
	}
}
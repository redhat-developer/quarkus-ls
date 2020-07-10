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
package org.eclipse.lsp4mp.jdt.core.jaxrs;

import static org.eclipse.lsp4mp.jdt.core.utils.AnnotationUtils.getAnnotation;
import static org.eclipse.lsp4mp.jdt.core.utils.AnnotationUtils.getAnnotationMemberValue;
import static org.eclipse.lsp4mp.jdt.core.utils.AnnotationUtils.hasAnnotation;
import static org.eclipse.lsp4mp.jdt.internal.jaxrs.JaxRsConstants.JAVAX_WS_RS_GET_ANNOTATION;
import static org.eclipse.lsp4mp.jdt.internal.jaxrs.JaxRsConstants.JAVAX_WS_RS_PATH_ANNOTATION;
import static org.eclipse.lsp4mp.jdt.internal.jaxrs.JaxRsConstants.PATH_VALUE;

import java.util.Collections;

import org.eclipse.jdt.core.IAnnotatable;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4mp.jdt.core.utils.IJDTUtils;

/**
 * JAX-RS utilities.
 * 
 * @author Angelo ZERR
 *
 */
public class JaxRsUtils {

	private JaxRsUtils() {

	}

	/**
	 * Returns the value of the JAX-RS Path annotation and null otherwise..
	 * 
	 * @param annotatable
	 * @return the value of the JAX-RS Path annotation and null otherwise..
	 * @throws JavaModelException
	 */
	public static String getJaxRsPathValue(IAnnotatable annotatable) throws JavaModelException {
		IAnnotation annotationPath = getAnnotation(annotatable, JAVAX_WS_RS_PATH_ANNOTATION);
		return annotationPath != null ? getAnnotationMemberValue(annotationPath, PATH_VALUE) : null;
	}

	/**
	 * Returns true if the given method has @GET annotation and false otherwise.
	 * 
	 * @param method the method.
	 * @return true if the given method has @GET annotation and false otherwise.
	 * @throws JavaModelException
	 */
	public static boolean isJaxRsRequestMethod(IMethod method) throws JavaModelException {
		return hasAnnotation(method, JAVAX_WS_RS_GET_ANNOTATION);
	}

	/**
	 * Create URL CodeLens.
	 * 
	 * @param baseURL          the base URL.
	 * @param rootPath         the JAX-RS path value.
	 * @param openURICommandId the open URI command and null otherwise.
	 * @param method           the method.
	 * @param utils            the JDT utilities.
	 * @return the code lens and null otherwise.
	 * @throws JavaModelException
	 */
	public static CodeLens createURLCodeLens(String baseURL, String rootPath, String openURICommandId, IMethod method,
			IJDTUtils utils) throws JavaModelException {
		CodeLens lens = createURLCodeLens(method, utils);
		if (lens != null) {
			String pathValue = getJaxRsPathValue(method);
			String url = buildURL(baseURL, rootPath, pathValue);
			lens.setCommand(
					new Command(url, openURICommandId != null ? openURICommandId : "", Collections.singletonList(url)));
		}
		return lens;
	}

	private static CodeLens createURLCodeLens(IMethod method, IJDTUtils utils) throws JavaModelException {
		ISourceRange r = method.getNameRange();
		if (r == null) {
			return null;
		}
		CodeLens lens = new CodeLens();
		final Range range = utils.toRange(method.getOpenable(), r.getOffset(), r.getLength());
		lens.setRange(range);
		return lens;
	}

	public static String buildURL(String... paths) {
		StringBuilder url = new StringBuilder();
		for (String path : paths) {
			if (path != null && !path.isEmpty()) {
				if (url.length() > 0 && path.charAt(0) == '/') {
					path = path.substring(1, path.length());
				}

				if (url.length() > 0 && url.charAt(url.length() - 1) != '/') {
					url.append('/');
				}
				url.append(path);
			}
		}
		return url.toString();
	}
}

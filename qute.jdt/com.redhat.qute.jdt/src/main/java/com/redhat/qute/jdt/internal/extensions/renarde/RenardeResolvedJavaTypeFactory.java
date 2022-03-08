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
package com.redhat.qute.jdt.internal.extensions.renarde;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

import com.redhat.qute.commons.InvalidMethodReason;
import com.redhat.qute.commons.JavaMethodInfo;
import com.redhat.qute.commons.ResolvedJavaTypeInfo;
import com.redhat.qute.commons.datamodel.resolvers.ValueResolverKind;
import com.redhat.qute.commons.jaxrs.JaxRsMethodKind;
import com.redhat.qute.commons.jaxrs.JaxRsParamKind;
import com.redhat.qute.commons.jaxrs.RestParam;
import com.redhat.qute.jdt.internal.resolver.ITypeResolver;
import com.redhat.qute.jdt.internal.template.resolvedtype.AbstractResolvedJavaTypeFactory;
import com.redhat.qute.jdt.utils.AnnotationUtils;

/**
 * Custom Factory to create an {@link ResolvedJavaTypeInfo} instance for Renarde
 * controller.
 * 
 * @author Angelo ZERR
 *
 */
public class RenardeResolvedJavaTypeFactory extends AbstractResolvedJavaTypeFactory {

	private static final String JAVAX_WS_RS_POST_ANNOTATION = "javax.ws.rs.POST";
	private static final String JAKARTA_WS_RS_POST_ANNOTATION = "jakarta.ws.rs.POST";

	private static final String REST_FORM_ANNOTATION = "org.jboss.resteasy.reactive.RestForm";

	private static final String JAVAX_WS_RS_FORM_PARAM_ANNOTATION = "javax.ws.rs.FormParam";

	private static final String JAKARTA_WS_RS_FORM_PARAM_ANNOTATION = "jakarta.ws.rs.FormParam";

	private static final String REST_PATH_ANNOTATION = "org.jboss.resteasy.reactive.RestPath";

	private static final String JAVAX_WS_RS_PATH_PARAM_ANNOTATION = "javax.ws.rs.PathParam";

	private static final String JAKARTA_WS_RS_PATH_PARAM_ANNOTATION = "jakarta.ws.rs.PathParam";

	private static final String REST_QUERY_ANNOTATION = "org.jboss.resteasy.reactive.RestQuery";

	private static final String JAVAX_WS_RS_QUERY_PARAM_ANNOTATION = "javax.ws.rs.QueryParam";

	private static final String JAKARTA_WS_RS_QUERY_PARAM_ANNOTATION = "jakarta.ws.rs.QueryParam";

	@Override
	public boolean isAdaptedFor(ValueResolverKind kind) {
		return kind == ValueResolverKind.Renarde;
	}

	@Override
	protected boolean isValidField(IField field, IType type) throws JavaModelException {
		return false;
	}

	@Override
	protected boolean isValidRecordField(IField field, IType type) {
		return false;
	}

	@Override
	protected InvalidMethodReason getValidMethodForQute(IMethod method, String typeName) {
		return null;
	}

	@Override
	protected JavaMethodInfo createMethod(IMethod method, ITypeResolver typeResolver) {
		JavaMethodInfo info = super.createMethod(method, typeResolver);
		collectJaxrsInfo(method, info);
		return info;
	}

	private static void collectJaxrsInfo(IMethod method, JavaMethodInfo info) {
		// By default all public methods are GET
		JaxRsMethodKind methodKind = JaxRsMethodKind.GET;
		// TODO : we support only @POST, we need to support @PUT, @DELETE, when we will need it.
		if (isPostMethod(method)) {
			methodKind = JaxRsMethodKind.POST;
		}
		info.setJaxRsMethodKind(methodKind);
		try {
			Map<String, RestParam> restParameters = null;
			ILocalVariable[] parameters = method.getParameters();
			for (ILocalVariable parameter : parameters) {
				// @RestForm, @FormParam
				IAnnotation formAnnotation = AnnotationUtils.getAnnotation(parameter, REST_FORM_ANNOTATION,
						JAVAX_WS_RS_FORM_PARAM_ANNOTATION, JAKARTA_WS_RS_FORM_PARAM_ANNOTATION);
				if (formAnnotation != null) {
					if (restParameters == null) {
						restParameters = new HashMap<>();
					}
					fillRestParam(parameter, formAnnotation, JaxRsParamKind.FORM, restParameters);
				} else {
					// @RestPath, @PathParam
					IAnnotation pathAnnotation = AnnotationUtils.getAnnotation(parameter, REST_PATH_ANNOTATION,
							JAVAX_WS_RS_PATH_PARAM_ANNOTATION, JAKARTA_WS_RS_PATH_PARAM_ANNOTATION);
					if (pathAnnotation != null) {
						if (restParameters == null) {
							restParameters = new HashMap<>();
						}
						fillRestParam(parameter, pathAnnotation, JaxRsParamKind.PATH, restParameters);
					} else {
						// @RestQuery, @QueryParam
						IAnnotation queryAnnotation = AnnotationUtils.getAnnotation(parameter, REST_QUERY_ANNOTATION,
								JAVAX_WS_RS_QUERY_PARAM_ANNOTATION, JAKARTA_WS_RS_QUERY_PARAM_ANNOTATION);
						if (queryAnnotation != null) {
							if (restParameters == null) {
								restParameters = new HashMap<>();
							}
							fillRestParam(parameter, queryAnnotation, JaxRsParamKind.QUERY, restParameters);
						}
					}
				}
			}
			if (restParameters != null) {
				info.setRestParameters(restParameters);
			}
		} catch (Exception e) {

		}
	}

	private static void fillRestParam(ILocalVariable parameter, IAnnotation formAnnotation,
			JaxRsParamKind parameterKind, Map<String, RestParam> restParameters) throws JavaModelException {
		String parameterName = parameter.getElementName();
		String formName = parameterName;
		String value = AnnotationUtils.getAnnotationMemberValue(formAnnotation, "value");
		if (value != null) {
			formName = value;
		}
		restParameters.put(parameterName, new RestParam(formName, parameterKind, false));
	}

	private static boolean isPostMethod(IMethod method) {
		try {
			return AnnotationUtils.hasAnnotation(method, JAVAX_WS_RS_POST_ANNOTATION, JAKARTA_WS_RS_POST_ANNOTATION);
		} catch (JavaModelException e) {
			return false;
		}
	}

}

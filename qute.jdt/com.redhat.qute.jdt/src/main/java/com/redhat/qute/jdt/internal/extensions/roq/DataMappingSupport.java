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
package com.redhat.qute.jdt.internal.extensions.roq;

import static com.redhat.qute.jdt.internal.QuteJavaConstants.VALUE_ANNOTATION_NAME;
import static com.redhat.qute.jdt.internal.extensions.roq.RoqJavaConstants.DATA_MAPPING_ANNOTATION;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IAnnotatable;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

import com.redhat.qute.commons.datamodel.resolvers.ValueResolverInfo;
import com.redhat.qute.commons.datamodel.resolvers.ValueResolverKind;
import com.redhat.qute.jdt.template.datamodel.AbstractAnnotationTypeReferenceDataModelProvider;
import com.redhat.qute.jdt.template.datamodel.SearchContext;
import com.redhat.qute.jdt.utils.AnnotationUtils;

/**
 * Roq @DataMapping annotation support.
 *
 * @author Angelo ZERR
 *
 * @see https://quarkus.io/guides/qute-reference#injecting-beans-directly-in-templates
 *
 */
public class DataMappingSupport extends AbstractAnnotationTypeReferenceDataModelProvider {

	private static final Logger LOGGER = Logger.getLogger(DataMappingSupport.class.getName());

	private static final String INJECT_NAMESPACE = "inject";

	private static final String[] ANNOTATION_NAMES = { DATA_MAPPING_ANNOTATION };

	@Override
	protected String[] getAnnotationNames() {
		return ANNOTATION_NAMES;
	}

	@Override
	protected void processAnnotation(IJavaElement javaElement, IAnnotation annotation, String annotationName,
			SearchContext context, IProgressMonitor monitor) throws JavaModelException {
		if (javaElement.getElementType() != IJavaElement.TYPE) {
			return;
		}
		// @DataMapping(value = "events", parentArray = true)
		// public record Events(List<Event> list) {
		// becomes --> inject:events

		IType type = (IType) javaElement;
		String value = getDataMappingAnnotationValue(type);
		if (StringUtils.isNoneBlank(value)) {
			collectResolversForInject(type, value, context.getDataModelProject().getValueResolvers());
		}
	}

	private static String getDataMappingAnnotationValue(IJavaElement javaElement) {
		try {
			IAnnotation namedAnnotation = AnnotationUtils.getAnnotation((IAnnotatable) javaElement,
					DATA_MAPPING_ANNOTATION);
			if (namedAnnotation != null) {
				return AnnotationUtils.getAnnotationMemberValue(namedAnnotation, VALUE_ANNOTATION_NAME);
			}
		} catch (JavaModelException e) {
			LOGGER.log(Level.SEVERE, "Error while getting @DataMapping annotation value.", e);
			return null;
		}
		return null;
	}

	private static void collectResolversForInject(IType type, String named, List<ValueResolverInfo> resolvers) {
		ValueResolverInfo resolver = new ValueResolverInfo();
		resolver.setNamed(named);
		resolver.setSourceType(type.getFullyQualifiedName());
		resolver.setSignature(type.getFullyQualifiedName());
		resolver.setNamespace(INJECT_NAMESPACE);
		resolver.setKind(ValueResolverKind.InjectedBean);
		resolvers.add(resolver);
	}
}

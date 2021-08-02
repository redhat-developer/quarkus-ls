/*******************************************************************************
* Copyright (c) 2020 Red Hat Inc. and others.
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License v. 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
* which is available at https://www.apache.org/licenses/LICENSE-2.0.
*
* SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.microprofile.jdt.internal.quarkus.scheduler.properties;

import static org.eclipse.lsp4mp.jdt.core.utils.JDTTypeUtils.getSourceMethod;
import static org.eclipse.lsp4mp.jdt.core.utils.JDTTypeUtils.getSourceType;
import static org.eclipse.lsp4mp.jdt.core.utils.JDTTypeUtils.isBinary;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.lsp4mp.jdt.core.AbstractAnnotationTypeReferencePropertiesProvider;
import org.eclipse.lsp4mp.jdt.core.IPropertiesCollector;
import org.eclipse.lsp4mp.jdt.core.SearchContext;

import com.redhat.microprofile.jdt.internal.quarkus.QuarkusConstants;

/**
 * Properties provider to collect Quarkus properties from Java methods
 * annotated with the "io.quarkus.scheduler.Scheduled".
 * Valid property values will be surrounded with curly braces.
 */
public class QuarkusScheduledPropertiesProvider extends AbstractAnnotationTypeReferencePropertiesProvider {

	private static final String[] ANNOTATION_NAMES = { QuarkusConstants.SCHEDULED_ANNOTATION };

	private static Pattern PROP_PATTERN = Pattern.compile("\\{(.*)\\}");

	@Override
	protected String[] getAnnotationNames() {
		return ANNOTATION_NAMES;
	}

	@Override
	protected void processAnnotation(IJavaElement javaElement, IAnnotation configPropertyAnnotation,
			String annotationName, SearchContext context, IProgressMonitor monitor) throws JavaModelException {
		if (javaElement.getElementType() == IJavaElement.METHOD) {
			String extensionName = null;
			IPropertiesCollector collector = context.getCollector();
			String description = null;
			String sourceMethod = getSourceMethod((IMethod) javaElement);
			String sourceType = getSourceType(javaElement);
			boolean binary = isBinary(javaElement);

			for (IMemberValuePair mvp : configPropertyAnnotation.getMemberValuePairs()) {
				String name = mvp.getValue().toString();
				if (mvp.getValueKind() == IMemberValuePair.K_STRING && name != null && !name.isEmpty()) {
					Matcher m = PROP_PATTERN.matcher(name);
					if (m.matches()) {
						name = m.group(1);
						addItemMetadata(collector, name, "java.lang.String", description, sourceType, null, sourceMethod, null,
								extensionName, binary);
					}
				}
			}
		}
	}

}

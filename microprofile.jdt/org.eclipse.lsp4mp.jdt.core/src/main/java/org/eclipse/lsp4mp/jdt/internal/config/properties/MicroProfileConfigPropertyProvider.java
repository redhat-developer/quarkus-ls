/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lsp4mp.jdt.internal.config.properties;

import static org.eclipse.lsp4mp.jdt.core.utils.AnnotationUtils.getAnnotationMemberValue;
import static org.eclipse.lsp4mp.jdt.core.utils.JDTTypeUtils.findType;
import static org.eclipse.lsp4mp.jdt.core.utils.JDTTypeUtils.getEnclosedType;
import static org.eclipse.lsp4mp.jdt.core.utils.JDTTypeUtils.getPropertyType;
import static org.eclipse.lsp4mp.jdt.core.utils.JDTTypeUtils.getResolvedTypeName;
import static org.eclipse.lsp4mp.jdt.core.utils.JDTTypeUtils.getSourceField;
import static org.eclipse.lsp4mp.jdt.core.utils.JDTTypeUtils.getSourceMethod;
import static org.eclipse.lsp4mp.jdt.core.utils.JDTTypeUtils.getSourceType;
import static org.eclipse.lsp4mp.jdt.core.utils.JDTTypeUtils.isBinary;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.lsp4mp.jdt.core.AbstractAnnotationTypeReferencePropertiesProvider;
import org.eclipse.lsp4mp.jdt.core.IPropertiesCollector;
import org.eclipse.lsp4mp.jdt.core.MicroProfileConfigConstants;
import org.eclipse.lsp4mp.jdt.core.SearchContext;

/**
 * Properties provider to collect MicroProfile properties from the Java fields
 * annotated with "org.eclipse.microprofile.config.inject.ConfigProperty"
 * annotation.
 * 
 * @author Angelo ZERR
 *
 */
public class MicroProfileConfigPropertyProvider extends AbstractAnnotationTypeReferencePropertiesProvider {

	private static final String[] ANNOTATION_NAMES = { MicroProfileConfigConstants.CONFIG_PROPERTY_ANNOTATION };

	@Override
	protected String[] getAnnotationNames() {
		return ANNOTATION_NAMES;
	}

	@Override
	protected void processAnnotation(IJavaElement javaElement, IAnnotation configPropertyAnnotation,
			String annotationName, SearchContext context, IProgressMonitor monitor) throws JavaModelException {
		if (javaElement.getElementType() == IJavaElement.FIELD
				|| javaElement.getElementType() == IJavaElement.LOCAL_VARIABLE) {
			IPropertiesCollector collector = context.getCollector();
			String name = getAnnotationMemberValue(configPropertyAnnotation,
					MicroProfileConfigConstants.CONFIG_PROPERTY_ANNOTATION_NAME);
			if (name != null && !name.isEmpty()) {
				IJavaProject javaProject = javaElement.getJavaProject();
				String varTypeName = getResolvedTypeName(javaElement);
				IType varType = findType(javaProject, varTypeName);
				String type = getPropertyType(varType, varTypeName);
				String description = null;
				String sourceType = getSourceType(javaElement);
				String sourceField = null;
				String sourceMethod = null;
				
				String defaultValue = getAnnotationMemberValue(configPropertyAnnotation,
						MicroProfileConfigConstants.CONFIG_PROPERTY_ANNOTATION_DEFAULT_VALUE);
				String extensionName = null;
				
				if (javaElement.getElementType() == IJavaElement.FIELD) {
					sourceField = getSourceField(javaElement);
				} else if (javaElement.getElementType() == IJavaElement.LOCAL_VARIABLE) {
					ILocalVariable localVariable = (ILocalVariable) javaElement;
					IMethod method = (IMethod) localVariable.getDeclaringMember();
					sourceMethod = getSourceMethod(method);
				}

				// Enumerations
				IType enclosedType = getEnclosedType(varType, type, javaProject);
				super.updateHint(collector, enclosedType);

				boolean binary = isBinary(javaElement);
				addItemMetadata(collector, name, type, description, sourceType, sourceField, sourceMethod, defaultValue,
						extensionName, binary);
			}
		}
	}

}

/*******************************************************************************
* Copyright (c) 2022 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.jdt.internal.template.datamodel;

import static com.redhat.qute.jdt.internal.QuteJavaConstants.LOCATION_ANNOTATION;
import static com.redhat.qute.jdt.internal.QuteJavaConstants.TEMPLATE_CLASS;
import static com.redhat.qute.jdt.utils.JDTQuteProjectUtils.getTemplatePath;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IAnnotatable;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.internal.core.manipulation.dom.ASTResolving;

import com.redhat.qute.commons.datamodel.DataModelParameter;
import com.redhat.qute.commons.datamodel.DataModelTemplate;
import com.redhat.qute.jdt.internal.AnnotationLocationSupport;
import com.redhat.qute.jdt.internal.template.TemplateDataSupport;
import com.redhat.qute.jdt.template.datamodel.AbstractFieldDeclarationTypeReferenceDataModelProvider;
import com.redhat.qute.jdt.template.datamodel.SearchContext;
import com.redhat.qute.jdt.utils.AnnotationUtils;

/**
 * Template field support.
 * 
 * <code>
 *  &#64;Inject
 *  Template hello;
 *
 *  ...
 *
 *   &#64;GET
 *   &#64;Produces(MediaType.TEXT_HTML)
 *   public TemplateInstance get(@QueryParam("name") String name) {
 *   	hello.data("age", 12);
 *   	hello.data("height", 1.50, "weight", 50L);
 *       return hello.data("name", name);
 *   }
 *   </code>
 *
 * @see <a href=
 *      "https://quarkus.io/guides/qute-reference#quarkus_integration">Quarkus
 *      Integration</a>
 * @author Angelo ZERR
 *
 */
public class TemplateFieldSupport extends AbstractFieldDeclarationTypeReferenceDataModelProvider {

	private static final Logger LOGGER = Logger.getLogger(TemplateFieldSupport.class.getName());

	private static final String[] TYPE_NAMES = { TEMPLATE_CLASS };

	private static final String KEY = TemplateFieldSupport.class.getName() + "#";

	@Override
	protected String[] getTypeNames() {
		// Pattern to retrieve Template field
		return TYPE_NAMES;
	}

	@Override
	protected void processField(IField field, SearchContext context, IProgressMonitor monitor)
			throws JavaModelException {
		ICompilationUnit compilationUnit = field.getCompilationUnit();
		AnnotationLocationSupport annotationLocationSupport = getAnnotationLocationSupport(compilationUnit, context);
		StringLiteral location = annotationLocationSupport
				.getLocationExpressionFromConstructorParameter(field.getElementName());
		collectDataModelTemplateForTemplateField(field, context.getDataModelProject().getTemplates(),
				location != null ? location.getLiteralValue() : null, monitor);
	}

	private static AnnotationLocationSupport getAnnotationLocationSupport(ICompilationUnit compilationUnit,
			SearchContext context) {
		@SuppressWarnings("unchecked")
		Map<ICompilationUnit, AnnotationLocationSupport> allSupport = (Map<ICompilationUnit, AnnotationLocationSupport>) context
				.get(KEY);
		if (allSupport == null) {
			allSupport = new HashMap<>();
			context.put(KEY, allSupport);
		}

		AnnotationLocationSupport unitSupport = allSupport.get(compilationUnit);
		if (unitSupport == null) {
			@SuppressWarnings("restriction")
			CompilationUnit root = ASTResolving.createQuickFixAST(compilationUnit, null);
			unitSupport = new AnnotationLocationSupport(root);
			allSupport.put(compilationUnit, unitSupport);
		}
		return unitSupport;
	}

	private static void collectDataModelTemplateForTemplateField(IField field,
			List<DataModelTemplate<DataModelParameter>> templates, String location, IProgressMonitor monitor) {
		DataModelTemplate<DataModelParameter> template = createTemplateDataModel(field, location, monitor);
		templates.add(template);
	}

	private static DataModelTemplate<DataModelParameter> createTemplateDataModel(IField field,
			String locationFromConstructorParameter, IProgressMonitor monitor) {

		String location = locationFromConstructorParameter != null ? locationFromConstructorParameter
				: getLocation(field);
		String fieldName = field.getElementName();
		// src/main/resources/templates/${methodName}.qute.html
		String templateUri = getTemplatePath(null, null, location != null ? location : fieldName, true).getTemplateUri();

		// Create template data model with:
		// - template uri : Qute template file which must be bind with data model.
		// - source type : the Java class which defines Templates
		// -
		DataModelTemplate<DataModelParameter> template = new DataModelTemplate<DataModelParameter>();
		template.setParameters(new ArrayList<>());
		template.setTemplateUri(templateUri);
		template.setSourceType(field.getDeclaringType().getFullyQualifiedName());
		template.setSourceField(fieldName);
		// Collect data parameters for the given template
		TemplateDataSupport.collectParametersFromDataMethodInvocation(field, template, monitor);
		return template;
	}

	private static String getLocation(IAnnotatable field) {
		try {
			IAnnotation annotation = AnnotationUtils.getAnnotation(field, LOCATION_ANNOTATION);
			if (annotation != null) {
				return AnnotationUtils.getAnnotationMemberValue(annotation, "value");
			}
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE,
					"Error while getting @Location of '" + ((IJavaElement) field).getElementName() + "'.", e);
		}
		return null;
	}
}

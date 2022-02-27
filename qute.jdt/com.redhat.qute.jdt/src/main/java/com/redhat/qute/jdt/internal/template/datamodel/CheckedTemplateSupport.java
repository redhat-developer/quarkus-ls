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

import static com.redhat.qute.jdt.internal.QuteJavaConstants.CHECKED_TEMPLATE_ANNOTATION;
import static com.redhat.qute.jdt.internal.QuteJavaConstants.OLD_CHECKED_TEMPLATE_ANNOTATION;
import static com.redhat.qute.jdt.internal.template.QuarkusIntegrationForQute.resolveSignature;
import static com.redhat.qute.jdt.utils.JDTQuteProjectUtils.getTemplatePath;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

import com.redhat.qute.commons.datamodel.DataModelParameter;
import com.redhat.qute.commons.datamodel.DataModelTemplate;
import com.redhat.qute.jdt.internal.template.TemplateDataSupport;
import com.redhat.qute.jdt.template.datamodel.AbstractAnnotationTypeReferenceDataModelProvider;
import com.redhat.qute.jdt.template.datamodel.SearchContext;
import com.redhat.qute.jdt.utils.JDTTypeUtils;

/**
 * CheckedTemplate support for template files:
 * 
 * <code>
 * &#64;CheckedTemplate
 *	static class Templates {
 *		static native TemplateInstance items(List<Item> items);
 *	}
 * 
 *  ...
 *  
 *  &#64;GET
 *	&#64;Produces(MediaType.TEXT_HTML)
 *	public TemplateInstance get() {
 * 		List<Item> items = new ArrayList<>();
 * 		items.add(new Item(new BigDecimal(10), "Apple"));
 * 		items.add(new Item(new BigDecimal(16), "Pear"));
 * 		items.add(new Item(new BigDecimal(30), "Orange"));
 * 		return Templates.items(items);
 *	}
 * </code>
 * 
 * 
 * @author Angelo ZERR
 *
 */
public class CheckedTemplateSupport extends AbstractAnnotationTypeReferenceDataModelProvider {

	private static final Logger LOGGER = Logger.getLogger(CheckedTemplateSupport.class.getName());

	private static final String[] ANNOTATION_NAMES = { CHECKED_TEMPLATE_ANNOTATION, OLD_CHECKED_TEMPLATE_ANNOTATION };

	@Override
	protected String[] getAnnotationNames() {
		return ANNOTATION_NAMES;
	}

	@Override
	protected void processAnnotation(IJavaElement javaElement, IAnnotation annotation, String annotationName,
			SearchContext context, IProgressMonitor monitor) throws JavaModelException {
		if (javaElement instanceof IType) {
			IType type = (IType) javaElement;
			collectDataModelTemplateForCheckedTemplate(type, context.getDataModelProject().getTemplates(), monitor);
		}
	}

	/**
	 * Collect data model template from @CheckedTemplate.
	 * 
	 * @param type      the Java type.
	 * @param templates the data model templates to update with collect of template.
	 * @param monitor   the progress monitor.
	 * @throws JavaModelException
	 */
	private static void collectDataModelTemplateForCheckedTemplate(IType type,
			List<DataModelTemplate<DataModelParameter>> templates, IProgressMonitor monitor) throws JavaModelException {
		boolean innerClass = type.getParent() != null && type.getParent().getElementType() == IJavaElement.TYPE;
		String className = !innerClass ? null
				: JDTTypeUtils.getSimpleClassName(
						type.getCompilationUnit() != null ? type.getCompilationUnit().getElementName()
								: type.getClassFile().getElementName());

		// Loop for each methods (book, book) and create a template data model per
		// method.
		IMethod[] methods = type.getMethods();
		for (IMethod method : methods) {
			DataModelTemplate<DataModelParameter> template = createTemplateDataModel(method, className, type, monitor);
			templates.add(template);
		}
	}

	private static DataModelTemplate<DataModelParameter> createTemplateDataModel(IMethod method, String className,
			IType type, IProgressMonitor monitor) {
		String methodName = method.getElementName();
		// src/main/resources/templates/${className}/${methodName}.qute.html
		String templateUri = getTemplatePath(className, methodName);

		// Create template data model with:
		// - template uri : Qute template file which must be bind with data model.
		// - source type : the Java class which defines Templates
		// -
		DataModelTemplate<DataModelParameter> template = new DataModelTemplate<DataModelParameter>();
		template.setParameters(new ArrayList<>());
		template.setTemplateUri(templateUri);
		template.setSourceType(type.getFullyQualifiedName());
		template.setSourceMethod(methodName);

		try {
			for (ILocalVariable methodParameter : method.getParameters()) {
				DataModelParameter parameter = createParameterDataModel(methodParameter, type);
				template.getParameters().add(parameter);
			}
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE,
					"Error while getting method template parameter of '" + method.getElementName() + "'.", e);
		}
		// Collect data parameters for the given template
		TemplateDataSupport.collectParametersFromDataMethodInvocation(method, template, monitor);
		return template;
	}

	private static DataModelParameter createParameterDataModel(ILocalVariable methodParameter, IType type)
			throws JavaModelException {
		String parameterName = methodParameter.getElementName();
		String parameterType = resolveSignature(methodParameter, type);

		DataModelParameter parameter = new DataModelParameter();
		parameter.setKey(parameterName);
		parameter.setSourceType(parameterType);
		return parameter;
	}

}

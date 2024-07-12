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
package com.redhat.qute.jdt.internal.template.datamodel;

import static com.redhat.qute.jdt.internal.QuteJavaConstants.TEMPLATE_INSTANCE_INTERFACE;
import static com.redhat.qute.jdt.utils.JDTQuteProjectUtils.getTemplatePath;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

import com.redhat.qute.commons.datamodel.DataModelParameter;
import com.redhat.qute.commons.datamodel.DataModelTemplate;
import com.redhat.qute.jdt.internal.resolver.ITypeResolver;
import com.redhat.qute.jdt.internal.template.TemplateDataSupport;
import com.redhat.qute.jdt.template.datamodel.AbstractInterfaceImplementationDataModelProvider;
import com.redhat.qute.jdt.template.datamodel.SearchContext;

/**
 * Template Records support for template files:
 * 
 * <code>
 * public class HelloResource {

 *  record Hello(String name) implements TemplateInstance {}
 * 
 *  ...
 *  
 *
 *  &#64;GET
 *  &#64;Produces(MediaType.TEXT_PLAIN)
 *  public TemplateInstance get(@QueryParam("name") String name) {
 *      return new Hello(name).data("bar", 100);
 *  }
 * </code>
 * 
 * @see <a href=
 *      "https://quarkus.io/guides/qute-reference#template-records">Template
 *      Records</a>
 * @author Angelo ZERR
 *
 */
public class TemplateRecordsSupport extends AbstractInterfaceImplementationDataModelProvider {

	private static final String[] INTERFACE_NAMES = { TEMPLATE_INSTANCE_INTERFACE };

	@Override
	protected String[] getInterfaceNames() {
		return INTERFACE_NAMES;
	}

	@Override
	protected void processType(IType type, SearchContext context, IProgressMonitor monitor) throws JavaModelException {
		if (!type.isRecord()) {
			return;
		}
		ITypeResolver typeResolver = context.getTypeResolver(type);
		collectDataModelTemplateForTemplateRecord(type, typeResolver, context.getDataModelProject().getTemplates(),
				monitor);
	}

	private static void collectDataModelTemplateForTemplateRecord(IType type, ITypeResolver typeResolver,
			List<DataModelTemplate<DataModelParameter>> templates, IProgressMonitor monitor) throws JavaModelException {
		DataModelTemplate<DataModelParameter> template = createTemplateDataModel(type, typeResolver, monitor);
		templates.add(template);
	}

	private static DataModelTemplate<DataModelParameter> createTemplateDataModel(IType type, ITypeResolver typeResolver,
			IProgressMonitor monitor) throws JavaModelException {

		String recordName = type.getElementName();
		// src/main/resources/templates/${recordName}.qute.html
		String templateUri = getTemplatePath(null, null, recordName, true).getTemplateUri();

		// Create template data model with:
		// - template uri : Qute template file which must be bind with data model.
		// - source type : the record class which defines Templates
		// -
		DataModelTemplate<DataModelParameter> template = new DataModelTemplate<DataModelParameter>();
		template.setParameters(new ArrayList<>());
		template.setTemplateUri(templateUri);
		template.setSourceType(type.getFullyQualifiedName());

		// Collect data parameters from the record fields
		for (IField field : type.getRecordComponents()) {
			DataModelParameter parameter = new DataModelParameter();
			parameter.setKey(field.getElementName());
			parameter.setSourceType(typeResolver.resolveTypeSignature(field.getTypeSignature()));
			template.getParameters().add(parameter);
		}

		// Collect data parameters for the given template
		TemplateDataSupport.collectParametersFromDataMethodInvocation(type, template, monitor);
		return template;
	}

}

/*******************************************************************************
* Copyright (c) 2021 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.jdt.internal.template;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.StringLiteral;

import com.redhat.qute.commons.datamodel.DataModelParameter;
import com.redhat.qute.commons.datamodel.DataModelTemplate;

/**
 * AST visitor used to collect {@link DataModelParameter} parameter for a given
 * {@link DataModelTemplate} template.
 * 
 * This visitor track the invocation of method
 * io.quarkus.qute.Template#data(String key, Object data) to collect parameters.
 * 
 * For instance, with this following code:
 * 
 * <code>
 * private final Template page;
 * ...
 * page.data("age", 13);
   page.data("name", "John");
 * </code>
 * 
 * the AST visitor will collect the following parameters:
 * 
 * <ul>
 * <li>parameter key='age', sourceType='int'</li>
 * <li>parameter key='name', sourceType='java.lang.String'</li>
 * </ul>
 * 
 * @author Angelo ZERR
 *
 */
public class TemplateDataCollector extends TemplateDataVisitor {

	private final DataModelTemplate<DataModelParameter> template;

	public TemplateDataCollector(DataModelTemplate<DataModelParameter> template, IProgressMonitor monitor) {
		this.template = template;
	}

	@Override
	protected boolean visitParameter(Object name, Object type) {
		String paramName = null;
		if (name instanceof StringLiteral) {
			paramName = ((StringLiteral) name).getLiteralValue();
		}
		if (paramName != null) {
			String paramType = "java.lang.Object";
			if (type instanceof Expression) {
				ITypeBinding binding = ((Expression) type).resolveTypeBinding();
				paramType = binding.getQualifiedName();
			}

			if (paramName != null && template.getParameter(paramName) == null) {
				DataModelParameter parameter = new DataModelParameter();
				parameter.setKey(paramName);
				parameter.setSourceType(paramType);
				parameter.setDataMethodInvocation(true);
				template.addParameter(parameter);
			}
		}
		return true;
	}
}

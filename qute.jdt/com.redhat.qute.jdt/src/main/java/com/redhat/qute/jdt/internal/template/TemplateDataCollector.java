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

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
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
public class TemplateDataCollector extends ASTVisitor {

	private static final String DATA_METHOD = "data";

	private final DataModelTemplate<DataModelParameter> template;

	public TemplateDataCollector(DataModelTemplate<DataModelParameter> template, IProgressMonitor monitor) {
		this.template = template;
	}

	@Override
	public boolean visit(MethodInvocation node) {
		String methodName = node.getName().getIdentifier();
		if (DATA_METHOD.equals(methodName)) {
			// .data("book", book)
			@SuppressWarnings("rawtypes")
			List arguments = node.arguments();
			String paramName = null;
			for (int i = 0; i < arguments.size(); i++) {
				if (i % 2 == 0) {
					paramName = null;
					Object name = arguments.get(i);
					if (name instanceof StringLiteral) {
						paramName = ((StringLiteral) name).getLiteralValue();
					}
				} else {
					String paramType = "java.lang.Object";
					Object type = arguments.get(i);
					if (type instanceof Expression) {
						ITypeBinding binding = ((Expression) type).resolveTypeBinding();
						paramType = binding.getQualifiedName();
					}

					if (paramName != null && template.getParameter(paramName) == null) {
						DataModelParameter parameter = new DataModelParameter();
						parameter.setKey(paramName);
						parameter.setSourceType(paramType);
						template.addParameter(parameter);
					}
				}
			}
		}
		return super.visit(node);
	}

}

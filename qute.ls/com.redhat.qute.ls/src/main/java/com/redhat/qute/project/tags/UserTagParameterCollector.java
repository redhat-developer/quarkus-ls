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
package com.redhat.qute.project.tags;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.redhat.qute.parser.expression.MethodPart;
import com.redhat.qute.parser.expression.ObjectPart;
import com.redhat.qute.parser.expression.Part;
import com.redhat.qute.parser.expression.Parts;
import com.redhat.qute.parser.expression.Parts.PartKind;
import com.redhat.qute.parser.template.ASTVisitor;
import com.redhat.qute.parser.template.Expression;
import com.redhat.qute.parser.template.LiteralSupport;
import com.redhat.qute.parser.template.Parameter;
import com.redhat.qute.parser.template.Section;
import com.redhat.qute.parser.template.sections.IfSection;
import com.redhat.qute.parser.template.sections.LetSection;
import com.redhat.qute.parser.template.sections.SetSection;
import com.redhat.qute.project.QuteProject;
import com.redhat.qute.project.datamodel.resolvers.ValueResolver;
import com.redhat.qute.utils.StringUtils;

/**
 * User tag parameters collector.
 * 
 * @author Angelo ZERR
 *
 */
public class UserTagParameterCollector extends ASTVisitor {

	private final QuteProject project;

	private final Map<String, UserTagParameter> parameters;

	private final List<List<ParamInfo>> parameterNamesStack;

	private List<String> globalVariables;

	private static class ParamInfo {
		public final String name;
		public final boolean assigned; // true if parameter comes from a #let, #set and false otherwise.
		public final String defaultValue; // ex : name?="foo", the default value is "foo"

		public ParamInfo(String name, boolean assigned, String defaultValue) {
			this.name = name;
			this.assigned = assigned;
			this.defaultValue = defaultValue;
		}
	}

	public UserTagParameterCollector(QuteProject project) {
		this.project = project;
		this.parameters = new LinkedHashMap<>();
		this.parameterNamesStack = new ArrayList<>();
	}

	@Override
	public boolean visit(IfSection node) {
		addOptionalStack(node);
		return super.visit(node);
	}

	@Override
	public void endVisit(IfSection node) {
		removeParameterStack();
		super.endVisit(node);
	}

	private void addOptionalStack(Section node) {
		List<ParamInfo> optionalParameterNames = null;
		List<Parameter> parameters = node.getParameters();
		for (Parameter parameter : parameters) {
			String name = parameter.getName();
			if (!StringUtils.isEmpty(name) && (parameter.isOptional())) {
				if (optionalParameterNames == null) {
					optionalParameterNames = new ArrayList<>();
				}
				optionalParameterNames.add(new ParamInfo(name, false, null));
			}
		}
		parameterNamesStack
				.add(optionalParameterNames != null ? optionalParameterNames : Collections.emptyList());
	}

	private void removeParameterStack() {
		parameterNamesStack.remove(parameterNamesStack.size() - 1);
	}

	public boolean visit(LetSection node) {
		addAssignedParameterStack(node);
		return super.visit(node);
	}

	@Override
	public void endVisit(LetSection node) {
		removeParameterStack();
		super.endVisit(node);
	}

	public boolean visit(SetSection node) {
		addAssignedParameterStack(node);
		return super.visit(node);
	}

	@Override
	public void endVisit(SetSection node) {
		removeParameterStack();
		super.endVisit(node);
	}

	private void addAssignedParameterStack(Section node) {
		List<ParamInfo> declaredParameterNames = null;
		List<Parameter> parameters = node.getParameters();
		for (Parameter parameter : parameters) {
			String name = parameter.getName();
			String defaultValue = parameter.hasDefaultValue() ? parameter.getValue() : null;
			if (!StringUtils.isEmpty(name)) {
				if (declaredParameterNames == null) {
					declaredParameterNames = new ArrayList<>();
				}
				declaredParameterNames.add(new ParamInfo(name, true, defaultValue));
			}
		}
		parameterNamesStack.add(declaredParameterNames != null ? declaredParameterNames : Collections.emptyList());
	}

	@Override
	public boolean visit(MethodPart node) {
		for (Parameter parameter : node.getParameters()) {
			Expression expression = parameter.getJavaTypeExpression();
			if (expression != null) {
				expression.accept(this);
			}
		}
		return super.visit(node);
	}

	@Override
	public boolean visit(ObjectPart node) {
		if (isValid(node)) {
			String partName = node.getPartName(); // {foo}

			// Get the parameter info from the parameter stack
			ParamInfo paramInfo = getParamInfo(partName);
			if (paramInfo != null && paramInfo.assigned && paramInfo.defaultValue == null) {
				// The part name (foo) is defined in parent section #let, #set
				// which have not default value
				// {#let foo="bar"}
				// {foo}
				// It is not a user tag parameter.
				return super.visit(node);
			}

			// Here we are in several usecase:
			// 1. foo is not declared in none parent section -> user tag parameter is
			// required
			// 2. foo is declared in #if section -> user tag parameter is optional
			// 3. foo is declared in #let section with default value (#let foo?="bar") ->
			// user tag parameter is optional and have a defaut value

			// Get or create the user tag parameter
			UserTagParameter parameter = parameters.get(partName);
			if (parameter == null) {
				parameter = new UserTagParameter(partName);
				parameter.setDefaultValue(paramInfo != null ? paramInfo.defaultValue : null);
				parameters.put(partName, parameter);
			}

			// Compute user tag parameter required flag if needed
			if (!parameter.isRequired() && parameter.getDefaultValue() == null) {
				boolean ignore = paramInfo != null && !paramInfo.assigned;
				if (!ignore) {
					boolean required = true;
					Parts parts = node.getParent();
					int index = parts.getPartIndex(node);
					if (index + 1 < parts.getChildCount()) {
						Part next = parts.getChild(index + 1);
						if (next != null && next.getPartKind() == PartKind.Method) {
							MethodPart methodPart = (MethodPart) next;
							if (methodPart.isOrOperator()) {
								required = false;
							}
						}
					} else {
						required = !node.isOptional();
					}
					parameter.setRequired(required);
				}
			}
		}
		return super.visit(node);
	}

	private ParamInfo getParamInfo(String partName) {
		for (List<ParamInfo> params : parameterNamesStack) {
			for (ParamInfo paramInfo : params) {
				if (paramInfo.name.equals(partName)) {
					return paramInfo;
				}
			}
		}
		return null;
	}

	public boolean isValid(ObjectPart node) {
		if (node.getNamespace() != null) {
			// ex : {uri:Login....}
			// The object part have a namespace,ignore it
			return false;
		}
		if (globalVariables == null) {
			List<ValueResolver> resolvers = project.getGlobalVariables().getNow(null);
			globalVariables = resolvers != null ? resolvers.stream()
					.map(ValueResolver::getName).collect(Collectors.toList())
					: Collections.emptyList();
		}
		if (globalVariables.contains(node.getPartName())) {
			// The object part is a global variable declared in Java with @TemplateGlobal,
			// ignore it
			return false;
		}
		if (LiteralSupport.getLiteralJavaType(node.getPartName()) != null) {
			// The part is a number, boolean, etc, ignore it
			return false;
		}
		return true;
	}

	public Map<String, UserTagParameter> getParameters() {
		return parameters;
	}
}

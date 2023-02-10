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
import com.redhat.qute.project.QuteProjectRegistry;
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

	private final List<List<String>> optionalParameterNamesStack;

	private final List<List<String>> ignoreParameterNamesStack;

	private List<String> globalVariables;

	public UserTagParameterCollector(QuteProject project) {
		this.project = project;
		this.parameters = new LinkedHashMap<>();
		this.optionalParameterNamesStack = new ArrayList<>();
		this.ignoreParameterNamesStack = new ArrayList<>();
	}

	@Override
	public boolean visit(IfSection node) {
		addOptionalStack(node);
		return super.visit(node);
	}

	@Override
	public void endVisit(IfSection node) {
		removeOptionalStack();
		super.endVisit(node);
	}

	private void addOptionalStack(Section node) {
		List<String> optionalParameterNames = null;
		List<Parameter> parameters = node.getParameters();
		for (Parameter parameter : parameters) {
			String name = parameter.getName();
			if (!StringUtils.isEmpty(name) && (parameter.isOptional())) {
				if (optionalParameterNames == null) {
					optionalParameterNames = new ArrayList<>();
				}
				optionalParameterNames.add(name);
			}
		}
		optionalParameterNamesStack
				.add(optionalParameterNames != null ? optionalParameterNames : Collections.emptyList());
	}

	private void removeOptionalStack() {
		optionalParameterNamesStack.remove(optionalParameterNamesStack.size() - 1);
	}

	public boolean visit(LetSection node) {
		addIgnoreStack(node);
		return super.visit(node);
	}

	@Override
	public void endVisit(LetSection node) {
		removeIgnoreStack();
		super.endVisit(node);
	}

	public boolean visit(SetSection node) {
		addIgnoreStack(node);
		return super.visit(node);
	}

	@Override
	public void endVisit(SetSection node) {
		removeIgnoreStack();
		super.endVisit(node);
	}

	private void addIgnoreStack(Section node) {
		List<String> ignoreParameterNames = null;
		List<Parameter> parameters = node.getParameters();
		for (Parameter parameter : parameters) {
			String name = parameter.getName();
			if (!StringUtils.isEmpty(name)) {
				if (ignoreParameterNames == null) {
					ignoreParameterNames = new ArrayList<>();
				}
				ignoreParameterNames.add(name);
			}
		}
		ignoreParameterNamesStack.add(ignoreParameterNames != null ? ignoreParameterNames : Collections.emptyList());
	}

	private void removeIgnoreStack() {
		ignoreParameterNamesStack.remove(ignoreParameterNamesStack.size() - 1);
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
			String partName = node.getPartName();

			for (List<String> ignoreNames : ignoreParameterNamesStack) {
				if (ignoreNames.contains(partName)) {
					return super.visit(node);
				}
			}

			// Get or create the user tag parameter
			UserTagParameter parameter = parameters.get(partName);
			if (parameter == null) {
				parameter = new UserTagParameter(partName);
				parameters.put(partName, parameter);
			}
			// Compute required flag if needed
			if (!parameter.isRequired()) {
				boolean ignore = false;
				for (List<String> optionalNames : optionalParameterNamesStack) {
					if (optionalNames.contains(partName)) {
						ignore = true;
					}
				}
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

	public boolean isValid(ObjectPart node) {
		if (node.getNamespace() != null) {
			// ex : {uri:Login....}
			// The object part have a namespace,ignore it
			return false;
		}
		if (globalVariables == null) {
			List<ValueResolver> resolvers = QuteProjectRegistry.getGlobalVariables(project).getNow(null);
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

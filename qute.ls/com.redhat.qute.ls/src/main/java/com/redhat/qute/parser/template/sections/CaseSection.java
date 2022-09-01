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
package com.redhat.qute.parser.template.sections;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.redhat.qute.parser.template.ASTVisitor;
import com.redhat.qute.parser.template.CaseOperator;
import com.redhat.qute.parser.template.Parameter;
import com.redhat.qute.parser.template.Section;
import com.redhat.qute.parser.template.SectionKind;

/**
 * Case section AST node.
 * 
 * <code>
 	{#switch person.name}
  		{#case 'John'} 
    		Hey John!
  		{#case 'Mary'}
    		Hey Mary!
	{/switch}
 * </code>
 * 
 * @author Angelo ZERR
 * 
 * @see https://quarkus.io/guides/qute-reference#when_section
 *
 */
public class CaseSection extends Section {

	public static final String TAG = "case";

	private static final Map<String, CaseOperator> caseOperators;

	static {
		caseOperators = new HashMap<>();
		registerOperator("gt", false, ">"); // greater than
		registerOperator("ge", false, ">="); // greater than or equal to
		registerOperator("lt", false, "<"); // less than
		registerOperator("le", false, "<="); // less than or equal to
		registerOperator("not", false, "ne", "!="); // not equals
		registerOperator("in", true); // Is in
		registerOperator("ni", true, "!in"); // Is not in
	}

	private static void registerOperator(String name, boolean isMulti, String... aliases) {
		CaseOperator operator = new CaseOperator(name, isMulti, aliases);
		caseOperators.put(operator.getName(), operator);
		if (aliases != null) {
			for (String alias : aliases) {
				caseOperators.put(alias, operator);
			}
		}
	}

	public CaseSection(int start, int end) {
		this(TAG, start, end);
	}

	CaseSection(String tag, int start, int end) {
		super(tag, start, end);
	}

	@Override
	public SectionKind getSectionKind() {
		return SectionKind.CASE;
	}

	@Override
	protected void accept0(ASTVisitor visitor) {
		boolean visitChildren = visitor.visit(this);
		if (visitChildren) {
			List<Parameter> parameters = getParameters();
			for (Parameter parameter : parameters) {
				acceptChild(visitor, parameter);
			}
			acceptChildren(visitor, getChildren());
		}
		visitor.endVisit(this);
	}

	public boolean shouldCompleteWhenSection() {
		CaseOperator operator = getOperator();
		int paramCount = getParameters().size();
		if (operator == null) {
			if (paramCount == 1) {
				// There is only parameter and it is not a operator
				return false;
			}
			return true;
		}
		if (paramCount == 2 && !operator.isMulti()) {
			// first parameter is operator that only allows single value, second is the
			// existing Enum
			return false;
		}
		return true;
	}

	private CaseOperator getOperator() {
		Parameter parameter = getValidParameterOperator();
		return parameter != null ? caseOperators.get(parameter.getName()) : null;
	}

	public Parameter getValidParameterOperator() {
		int paramCount = getParameters().size();
		if (paramCount == 0) {
			return null;
		}
		Parameter parameter = getParameterAtIndex(0);
		return caseOperators.containsKey(parameter.getName()) ? parameter : null;
	}

	@Override
	public boolean isValidOperator(String partName) {
		return caseOperators.containsKey(partName);
	}

	public boolean isCaseOperator(Parameter parameter) {
		return parameter == getValidParameterOperator();
	}
}

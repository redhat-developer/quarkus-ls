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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

	private static final Set<String> completionOperators;

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

	static {
		completionOperators = new HashSet<String>();
		completionOperators.add(">");
		completionOperators.add(">=");
		completionOperators.add("<");
		completionOperators.add("<=");
		completionOperators.add("!=");
		completionOperators.add("in");
		completionOperators.add("!in");
	}

	public static Set<String> getCompletionOperators() {
		return completionOperators;
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
		super(TAG, start, end);
	}

	CaseSection(String tag, int start, int end) {
		super(tag, start, end);
	}

	@Override
	public SectionKind getSectionKind() {
		return SectionKind.CASE;
	}

	@Override
	public List<SectionKind> getBlockLabels() {
		return List.of(SectionKind.IS, SectionKind.CASE, SectionKind.ELSE);
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

	@Override
	public Set<String> getAllowedOperators() {
		return caseOperators.keySet();
	}

	/**
	 * Returns true if completion should be done in the current case section.
	 *
	 * @return true if completion should be done in the current case section.
	 */
	public boolean shouldCompleteWhenSection() {
		int paramCount = getParameters().size();
		if (paramCount == 0) {
			return true;
		}
		CaseOperator operator = getCaseOperator();
		if (operator == null) {
			if (paramCount == 0) {
				// There is more than 1 parameter and it is not a operator
				return true;
			}
			return false;
		}
		if (paramCount >= 2 && !operator.isMulti()) {
			// first parameter is operator that only allows single value, second is the
			// existing Enum
			return false;
		}
		return true;
	}

	/**
	 * Returns the parameter of a valid operator if exists in the current case
	 * section.
	 *
	 * @return the parameter of a valid operator if exists in the current case
	 *         section.
	 */
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

	/**
	 * Returns the case operator if exists and valid, and null otherwise.
	 *
	 * @return the case operator if exists and valid, and null otherwise.
	 */
	public CaseOperator getCaseOperator() {
		Parameter parameter = getValidParameterOperator();
		return parameter != null ? caseOperators.get(parameter.getName()) : null;
	}

	/**
	 * Returns true if the given parameter is a valid case operator.
	 *
	 * @param parameter the parameter.
	 *
	 * @return true if the given parameter is a valid case operator.
	 */
	public boolean isCaseOperator(Parameter parameter) {
		return parameter == getValidParameterOperator();
	}

	/**
	 * Returns true if the offset is in the expected operator position.
	 *
	 * @return true if the offset is in the expected operator position.
	 */
	public boolean isInOperatorPosition(int offset) {
		// Ex:
		// {#case | ON} --> is in the position where the operator could be
		return offset >= getStartTagNameCloseOffset() && offset <= getStartParametersOffset();
	}

	/**
	 * Returns true if the given operator expects mutiple parameters.
	 *
	 * @return true if the given operator expects mutiple parameters.
	 */
	public static boolean isMulti(String operator) {
		return caseOperators.get(operator).isMulti();
	}

}

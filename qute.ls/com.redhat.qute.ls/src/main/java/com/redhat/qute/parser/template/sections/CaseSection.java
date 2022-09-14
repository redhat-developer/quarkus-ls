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

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.redhat.qute.parser.parameter.ParameterParser;
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

	public static enum CompletionCaseResult {
		ALL_OPERATOR_AND_FIELD, //
		ALL_OPERATOR, //
		MULTI_OPERATOR_ONLY, //
		FIELD_ONLY, //
		NONE;
	}

	private static final Map<String, CaseOperator> caseOperators;

	static {
		caseOperators = new LinkedHashMap<>();
		// https://quarkus.io/guides/qute-reference#when_section
		registerOperator("gt", "Greater than. Example: {#case gt 10}.", false, ">"); // greater than
		registerOperator("ge", "Greater than or equal to. Example: {#case >= 10}", false, ">="); // greater than or
		// equal to
		registerOperator("lt", "Less than. Example: {#case < 10}.", false, "<"); // less than
		registerOperator("le", "Less than or equal to. Example: {#case le 10}.", false, "<="); // less than or equal to
		registerOperator("not", "Not equal. Example: {#is not 10},{#case != 10}.", false, "ne", "!="); // not equals
		registerOperator("in", "Is in. Example: {#is in 'foo' 'bar' 'baz'}.", true); // Is in
		registerOperator("ni", "Is not in. Example: {#is !in 1 2 3}.", true, "!in"); // Is not in
	}

	private static void registerOperator(String name, String documentation, boolean isMulti, String... aliases) {
		CaseOperator operator = new CaseOperator(name, documentation, null, isMulti);
		caseOperators.put(operator.getName(), operator);
		if (aliases != null) {
			for (String alias : aliases) {
				CaseOperator aliasOperator = operator = new CaseOperator(alias, documentation, name, isMulti);
				caseOperators.put(alias, aliasOperator);
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
	protected void initializeParameters(List<Parameter> parameters) {
		// All parameters can have expression.
		// Ex :
		// - {#case OFF}
		// - {#case in ON OFF}
		for (Parameter parameter : parameters) {
			// Force the compute of parameter name here to support '=' in the name for some
			// operator like:
			// - !=
			// - >=
			parameter.setStartValue(parameter.getEnd());
			parameter.getName();
			parameter.setStartValue(NULL_VALUE);
			parameter.setCanHaveExpression(true);
		}
	}

	protected List<Parameter> collectParameters() {
		// Don't split parameters with '=' to support operator like
		// - !=
		// - >=
		boolean splitWithEquals = false;
		return ParameterParser.parse(this, false, splitWithEquals);
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

	@SuppressWarnings("unchecked")
	@Override
	public Collection<CaseOperator> getAllowedOperators() {
		return caseOperators.values();
	}

	/**
	 * Returns the CompletionCaseResult of the current completion request.
	 *
	 * @return the CompletionCaseResult of the current completion request.
	 */
	public CompletionCaseResult getCompletionCaseResultAt(int offset, Parameter triggeredParameter) {
		List<Parameter> parameters = super.getParameters();
		Parameter firstParameter = getParameterAtIndex(0);
		int increment = triggeredParameter != null ? 1 : 0;
		if (parameters.size() == (0 + increment)) {
			// {#case |}
			// {#case i|n}
			return CompletionCaseResult.ALL_OPERATOR_AND_FIELD;
		}

		boolean beforeFirstParam = (offset < firstParameter.getStart() + increment);
		CaseOperator operator = getCaseOperator();
		if (parameters.size() == (1 + increment)) {
			if (beforeFirstParam) {
				// {#case | ON}
				// {#case i|n ON}
				return CompletionCaseResult.ALL_OPERATOR;
			}
			if (operator == null) {
				// {#case ON |}
				// {#case ON O|FF}
				return CompletionCaseResult.NONE;
			}
			// {#case in |}
			// {#case in O|N}
			return CompletionCaseResult.FIELD_ONLY;
		}
		if (parameters.size() >= (2 + increment)) {
			if (operator == null && beforeFirstParam) {
				// {#case | ON OFF}
				return CompletionCaseResult.MULTI_OPERATOR_ONLY;
			}
			if (operator == null || !operator.isMulti() || beforeFirstParam) {
				// {#case BREAK ON OFF |}
				return CompletionCaseResult.NONE;
			}
			// {#case in ON OFF |}
			return CompletionCaseResult.FIELD_ONLY;
		}
		return CompletionCaseResult.NONE;
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

}

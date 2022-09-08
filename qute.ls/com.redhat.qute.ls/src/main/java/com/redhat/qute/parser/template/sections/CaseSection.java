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
import java.util.LinkedHashSet;
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

	static {
		caseOperators = new LinkedHashMap<>();
		registerOperator("gt", "TODO DOC", false, ">"); // greater than
		registerOperator("ge", "TODO DOC", false, ">="); // greater than or equal to
		registerOperator("lt", "TODO DOC", false, "<"); // less than
		registerOperator("le", "TODO DOC", false, "<="); // less than or equal to
		registerOperator("not", "TODO DOC", false, "ne", "!="); // not equals
		registerOperator("in", "TODO DOC", true); // Is in
		registerOperator("ni", "TODO DOC", true, "!in"); // Is not in
	}

	private static void registerOperator(String name, String documentation, boolean isMulti, String... aliases) {
		CaseOperator operator = new CaseOperator(name, documentation, null, isMulti);
		caseOperators.put(operator.getName(), operator);
		if (aliases != null) {
			for (String alias : aliases) {
				CaseOperator aliasOperator = operator = new CaseOperator(name, documentation, null, isMulti);
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
}

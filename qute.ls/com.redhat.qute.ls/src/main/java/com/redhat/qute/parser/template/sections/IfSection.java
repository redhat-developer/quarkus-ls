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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.redhat.qute.parser.condition.ConditionExpression;
import com.redhat.qute.parser.condition.ConditionParser;
import com.redhat.qute.parser.template.ASTVisitor;
import com.redhat.qute.parser.template.Operator;
import com.redhat.qute.parser.template.Parameter;
import com.redhat.qute.parser.template.Section;
import com.redhat.qute.parser.template.SectionKind;

/**
 * If section AST node.
 * 
 * <code>
 	{#if item.price > 10} 
  		{item.name}
	{/if}
 * </code>
 * 
 * @author Angelo ZERR
 * 
 * @see https://quarkus.io/guides/qute-reference#if_section
 */
public class IfSection extends Section {

	public static final String TAG = "if";

	private static final Map<String, Operator> operators;

	static {
		operators = new LinkedHashMap<>();
		// https://quarkus.io/guides/qute-reference#if_section
		registerOperator("!", "TODO DOC"); // logical complement
		registerOperator("gt", "TODO DOC", ">"); // greater than
		registerOperator("ge", "TODO DOC", ">="); // greater than or equal to
		registerOperator("lt", "TODO DOC", "<"); // less than
		registerOperator("le", "TODO DOC", "<="); // less than or equal to
		registerOperator("eq", "TODO DOC", "==", "is"); // equals
		registerOperator("ne", "TODO DOC", "!="); // not equals
		registerOperator("&&", "TODO DOC", "and"); // logical AND (short-circuiting)
		registerOperator("||", "TODO DOC", "or"); // logical OR (short-circuiting)
	}

	public IfSection(int start, int end) {
		super(TAG, start, end);
	}

	private static void registerOperator(String name, String documentation, String... aliases) {
		Operator operator = new Operator(name, documentation, null);
		operators.put(operator.getName(), operator);
		if (aliases != null) {
			for (String alias : aliases) {
				Operator aliasOperator = new Operator(alias, documentation, name);
				operators.put(alias, aliasOperator);
			}
		}
	}

	@Override
	public SectionKind getSectionKind() {
		return SectionKind.IF;
	}

	@Override
	public List<SectionKind> getBlockLabels() {
		return Collections.singletonList(SectionKind.ELSE);
	}

	@Override
	protected List<Parameter> collectParameters() {
		ConditionExpression conditionExpression = ConditionParser.parse(this, getOwnerTemplate().getCancelChecker());
		return conditionExpression.getAllParameters();
	}

	@Override
	protected void initializeParameters(List<Parameter> parameters) {
		// All parameters can have expression (ex : {#if age=10} except operators
		boolean shouldBeAnOperator = false;
		for (Parameter parameter : parameters) {
			parameter.setCanHaveExpression(!shouldBeAnOperator);
			shouldBeAnOperator = !shouldBeAnOperator;
		}
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
	public boolean isValidOperator(String partName) {
		return operators.containsKey(partName);
	}

	@Override
	public Collection<Operator> getAllowedOperators() {
		return operators.values();
	}
}

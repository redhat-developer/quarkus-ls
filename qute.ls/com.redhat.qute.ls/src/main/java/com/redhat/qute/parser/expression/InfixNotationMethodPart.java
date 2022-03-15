/*******************************************************************************
* Copyright (c) 2022 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.parser.expression;

import java.util.HashMap;
import java.util.Map;

/**
 * Method part in Infix notation context.
 * 
 * <code>
 * 
 *  {name methodName 'param'} 
 * 
 * </code>
 * 
 * @author Angelo ZERR
 * 
 * @see https://quarkus.io/guides/qute-reference#virtual_methods
 */
public class InfixNotationMethodPart extends MethodPart {

	private static final Map<String, String> operators;

	static {
		// https://quarkus.io/guides/qute-reference#built-in-resolvers
		operators = new HashMap<>();
		// Elvis Operator: {person.name ?: 'John'}, {person.name or 'John'},
		// {person.name.or('John')}
		operators.put("?:", "or");
		// Ternary Operator: {item.isActive ? item.name : 'Inactive item'} outputs the
		// value of item.name if item.isActive resolves to true.
		operators.put("?", "ifTruthy");
		operators.put(":", null);
		// Logical AND Operator: {person.isActive && person.hasStyle}
		operators.put("&&", null);
		// Logical OR Operator: {person.isActive || person.hasStyle}
		operators.put("||", null);
	}

	public InfixNotationMethodPart(int start, int end) {
		super(start, end);
	}

	@Override
	public boolean hasOpenBracket() {
		return true;
	}

	@Override
	public boolean hasCloseBracket() {
		return true;
	}

	@Override
	public int getStartParametersOffset() {
		// {name or |param}
		return super.getEndName() + 1;
	}

	@Override
	public int getEndParametersOffset() {
		// {name or param|}
		return super.getEnd();
	}

	@Override
	public boolean isInfixNotation() {
		return true;
	}

	/**
	 * Returns true if method part used in infix notation is an operator (ex : ?:)
	 * and false otherwise.
	 * 
	 * @see https://quarkus.io/guides/qute-reference#built-in-resolvers
	 */
	@Override
	public boolean isOperator() {
		return operators.containsKey(getPartName());
	}
}

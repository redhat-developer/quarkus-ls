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
package com.redhat.qute.parser.condition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.redhat.qute.parser.parameter.ParameterParser;
import com.redhat.qute.parser.template.Parameter;
import com.redhat.qute.parser.template.ParametersContainer;

/**
 * Expression used in #if section used to store each offsets of start, end
 * bracket.
 * 
 * @author Angelo ZERR
 *
 */
public class ConditionExpression {

	private final int start;
	private final int end;
	private final ParametersContainer container;

	private final List<Integer> offsets;

	public ConditionExpression(int start, int end, ParametersContainer container, Object object) {
		this.start = start;
		this.end = end;
		this.container = container;
		this.offsets = new ArrayList<>();
	}

	public int getStart() {
		return start;
	}

	public int getEnd() {
		return end;
	}

	public void setStartBracket(int startBracket) {
		offsets.add(startBracket);
	}

	public void setEndBracket(int endBracket) {
		offsets.add(endBracket);
	}

	/**
	 * Returns the all parameters declared in the condition expression.
	 * 
	 * @return the all parameters declared in the condition expression.
	 */
	public List<Parameter> getAllParameters() {
		if (offsets.isEmpty()) {
			return parseParameters(start, end);
		}
		List<Parameter> allParameters = new ArrayList<Parameter>();
		int start = this.start;
		for (Integer offset : offsets) {
			allParameters.addAll(parseParameters(start, offset));
			start = offset;
		}
		allParameters.addAll(parseParameters(start, end));
		return allParameters;
	}

	public List<Parameter> parseParameters(int start, int end) {
		String text = container.getTemplateContent();
		char bracket = text.charAt(start);
		if (bracket == '(' || bracket == ')') {
			start++;
		}
		if (start >= end) {
			return Collections.emptyList();
		}
		return ParameterParser.parse(start, end, container, false, false);
	}
}

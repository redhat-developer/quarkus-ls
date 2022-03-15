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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.redhat.qute.parser.StringParametersContainer;
import com.redhat.qute.parser.template.Parameter;

/**
 * Condition parser tests.
 * 
 * @author Angelo ZERR
 *
 */
public class ConditionParserTest {

	@Test
	public void composite() {
		StringParametersContainer content = new StringParametersContainer("(a > b or (c > d and e > f)) or g > h");
		ConditionExpression expression = ConditionParser.parse(content, null);
		assertEquals(0, expression.getStart());
		assertEquals(37, expression.getEnd());

		// Parameters
		List<Parameter> allParameters = expression.getAllParameters();
		assertEquals(15, allParameters.size());
		List<String> allNames = allParameters //
				.stream() //
				.map(p -> p.getName()).collect(Collectors.toList());
		assertEquals(Arrays.asList("a", ">", "b", "or", "c", ">", "d", "and", "e", ">", "f", "or", "g", ">", "h"),
				allNames);
	}

	@Test
	public void composite2() {
		StringParametersContainer content = new StringParametersContainer("a > b or (c > d and e > f) or g > h");
		ConditionExpression expression = ConditionParser.parse(content, null);
		assertEquals(0, expression.getStart());
		assertEquals(35, expression.getEnd());

		// Parameters
		List<Parameter> allParameters = expression.getAllParameters();
		assertEquals(15, allParameters.size());
		List<String> allNames = allParameters //
				.stream() //
				.map(p -> p.getName()).collect(Collectors.toList());
		assertEquals(Arrays.asList("a", ">", "b", "or", "c", ">", "d", "and", "e", ">", "f", "or", "g", ">", "h"),
				allNames);
	}

	@Test
	public void composite3() {
		StringParametersContainer content = new StringParametersContainer("(true) && (true)");
		ConditionExpression expression = ConditionParser.parse(content, null);
		assertEquals(0, expression.getStart());
		assertEquals(16, expression.getEnd());

		// Parameters
		List<Parameter> allParameters = expression.getAllParameters();
		assertEquals(3, allParameters.size());
		List<String> allNames = allParameters //
				.stream() //
				.map(p -> p.getName()).collect(Collectors.toList());
		assertEquals(Arrays.asList("true", "&&", "true"), allNames);
	}

}

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
package com.redhat.qute.parser.parameter;

import java.util.ArrayList;
import java.util.List;

import com.redhat.qute.parser.CancelChecker;
import com.redhat.qute.parser.parameter.scanner.ParameterScanner;
import com.redhat.qute.parser.parameter.scanner.TokenType;
import com.redhat.qute.parser.template.Parameter;
import com.redhat.qute.parser.template.ParametersContainer;

public class ParameterParser {

	private static CancelChecker DEFAULT_CANCEL_CHECKER = () -> {
	};

	public static List<Parameter> parse(ParametersContainer container, boolean methodParameters,
			boolean splitWithEquals) {
		int start = container.getStartParametersOffset();
		int end = container.getEndParametersOffset();
		return parse(start, end, container, methodParameters, splitWithEquals);
	}

	public static List<Parameter> parse(int start, int end, ParametersContainer container, boolean methodParameters,
			boolean splitWithEquals) {
		CancelChecker cancelChecker = container.getCancelChecker();
		if (cancelChecker == null) {
			cancelChecker = DEFAULT_CANCEL_CHECKER;
		}
		String text = container.getTemplateContent();
		ParameterScanner scanner = ParameterScanner.createScanner(text, start, end, methodParameters, splitWithEquals);
		TokenType token = scanner.scan();
		List<Parameter> parameters = new ArrayList<>();
		Parameter currentParameter = null;
		while (token != TokenType.EOS) {
			cancelChecker.checkCanceled();
			int tokenOffset = scanner.getTokenOffset();
			int tokenEnd = scanner.getTokenEnd();
			switch (token) {
			case Whitespace:
				currentParameter = null;
				break;
			case ParameterName:
				currentParameter = new Parameter(tokenOffset, tokenEnd);
				currentParameter.setParameterParent(container);
				parameters.add(currentParameter);
				break;
			case Assign:
				if (currentParameter != null) {
					currentParameter.setAssignOffset(tokenOffset);
				}
				break;
			case ParameterValue:
				if (currentParameter != null) {
					// current parameter can be null when parameter name is not defined
					// ex : {#let =123} (instead of {#let name=123})
					currentParameter.setStartValue(tokenOffset);
					currentParameter.setEndValue(tokenEnd);
				}
				break;
			default:
			}
			token = scanner.scan();
		}
		return parameters;
	}
}

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

import com.redhat.qute.parser.CancelChecker;
import com.redhat.qute.parser.condition.scanner.ConditionScanner;
import com.redhat.qute.parser.condition.scanner.TokenType;
import com.redhat.qute.parser.template.ParametersContainer;

/**
 * Condition expression (used in #if) parser.
 * 
 * @author Angelo ZERR
 *
 */
public class ConditionParser {

	private static CancelChecker DEFAULT_CANCEL_CHECKER = () -> {
	};

	public static ConditionExpression parse(ParametersContainer container, CancelChecker cancelChecker) {
		if (cancelChecker == null) {
			cancelChecker = DEFAULT_CANCEL_CHECKER;
		}
		String text = container.getTemplateContent();
		int start = container.getStartParametersOffset();
		int end = container.getEndParametersOffset();
		ConditionScanner scanner = ConditionScanner.createScanner(text, start, end);
		TokenType token = scanner.scan();
		ConditionExpression currentCondition = new ConditionExpression(start, end, container, null);
		while (token != TokenType.EOS) {
			cancelChecker.checkCanceled();
			int tokenOffset = scanner.getTokenOffset();
			switch (token) {
			case StartBracketCondition:
				// |(a > b)
				currentCondition.setStartBracket(tokenOffset);
				break;
			case EndBracketCondition:
				// (a > b|)
				currentCondition.setEndBracket(tokenOffset);
				break;
			default:
			}
			token = scanner.scan();
		}
		return currentCondition;
	}
}

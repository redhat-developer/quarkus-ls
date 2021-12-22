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
package com.redhat.qute.parser.expression;

import java.util.ArrayList;
import java.util.List;

import com.redhat.qute.parser.CancelChecker;
import com.redhat.qute.parser.expression.Parts.PartKind;
import com.redhat.qute.parser.expression.scanner.ExpressionScanner;
import com.redhat.qute.parser.expression.scanner.TokenType;
import com.redhat.qute.parser.template.Expression;
import com.redhat.qute.parser.template.Node;
import com.redhat.qute.parser.template.Template;

/**
 * Qute expression content parser.
 * 
 * @author Angelo ZERR
 *
 */
public class ExpressionParser {

	private static CancelChecker DEFAULT_CANCEL_CHECKER = () -> {
	};

	/**
	 * Returns the parsing result of the given expression content.
	 * 
	 * @param expression    the content to parse.
	 * @param cancelChecker the cancel checker.
	 * 
	 * @return the parsing result of the given expression content.
	 */
	public static List<Node> parse(Expression expression, CancelChecker cancelChecker) {
		if (cancelChecker == null) {
			cancelChecker = DEFAULT_CANCEL_CHECKER;
		}
		Template template = expression.getOwnerTemplate();
		String text = template.getText();
		int start = expression.getStartContentOffset();
		int end = expression.getEndContentOffset();
		ExpressionScanner scanner = ExpressionScanner.createScanner(text, start, end);
		TokenType token = scanner.scan();
		List<Node> expressionContent = new ArrayList<>();
		Parts currentParts = null;
		while (token != TokenType.EOS) {
			cancelChecker.checkCanceled();
			int tokenOffset = scanner.getTokenOffset();
			int tokenEnd = scanner.getTokenEnd();
			switch (token) {
			case Whitespace:
				currentParts = null;
				break;
			case NamespacePart:
				currentParts = new Parts(tokenOffset, tokenEnd);
				currentParts.setExpressionParent(expression);
				expressionContent.add(currentParts);
				NamespacePart namespacePart = new NamespacePart(tokenOffset, tokenEnd);
				currentParts.addPart(namespacePart);
				break;
			case ObjectPart:
				if (!(currentParts != null && currentParts.getChildCount() == 1
						&& currentParts.getChild(0).getPartKind() == PartKind.Namespace)) {
					currentParts = new Parts(tokenOffset, tokenEnd);
					currentParts.setExpressionParent(expression);
					expressionContent.add(currentParts);
				}
				if (currentParts != null) {
					ObjectPart objectPart = new ObjectPart(tokenOffset, tokenEnd);
					currentParts.addPart(objectPart);
				}
				break;
			case PropertyPart:
				if (currentParts != null) {
					PropertyPart propertyPart = new PropertyPart(tokenOffset, tokenEnd);
					currentParts.addPart(propertyPart);
				}
				break;
			case MethodPart:
				if (currentParts != null) {
					MethodPart methodPart = new MethodPart(tokenOffset, tokenEnd);
					currentParts.addPart(methodPart);
				}
				break;
			case Dot:
				if (currentParts != null) {
					currentParts.addDot(tokenOffset);
				}
				break;
			case ColonSpace:
				if (currentParts != null) {
					currentParts.addColonSpace(tokenOffset);
				}
				break;
			case OpenBracket:
				if (currentParts != null) {
					Node last = currentParts.getLastChild();
					if (last instanceof MethodPart) {
						((MethodPart) last).setOpenBracket(tokenOffset);
					}
				}
				break;
			case CloseBracket:
				if (currentParts != null) {
					Node last = currentParts.getLastChild();
					if (last instanceof MethodPart) {
						((MethodPart) last).setCloseBracket(tokenOffset);
					}
				}
				break;
			case StartString:
			case String:
			case EndString:
				// ignore string tokens
				break;
			default:
				currentParts = null;
				break;
			}
			token = scanner.scan();
		}
		// adjust end offset for the current parts
		if (currentParts != null) {
			currentParts.setEnd(end);
			Node last = currentParts.getLastChild();
			if (last instanceof MethodPart) {
				if (!last.isClosed()) {
					// the current method is not closed with ')', adjust end method with the end
					// offset.
					((MethodPart) last).setEnd(end);
				}
			}
		}
		return expressionContent;
	}

}

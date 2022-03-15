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
	 * @param expression              the content to parse.
	 * @param canSupportInfixNotation true if expression can support infix notation
	 *                                and false otherwise.
	 * @param cancelChecker           the cancel checker.
	 * 
	 * @return the parsing result of the given expression content.
	 */
	public static List<Node> parse(Expression expression, boolean canSupportInfixNotation,
			CancelChecker cancelChecker) {
		if (cancelChecker == null) {
			cancelChecker = DEFAULT_CANCEL_CHECKER;
		}
		Template template = expression.getOwnerTemplate();
		String text = template.getText();
		int start = expression.getStartContentOffset();
		int end = expression.getEndContentOffset();
		ExpressionScanner scanner = ExpressionScanner.createScanner(text, canSupportInfixNotation, start, end);
		TokenType token = scanner.scan();
		List<Node> expressionContent = new ArrayList<>();
		Parts currentParts = null;
		while (token != TokenType.EOS) {
			cancelChecker.checkCanceled();
			int tokenOffset = scanner.getTokenOffset();
			int tokenEnd = scanner.getTokenEnd();
			switch (token) {
			case Whitespace:
				// In infix notation there is an one parts, because space means:
				// - the call of a method (ex : {name |or}
				// - the call of a parameter method (ex : {name or |'param'}
				if (!canSupportInfixNotation) {
					// Not in infix notation context, create a new parts
					currentParts = null;
				}
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
			case InfixMethodPart:
				if (currentParts != null) {
					MethodPart methodPart = new InfixNotationMethodPart(tokenOffset, tokenEnd);
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
			case InfixParameter:
			case StartString:
			case String:
			case EndString:
				// Adjust end of method part with the end of the infix parameter (here 1)
				// foo charAt 1|
				adjustEndOfInfixNotationMethod(currentParts, scanner, tokenEnd);
				break;
			default:
				currentParts = null;
				break;
			}
			token = scanner.scan();			
		}
		// adjust end offset for the current parts
		if (currentParts != null) {
			Node last = currentParts.getLastChild();
			int endParts = currentParts.getEnd();
			char c = text.charAt(endParts -1);
			if (c != '.') {
				// not in case {item.     }
				currentParts.setEnd(end);
				if (last instanceof MethodPart) {
					if (!last.isClosed()) {
						// the current method is not closed with ')', adjust end method with the end
						// offset.
						((MethodPart) last).setEnd(end);
					}
				}
			}
		}
		return expressionContent;
	}

	public static void adjustEndOfInfixNotationMethod(Parts currentParts, ExpressionScanner scanner, int tokenEnd) {
		if (currentParts != null/* && scanner.isInInfixNotation() */) {
			Node last = currentParts.getLastChild();
			if (last instanceof MethodPart) {
				MethodPart methodPart = (MethodPart) last;
				methodPart.setEnd(tokenEnd);
			}
		}
	}

}

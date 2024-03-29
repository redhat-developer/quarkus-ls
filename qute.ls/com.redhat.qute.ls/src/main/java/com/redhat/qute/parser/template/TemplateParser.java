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
package com.redhat.qute.parser.template;

import java.util.Objects;

import com.redhat.qute.ls.commons.TextDocument;
import com.redhat.qute.parser.CancelChecker;
import com.redhat.qute.parser.scanner.Scanner;
import com.redhat.qute.parser.template.scanner.ScannerState;
import com.redhat.qute.parser.template.scanner.TemplateScanner;
import com.redhat.qute.parser.template.scanner.TokenType;
import com.redhat.qute.parser.template.sections.CaseSection;
import com.redhat.qute.parser.template.sections.DefaultSectionFactory;
import com.redhat.qute.parser.template.sections.ElseSection;
import com.redhat.qute.parser.template.sections.IsSection;
import com.redhat.qute.parser.template.sections.SectionFactory;

/**
 * The Qute template parser.
 * 
 * @author Angelo ZERR
 *
 */
public class TemplateParser {

	private static final CancelChecker DEFAULT_CANCEL_CHECKER = () -> {
	};

	private static final SectionFactory DEFAULT_SECTION_FACTORY = new DefaultSectionFactory();

	public static Template parse(String content, String uri) {
		return parse(content, uri, DEFAULT_CANCEL_CHECKER);
	}

	public static Template parse(String text, String uri, CancelChecker cancelChecker) {
		return parse(new TextDocument(text, uri), cancelChecker);
	}

	public static Template parse(TextDocument textDocument, CancelChecker cancelChecker) {
		return parse(textDocument, DEFAULT_SECTION_FACTORY, cancelChecker);
	}

	public static Template parse(TextDocument textDocument, SectionFactory sectionFactory,
			CancelChecker cancelChecker) {
		if (cancelChecker == null) {
			cancelChecker = DEFAULT_CANCEL_CHECKER;
		}
		Template template = new Template(textDocument);
		template.setCancelChecker(cancelChecker);

		Node curr = template;

		String content = textDocument.getText();
		int endTagOpenOffset = -1;
		int startSectionOffset = -1;
		int endSectionOffset = -1;
		Scanner<TokenType, ScannerState> scanner = TemplateScanner.createScanner(content);
		TokenType token = scanner.scan();
		while (token != TokenType.EOS) {
			cancelChecker.checkCanceled();

			curr = createSectionIfNeeded(startSectionOffset, endSectionOffset, curr, scanner, token, sectionFactory);
			startSectionOffset = -1;
			endSectionOffset = -1;

			switch (token) {

			case StartTagOpen: {
				if (!curr.isClosed() && curr.getParent() != null) {
					// The next node's parent (curr) is not closed at this point
					// so the node's parent (curr) will have its end position updated
					// to a newer end position.
					curr.setEnd(scanner.getTokenOffset());
				}
				if (canSwithToParentNode(curr)) {
					// The next node being considered is a child of 'curr'
					// and if 'curr' is already closed then 'curr' was not updated properly.
					curr = curr.getParent();
				}
				startSectionOffset = scanner.getTokenOffset();
				endSectionOffset = scanner.getTokenEnd();
				break;
			}

			case StartTag: {
				curr.setEnd(scanner.getTokenEnd());
				break;
			}

			case StartTagClose:
				if (curr.getKind() == NodeKind.Section) {
					Section section = (Section) curr;
					curr.setEnd(scanner.getTokenEnd()); // might be later set to end tag position
					section.setStartTagCloseOffset(scanner.getTokenOffset());

					// never enters isEmptyElement() is always false
					if (section.getTag() != null && isEmptyElement(section.getTag()) && curr.getParent() != null) {
						curr.setClosed(true);
						curr = curr.getParent();
					}
				}
				curr.setEnd(scanner.getTokenEnd());
				break;

			case EndTagOpen:
				endTagOpenOffset = scanner.getTokenOffset();
				curr.setEnd(endTagOpenOffset);
				break;

			case EndTag:
				// end tag (ex: {/if})
				String closeTag = scanner.getTokenText();
				Node current = curr;

				/**
				 * eg: <a><b><c></d> will set a,b,c end position to the start of |</d>
				 */
				while (!(curr.getKind() == NodeKind.Section && Objects.equals(((Section) curr).getTag(), closeTag))
						&& curr.getParent() != null) {
					curr.setEnd(endTagOpenOffset);
					curr = curr.getParent();
				}
				if (curr != template) {
					curr.setClosed(true);
					if (curr.getKind() == NodeKind.Section) {
						((Section) curr).setEndTagOpenOffset(endTagOpenOffset);
					}
					curr.setEnd(scanner.getTokenEnd());
				} else {
					// element open tag not found (ex: {#foo}) add a fake element which only has an
					// end tag (no start tag).
					Section section = sectionFactory.createSection(closeTag, scanner.getTokenOffset() - 2,
							scanner.getTokenEnd());
					section.setEndTagOpenOffset(endTagOpenOffset);
					current.addChild(section);
					curr = section;
				}
				break;

			case StartTagSelfClose:
				if (curr.getParent() != null) {
					curr.setClosed(true);
					Section section = (Section) curr;
					section.setSelfClosed(true);
					section.setStartTagCloseOffset(scanner.getTokenOffset());
					curr.setEnd(scanner.getTokenEnd());
					curr = curr.getParent();
				}
				break;

			case EndTagSelfClose:
				if (curr.getParent() != null) {
					Section section = (Section) curr;
					curr.setClosed(true);
					curr.setEnd(scanner.getTokenEnd());
					section.setEndTagOpenOffset(scanner.getTokenOffset());
					section.setEndTagCloseOffset(scanner.getTokenEnd() - 1);
					curr = curr.getParent();
				} else {
					Section section = sectionFactory.createSection("", scanner.getTokenOffset() - 2,
							scanner.getTokenEnd());
					section.setEndTagOpenOffset(scanner.getTokenOffset());
					section.setEndTagCloseOffset(scanner.getTokenEnd() - 1);
					curr.addChild(section);
					
				}
				break;

			case EndTagClose:
				if (curr.getParent() != null) {
					Section section = (Section) curr;
					curr.setEnd(scanner.getTokenEnd());
					section.setEndTagCloseOffset(scanner.getTokenOffset());
					curr = curr.getParent();
				}
				break;

			case StartExpression: {
				// In case the tag before the expression (curr) was not properly closed
				// curr should be set to the root node.
				if (curr.isClosed() && curr.getKind() != NodeKind.Template) {
					curr = curr.getParent();
				}
				int start = scanner.getTokenOffset();
				int end = scanner.getTokenEnd();
				Expression expression = new Expression(start, end);
				curr.addChild(expression);
				curr = expression;
				break;
			}

			case EndExpression: {
				int end = scanner.getTokenEnd();
				Expression expression = (Expression) curr;
				expression.setClosed(true);
				expression.setEnd(end);
				curr = curr.getParent();
				break;
			}

			case StartComment: {
				// In case the tag before the expression (curr) was not properly closed
				// curr should be set to the root node.
				if (curr.isClosed() && curr.getKind() != NodeKind.Template) {
					curr = curr.getParent();
				}
				int start = scanner.getTokenOffset();
				int end = scanner.getTokenEnd();
				Comment comment = new Comment(start, end);
				curr.addChild(comment);
				curr = comment;
				break;
			}

			case Comment: {
				Comment comment = (Comment) curr;
				comment.setStartContent(scanner.getTokenOffset());
				comment.setEndContent(scanner.getTokenEnd());
				break;
			}

			case EndComment: {
				int end = scanner.getTokenEnd();
				Comment comment = (Comment) curr;
				comment.setClosed(true);
				comment.setEnd(end);
				curr = curr.getParent();
				break;
			}

			case CDATATagOpen:
			case CDATAOldTagOpen: {
				// In case the tag before the expression (curr) was not properly closed
				// curr should be set to the root node.
				if (curr.isClosed() && curr.getKind() != NodeKind.Template) {
					curr = curr.getParent();
				}
				int start = scanner.getTokenOffset();
				int end = scanner.getTokenEnd();
				CData cdata = new CData(start, end);
				curr.addChild(cdata);
				curr = cdata;
				break;
			}

			case CDATAContent: {
				CData cdata = (CData) curr;
				cdata.setStartContent(scanner.getTokenOffset());
				cdata.setEndContent(scanner.getTokenEnd());
				break;
			}

			case CDATATagClose:
			case CDATAOldTagClose: {
				int end = scanner.getTokenEnd();
				CData cdata = (CData) curr;
				cdata.setClosed(true);
				cdata.setEnd(end);
				curr = curr.getParent();
				break;
			}

			case StartParameterDeclaration: {
				// In case the tag before the expression (curr) was not properly closed
				// curr should be set to the root node.
				if (curr.isClosed() && curr.getKind() != NodeKind.Template) {
					curr = curr.getParent();
				}
				int start = scanner.getTokenOffset();
				int end = scanner.getTokenEnd();
				ParameterDeclaration parameter = new ParameterDeclaration(start, end);
				curr.addChild(parameter);
				curr = parameter;
				break;
			}

			case EndParameterDeclaration: {
				int end = scanner.getTokenEnd();
				ParameterDeclaration parameter = (ParameterDeclaration) curr;
				parameter.setClosed(true);
				parameter.setEnd(end);
				curr = curr.getParent();
				break;
			}

			case Content: {
				int start = scanner.getTokenOffset();
				int end = scanner.getTokenEnd();
				Text text = new Text(start, end);
				curr.addChild(text);
				break;
			}
			default:
			}
			token = scanner.scan();
		}

		curr = createSectionIfNeeded(startSectionOffset, endSectionOffset, curr, scanner, token, sectionFactory);

		while (curr.getParent() != null) {
			curr.setEnd(content.length());
			curr = curr.getParent();
		}
		return template;
	}

	/**
	 * Returns true if the given node can switch to the parent node and false
	 * otherwise.
	 * 
	 * @param node the AST node.
	 * 
	 * @return true if the given node can switch to the parent node and false
	 *         otherwise.
	 */
	private static boolean canSwithToParentNode(Node node) {
		if (node.getKind() == NodeKind.ParameterDeclaration) {
			return true;
		}
		if (node.getKind() == NodeKind.Template) {
			return false;
		}
		return node.isClosed();
	}

	private static Node createSectionIfNeeded(int startSectionOffset, int endSectionOffset, Node curr,
			Scanner<TokenType, ScannerState> scanner, TokenType token, SectionFactory sectionFactory) {
		if (startSectionOffset != -1) {
			String tag = null;
			if (token == TokenType.StartTag) {
				tag = scanner.getTokenText();
			}
			Section section = sectionFactory.createSection(tag, startSectionOffset, endSectionOffset);
			section.setStartTagOpenOffset(startSectionOffset);
			curr.addChild(section);
			curr = section;
		}
		return curr;
	}

	private static boolean isEmptyElement(String tag) {
		return CaseSection.TAG.equals(tag) || ElseSection.TAG.equals(tag) || IsSection.TAG.equals(tag);
	}
}

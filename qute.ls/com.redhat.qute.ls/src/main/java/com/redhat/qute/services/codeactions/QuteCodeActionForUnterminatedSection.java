/*******************************************************************************
* Copyright (c) 2023 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.services.codeactions;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Range;

import com.redhat.qute.ls.commons.BadLocationException;
import com.redhat.qute.ls.commons.CodeActionFactory;
import com.redhat.qute.ls.commons.LineIndentInfo;
import com.redhat.qute.parser.template.Node;
import com.redhat.qute.parser.template.NodeKind;
import com.redhat.qute.parser.template.RangeOffset;
import com.redhat.qute.parser.template.Section;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.parser.validator.QuteSyntaxErrorCode;
import com.redhat.qute.utils.QutePositionUtility;

/**
 * Code actions for {@link QuteSyntaxErrorCode#UNTERMINATED_SECTION}.
 *
 */
public class QuteCodeActionForUnterminatedSection extends AbstractQuteCodeAction {

	private static final Logger LOGGER = Logger.getLogger(QuteCodeActionForUnterminatedSection.class.getName());

	@Override
	public void doCodeActions(CodeActionRequest request, List<CompletableFuture<Void>> codeActionResolveFutures,
			List<CodeAction> codeActions) {
		try {
			Node node = request.getCoveredNode();
			if (node == null) {
				return;
			}
			Section section = node.getKind() == NodeKind.Section ? (Section) node : null;
			if (section == null) {
				return;
			}
			Template template = request.getTemplate();
			Diagnostic diagnostic = request.getDiagnostic();

			int insertTextOffset = section.getStartTagCloseOffset() + 1;

			// If there is any content, insert after
			for (int i = section.getChildCount() - 1; i > 0; i--) {
				if (section.getChild(i).getKind() != NodeKind.Text) {
					insertTextOffset = section.getChild(i).getEnd();
				}
			}
			Range range = QutePositionUtility.createRange(new RangeOffset(insertTextOffset, insertTextOffset),
					template);

			Range startTagRange = QutePositionUtility.createRange(
					new RangeOffset(section.getStartTagOpenOffset(), section.getStartTagCloseOffset()),
					template);

			// ex: Insert expected {/if} end section
			CodeAction insertMissingEndSection = CodeActionFactory.replace(
					"Insert expected {/" + section.getTag() + "} end section", range,
					generateEndTag(template, section.getTag(), startTagRange), template.getTextDocument(),
					diagnostic);
			codeActions.add(insertMissingEndSection);
		} catch (BadLocationException e) {
			LOGGER.log(Level.SEVERE, "Creation of generate missing end section code action failed", e);
		}
	}

	private static String generateEndTag(Template template, String sectionName, Range range)
			throws BadLocationException {
		StringBuilder str = new StringBuilder();
		int lineNumber = range.getStart().getLine();
		LineIndentInfo indentInfo = template.lineIndentInfo(lineNumber);
		String lineDelimiter = indentInfo.getLineDelimiter();
		String whitespacesIndent = indentInfo.getWhitespacesIndent();
		str.append(lineDelimiter);
		str.append(whitespacesIndent);
		str.append("{/" + sectionName + "}");
		return str.toString();
	}

}

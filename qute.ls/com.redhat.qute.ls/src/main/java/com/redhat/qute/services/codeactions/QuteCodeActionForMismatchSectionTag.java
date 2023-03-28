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
import com.redhat.qute.parser.template.Node;
import com.redhat.qute.parser.template.NodeKind;
import com.redhat.qute.parser.template.RangeOffset;
import com.redhat.qute.parser.template.Section;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.parser.validator.QuteSyntaxErrorCode;
import com.redhat.qute.utils.QutePositionUtility;

/**
 * Code actions for
 * {@link QuteSyntaxErrorCode#SECTION_END_DOES_NOT_MATCH_START}.
 *
 */
public class QuteCodeActionForMismatchSectionTag extends AbstractQuteCodeAction {

	private static final Logger LOGGER = Logger.getLogger(QuteCodeActionForMismatchSectionTag.class.getName());

	@Override
	public void doCodeActions(CodeActionRequest request, List<CompletableFuture<Void>> codeActionResolveFutures,
			List<CodeAction> codeActions) {
		try {
			Node node = request.getCoveredNode();
			if (node == null) {
				return;
			}
			Section orphanSection = node.getKind() == NodeKind.Section ? (Section) node : null;
			if (orphanSection == null) {
				return;
			}
			String replaceEndSectionName = orphanSection.getParentSection().getTag();

			Template template = request.getTemplate();
			Diagnostic diagnostic = request.getDiagnostic();

			Range range = QutePositionUtility.createRange(
					new RangeOffset(orphanSection.getEndTagOpenOffset() + 2, orphanSection.getEndTagCloseOffset()),
					template);
			// ex: Replace mismatched section {/elsa} end tag with {/else}
			String message = "Replace mismatched section {/" + orphanSection.getTag() + "} end tag with {/"
					+ replaceEndSectionName + "}";
			CodeAction replaceMismatchedSectionEndTag = CodeActionFactory.replace(
					message, range, replaceEndSectionName, template.getTextDocument(), diagnostic);
			codeActions.add(replaceMismatchedSectionEndTag);
		} catch (BadLocationException e) {
			LOGGER.log(Level.SEVERE, "Creation of correcting mismatched section end tag code action failed", e);
		}
	}

}

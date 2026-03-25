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
package com.redhat.qute.services.commands;

import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.ExecuteCommandParams;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import com.redhat.qute.ls.api.QuteTemplateProvider;
import com.redhat.qute.parser.template.Node;
import com.redhat.qute.parser.template.NodeKind;
import com.redhat.qute.parser.template.Section;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.settings.SharedSettings;

/**
 * Qute Command "qute.refactor.surround.with" to support surround:
 * 
 * <ul>
 * <li>Surround with Section (Wrap)</li>
 * <li>Surround with Comments</li>
 * </ul>
 *
 */
public class QuteSurroundWithCommandHandler extends AbstractTemplateCommandHandler {
 
	public static final String COMMAND_ID = "qute.refactor.surround.with";

	public static class SurroundWithResponse {

		private TextEdit start;

		private TextEdit end;

		public SurroundWithResponse() {
		}

		public SurroundWithResponse(TextEdit start, TextEdit end) {
			this.start = start;
			this.end = end;
		}

		public TextEdit getStart() {
			return start;
		}

		public TextEdit getEnd() {
			return end;
		}
	}

	public static enum SurroundWithKind {
		section, //
		comments, //
		cdata;

		public static SurroundWithKind get(String kind) {
			return valueOf(kind);
		}
	}

	public QuteSurroundWithCommandHandler(QuteTemplateProvider templateProvider) {
		super(templateProvider);
	}

	@Override
	protected CompletableFuture<Object> executeCommand(Template template, ExecuteCommandParams params,
			SharedSettings sharedSettings, CancelChecker cancelChecker) throws Exception {
		// Get parameters
		Range selection = ArgumentsUtils.getArgAt(params, 1, Range.class);
		SurroundWithKind kind = SurroundWithKind.get(ArgumentsUtils.getArgAt(params, 2, String.class));

		// Surround process
		boolean emptySelection = selection.getStart().equals(selection.getEnd());
		StringBuilder startText = null;
		StringBuilder endText = null;
		Position startPos = selection.getStart();
		Position endPos = selection.getEnd();

		int offset = template.offsetAt(selection.getStart());
		Node node = template.findNodeAt(offset);

		// Adjust selection if needed
		if (emptySelection && node.getKind() == NodeKind.Section) {
			Section section = (Section) node;
			if (section.isInStartTagName(offset) || section.isInEndTagName(offset)) {
				startPos = template.positionAt(section.getStart());
				endPos = template
						.positionAt(section.isEndTagClosed() ? section.getEndTagCloseOffset() + 1 : section.getEnd());
			}
		}

		switch (kind) {
			case comments:
				startText = new StringBuilder("{!");
				endText = new StringBuilder("!}");

				break;
			case cdata:
				startText = new StringBuilder("{|");
				endText = new StringBuilder("|}");
				break;
			default:
				// Start Section
				startText = new StringBuilder("{#");
				startText.append("}");

				// End Section
				endText = new StringBuilder("{/");
				endText.append("}");
		}
		TextEdit start = new TextEdit(new Range(startPos, startPos), startText.toString());
		TextEdit end = new TextEdit(new Range(endPos, endPos), endText.toString());
		return CompletableFuture.completedFuture(new SurroundWithResponse(start, end));
	}
}

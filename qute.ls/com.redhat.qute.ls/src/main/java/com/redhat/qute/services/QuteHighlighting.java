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
package com.redhat.qute.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.lsp4j.DocumentHighlight;
import org.eclipse.lsp4j.DocumentHighlightKind;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import com.redhat.qute.ls.commons.BadLocationException;
import com.redhat.qute.parser.expression.ObjectPart;
import com.redhat.qute.parser.expression.Part;
import com.redhat.qute.parser.expression.Parts.PartKind;
import com.redhat.qute.parser.template.Node;
import com.redhat.qute.parser.template.Section;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.utils.QutePositionUtility;
import com.redhat.qute.utils.QuteSearchUtils;

/**
 * Qute highlighting support.
 *
 * @author Angelo ZERR
 *
 */
class QuteHighlighting {

	private static final Logger LOGGER = Logger.getLogger(QuteHighlighting.class.getName());

	public List<DocumentHighlight> findDocumentHighlights(Template template, Position position,
			CancelChecker cancelChecker) {
		try {
			List<DocumentHighlight> highlights = new ArrayList<>();

			int offset = template.offsetAt(position);
			Node node = template.findNodeAt(offset);
			if (node == null) {
				return Collections.emptyList();
			}
			node = QutePositionUtility.findBestNode(offset, node);
			switch (node.getKind()) {
			case ParameterDeclaration:
			case Parameter:
				highlightReferenceObjectPart(node, offset, highlights, cancelChecker);
				break;
			case ExpressionPart:
				Part part = (Part) node;
				if (part.getPartKind() == PartKind.Object) {
					highlightDeclaredObject((ObjectPart) part, highlights, cancelChecker);
				}
				break;
			case Section:
				higlightSection((Section) node, offset, position, highlights, cancelChecker);
				break;
			default:
			}
			return highlights;
		} catch (BadLocationException e) {
			LOGGER.log(Level.SEVERE, "In QuteHighlighting the client provided Position is at a BadLocation", e);
			return Collections.emptyList();
		}
	}

	private void highlightDeclaredObject(ObjectPart part, List<DocumentHighlight> highlights,
			CancelChecker cancelChecker) {
		QuteSearchUtils.searchDeclaredObject(part, //
				(referencedNode, referencedRange) -> {
					highlights.add(new DocumentHighlight(referencedRange,
							highlights.isEmpty() ? DocumentHighlightKind.Read : DocumentHighlightKind.Write));
				}, true, cancelChecker);
	}

	private static void highlightReferenceObjectPart(Node node, int offset, List<DocumentHighlight> highlights,
			CancelChecker cancelChecker) {
		QuteSearchUtils.searchReferencedObjects(node, offset, //
				(referencedNode, referencedRange) -> {
					highlights.add(new DocumentHighlight(referencedRange,
							highlights.isEmpty() ? DocumentHighlightKind.Write : DocumentHighlightKind.Read));
				}, true, cancelChecker);
	}

	private static void higlightSection(Section section, int offset, Position position,
			List<DocumentHighlight> highlights, CancelChecker cancelChecker) throws BadLocationException {
		if ((section.isInStartTagName(offset) && section.hasEndTag())
				|| (section.isInEndTagName(offset) && section.isInEndTagName(offset))) {
			Range startTagRange = QutePositionUtility.selectStartTagName(section);
			Range endTagRange = QutePositionUtility.selectEndTagName(section);
			fillHighlightsList(startTagRange, endTagRange, highlights);
		} else {
			highlightReferenceObjectPart(section, offset, highlights, cancelChecker);
		}
	}

	private static void fillHighlightsList(Range startTagRange, Range endTagRange, List<DocumentHighlight> result) {
		if (startTagRange != null) {
			result.add(new DocumentHighlight(startTagRange, DocumentHighlightKind.Read));
		}
		if (endTagRange != null) {
			result.add(new DocumentHighlight(endTagRange, DocumentHighlightKind.Read));
		}
	}

	public static boolean doesTagCoverPosition(Range startTagRange, Range endTagRange, Position position) {
		return startTagRange != null && covers(startTagRange, position)
				|| endTagRange != null && covers(endTagRange, position);
	}

	public static boolean covers(Range range, Position position) {
		return isBeforeOrEqual(range.getStart(), position) && isBeforeOrEqual(position, range.getEnd());
	}

	public static boolean isBeforeOrEqual(Position pos1, Position pos2) {
		return pos1.getLine() < pos2.getLine()
				|| (pos1.getLine() == pos2.getLine() && pos1.getCharacter() <= pos2.getCharacter());
	}

}

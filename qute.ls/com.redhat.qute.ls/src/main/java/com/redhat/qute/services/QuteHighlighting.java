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
import com.redhat.qute.parser.template.NodeKind;
import com.redhat.qute.parser.template.Parameter;
import com.redhat.qute.parser.template.Section;
import com.redhat.qute.parser.template.SectionKind;
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
					ObjectPart objectPart = (ObjectPart) part;
					highlightDeclaredObject(objectPart, highlights, cancelChecker);
					if (highlights.size() == 1 && part.isOptional()) {
						Parameter parameter = objectPart.getOwnerParameter();
						if (parameter != null && parameter.getOwnerSection() != null
								&& parameter.getOwnerSection().getSectionKind() == SectionKind.IF) {
							// Case with {#if fo|o??} {foo}
							// In this case, the foo?? has the same behavior than a parameter declaration,
							// search the
							// referenced object parts
							highlights.clear();
							highlightReferenceObjectPart(parameter, offset, highlights, cancelChecker);
						}
					}
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
		if (section.isInStartTagName(offset) || section.isInEndTagName(offset)) {
			highlightSectionTag(section, highlights);
			if (!section.getBlockLabels().isEmpty()) {
				// The section can have nested block (ex #for can have #else)
				for (Node node : section.getChildren()) {
					if (node.getKind() == NodeKind.Section
							&& section.getBlockLabels().contains(((Section) node).getSectionKind())) {
						Section nestedBlock = (Section) node;
						highlightSectionTag(nestedBlock, highlights);
					}
				}
			} else {
				// Get parent section
				Section parentSection = section.getParentSection();
				if (parentSection != null && parentSection.getBlockLabels().contains(section.getSectionKind())) {
					highlightSectionTag(parentSection, highlights);
				}
			}
		} else {
			highlightReferenceObjectPart(section, offset, highlights, cancelChecker);
		}
	}

	public static void highlightSectionTag(Section section, List<DocumentHighlight> highlights) {
		Range startTagRange = QutePositionUtility.selectStartTagName(section);
		highlight(startTagRange, DocumentHighlightKind.Read, highlights);
		Range endTagRange = section.hasEndTag() ? QutePositionUtility.selectEndTagName(section) : null;
		highlight(endTagRange, DocumentHighlightKind.Read, highlights);
	}

	private static void highlight(Range range, DocumentHighlightKind kind, List<DocumentHighlight> result) {
		if (range != null) {
			result.add(new DocumentHighlight(range, kind));
		}
	}

}

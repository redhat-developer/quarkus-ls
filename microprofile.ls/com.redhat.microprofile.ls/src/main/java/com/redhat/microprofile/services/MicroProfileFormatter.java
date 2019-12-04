/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.microprofile.services;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.util.Ranges;

import com.redhat.microprofile.ls.commons.BadLocationException;
import com.redhat.microprofile.ls.commons.TextDocument;
import com.redhat.microprofile.model.Node;
import com.redhat.microprofile.model.PropertiesModel;
import com.redhat.microprofile.model.Property;
import com.redhat.microprofile.model.Node.NodeType;
import com.redhat.microprofile.settings.MicroProfileFormattingSettings;
import com.redhat.microprofile.utils.PositionUtils;

/**
 * Provides formatting support for an application.properties file
 * 
 * Formatting rules:
 * Remove extra newlines in between properties
 * Remove whitespaces before and after properties
 * Add/remove spacing surrounding the equals sign, depending on <code>QuarkusFormattingSettings</code>
 */
class MicroProfileFormatter {

	private static final Logger LOGGER = Logger.getLogger(MicroProfileFormatter.class.getName());

	/**
	 * Returns a <code>List<TextEdit></code> containing one <code>TextEdit</code> that formats the
	 * the application.properties file represented by <code>document</code>.
	 *
	 * @param document           the properties model document
	 * @param formattingSettings the client's formatting settings
	 * @return a <code>List<TextEdit></code> that formats the
	 * the application.properties file represented by <code>document</code>.
	 */
	public List<? extends TextEdit> format(PropertiesModel document, MicroProfileFormattingSettings formattingSettings) {

		Range fullRange = PositionUtils.createRange(document);
		
		if (fullRange == null) {
			return null;
		}
		return format(document, fullRange, formattingSettings);
	}

	/**
	 * Returns a <code>List<TextEdit></code> containing one <code>TextEdit</code> that formats the
	 * the application.properties file represented by <code>document</code>, within the lines 
	 * covered by the specified <code>range</code>.
	 
	 * @param document           the properties model document
	 * @param range              the range specifying the lines to format
	 * @param formattingSettings the client's <code>QuarkusFormattingSettings</code>
	 * @return Returns a <code>List<TextEdit></code> that formats the
	 * the application.properties file represented by <code>document</code>, within the lines
	 * covered by the specified <code>range</code>.
	 */
	public List<? extends TextEdit> format(PropertiesModel document, Range range,
			MicroProfileFormattingSettings formattingSettings) {
		
		try {
			enlargeRangeToGutters(range, document.getDocument());
		} catch (BadLocationException e) {
			LOGGER.log(Level.SEVERE, "Formatting failed due to BadLocation", e);
			return null;
		}

		StringBuilder builder = new StringBuilder();

		for (Node child: document.getChildren()) {
			if (child.getNodeType() == NodeType.PROPERTY && isNodeInsideRange(child, range)) {
				Property property = (Property) child;
				formatPropertyAndAdd(property, formattingSettings.isSurroundEqualsWithSpaces(), builder);
			} else if (child.getNodeType() == NodeType.COMMENTS) {
				builder.append(child.getText());
				builder.append("\n");
			}
		}

		List<TextEdit> edits = new ArrayList<>(1);
		edits.add(new TextEdit(range, builder.toString().trim()));
		return edits;
	}

	/**
	 * Enlarges <code>range</code> so that the range's <code>start</code> and
	 * <code>end</code> positions are located at the start of the line and end
	 * of the line respectively.
	 * @param range        the <code>Range</code> to enlarge
	 * @param textDocument the <code>TextDocument</code> for <code>range</code>
	 * @throws BadLocationException
	 */
	private static void enlargeRangeToGutters(Range range, TextDocument textDocument) throws BadLocationException {
		Position start = range.getStart();
		Position end = range.getEnd();
		
		start.setCharacter(0);

		if (end.getCharacter() == 0 && end.getLine() > 0) {
			end.setLine(end.getLine() - 1);
		}

		end.setCharacter(textDocument.lineText(end.getLine()).length());
	}

	/**
	 * Appends the formatted <code>String</code> representing <code>property</code> to
	 * <code>builder</code>.
	 *
	 * @param property     the <code>Property</code> to format and add
	 * @param insertSpaces determines whether to insert spaces that surround the equals sign, if the equals sign exists
	 * @param builder      the <code>StringBuilder</code> that accumulates the formatted properties
	 */
	private static void formatPropertyAndAdd(Property property, boolean insertSpaces, StringBuilder builder) {
		
		boolean keyExists = property.getKey() != null;
		boolean delimiterExists = property.getDelimiterAssign() != null;
		boolean valueExists = property.getValue() != null;

		if (!delimiterExists && !valueExists) {
			builder.append(property.getText().trim());
			builder.append("\n");
			return;
		}

		if (keyExists) {
			builder.append(property.getKey().getText());
		}

		if (delimiterExists) {
			if (insertSpaces) {
				builder.append(" ");
			}
			builder.append("=");
		}

		if (valueExists) {
			if (insertSpaces) {
				builder.append(" ");
			}
			builder.append(property.getValue().getValue());
		}

		if (keyExists || delimiterExists || valueExists) {
			builder.append("\n");
		}
	}

	/**
	 * Returns true only if the positioning of <code>node</code> is inside
	 * or is equal to <code>range</code>
	 * @param node  the <code>Node</code> to check the overlap for
	 * @param range the <code>Range</code> to check the overlap with
	 * @return
	 */
	private static boolean isNodeInsideRange(Node node, Range range) {
		return Ranges.containsRange(range, PositionUtils.createRange(node));
	}

}
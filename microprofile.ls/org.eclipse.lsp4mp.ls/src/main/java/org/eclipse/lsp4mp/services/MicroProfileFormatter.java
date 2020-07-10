/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lsp4mp.services;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4mp.ls.commons.BadLocationException;
import org.eclipse.lsp4mp.ls.commons.TextDocument;
import org.eclipse.lsp4mp.model.Node;
import org.eclipse.lsp4mp.model.PropertiesModel;
import org.eclipse.lsp4mp.model.Property;
import org.eclipse.lsp4mp.model.Node.NodeType;
import org.eclipse.lsp4mp.settings.MicroProfileFormattingSettings;
import org.eclipse.lsp4mp.utils.PositionUtils;

/**
 * Provides formatting support for an application.properties file
 * 
 * Formatting rules:
 * <ul>
 * 
 * <li>Remove extra newlines in between properties</li>
 * <li>Remove whitespaces before and after properties</li>
 * <li>Add/remove spacing surrounding the equals sign, depending on
 * <code>MicroProfileFormattingSettings</code></li>
 * </ul>
 * 
 */
class MicroProfileFormatter {

	private static final Logger LOGGER = Logger.getLogger(MicroProfileFormatter.class.getName());

	/**
	 * Returns a <code>List<TextEdit></code> containing one <code>TextEdit</code>
	 * that formats the the application.properties file represented by
	 * <code>document</code>.
	 *
	 * @param document           the properties model document
	 * @param formattingSettings the client's formatting settings
	 * @return a <code>List<TextEdit></code> that formats the the
	 *         application.properties file represented by <code>document</code>.
	 */
	public List<? extends TextEdit> format(PropertiesModel document,
			MicroProfileFormattingSettings formattingSettings) {

		Range fullRange = PositionUtils.createRange(document);

		if (fullRange == null) {
			return null;
		}
		return format(document, fullRange, formattingSettings);
	}

	/**
	 * Returns a <code>List<TextEdit></code> containing one <code>TextEdit</code>
	 * that formats the the application.properties file represented by
	 * <code>document</code>, within the lines covered by the specified
	 * <code>range</code>.
	 * 
	 * @param document           the properties model document
	 * @param range              the range specifying the lines to format
	 * @param formattingSettings the client's <code>MicroProfileFormattingSettings</code>
	 * @return Returns a <code>List<TextEdit></code> that formats the the
	 *         application.properties file represented by <code>document</code>,
	 *         within the lines covered by the specified <code>range</code>.
	 */
	public List<? extends TextEdit> format(PropertiesModel document, Range range,
			MicroProfileFormattingSettings formattingSettings) {

		int startOffset = -1;
		int endOffset = -1;
		String lineDelimiter = null;
		try {
			enlargeRangeToGutters(range, document.getDocument());
			startOffset = document.offsetAt(range.getStart());
			endOffset = document.offsetAt(range.getEnd());
			lineDelimiter = document.getDocument().lineDelimiter(range.getStart().getLine());
		} catch (BadLocationException e) {
			LOGGER.log(Level.SEVERE, "Formatting failed due to BadLocation", e);
			return null;
		}

		StringBuilder formattedContent = new StringBuilder();

		for (Node child : document.getChildren()) {
			if (child.getStart() >= startOffset && child.getEnd() <= endOffset) {
				if (child.getNodeType() == NodeType.PROPERTY) {
					Property property = (Property) child;
					formatPropertyAndAdd(property, formattingSettings.isSurroundEqualsWithSpaces(), lineDelimiter,
							formattedContent);
				} else if (child.getNodeType() == NodeType.COMMENTS) {
					formattedContent.append(child.getText());
					formattedContent.append(lineDelimiter);
				}
			}
		}

		return Arrays.asList(new TextEdit(range, formattedContent.toString().trim()));
	}

	/**
	 * Enlarges <code>range</code> so that the range's <code>start</code> and
	 * <code>end</code> positions are located at the start of the line and end of
	 * the line respectively.
	 * 
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
	 * Appends the formatted <code>String</code> representing <code>property</code>
	 * to <code>builder</code>.
	 *
	 * @param property         the <code>Property</code> to format and add
	 * @param insertSpaces     determines whether to insert spaces that surround the
	 *                         equals sign, if the equals sign exists
	 * @param formattedContent the <code>StringBuilder</code> that accumulates the
	 *                         formatted properties
	 */
	private static void formatPropertyAndAdd(Property property, boolean insertSpaces, String lineDelimiter,
			StringBuilder formattedContent) {

		boolean keyExists = property.getKey() != null;
		boolean delimiterExists = property.getDelimiterAssign() != null;
		boolean valueExists = property.getValue() != null;

		if (!delimiterExists && !valueExists) {
			formattedContent.append(property.getText().trim());
			formattedContent.append(lineDelimiter);
			return;
		}

		if (keyExists) {
			formattedContent.append(property.getKey().getText());
		}

		if (delimiterExists) {
			if (insertSpaces) {
				formattedContent.append(" ");
			}
			formattedContent.append("=");
		}

		if (valueExists) {
			if (insertSpaces) {
				formattedContent.append(" ");
			}
			formattedContent.append(property.getValue().getText().trim());
		}

		if (keyExists || delimiterExists || valueExists) {
			formattedContent.append(lineDelimiter);
		}
	}

}
/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.quarkus.services;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.redhat.quarkus.commons.ExtendedConfigDescriptionBuildItem;
import com.redhat.quarkus.commons.QuarkusProjectInfo;
import com.redhat.quarkus.ls.commons.BadLocationException;
import com.redhat.quarkus.ls.commons.TextDocument;
import com.redhat.quarkus.settings.QuarkusHoverSettings;
import com.redhat.quarkus.utils.DocumentationUtils;
import com.redhat.quarkus.utils.PositionUtils;
import com.redhat.quarkus.utils.PropertiesScannerUtils;
import com.redhat.quarkus.utils.PropertiesScannerUtils.PropertiesToken;

import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.MarkupKind;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

/**
 * Retreives hover documentation and creating Hover object
 */
class QuarkusHover {

	private static final Logger LOGGER = Logger.getLogger(QuarkusCompletions.class.getName());

	/**
	 * Returns Hover object for the currently hovered token
	 * 
	 * @param document      the document
	 * @param position      the hover position
	 * @param projectInfo   the Quarkus project information
	 * @param hoverSettings the hover settings
	 * @return Hover object for the currently hovered token
	 */
	public Hover doHover(TextDocument document, Position position, QuarkusProjectInfo projectInfo,
			QuarkusHoverSettings hoverSettings) {

		int offset;
		try {
			offset = document.offsetAt(position);
		} catch (BadLocationException e) {
			LOGGER.log(Level.SEVERE, "In QuarkusHover, position error", e);
			return null;
		}

		String text = document.getText();
		int startLineOffset = offset - position.getCharacter();
		PropertiesToken token = PropertiesScannerUtils.getTokenAt(text, startLineOffset, offset);
		
		switch (token.getType()) {
			case COMMENTS:
				// no hover documentation
				break;
			case KEY:
				// hover documentation on property key
				return getPropertyKeyHover(document, token, projectInfo, hoverSettings);
			case VALUE:
				// no hover documentation
				break;
			}

		return null;
	}

	/**
	 * Returns the documentation hover for property key represented by <code>token</code>
	 * 
	 * @param document      the document
	 * @param token         the token representing the property key being hovered
	 * @param projectInfo   the Quarkus project information
	 * @param hoverSettings the hover settings
	 * @return the documentation hover for property key represented by token
	 */
	private static Hover getPropertyKeyHover(TextDocument document, PropertiesToken token, QuarkusProjectInfo projectInfo, 
			QuarkusHoverSettings hoverSettings) {

		String keyName = null;
		try {
			int keyLength = (token.getEnd() - token.getStart()) + 1;
			Position positionStart = document.positionAt(token.getStart());
			String line = document.lineText(positionStart.getLine());
			keyName = line.substring(0, keyLength).trim();
		} catch (BadLocationException e) {
			LOGGER.log(Level.SEVERE, "In QuarkusHover, position error", e);
			return null;
		}
		
		Hover hover = new Hover();
		boolean markdownSupported = hoverSettings.isContentFormatSupported(MarkupKind.MARKDOWN);
		for (ExtendedConfigDescriptionBuildItem property : projectInfo.getProperties()) {
			if (keyName.equals(property.getPropertyName())) {
				MarkupContent markupContent = DocumentationUtils.getDocumentation(property, markdownSupported);
				hover.setContents(markupContent);
				hover.setRange(PositionUtils.createRange(token.getStart(), token.getEnd(), document));
				return hover;
			}
		}
		return null;
	}
}
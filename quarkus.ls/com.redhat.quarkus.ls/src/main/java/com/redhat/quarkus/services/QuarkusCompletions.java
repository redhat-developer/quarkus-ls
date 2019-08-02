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

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import com.redhat.quarkus.commons.ExtendedConfigDescriptionBuildItem;
import com.redhat.quarkus.commons.QuarkusProjectInfo;
import com.redhat.quarkus.ls.commons.BadLocationException;
import com.redhat.quarkus.ls.commons.TextDocument;
import com.redhat.quarkus.utils.PropertiesScannerUtils;
import com.redhat.quarkus.utils.PropertiesScannerUtils.PropertiesToken;

/**
 * The Quarkus completions
 * 
 * @author Angelo ZERR
 *
 */
class QuarkusCompletions {

	private static final Logger LOGGER = Logger.getLogger(QuarkusCompletions.class.getName());

	public CompletionList doComplete(TextDocument document, Position position, QuarkusProjectInfo projectInfo,
			CancelChecker cancelChecker) {
		CompletionList list = new CompletionList();
		String text = document.getText();
		int offset;
		try {
			offset = document.offsetAt(position);
		} catch (BadLocationException e) {
			LOGGER.log(Level.SEVERE, "In QuarkusCompletion, position error", e);
			return list;
		}
		int startLineOffset = offset - position.getCharacter();
		PropertiesToken token = PropertiesScannerUtils.getTokenAt(text, startLineOffset, offset);
		switch (token.getType()) {
		case COMMENTS:
			// no completions
			break;
		case KEY:
			// completion on property key
			collectPropertyKeySuggestions(document, token, projectInfo, list);
			break;
		case VALUE:
			// completion on property value
			collectPropertyValueSuggestions(document, token, projectInfo, list);
			break;
		}
		return list;
	}

	/**
	 * Collect property keys.
	 * 
	 * @param document    the document
	 * @param token       the token
	 * @param projectInfo the Quarkus project information
	 * @param list        the completion list to fill
	 */
	private static void collectPropertyKeySuggestions(TextDocument document, PropertiesToken token,
			QuarkusProjectInfo projectInfo, CompletionList list) {
		for (ExtendedConfigDescriptionBuildItem property : projectInfo.getProperties()) {
			CompletionItem item = new CompletionItem(property.getPropertyName());
			list.getItems().add(item);
		}
	}

	/**
	 * Collect property values.
	 * 
	 * @param document    the document
	 * @param token       the token
	 * @param projectInfo the Quarkus project information
	 * @param list        the completion list to fill
	 */
	private static void collectPropertyValueSuggestions(TextDocument document, PropertiesToken token,
			QuarkusProjectInfo projectInfo, CompletionList list) {

	}
}

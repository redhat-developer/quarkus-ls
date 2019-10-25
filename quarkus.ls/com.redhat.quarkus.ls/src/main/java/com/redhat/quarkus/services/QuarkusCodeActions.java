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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.redhat.quarkus.commons.ExtendedConfigDescriptionBuildItem;
import com.redhat.quarkus.commons.QuarkusProjectInfo;
import com.redhat.quarkus.ls.commons.BadLocationException;
import com.redhat.quarkus.ls.commons.CodeActionFactory;
import com.redhat.quarkus.ls.commons.TextDocument;
import com.redhat.quarkus.model.PropertiesModel;
import com.redhat.quarkus.model.PropertyKey;
import com.redhat.quarkus.settings.QuarkusFormattingSettings;
import com.redhat.quarkus.utils.PositionUtils;
import com.redhat.quarkus.utils.QuarkusPropertiesUtils;
import com.redhat.quarkus.utils.StringUtils;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionContext;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

/**
 * The Quarkus code actions
 * 
 * @author Angelo ZERR
 *
 */
class QuarkusCodeActions {

	private static final float MAX_DISTANCE_DIFF_RATIO = 0.1f;

	private static final Logger LOGGER = Logger.getLogger(QuarkusCodeActions.class.getName());

	/**
	 * Returns code actions for the given diagnostics of the application.properties
	 * <code>document</code> by using the given Quarkus properties metadata
	 * <code>projectInfo</code>.
	 * 
	 * @param context            the code action context
	 * @param range              the range
	 * @param document           the properties model.
	 * @param projectInfo        the Quarkus properties
	 * @param formattingSettings the formatting settings.
	 * @return the result of the code actions.
	 */
	public List<CodeAction> doCodeActions(CodeActionContext context, Range range, PropertiesModel document,
			QuarkusProjectInfo projectInfo, QuarkusFormattingSettings formattingSettings) {
		List<CodeAction> codeActions = new ArrayList<>();
		if (context.getDiagnostics() != null) {
			doCodeActionForAllRequired(context.getDiagnostics(), document, formattingSettings, codeActions);
			// Loop for all diagnostics
			for (Diagnostic diagnostic : context.getDiagnostics()) {
				if (ValidationType.unknown.name().equals(diagnostic.getCode())) {
					// Manage code action for unknown
					doCodeActionsForUnknown(diagnostic, document, projectInfo, codeActions);
				}
			}
		}
		return codeActions;
	}

	/**
	 * Creation code action for 'unknown' property by searching similar name from
	 * the known Quarkus properties.
	 * 
	 * <p>
	 * LIMITATION: mapped property are not supported.
	 * </p>
	 * 
	 * @param diagnostic  the diagnostic
	 * @param document    the properties model.
	 * @param projectInfo the Quarkus properties
	 * @param codeActions code actions list to fill.
	 */
	private void doCodeActionsForUnknown(Diagnostic diagnostic, PropertiesModel document,
			QuarkusProjectInfo projectInfo, List<CodeAction> codeActions) {
		try {
			// Get property name by using the diagnostic range
			PropertyKey propertyKey = (PropertyKey) document.findNodeAt(diagnostic.getRange().getStart());
			String propertyName = propertyKey.getPropertyName();
			// Loop for each metadata property
			for (ExtendedConfigDescriptionBuildItem metaProperty : projectInfo.getProperties()) {
				if (QuarkusPropertiesUtils.isMappedProperty(metaProperty.getPropertyName())) {
					// FIXME: support mapped property
				} else {
					// Check if the property name is similar to the metadata name
					if (isSimilar(metaProperty.getPropertyName(), propertyName)) {
						Range range = PositionUtils.createRange(propertyKey);
						CodeAction replaceAction = CodeActionFactory.replace(
								"Did you mean '" + metaProperty.getPropertyName() + "' ?", range,
								metaProperty.getPropertyName(), document.getDocument(), diagnostic);
						codeActions.add(replaceAction);
					}
				}
			}
		} catch (BadLocationException e) {
			LOGGER.log(Level.SEVERE, "In QuarkusCodeActions, position error", e);
		}
	}

	/**
	 * Returns the <code>Position</code> to insert the missing required code action property into
	 * @param textDocument the text document
	 * @return the <code>Position</code> to insert the missing required code action property into
	 * @throws BadLocationException
	 */
	private Position getPositionForRequiredCodeAction(TextDocument textDocument) throws BadLocationException {
		String textDocumentText = textDocument.getText();
		
		if (!StringUtils.hasText(textDocumentText)) {
			return new Position(0, 0);
		}

		for (int i = textDocumentText.length() - 1; i >= 0; i--) {
			if (!Character.isWhitespace(textDocumentText.charAt(i))) {
				return textDocument.positionAt(i + 1);
			}
		}

		// should never happen
		return null;
	}

	/**
	 * Returns the missing required property name from <code>diagnosticMessage</code>
	 * @param diagnosticMessage the diagnostic message containing the property name in single quotes
	 * @return the missing required property name from <code>diagnosticMessage</code>
	 */
	private String getPropertyNameFromRequiredMessage(String diagnosticMessage) {
		int start = diagnosticMessage.indexOf('\'') + 1;
		int end = diagnosticMessage.indexOf('\'', start);
		return diagnosticMessage.substring(start, end);
	}

	/**
	 * Create a code action that inserts all missing required properties and equals signs if there
	 * are more than one missing required properties.
	 * 
	 * @param diagnostics        the diagnostics, one for each missing required property
	 * @param document           the properties model
	 * @param formattingSettings the formatting settings
	 * @param codeActions        the code actions list to fill
	 */
	private void doCodeActionForAllRequired(List<Diagnostic> diagnostics, PropertiesModel document,
			QuarkusFormattingSettings formattingSettings, List<CodeAction> codeActions) {
		
		TextDocument textDocument = document.getDocument();
		List<Diagnostic> requiredDiagnostics = diagnostics.stream().filter(d -> ValidationType.required.name().equals(d.getCode())).collect(Collectors.toList());

		if (requiredDiagnostics.isEmpty()) {
			return;
		}

		try {
			Position position = getPositionForRequiredCodeAction(textDocument);
			String lineDelimiter = document.getDocument().lineDelimiter(0);
			String assign = formattingSettings.isSurroundEqualsWithSpaces() ? " = " : "=";

			StringBuilder stringToInsert = new StringBuilder();

			if (StringUtils.hasText(textDocument.getText())) {
				stringToInsert.append(lineDelimiter);
			}

		for (int i = 0; i < requiredDiagnostics.size(); i++) {
			Diagnostic diagnostic = requiredDiagnostics.get(i);
			stringToInsert.append(getPropertyNameFromRequiredMessage(diagnostic.getMessage()));
			stringToInsert.append(assign);

			if (i < requiredDiagnostics.size() - 1) {
				stringToInsert.append(lineDelimiter);
			}
		}

			CodeAction insertAction = CodeActionFactory.insert(
					"Add all missing required properties?", position,
					stringToInsert.toString(), textDocument, requiredDiagnostics);
			codeActions.add(insertAction);
		} catch (BadLocationException e) {
			LOGGER.log(Level.SEVERE, "In QuarkusCodeActions, position error", e);
		}
		
	}

	private static boolean isSimilar(String reference, String current) {
		int threshold = Math.round(MAX_DISTANCE_DIFF_RATIO * reference.length());
		LevenshteinDistance levenshteinDistance = new LevenshteinDistance(threshold);
		return levenshteinDistance.apply(reference, current) != -1;
	}
}

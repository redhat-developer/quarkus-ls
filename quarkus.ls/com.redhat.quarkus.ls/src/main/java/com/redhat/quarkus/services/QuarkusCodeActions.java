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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.redhat.quarkus.commons.EnumItem;
import com.redhat.quarkus.commons.ExtendedConfigDescriptionBuildItem;
import com.redhat.quarkus.commons.QuarkusProjectInfo;
import com.redhat.quarkus.ls.commons.BadLocationException;
import com.redhat.quarkus.ls.commons.CodeActionFactory;
import com.redhat.quarkus.ls.commons.TextDocument;
import com.redhat.quarkus.ls.commons.client.CommandKind;
import com.redhat.quarkus.ls.commons.client.ConfigurationItemEdit;
import com.redhat.quarkus.ls.commons.client.ConfigurationItemEditType;
import com.redhat.quarkus.model.PropertiesModel;
import com.redhat.quarkus.model.Property;
import com.redhat.quarkus.model.PropertyKey;
import com.redhat.quarkus.model.PropertyValue;
import com.redhat.quarkus.model.values.ValuesRulesManager;
import com.redhat.quarkus.settings.QuarkusCommandCapabilities;
import com.redhat.quarkus.settings.QuarkusFormattingSettings;
import com.redhat.quarkus.utils.PositionUtils;
import com.redhat.quarkus.utils.QuarkusPropertiesUtils;
import com.redhat.quarkus.utils.StringUtils;

import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionContext;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Command;
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
	 * @param context             the code action context
	 * @param range               the range
	 * @param document            the properties model.
	 * @param projectInfo         the Quarkus properties
	 * @param formattingSettings  the formatting settings.
	 * @param commandCapabilities the command capabilities
	 * @return the result of the code actions.
	 */
	public List<CodeAction> doCodeActions(CodeActionContext context, Range range, PropertiesModel document,
			QuarkusProjectInfo projectInfo, ValuesRulesManager valuesRulesManager,
			QuarkusFormattingSettings formattingSettings, QuarkusCommandCapabilities commandCapabilities) {
		List<CodeAction> codeActions = new ArrayList<>();
		if (context.getDiagnostics() != null) {
			doCodeActionForAllRequired(context.getDiagnostics(), document, formattingSettings, codeActions);
			// Loop for all diagnostics
			for (Diagnostic diagnostic : context.getDiagnostics()) {
				if (ValidationType.unknown.name().equals(diagnostic.getCode())) {
					// Manage code action for unknown
					doCodeActionsForUnknown(diagnostic, document, projectInfo, commandCapabilities, codeActions);
				} else if (ValidationType.value.name().equals(diagnostic.getCode())) {
					doCodeActionsForUnknownEnumValue(diagnostic, document, projectInfo, valuesRulesManager, codeActions);
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
	 * @param diagnostic          the diagnostic
	 * @param document            the properties model.
	 * @param projectInfo         the Quarkus properties
	 * @param commandCapabilities the command capabilities
	 * @param codeActions         code actions list to fill.
	 */
	private void doCodeActionsForUnknown(Diagnostic diagnostic, PropertiesModel document,
			QuarkusProjectInfo projectInfo, QuarkusCommandCapabilities commandCapabilities,
			List<CodeAction> codeActions) {
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

			if (commandCapabilities.isCommandSupported(CommandKind.COMMAND_CONFIGURATION_UPDATE)) {
				doCodeActionForIgnoreUnknownValidation(propertyName, diagnostic, codeActions);
			}
		} catch (BadLocationException e) {
			LOGGER.log(Level.SEVERE, "In QuarkusCodeActions, position error", e);
		}
	}

	/**
	 * Create code action for suggesting similar known enum values for unknown enum values.
	 * If no enum values are similar, code actions are created for each possible enum value.
	 * 
	 * 
	 * Code action(s) are created only if the property contained within
	 * the <code>diagnostic</code> range expects an enum value
	 *
	 * @param diagnostic         the diagnostic
	 * @param document           the properties model
	 * @param projectInfo        the Quarkus properties
	 * @param valuesRulesManager the ValueRulesManager
	 * @param codeActions        the code actions list to fill
	 */
	private void doCodeActionsForUnknownEnumValue(Diagnostic diagnostic, PropertiesModel document,
			QuarkusProjectInfo projectInfo, ValuesRulesManager valuesRulesManager, List<CodeAction> codeActions) {
		try {
			PropertyValue propertyValue = (PropertyValue) document.findNodeAt(diagnostic.getRange().getStart());
			PropertyKey propertyKey = ((Property) propertyValue.getParent()).getKey();
			String value = propertyValue.getValue();
			String propertyName = propertyKey.getPropertyName();
			
			ExtendedConfigDescriptionBuildItem metaProperty = QuarkusPropertiesUtils.getProperty(propertyName, projectInfo);
			if (metaProperty == null) {
				return;
			}
			
			Collection<EnumItem> enums = QuarkusPropertiesUtils.getEnums(metaProperty, document, valuesRulesManager);
			if (enums == null || enums.isEmpty()) {
				return;
			}

			Collection<EnumItem> similarEnums = new ArrayList<>();
			for (EnumItem e : enums) {
				if (isSimilarPropertyValue(e.getName(), value)) {
					similarEnums.add(e);
				}
			}

			Range range = PositionUtils.createRange(propertyValue);

			if (!similarEnums.isEmpty()) {
				// add code actions for all similar enums
				for (EnumItem e : similarEnums) {
					CodeAction replaceAction = CodeActionFactory.replace(
							"Did you mean '" + e.getName() + "'?", range,
							e.getName(), document.getDocument(), diagnostic);
					codeActions.add(replaceAction);
				}
			} else {
				// add code actions for all enums
				for (EnumItem e : enums) {
					CodeAction replaceAction = CodeActionFactory.replace(
							"Replace with '" + e.getName() + "'?", range,
							e.getName(), document.getDocument(), diagnostic);
					codeActions.add(replaceAction);
				}
			}

		} catch (BadLocationException e) {
			LOGGER.log(Level.SEVERE, "In QuarkusCodeActions, position error", e);
		}
	}

	/**
	 * Create a code action that adds <code>propertyName</code> to user's unknown
	 * validation excluded array
	 * 
	 * @param propertyName the property name to add to array for code action
	 * @param diagnostic   the corresponding unknown property diagnostic
	 * @param codeActions  the list of code actions
	 */
	private void doCodeActionForIgnoreUnknownValidation(String propertyName, Diagnostic diagnostic,
			List<CodeAction> codeActions) {
		CodeAction insertCodeAction = new CodeAction(
				"Exclude '" + propertyName + "' from unknown property validation?");

		ConfigurationItemEdit configItemEdit = new ConfigurationItemEdit("quarkus.tools.validation.unknown.excluded",
				ConfigurationItemEditType.add, propertyName);

		Command command = new Command("Add " + propertyName + " to unknown excluded array",
				CommandKind.COMMAND_CONFIGURATION_UPDATE, Collections.singletonList(configItemEdit));
		insertCodeAction.setCommand(command);
		insertCodeAction.setKind(CodeActionKind.QuickFix);
		insertCodeAction.setDiagnostics(Collections.singletonList(diagnostic));
		codeActions.add(insertCodeAction);
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

	private static boolean isSimilarPropertyValue(String reference, String current) {
		return reference.startsWith(current) ? true: isSimilar(reference, current);
	}

	private static boolean isSimilar(String reference, String current) {
		int threshold = Math.round(MAX_DISTANCE_DIFF_RATIO * reference.length());
		LevenshteinDistance levenshteinDistance = new LevenshteinDistance(threshold);
		return levenshteinDistance.apply(reference, current) != -1;
	}
}

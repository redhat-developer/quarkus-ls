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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.redhat.microprofile.commons.MicroProfileProjectInfo;
import com.redhat.microprofile.commons.metadata.ItemHint.ValueHint;
import com.redhat.microprofile.commons.metadata.ItemMetadata;
import com.redhat.microprofile.ls.commons.BadLocationException;
import com.redhat.microprofile.ls.commons.CodeActionFactory;
import com.redhat.microprofile.ls.commons.TextDocument;
import com.redhat.microprofile.ls.commons.client.CommandKind;
import com.redhat.microprofile.ls.commons.client.ConfigurationItemEdit;
import com.redhat.microprofile.ls.commons.client.ConfigurationItemEditType;
import com.redhat.microprofile.model.PropertiesModel;
import com.redhat.microprofile.model.Property;
import com.redhat.microprofile.model.PropertyKey;
import com.redhat.microprofile.model.PropertyValue;
import com.redhat.microprofile.model.values.ValuesRulesManager;
import com.redhat.microprofile.settings.MicroProfileCommandCapabilities;
import com.redhat.microprofile.settings.MicroProfileFormattingSettings;
import com.redhat.microprofile.utils.MicroProfilePropertiesUtils;
import com.redhat.microprofile.utils.PositionUtils;
import com.redhat.microprofile.utils.StringUtils;

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
class MicroProfileCodeActions {

	private static final float MAX_DISTANCE_DIFF_RATIO = 0.1f;

	private static final Logger LOGGER = Logger.getLogger(MicroProfileCodeActions.class.getName());

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
			MicroProfileProjectInfo projectInfo, ValuesRulesManager valuesRulesManager,
			MicroProfileFormattingSettings formattingSettings, MicroProfileCommandCapabilities commandCapabilities) {
		List<CodeAction> codeActions = new ArrayList<>();
		if (context.getDiagnostics() != null) {
			doCodeActionForAllRequired(context.getDiagnostics(), document, formattingSettings, codeActions);
			// Loop for all diagnostics
			for (Diagnostic diagnostic : context.getDiagnostics()) {
				if (ValidationType.unknown.name().equals(diagnostic.getCode())) {
					// Manage code action for unknown
					doCodeActionsForUnknown(diagnostic, document, projectInfo, commandCapabilities, codeActions);
				} else if (ValidationType.value.name().equals(diagnostic.getCode())) {
					doCodeActionsForUnknownEnumValue(diagnostic, document, projectInfo, valuesRulesManager,
							codeActions);
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
			MicroProfileProjectInfo projectInfo, MicroProfileCommandCapabilities commandCapabilities,
			List<CodeAction> codeActions) {
		try {
			// Get property name by using the diagnostic range
			PropertyKey propertyKey = (PropertyKey) document.findNodeAt(diagnostic.getRange().getStart());
			String propertyName = propertyKey.getPropertyName();
			// Loop for each metadata property
			for (ItemMetadata metaProperty : projectInfo.getProperties()) {
				String name = metaProperty.getName();
				if (MicroProfilePropertiesUtils.isMappedProperty(name)) {
					// FIXME: support mapped property
				} else {
					// Check if the property name is similar to the metadata name
					if (isSimilar(metaProperty.getName(), propertyName)) {
						Range range = PositionUtils.createRange(propertyKey);
						CodeAction replaceAction = CodeActionFactory.replace("Did you mean '" + name + "' ?", range,
								name, document.getDocument(), diagnostic);
						codeActions.add(replaceAction);
					}
				}
			}

			if (commandCapabilities.isCommandSupported(CommandKind.COMMAND_CONFIGURATION_UPDATE)) {
				doCodeActionForIgnoreUnknownValidation(propertyName, diagnostic, document, projectInfo, codeActions);
			}
		} catch (BadLocationException e) {
			LOGGER.log(Level.SEVERE, "In QuarkusCodeActions, position error", e);
		}
	}

	/**
	 * Create code action for suggesting similar known enum values for unknown enum
	 * values. If no enum values are similar, code actions are created for each
	 * possible enum value.
	 * 
	 * 
	 * Code action(s) are created only if the property contained within the
	 * <code>diagnostic</code> range expects an enum value
	 *
	 * @param diagnostic         the diagnostic
	 * @param document           the properties model
	 * @param projectInfo        the Quarkus properties
	 * @param valuesRulesManager the ValueRulesManager
	 * @param codeActions        the code actions list to fill
	 */
	private void doCodeActionsForUnknownEnumValue(Diagnostic diagnostic, PropertiesModel document,
			MicroProfileProjectInfo projectInfo, ValuesRulesManager valuesRulesManager, List<CodeAction> codeActions) {
		try {
			PropertyValue propertyValue = (PropertyValue) document.findNodeAt(diagnostic.getRange().getStart());
			PropertyKey propertyKey = ((Property) propertyValue.getParent()).getKey();
			String value = propertyValue.getValue();
			String propertyName = propertyKey.getPropertyName();

			ItemMetadata metaProperty = MicroProfilePropertiesUtils.getProperty(propertyName, projectInfo);
			if (metaProperty == null) {
				return;
			}

			Collection<ValueHint> enums = MicroProfilePropertiesUtils.getEnums(metaProperty, projectInfo, document,
					valuesRulesManager);
			if (enums == null || enums.isEmpty()) {
				return;
			}

			Collection<ValueHint> similarEnums = new ArrayList<>();
			for (ValueHint e : enums) {
				if (isSimilarPropertyValue(e.getValue(), value)) {
					similarEnums.add(e);
				}
			}

			Range range = PositionUtils.createRange(propertyValue);

			if (!similarEnums.isEmpty()) {
				// add code actions for all similar enums
				for (ValueHint e : similarEnums) {
					CodeAction replaceAction = CodeActionFactory.replace("Did you mean '" + e.getValue() + "'?", range,
							e.getValue(), document.getDocument(), diagnostic);
					codeActions.add(replaceAction);
				}
			} else {
				// add code actions for all enums
				for (ValueHint e : enums) {
					CodeAction replaceAction = CodeActionFactory.replace("Replace with '" + e.getValue() + "'?", range,
							e.getValue(), document.getDocument(), diagnostic);
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
	 * @param document     the properties model
	 * @param projectInfo  the Quarkus properties
	 * @param codeActions  the list of code actions
	 */
	private void doCodeActionForIgnoreUnknownValidation(String propertyName, Diagnostic diagnostic,
			PropertiesModel document, MicroProfileProjectInfo projectInfo, List<CodeAction> codeActions) {

		codeActions.add(createAddToExcludedCodeAction(propertyName, diagnostic));

		while (hasParentKey(propertyName)) {
			propertyName = getParentKey(propertyName);
			if (!propertyName.equals("quarkus")) {
				String globPattern = propertyName + ".*";
				codeActions.add(createAddToExcludedCodeAction(globPattern, diagnostic));
			}
		}
	}

	/**
	 * Returns a code action for <code>diagnostic</code> that causes <code>item</code> to
	 * be added to <code>quarkus.tools.validation.unknown.excluded</code> client configuration
	 * 
	 * @param item       the item to add to the client configuration array
	 * @param diagnostic the diagnostic for the <code>CodeAction</code>
	 * @return a code action that causes <code>item</code> to be added to
	 * <code>quarkus.tools.validation.unknown.excluded</code> client configuration
	 */
	private CodeAction createAddToExcludedCodeAction(String item, Diagnostic diagnostic) {
		CodeAction insertCodeAction = new CodeAction(
				"Exclude '" + item + "' from unknown property validation?");

		ConfigurationItemEdit configItemEdit = new ConfigurationItemEdit("quarkus.tools.validation.unknown.excluded",
				ConfigurationItemEditType.add, item);

		Command command = new Command("Add " + item + " to unknown excluded array",
				CommandKind.COMMAND_CONFIGURATION_UPDATE, Collections.singletonList(configItemEdit));
		insertCodeAction.setCommand(command);
		insertCodeAction.setKind(CodeActionKind.QuickFix);
		insertCodeAction.setDiagnostics(Collections.singletonList(diagnostic));
		return insertCodeAction;
	}

	/**
	 * Create a code action that inserts all missing required properties and equals
	 * signs if there are more than one missing required properties.
	 * 
	 * @param diagnostics        the diagnostics, one for each missing required
	 *                           property
	 * @param document           the properties model
	 * @param formattingSettings the formatting settings
	 * @param codeActions        the code actions list to fill
	 */
	private void doCodeActionForAllRequired(List<Diagnostic> diagnostics, PropertiesModel document,
			MicroProfileFormattingSettings formattingSettings, List<CodeAction> codeActions) {

		TextDocument textDocument = document.getDocument();
		List<Diagnostic> requiredDiagnostics = diagnostics.stream()
				.filter(d -> ValidationType.required.name().equals(d.getCode())).collect(Collectors.toList());

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

			CodeAction insertAction = CodeActionFactory.insert("Add all missing required properties?", position,
					stringToInsert.toString(), textDocument, requiredDiagnostics);
			codeActions.add(insertAction);
		} catch (BadLocationException e) {
			LOGGER.log(Level.SEVERE, "In QuarkusCodeActions, position error", e);
		}

	}

	/**
	 * Returns true if <code>propertyName</code> has a parent
	 * key, false otherwise
	 * 
	 * For example, the parent key for "quarkus.http.cors" is
	 * "quarkus.http"
	 * 
	 * @param propertyName the property name to check
	 * @return true if <code>propertyName</code> has a parent
	 * key, false otherwise
	 */
	private boolean hasParentKey(String propertyName) {
		return propertyName.lastIndexOf('.') >= 0;
	}

	/**
	 * Returns the parent key for <code>propertyName</code>
	 * 
	 * For example, the parent key for "quarkus.http.cors" is
	 * "quarkus.http"
	 * 
	 * @param propertyName the property name
	 * @return the parent key for <code>propertyName</code>
	 */
	private String getParentKey(String propertyName) {
		return propertyName.substring(0, propertyName.lastIndexOf('.'));
	}

	/**
	 * Returns the <code>Position</code> to insert the missing required code action
	 * property into
	 * 
	 * @param textDocument the text document
	 * @return the <code>Position</code> to insert the missing required code action
	 *         property into
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
	 * Returns the missing required property name from
	 * <code>diagnosticMessage</code>
	 * 
	 * @param diagnosticMessage the diagnostic message containing the property name
	 *                          in single quotes
	 * @return the missing required property name from
	 *         <code>diagnosticMessage</code>
	 */
	private String getPropertyNameFromRequiredMessage(String diagnosticMessage) {
		int start = diagnosticMessage.indexOf('\'') + 1;
		int end = diagnosticMessage.indexOf('\'', start);
		return diagnosticMessage.substring(start, end);
	}

	private static boolean isSimilarPropertyValue(String reference, String current) {
		return reference.startsWith(current) ? true : isSimilar(reference, current);
	}

	private static boolean isSimilar(String reference, String current) {
		int threshold = Math.round(MAX_DISTANCE_DIFF_RATIO * reference.length());
		LevenshteinDistance levenshteinDistance = new LevenshteinDistance(threshold);
		return levenshteinDistance.apply(reference, current) != -1;
	}
}

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

import static com.redhat.qute.ls.commons.CodeActionFactory.createCommand;
import static com.redhat.qute.services.diagnostics.QuteDiagnosticContants.DIAGNOSTIC_DATA_ITERABLE;
import static com.redhat.qute.services.diagnostics.QuteDiagnosticContants.DIAGNOSTIC_DATA_NAME;
import static com.redhat.qute.services.diagnostics.QuteDiagnosticContants.DIAGNOSTIC_DATA_TAG;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionContext;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

import com.google.gson.JsonObject;
import com.redhat.qute.ls.commons.BadLocationException;
import com.redhat.qute.ls.commons.CodeActionFactory;
import com.redhat.qute.ls.commons.TextDocument;
import com.redhat.qute.ls.commons.client.ConfigurationItemEdit;
import com.redhat.qute.ls.commons.client.ConfigurationItemEditType;
import com.redhat.qute.parser.expression.ObjectPart;
import com.redhat.qute.parser.template.Expression;
import com.redhat.qute.parser.template.Node;
import com.redhat.qute.parser.template.NodeKind;
import com.redhat.qute.parser.template.Section;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.project.QuteProject;
import com.redhat.qute.services.commands.QuteClientCommandConstants;
import com.redhat.qute.services.diagnostics.QuteErrorCode;
import com.redhat.qute.settings.SharedSettings;
import com.redhat.qute.utils.QutePositionUtility;

/**
 * Qute code actions support.
 *
 * @author Angelo ZERR
 *
 */
class QuteCodeActions {

	private static final Logger LOGGER = Logger.getLogger(QuteCodeActions.class.getName());

	private static final String UNDEFINED_OBJECT_CODEACTION_TITLE = "Declare `{0}` with parameter declaration.";

	private static final String UNDEFINED_SECTION_TAG_CODEACTION_TITLE = "Create the user tag file `{0}`.";

	// Enable/Disable Qute validation

	private static final String QUTE_VALIDATION_ENABLED_SECTION = "qute.validation.enabled";

	private static final String DISABLE_VALIDATION_ON_PROJECT_LEVEL_TITLE = "Disable Qute validation for the `{0}` project.";

	private static final String QUTE_VALIDATION_EXCLUDED_SECTION = "qute.validation.excluded";

	private static final String EXCLUDED_VALIDATION_TITLE = "Exclude this file from validation.";

	public CompletableFuture<List<CodeAction>> doCodeActions(Template template, CodeActionContext context, Range range,
			SharedSettings sharedSettings) {
		List<CodeAction> codeActions = new ArrayList<>();
		List<Diagnostic> diagnostics = context.getDiagnostics();
		if (diagnostics != null && !diagnostics.isEmpty()) {
			boolean canUpdateConfiguration = sharedSettings.getCommandCapabilities()
					.isCommandSupported(QuteClientCommandConstants.COMMAND_CONFIGURATION_UPDATE);
			if (canUpdateConfiguration) {
				// For each error, we provide the following quick fix:
				//
				// "Disable Qute validation for the `qute-quickstart` project."
				//
				// which will update the setting on client side to disable the Qute validation.
				doCodeActionToDisableValidation(template, diagnostics, codeActions);
			}
			for (Diagnostic diagnostic : diagnostics) {
				QuteErrorCode errorCode = QuteErrorCode.getErrorCode(diagnostic.getCode());
				if (errorCode != null) {
					switch (errorCode) {
					case UndefinedObject:
						// The following Qute template:
						// {undefinedObject}
						//
						// will provide a quickfix like:
						//
						// Declare `undefinedObject` with parameter declaration."
						doCodeActionsForUndefinedObject(template, diagnostic, codeActions);
						break;
					case UndefinedSectionTag:
						// The following Qute template:
						// {#undefinedTag }
						//
						// will provide a quickfix like:
						//
						// Create `undefinedTag`"
						doCodeActionsForUndefinedSectionTag(template, diagnostic, codeActions);
						break;
					default:
						break;
					}
				}
			}
		}
		return CompletableFuture.completedFuture(codeActions);
	}

	private static void doCodeActionsForUndefinedObject(Template template, Diagnostic diagnostic,
			List<CodeAction> codeActions) {
		try {
			String varName = null;
			boolean isIterable = false;
			JsonObject data = (JsonObject) diagnostic.getData();
			if (data != null) {
				varName = data.get(DIAGNOSTIC_DATA_NAME).getAsString();
				isIterable = data.get(DIAGNOSTIC_DATA_ITERABLE).getAsBoolean();
			} else {
				int offset = template.offsetAt(diagnostic.getRange().getStart());
				Node node = template.findNodeAt(offset);
				node = QutePositionUtility.findBestNode(offset, node);
				if (node.getKind() == NodeKind.Expression) {
					Expression expression = (Expression) node;
					ObjectPart part = expression.getObjectPart();
					if (part != null) {
						varName = part.getPartName();
					}
				}
			}

			if (varName != null) {
				TextDocument document = template.getTextDocument();
				String lineDelimiter = document.lineDelimiter(0);

				String title = MessageFormat.format(UNDEFINED_OBJECT_CODEACTION_TITLE, varName);

				Position position = new Position(0, 0);

				StringBuilder insertText = new StringBuilder("{@");
				if (isIterable) {
					insertText.append("java.util.List");
				} else {
					insertText.append("java.lang.String");
				}
				insertText.append(" ");
				insertText.append(varName);
				insertText.append("}");
				insertText.append(lineDelimiter);

				CodeAction insertParameterDeclarationQuickFix = CodeActionFactory.insert(title, position,
						insertText.toString(), document, diagnostic);
				codeActions.add(insertParameterDeclarationQuickFix);
			}

		} catch (BadLocationException e) {

		}
	}

	private static void doCodeActionToDisableValidation(Template template, List<Diagnostic> diagnostics,
			List<CodeAction> codeActions) {
		String templateUri = template.getUri();
		// Disable Qute validation for the project
		String projectUri = template.getProjectUri();
		String title = MessageFormat.format(DISABLE_VALIDATION_ON_PROJECT_LEVEL_TITLE, projectUri);
		CodeAction disableValidationQuickFix = createConfigurationUpdateCodeAction(title, templateUri,
				QUTE_VALIDATION_ENABLED_SECTION, false, ConfigurationItemEditType.update, diagnostics);
		codeActions.add(disableValidationQuickFix);

		// Disable Qute validation for the template file
		title = MessageFormat.format(EXCLUDED_VALIDATION_TITLE, template.getTemplateId());
		CodeAction disableValidationForTemplateQuickFix = createConfigurationUpdateCodeAction(title, templateUri,
				QUTE_VALIDATION_EXCLUDED_SECTION, templateUri, ConfigurationItemEditType.add, diagnostics);
		codeActions.add(disableValidationForTemplateQuickFix);
	}

	/**
	 * Create the configuration update (done on client side) quick fix.
	 *
	 * @param title       the displayed name of the QuickFix.
	 * @param sectionName the section name of the settings to update.
	 * @param item        the section value of the settings to update.
	 * @param editType    the configuration edit type.
	 * @param diagnostic  the diagnostic list that this CodeAction will fix.
	 *
	 * @return the configuration update (done on client side) quick fix.
	 */
	private static CodeAction createConfigurationUpdateCodeAction(String title, String scopeUri, String sectionName,
			Object sectionValue, ConfigurationItemEditType editType, List<Diagnostic> diagnostics) {
		ConfigurationItemEdit configItemEdit = new ConfigurationItemEdit(sectionName, editType, sectionValue);
		configItemEdit.setScopeUri(scopeUri);
		return createCommand(title, QuteClientCommandConstants.COMMAND_CONFIGURATION_UPDATE,
				Collections.singletonList(configItemEdit), diagnostics);
	}

	private static void doCodeActionsForUndefinedSectionTag(Template template, Diagnostic diagnostic,
			List<CodeAction> codeActions) {
		QuteProject project = template.getProject();
		if (project == null) {
			return;
		}
		try {
			String tagName = null;
			JsonObject data = (JsonObject) diagnostic.getData();
			if (data != null) {
				tagName = data.get(DIAGNOSTIC_DATA_TAG).getAsString();
			} else {
				int offset = template.offsetAt(diagnostic.getRange().getStart());
				Node node = template.findNodeAt(offset);
				node = QutePositionUtility.findBestNode(offset, node);
				if (node.getKind() == NodeKind.Section) {
					Section section = (Section) node;
					tagName = section.getTag();
				}
			}
			if (tagName == null) {
				return;
			}

			// TODO : use a settings to know the preferred file extension
			String preferedFileExtension = ".html";
			String tagFileUri = project.getTagsDir().resolve(tagName + preferedFileExtension).toUri().toString();
			String title = MessageFormat.format(UNDEFINED_SECTION_TAG_CODEACTION_TITLE, tagName);
			CodeAction createUserTagFile = CodeActionFactory.createFile(title, tagFileUri, "", diagnostic);
			codeActions.add(createUserTagFile);
		} catch (BadLocationException e) {
			LOGGER.log(Level.SEVERE, "Creation of undefined user tag code action failed", e);
		}
	}
}

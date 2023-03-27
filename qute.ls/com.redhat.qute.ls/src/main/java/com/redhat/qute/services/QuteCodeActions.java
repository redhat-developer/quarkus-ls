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

import static com.redhat.qute.services.codeactions.AbstractQuteCodeAction.createConfigurationUpdateCodeAction;

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
import org.eclipse.lsp4j.Range;

import com.redhat.qute.ls.api.QuteTemplateJavaTextEditProvider;
import com.redhat.qute.ls.commons.BadLocationException;
import com.redhat.qute.ls.commons.client.ConfigurationItemEditType;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.project.QuteProjectRegistry;
import com.redhat.qute.services.codeactions.CodeActionRequest;
import com.redhat.qute.services.codeactions.QuteCodeActionForMissingInputs;
import com.redhat.qute.services.codeactions.QuteCodeActionForMissingParameters;
import com.redhat.qute.services.codeactions.QuteCodeActionForUndefinedNamespace;
import com.redhat.qute.services.codeactions.QuteCodeActionForUndefinedObject;
import com.redhat.qute.services.codeactions.QuteCodeActionForUndefinedSectionTag;
import com.redhat.qute.services.codeactions.QuteCodeActionForUnknownMethod;
import com.redhat.qute.services.codeactions.QuteCodeActionForUnknownProperty;
import com.redhat.qute.services.commands.QuteClientCommandConstants;
import com.redhat.qute.services.diagnostics.QuteErrorCode;
import com.redhat.qute.settings.SharedSettings;

/**
 * Qute code actions support.
 *
 * @author Angelo ZERR
 *
 */
class QuteCodeActions {

	private static final Logger LOGGER = Logger.getLogger(QuteCodeActions.class.getName());

	private static CompletableFuture<List<CodeAction>> NO_CODEACTION = CompletableFuture
			.completedFuture(Collections.emptyList());

	// Enable/Disable Qute validation

	private static final String QUTE_VALIDATION_ENABLED_SECTION = "qute.validation.enabled";

	private static final String DISABLE_VALIDATION_ON_PROJECT_LEVEL_TITLE = "Disable Qute validation for the `{0}` project.";

	private static final String QUTE_VALIDATION_EXCLUDED_SECTION = "qute.validation.excluded";

	private static final String EXCLUDED_VALIDATION_TITLE = "Exclude this file from validation.";

	private final QuteCodeActionForUndefinedObject codeActionForUndefinedObject;

	private final QuteCodeActionForUndefinedNamespace codeActionForUndefinedNamespace;

	private final QuteCodeActionForUnknownProperty codeActionForUnknownProperty;

	private final QuteCodeActionForUnknownMethod codeActionForUnknownMethod;

	private final QuteCodeActionForUndefinedSectionTag codeActionForUndefinedSectionTag;

	private final QuteCodeActionForMissingInputs codeActionForMissingInputs;

	private final QuteCodeActionForMissingParameters codeActionForMissingParameters;

	public QuteCodeActions(QuteProjectRegistry projectRegistry) {
		this.codeActionForUndefinedObject = new QuteCodeActionForUndefinedObject();
		this.codeActionForUndefinedNamespace = new QuteCodeActionForUndefinedNamespace();
		this.codeActionForUnknownProperty = new QuteCodeActionForUnknownProperty(projectRegistry);
		this.codeActionForUnknownMethod = new QuteCodeActionForUnknownMethod(projectRegistry);
		this.codeActionForUndefinedSectionTag = new QuteCodeActionForUndefinedSectionTag();
		this.codeActionForMissingInputs = new QuteCodeActionForMissingInputs(projectRegistry);
		this.codeActionForMissingParameters = new QuteCodeActionForMissingParameters();
	}

	public CompletableFuture<List<CodeAction>> doCodeActions(Template template, CodeActionContext context, Range range,
			QuteTemplateJavaTextEditProvider javaTextEditProvider, SharedSettings sharedSettings) {
		List<CodeAction> codeActions = new ArrayList<>();
		List<Diagnostic> diagnostics = context.getDiagnostics();
		if (diagnostics == null || diagnostics.isEmpty()) {
			return CompletableFuture.completedFuture(Collections.emptyList());
		}

		List<CompletableFuture<Void>> codeActionResolveFutures = new ArrayList<>();
		for (Diagnostic diagnostic : diagnostics) {
			QuteErrorCode errorCode = QuteErrorCode.getErrorCode(diagnostic.getCode());
			if (errorCode != null) {
				try {
					CodeActionRequest request = new CodeActionRequest(template, range.getEnd(), diagnostic,
							javaTextEditProvider, sharedSettings);
					switch (errorCode) {
						case UndefinedObject:
							// The following Qute template:
							// {undefinedObject}
							//
							// will provide a quickfix like:
							//
							// Declare `undefinedObject` with parameter declaration."
							codeActionForUndefinedObject.doCodeActions(request, codeActionResolveFutures, codeActions);
							break;
						case UndefinedNamespace:
							// The following Qute template:
							// {undefinedNamespace:xyz}
							codeActionForUndefinedNamespace.doCodeActions(request, codeActionResolveFutures,
									codeActions);
							break;
						case UnknownProperty:
							codeActionForUnknownProperty.doCodeActions(request, codeActionResolveFutures, codeActions);
							break;
						case UnknownMethod:
							codeActionForUnknownMethod.doCodeActions(request, codeActionResolveFutures, codeActions);
							break;
						case UndefinedSectionTag:
							// The following Qute template:
							// {#undefinedTag }
							//
							// will provide a quickfix like:
							//
							// Create `undefinedTag`"
							codeActionForUndefinedSectionTag.doCodeActions(request, codeActionResolveFutures,
									codeActions);
							break;
						case MissingExpectedInput:
							// The following Qute template:
							// {#form uri:Login.manualLogin() id="login"}
							//
							// {/form}
							// with some required inputs to be defined within the form section
							// will provide 2 quickfixs:
							//
							// Insert required input forms
							// Insert all input forms
							codeActionForMissingInputs.doCodeActions(request, codeActionResolveFutures, codeActions);
							break;
						case MissingRequiredParameter:
							// The following Qute template:
							// {#input /}
							// with some required parameters defined in the user tag html
							// will provide quickfix:
							//
							// Insert requried parameters
							//
							// Result: <input name="{name}" class="{class}" >
							codeActionForMissingParameters.doCodeActions(request, codeActionResolveFutures,
									codeActions);
							break;
						default:
							break;
					}
				} catch (BadLocationException e) {
					LOGGER.log(Level.SEVERE, "Failed creating CodeAction Request", e);
					return NO_CODEACTION;
					// Do nothing
				}
			}
		}

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

		CompletableFuture<Void>[] registrationsArray = new CompletableFuture[codeActionResolveFutures.size()];
		codeActionResolveFutures.toArray(registrationsArray);
		return CompletableFuture.allOf(registrationsArray).thenApply((Void _void) -> {
			return codeActions;
		});
	}

	private static void doCodeActionToDisableValidation(Template template, List<Diagnostic> diagnostics,
			List<CodeAction> codeActions) {
		String templateUri = template.getUri();

		// Disable Qute validation for the template file
		String title = MessageFormat.format(EXCLUDED_VALIDATION_TITLE, template.getTemplateId());
		CodeAction disableValidationForTemplateQuickFix = createConfigurationUpdateCodeAction(title, templateUri,
				QUTE_VALIDATION_EXCLUDED_SECTION, templateUri, ConfigurationItemEditType.add, diagnostics);
		codeActions.add(disableValidationForTemplateQuickFix);

		// Disable Qute validation for the project
		String projectUri = template.getProjectUri();
		title = MessageFormat.format(DISABLE_VALIDATION_ON_PROJECT_LEVEL_TITLE, projectUri);
		CodeAction disableValidationQuickFix = createConfigurationUpdateCodeAction(title, templateUri,
				QUTE_VALIDATION_ENABLED_SECTION, false, ConfigurationItemEditType.update, diagnostics);
		codeActions.add(disableValidationQuickFix);

	}

}

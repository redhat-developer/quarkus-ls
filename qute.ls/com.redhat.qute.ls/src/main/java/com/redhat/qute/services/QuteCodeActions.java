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
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionContext;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;

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
import com.redhat.qute.parser.template.ObjectPartASTVisitor;
import com.redhat.qute.parser.template.Parameter;
import com.redhat.qute.parser.template.Section;
import com.redhat.qute.parser.template.SectionKind;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.parser.template.sections.WithSection;
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

	private static final String UNDEFINED_VARIABLE_CODEACTION_TITLE = "Declare `{0}` with parameter declaration.";

	private static final String UNDEFINED_SECTION_TAG_CODEACTION_TITLE = "Create the user tag file `{0}`.";

	// Enable/Disable Qute validation

	private static final String QUTE_VALIDATION_ENABLED_SECTION = "qute.validation.enabled";

	private static final String DISABLE_VALIDATION_ON_PROJECT_LEVEL_TITLE = "Disable Qute validation for the `{0}` project.";

	private static final String QUTE_VALIDATION_EXCLUDED_SECTION = "qute.validation.excluded";

	private static final String EXCLUDED_VALIDATION_TITLE = "Exclude this file from validation.";

	private static final String QUTE_DEPRICATED_WITH_SECTION = "Replace `#with` with `#let`.";

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
						doCodeActionsForUndefinedVariable(template, diagnostic, codeActions);
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
					case NotRecommendedWithSection:
						// The following Qute template:
						// {#with }
						//
						// will provide a quickfix like:
						//
						// Replace `with` with `let`.
						doCodeActionsForNotRecommendedWithSection(template, diagnostic, codeActions);
						break;
					default:
						break;
					}
				}
			}
		}
		return CompletableFuture.completedFuture(codeActions);
	}

	private static void doCodeActionsForUndefinedVariable(Template template, Diagnostic diagnostic,
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

				String title = MessageFormat.format(UNDEFINED_VARIABLE_CODEACTION_TITLE, varName);

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
	 * Create CodeAction for unrecommended `with` Qute syntax.
	 *
	 * e.g. <code>
	 * {#with item}
	 *   {name}
	 * {/with}
	 * </code> becomes <code>
	 * {#let name=item.name}
	 *   {name}
	 * {/let}
	 * </code>
	 *
	 * @param template    the Qute template.
	 * @param diagnostic  the diagnostic list that this CodeAction will fix.
	 * @param codeActions the list of CodeActions to perform.
	 *
	 */
	private static void doCodeActionsForNotRecommendedWithSection(Template template, Diagnostic diagnostic,
			List<CodeAction> codeActions) {
		Range withSectionRange = diagnostic.getRange();
		try {
			// 1. Retrieve the #with section node using diagnostic range
			int withSectionStart = template.offsetAt(withSectionRange.getStart());
			WithSection withSection = (WithSection) template.findNodeAt(withSectionStart + 1);

			// 2. Initialize TextEdit array
			List<TextEdit> edits = new ArrayList<TextEdit>();

			// 2.1 Create text edit to update section start from #with to #let
			// and collect all object parts from expression for text edit
			// (e.g. {name} -> {#let name=item.name})
			edits.add(createWithSectionOpenEdit(template, withSection));

			// 2.2 Create text edit to update section end from /with to /let
			edits.add(createWithSectionCloseEdit(template, withSection));

			// 3. Create CodeAction
			CodeAction replaceWithSection = CodeActionFactory.replace(QUTE_DEPRICATED_WITH_SECTION, edits,
					template.getTextDocument(), diagnostic);
			codeActions.add(replaceWithSection);
		} catch (BadLocationException e) {
			LOGGER.log(Level.SEVERE, "Creation of not recommended with section code action failed", e);
		}
	}

	/**
	 * Create text edit to replace unrecommended with section opening tag
	 *
	 * @param template    the Qute template.
	 * @param withSection the Qute with section
	 * @return
	 * @throws BadLocationException
	 */
	private static TextEdit createWithSectionOpenEdit(Template template, WithSection withSection)
			throws BadLocationException {
		String objectPartName = withSection.getObjectParameter() != null ? withSection.getObjectParameter().getName()
				: "";
		// Use set to avoid duplicate parameters
//		Set<String> withObjectParts = new HashSet<String>();

		ObjectPartASTVisitor visitor = new ObjectPartASTVisitor();
		withSection.accept(visitor);

		// Retrieve all expressions in #with section
//		findObjectParts(withSection, withObjectParts);
		Set<String> withObjectParts = visitor.getObjectPartNames();

		List<String> letObjectPartParameterList = new ArrayList<String>();
		for (String objectPart : withObjectParts) {
			letObjectPartParameterList.add(MessageFormat.format("{1}={0}.{1}", objectPartName, objectPart));
		}

		// Build text edit string
		String letObjectPartParameters = String.join(" ", letObjectPartParameterList);
		String withSectionOpenReplacementText = MessageFormat.format("#let {0}", letObjectPartParameters);
		Range withSectionOpen = new Range(template.positionAt(withSection.getStartTagNameOpenOffset()),
				template.positionAt(withSection.getStartTagCloseOffset()));

		return new TextEdit(withSectionOpen, withSectionOpenReplacementText);
	}

	/**
	 * Find all object parts by traversing AST Nodes, while retrieveing Expressions
	 * nested in Sections with recursion
	 *
	 * @param node        current node in AST traversal
	 * @param objectParts set of found object parts
	 */
	private static void findObjectParts(Node node, Set<String> objectParts) {
		List<Node> children = node.getChildren();
		// Base case: Node is an expression
		if (children.isEmpty()) {
			if (node.getKind() == NodeKind.Expression) {
				objectParts.add(((Expression) node).getContent());
			}
		}
		for (Node child : children) {
			if (child.getKind() == NodeKind.Expression) {
				objectParts.add(((Expression) child).getContent());
			} else if (child.getKind() == NodeKind.Section && ((Section) child).getSectionKind() != SectionKind.WITH) {
				for (Parameter param : ((Section) child).getParameters()) {
					objectParts.add(param.getValue());
				}
				// Recursive call to traverse nested non-WithSection Sections
				findObjectParts(child, objectParts);
			}
		}
	}

	/**
	 * Create text edit to replace unrecommended with section closing tag
	 *
	 * @param template    the Qute template.
	 * @param withSection the Qute with section
	 * @return
	 * @throws BadLocationException
	 */
	private static TextEdit createWithSectionCloseEdit(Template template, WithSection withSection)
			throws BadLocationException {
		String withSectionCloseReplacementText = "/let";
		Range withSectionClose = new Range(template.positionAt(withSection.getEndTagNameOpenOffset()),
				template.positionAt(withSection.getEndTagCloseOffset()));
		return new TextEdit(withSectionClose, withSectionCloseReplacementText);
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

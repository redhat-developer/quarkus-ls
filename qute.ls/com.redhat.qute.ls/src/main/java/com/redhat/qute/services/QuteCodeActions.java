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

import static com.redhat.qute.ls.commons.CodeActionFactory.createCodeActionWithData;
import static com.redhat.qute.ls.commons.CodeActionFactory.createCommand;
import static com.redhat.qute.ls.commons.CodeActionFactory.insert;
import static com.redhat.qute.services.diagnostics.QuteDiagnosticContants.DIAGNOSTIC_DATA_ITERABLE;
import static com.redhat.qute.services.diagnostics.QuteDiagnosticContants.DIAGNOSTIC_DATA_NAME;
import static com.redhat.qute.services.diagnostics.QuteDiagnosticContants.DIAGNOSTIC_DATA_TAG;
import static com.redhat.qute.utils.StringUtils.isSimilar;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionContext;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

import com.google.gson.JsonObject;
import com.redhat.qute.commons.GenerateMissingJavaMemberParams;
import com.redhat.qute.commons.GenerateMissingJavaMemberParams.MemberType;
import com.redhat.qute.commons.JavaFieldInfo;
import com.redhat.qute.commons.JavaMethodInfo;
import com.redhat.qute.commons.ResolvedJavaTypeInfo;
import com.redhat.qute.ls.api.QuteTemplateGenerateMissingJavaMember;
import com.redhat.qute.ls.commons.BadLocationException;
import com.redhat.qute.ls.commons.CodeActionFactory;
import com.redhat.qute.ls.commons.TextDocument;
import com.redhat.qute.ls.commons.client.ConfigurationItemEdit;
import com.redhat.qute.ls.commons.client.ConfigurationItemEditType;
import com.redhat.qute.parser.expression.ObjectPart;
import com.redhat.qute.parser.expression.Part;
import com.redhat.qute.parser.template.Expression;
import com.redhat.qute.parser.template.Node;
import com.redhat.qute.parser.template.NodeKind;
import com.redhat.qute.parser.template.ParameterDeclaration;
import com.redhat.qute.parser.template.Section;
import com.redhat.qute.parser.template.SectionMetadata;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.project.QuteProject;
import com.redhat.qute.project.datamodel.ExtendedDataModelParameter;
import com.redhat.qute.project.datamodel.ExtendedDataModelTemplate;
import com.redhat.qute.project.datamodel.JavaDataModelCache;
import com.redhat.qute.project.datamodel.resolvers.ValueResolver;
import com.redhat.qute.services.commands.QuteClientCommandConstants;
import com.redhat.qute.services.diagnostics.DiagnosticDataFactory;
import com.redhat.qute.services.diagnostics.QuteErrorCode;
import com.redhat.qute.services.diagnostics.UnknownPropertyData;
import com.redhat.qute.settings.QuteValidationSettings.Severity;
import com.redhat.qute.settings.SharedSettings;
import com.redhat.qute.utils.QutePositionUtility;
import com.redhat.qute.utils.UserTagUtils;

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

	private static final String SET_IGNORE_SEVERITY_TITLE = "Ignore `{0}` problem.";

	private static final String UNDEFINED_OBJECT_SEVERITY_SETTING = "qute.validation.undefinedObject.severity";

	private static final String UNDEFINED_NAMESPACE_SEVERITY_SETTING = "qute.validation.undefinedNamespace.severity";

	private static final String APPEND_TO_TEMPLATE_EXTENSIONS = "Create template extension `%s()` in detected template extensions class.";

	private static final String CREATE_TEMPLATE_EXTENSIONS = "Create template extension `%s()` in a new template extensions class.";

	private static final String CREATE_GETTER = "Create getter `get%s()` in `%s`.";

	private static final String CREATE_PUBLIC_FIELD = "Create public field `%s` in `%s`.";
	private final JavaDataModelCache javaCache;

	public QuteCodeActions(JavaDataModelCache javaCache) {
		this.javaCache = javaCache;
	}

	public CompletableFuture<List<CodeAction>> doCodeActions(Template template, CodeActionContext context, Range range,
			QuteTemplateGenerateMissingJavaMember resolver, SharedSettings sharedSettings) {
		List<CodeAction> codeActions = new ArrayList<>();
		List<Diagnostic> diagnostics = context.getDiagnostics();

		List<CompletableFuture<Void>> codeActionResolveFutures = new ArrayList<>();

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
						doCodeActionsForUndefinedObject(template, diagnostic, errorCode, codeActions);
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
					case UndefinedNamespace:
						// The following Qute template:
						// {undefinedNamespace:xyz}
						doCodeActionToSetIgnoreSeverity(template, Collections.singletonList(diagnostic), errorCode,
								codeActions, UNDEFINED_NAMESPACE_SEVERITY_SETTING);
						break;
					case UnknownMethod:
						// CodeAction(s) to replace text with similar suggestions
						try {
							String varName = QutePositionUtility
									.findBestNode(template.offsetAt(diagnostic.getRange().getStart()), template
											.findNodeBefore(template.offsetAt(diagnostic.getRange().getStart())))
									.getNodeName();
							doCodeActionsForSimilarValues(template, diagnostic, codeActions, varName);
						} catch (BadLocationException e) {
						}
						break;
					case UnknownProperty:
						doCodeActionToCreateProperty(template, diagnostic, errorCode, codeActions,
								codeActionResolveFutures, resolver, sharedSettings);
						break;
					default:
						break;
					}

					// CodeAction(s) to replace text with similar suggestions
					try {
						String varName = QutePositionUtility
								.findBestNode(template.offsetAt(diagnostic.getRange().getStart()),
										template.findNodeBefore(template.offsetAt(diagnostic.getRange().getStart())))
								.getNodeName();
						doCodeActionsForSimilarValues(template, diagnostic, codeActions, varName);
					} catch (BadLocationException e) {
					}
					break;
				}
			}
		}
		CompletableFuture<Void>[] registrationsArray = new CompletableFuture[codeActionResolveFutures.size()];
		codeActionResolveFutures.toArray(registrationsArray);
		return CompletableFuture.allOf(registrationsArray).thenApply((Void _void) -> {
			return codeActions;
		});
	}

	private void doCodeActionToCreateProperty(Template template, Diagnostic diagnostic, QuteErrorCode errorCode,
			List<CodeAction> codeActions, List<CompletableFuture<Void>> registrations,
			QuteTemplateGenerateMissingJavaMember resolver, SharedSettings settings) {

		UnknownPropertyData unknownPropertyData = DiagnosticDataFactory.getMissingMemberData(diagnostic);
		if (unknownPropertyData == null) {
			return;
		}
		String missingProperty = unknownPropertyData.getProperty();
		String resolvedType = unknownPropertyData.getSignature();
		String propertyCapitalized = missingProperty.substring(0, 1).toUpperCase() + missingProperty.substring(1);

		String projectUri = template.getProjectUri();

		GenerateMissingJavaMemberParams publicFieldParams = new GenerateMissingJavaMemberParams(MemberType.Field,
				missingProperty, resolvedType, projectUri);
		GenerateMissingJavaMemberParams getterParams = new GenerateMissingJavaMemberParams(MemberType.Getter,
				missingProperty, resolvedType, projectUri);
		GenerateMissingJavaMemberParams appendToTemplateExtensionsParams = new GenerateMissingJavaMemberParams(
				MemberType.AppendTemplateExtension, missingProperty, resolvedType, projectUri);
		GenerateMissingJavaMemberParams createTemplateExtensionsParams = new GenerateMissingJavaMemberParams(
				MemberType.CreateTemplateExtension, missingProperty, resolvedType, projectUri);

		CodeAction createPublicField = createCodeActionWithData(
				String.format(CREATE_PUBLIC_FIELD, missingProperty, resolvedType), publicFieldParams,
				Collections.singletonList(diagnostic));
		CodeAction createGetter = createCodeActionWithData(
				String.format(CREATE_GETTER, propertyCapitalized, resolvedType), getterParams,
				Collections.singletonList(diagnostic));
		CodeAction appendToTemplateExtensions = createCodeActionWithData(
				String.format(APPEND_TO_TEMPLATE_EXTENSIONS, missingProperty), appendToTemplateExtensionsParams,
				Collections.singletonList(diagnostic));
		CodeAction createTemplateExtensions = createCodeActionWithData(
				String.format(CREATE_TEMPLATE_EXTENSIONS, missingProperty), createTemplateExtensionsParams,
				Collections.singletonList(diagnostic));
		codeActions.add(createPublicField);
		codeActions.add(createGetter);
		codeActions.add(appendToTemplateExtensions);
		codeActions.add(createTemplateExtensions);

		registrations.add(resolver.generateMissingJavaMember(publicFieldParams) //
				.thenAccept((workspaceEdit) -> {
					if (workspaceEdit == null) {
						return;
					}
					createPublicField.setEdit(workspaceEdit);
				}));

		registrations.add(resolver.generateMissingJavaMember(getterParams) //
				.thenAccept((workspaceEdit) -> {
					if (workspaceEdit == null) {
						return;
					}
					createGetter.setEdit(workspaceEdit);
				}));

		registrations.add(resolver.generateMissingJavaMember(appendToTemplateExtensionsParams) //
				.thenAccept((workspaceEdit) -> {
					if (workspaceEdit == null) {
						return;
					}
					appendToTemplateExtensions.setEdit(workspaceEdit);
				}));

		registrations.add(resolver.generateMissingJavaMember(createTemplateExtensionsParams) //
				.thenAccept((workspaceEdit) -> {
					if (workspaceEdit == null) {
						return;
					}
					createTemplateExtensions.setEdit(workspaceEdit);
				}));

	}

	private void doCodeActionsForUndefinedObject(Template template, Diagnostic diagnostic, QuteErrorCode errorCode,
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

				// CodeAction to set validation severity to ignore
				doCodeActionToSetIgnoreSeverity(template, Collections.singletonList(diagnostic), errorCode, codeActions,
						UNDEFINED_OBJECT_SEVERITY_SETTING);

				// CodeAction to append ?? to object to make it optional
				doCodeActionToAddOptionalSuffix(template, diagnostic, codeActions);

				// CodeAction(s) to replace text with similar suggestions
				doCodeActionsForSimilarValues(template, diagnostic, codeActions, varName);
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
	 * CodeAction to change severity setting value to "ignore"
	 *
	 * @param template        the Qute template
	 * @param diagnostics     a singleton list diagnostic to set to ignore
	 * @param errorCode       the Qute error code.
	 * @param codeActions     list of CodeActions
	 * @param severitySetting the severity setting to set to ignore
	 */
	private static void doCodeActionToSetIgnoreSeverity(Template template, List<Diagnostic> diagnostics,
			QuteErrorCode errorCode, List<CodeAction> codeActions, String severitySetting) {
		String title = MessageFormat.format(SET_IGNORE_SEVERITY_TITLE, errorCode.getCode());
		CodeAction setIgnoreSeverityQuickFix = createConfigurationUpdateCodeAction(title, template.getUri(),
				severitySetting, Severity.ignore.name(), ConfigurationItemEditType.update, diagnostics);
		codeActions.add(setIgnoreSeverityQuickFix);
	}

	/**
	 * CodeAction to append ?? to object to make it optional
	 *
	 * @param template    the Qute template
	 * @param diagnostic  the UndefinedVariable diagnostic
	 * @param codeActions list of CodeActions
	 * @throws BadLocationException
	 */
	private static void doCodeActionToAddOptionalSuffix(Template template, Diagnostic diagnostic,
			List<CodeAction> codeActions) throws BadLocationException {
		Position objectEnd = diagnostic.getRange().getEnd();
		int diagnosticStartOffset = template.offsetAt(diagnostic.getRange().getStart());
		int diagnosticEndOffset = template.offsetAt(objectEnd);
		String title = MessageFormat.format("Append ?? to undefined object `{0}`",
				template.getText(diagnosticStartOffset, diagnosticEndOffset));
		CodeAction appendOptionalSuffix = insert(title, objectEnd, "??", template.getTextDocument(), diagnostic);
		codeActions.add(appendOptionalSuffix);
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

	private void doCodeActionsForSimilarValues(Template template, Diagnostic diagnostic, List<CodeAction> codeActions,
			String varName) throws BadLocationException {
		Range diagnosticRange = diagnostic.getRange();
		int offset = template.offsetAt(diagnosticRange.getEnd());

		Node nodeBefore = template.findNodeBefore(offset);
		if (nodeBefore == null) {
			return;
		}

		Node node = QutePositionUtility.findBestNode(offset, nodeBefore);

		if (node.getKind() == NodeKind.ExpressionPart) {
			Range rangeValue = new Range(
					new Position(diagnosticRange.getStart().getLine(), diagnosticRange.getStart().getCharacter()),
					new Position(diagnosticRange.getEnd().getLine(), diagnosticRange.getEnd().getCharacter()));

			Collection<String> availableValues = new TreeSet<>();

			switch (((Part) node).getPartKind()) {
			case Object:
				collectAvailableValuesForObjectPart(node, template, availableValues);
			case Namespace:
				collectAvailableValuesForNamespacePart(node, template, availableValues);
				break;
			case Method:
			case Property:
				String signature = null;
				JsonObject data = (JsonObject) diagnostic.getData();
				if (data != null) {
					signature = data.get(DIAGNOSTIC_DATA_TAG).getAsString();
					collectAvailableValuesForJavaTypePart(node, template, signature, availableValues);
					break;
				}
			}

			for (String value : availableValues) {
				if (isSimilar(value, varName)) {
					CodeAction similarCodeAction = CodeActionFactory.replace("Did you mean '" + value + "'?",
							rangeValue, value, template.getTextDocument(), diagnostic);
					codeActions.add(similarCodeAction);
				}
			}
		}
	}

	private void collectAvailableValuesForObjectPart(Node node, Template template, Collection<String> availableValues) {
		String projectUri = template.getProjectUri();

		List<ValueResolver> globalResolvers = javaCache.getGlobalVariables(projectUri);
		for (ValueResolver resolver : globalResolvers) {
			availableValues.add(resolver.getName());
		}

		List<String> aliases = template.getChildren().stream() //
				.filter(n -> n.getKind() == NodeKind.ParameterDeclaration) //
				.map(n -> ((ParameterDeclaration) n).getAlias()) //
				.filter(alias -> alias != null) //
				.collect(Collectors.toList());
		for (String alias : aliases) {
			availableValues.add(alias);
		}

		ExtendedDataModelTemplate dataModel = javaCache.getDataModelTemplate(template).getNow(null);
		if (dataModel != null) {
			for (ExtendedDataModelParameter parameter : dataModel.getParameters()) {
				availableValues.add(parameter.getKey());
			}
		}

		Section section = node != null ? node.getParentSection() : null;
		if (section != null) {
			List<SectionMetadata> metadatas = section.getMetadata();
			for (SectionMetadata metadata : metadatas) {
				availableValues.add(metadata.getName());
			}
		}

		Collection<SectionMetadata> specialKeysMetadatas = UserTagUtils.getSpecialKeys();
		for (SectionMetadata metadata : specialKeysMetadatas) {
			String name = metadata.getName();
			if (!availableValues.contains(name)) {
				availableValues.add(name);
			}
		}
	}

	private void collectAvailableValuesForNamespacePart(Node node, Template template,
			Collection<String> availableValues) {
		String projectUri = template.getProjectUri();

		String namespace = ((Part) node).getNamespace();
		if (namespace != null) {
			List<ValueResolver> namespaceResolvers = javaCache.getNamespaceResolvers(namespace, projectUri);
			for (ValueResolver resolver : namespaceResolvers) {
				boolean useNamespaceInTextEdit = namespace == null;
				String named = resolver.getNamed();
				if (named != null) {
					// @Named("user")
					// User getUser();
					String label = useNamespaceInTextEdit ? resolver.getNamespace() + ':' + named : named;
					if (!availableValues.contains(label)) {
						availableValues.add(label);
					}
				}
			}
		}
	}

	private void collectAvailableValuesForJavaTypePart(Node node, Template template, String signature,
			Collection<String> availableValues) {
		String projectUri = template.getProjectUri();

		ResolvedJavaTypeInfo resolvedJavaType = javaCache.resolveJavaType(signature, projectUri).getNow(null);

		for (JavaFieldInfo field : resolvedJavaType.getFields()) {
			availableValues.add(field.getName());
		}

		for (JavaMethodInfo method : resolvedJavaType.getMethods()) {
			availableValues.add(method.getName());
		}
	}
}

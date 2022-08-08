/*******************************************************************************
* Copyright (c) 2022 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.services.codeactions;

import static com.redhat.qute.ls.commons.CodeActionFactory.createCodeActionWithData;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Diagnostic;

import com.redhat.qute.commons.GenerateMissingJavaMemberParams;
import com.redhat.qute.commons.GenerateMissingJavaMemberParams.MemberType;
import com.redhat.qute.commons.ResolvedJavaTypeInfo;
import com.redhat.qute.ls.api.QuteTemplateJavaTextEditProvider;
import com.redhat.qute.ls.commons.BadLocationException;
import com.redhat.qute.parser.expression.Part;
import com.redhat.qute.parser.template.Node;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.project.datamodel.JavaDataModelCache;
import com.redhat.qute.services.diagnostics.QuteErrorCode;
import com.redhat.qute.settings.SharedSettings;

/**
 * Code actions for {@link QuteErrorCode#UnknownProperty}.
 *
 * @author Angelo ZERR
 *
 */
public class QuteCodeActionForUnknownProperty extends AbstractQuteCodeAction {

	private static final Logger LOGGER = Logger.getLogger(QuteCodeActionForUnknownProperty.class.getName());

	private static final String APPEND_TO_TEMPLATE_EXTENSIONS = "Create template extension `{0}()` in class `{1}`.";

	private static final String CREATE_TEMPLATE_EXTENSIONS = "Create template extension `{0}()` in a new template extensions class.";

	private static final String CREATE_GETTER = "Create getter `get{0}()` in `{1}`.";

	private static final String CREATE_PUBLIC_FIELD = "Create public field `{0}` in `{1}`.";

	public QuteCodeActionForUnknownProperty(JavaDataModelCache javaCache) {
		super(javaCache);
	}

	@Override
	public void doCodeActions(CodeActionRequest request, List<CompletableFuture<Void>> codeActionResolveFutures,
			List<CodeAction> codeActions) {
		try {
			Node node = request.getCoveredNode();
			if (node == null) {
				return;
			}
			Template template = request.getTemplate();
			Diagnostic diagnostic = request.getDiagnostic();
			QuteTemplateJavaTextEditProvider javaTextEditProvider = request.getTextEditProvider();
			SharedSettings sharedSettings = request.getSharedSettings();
			Set<String> namespaces = this.javaCache.getAllTemplateExtensionsClasses(template.getProjectUri());

			Part propertyPart = (Part) request.getCoveredNode();
			ResolvedJavaTypeInfo baseResolvedType = request.getJavaTypeOfCoveredNode(javaCache);

			if (baseResolvedType != null) {

				String missingProperty = propertyPart.getPartName();
				String resolvedType = baseResolvedType.getSignature();
				String projectUri = template.getProjectUri();
				String propertyCapitalized = missingProperty.substring(0, 1).toUpperCase()
						+ missingProperty.substring(1);
				String fileUri = template.getUri();

				if (!baseResolvedType.isBinary()) {
					doCodeActionToCreateField(missingProperty, resolvedType, projectUri, fileUri, propertyCapitalized,
							diagnostic, javaTextEditProvider, sharedSettings, codeActionResolveFutures, codeActions);
					doCodeActionToCreateGetter(missingProperty, resolvedType, projectUri, fileUri, propertyCapitalized,
							diagnostic, javaTextEditProvider, sharedSettings, codeActionResolveFutures, codeActions);
				}

				doCodeActionToAddTemplateExtension(missingProperty, resolvedType, projectUri, fileUri, diagnostic,
						javaTextEditProvider, namespaces, sharedSettings, codeActionResolveFutures, codeActions);
				doCodeActionToCreateTemplateExtensionsClass(missingProperty, resolvedType, projectUri, fileUri,
						propertyCapitalized, diagnostic, javaTextEditProvider, sharedSettings, codeActionResolveFutures,
						codeActions);
			}

		} catch (BadLocationException e) {
			LOGGER.log(Level.SEVERE, "Creation of unknown property code action failed", e);
		}
	}

	private static void doCodeActionToCreateField(String missingProperty, String resolvedType, String projectUri,
			String fileUri, String propertyCapitalized, Diagnostic diagnostic,
			QuteTemplateJavaTextEditProvider javaTextEditProvider, SharedSettings settings,
			List<CompletableFuture<Void>> registrations, List<CodeAction> codeActions) {
		GenerateMissingJavaMemberParams publicFieldParams = new GenerateMissingJavaMemberParams(MemberType.Field,
				missingProperty, resolvedType, projectUri);
		CodeAction createPublicField = createCodeActionWithData(
				MessageFormat.format(CREATE_PUBLIC_FIELD, missingProperty, resolvedType), publicFieldParams,
				Collections.singletonList(diagnostic));
		codeActions.add(createPublicField);
		if (settings.getCodeActionSettings().isResolveSupported()) {
			createPublicField.setData(new CodeActionUnresolvedData(fileUri,
					CodeActionResolverKind.GenerateMissingMember, publicFieldParams));
		} else {
			registrations.add(javaTextEditProvider.generateMissingJavaMember(publicFieldParams) //
					.thenAccept((workspaceEdit) -> {
						if (workspaceEdit == null) {
							return;
						}
						createPublicField.setEdit(workspaceEdit);
					}));
		}
	}

	private static void doCodeActionToCreateGetter(String missingProperty, String resolvedType, String projectUri,
			String fileUri, String propertyCapitalized, Diagnostic diagnostic,
			QuteTemplateJavaTextEditProvider javaTextEditProvider, SharedSettings settings,
			List<CompletableFuture<Void>> registrations, List<CodeAction> codeActions) {
		GenerateMissingJavaMemberParams getterParams = new GenerateMissingJavaMemberParams(MemberType.Getter,
				missingProperty, resolvedType, projectUri);
		CodeAction createGetter = createCodeActionWithData(
				MessageFormat.format(CREATE_GETTER, propertyCapitalized, resolvedType), getterParams,
				Collections.singletonList(diagnostic));
		codeActions.add(createGetter);
		if (settings.getCodeActionSettings().isResolveSupported()) {
			createGetter.setData(
					new CodeActionUnresolvedData(fileUri, CodeActionResolverKind.GenerateMissingMember, getterParams));
		} else {
			registrations.add(javaTextEditProvider.generateMissingJavaMember(getterParams) //
					.thenAccept((workspaceEdit) -> {
						if (workspaceEdit == null) {
							return;
						}
						createGetter.setEdit(workspaceEdit);
					}));
		}
	}

	private static void doCodeActionToAddTemplateExtension(String missingProperty, String resolvedType,
			String projectUri, String fileUri, Diagnostic diagnostic, QuteTemplateJavaTextEditProvider javaTextEditProvider,
			Set<String> templateExtensionsClasses, SharedSettings settings, List<CompletableFuture<Void>> registrations,
			List<CodeAction> codeActions) {

		for (String templateExtensionsClass : templateExtensionsClasses) {

			GenerateMissingJavaMemberParams appendToTemplateExtensionsParams = new GenerateMissingJavaMemberParams(
					MemberType.AppendTemplateExtension, missingProperty, resolvedType, projectUri,
					templateExtensionsClass);
			CodeAction appendToTemplateExtensions = createCodeActionWithData(
					MessageFormat.format(APPEND_TO_TEMPLATE_EXTENSIONS, missingProperty, templateExtensionsClass),
					appendToTemplateExtensionsParams, Collections.singletonList(diagnostic));
			codeActions.add(appendToTemplateExtensions);

			if (settings.getCodeActionSettings().isResolveSupported()) {
				appendToTemplateExtensions.setData(new CodeActionUnresolvedData(fileUri,
						CodeActionResolverKind.GenerateMissingMember, appendToTemplateExtensionsParams));
			} else {
				registrations.add(javaTextEditProvider.generateMissingJavaMember(appendToTemplateExtensionsParams) //
						.thenAccept((workspaceEdit) -> {
							if (workspaceEdit == null) {
								return;
							}
							appendToTemplateExtensions.setEdit(workspaceEdit);
						}));
			}
		}
	}

	private static void doCodeActionToCreateTemplateExtensionsClass(String missingProperty, String resolvedType,
			String projectUri, String fileUri, String propertyCapitalized, Diagnostic diagnostic,
			QuteTemplateJavaTextEditProvider javaTextEditProvider, SharedSettings settings,
			List<CompletableFuture<Void>> registrations, List<CodeAction> codeActions) {
		GenerateMissingJavaMemberParams createTemplateExtensionsParams = new GenerateMissingJavaMemberParams(
				MemberType.CreateTemplateExtension, missingProperty, resolvedType, projectUri);
		CodeAction createTemplateExtensions = createCodeActionWithData(
				MessageFormat.format(CREATE_TEMPLATE_EXTENSIONS, missingProperty), createTemplateExtensionsParams,
				Collections.singletonList(diagnostic));
		codeActions.add(createTemplateExtensions);
		if (settings.getCodeActionSettings().isResolveSupported()) {
			createTemplateExtensions.setData(new CodeActionUnresolvedData(fileUri,
					CodeActionResolverKind.GenerateMissingMember, createTemplateExtensionsParams));
		} else {
			registrations.add(javaTextEditProvider.generateMissingJavaMember(createTemplateExtensionsParams) //
					.thenAccept((workspaceEdit) -> {
						if (workspaceEdit == null) {
							return;
						}
						createTemplateExtensions.setEdit(workspaceEdit);
					}));
		}
	}

}

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
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Diagnostic;

import com.redhat.qute.commons.GenerateMissingJavaMemberParams;
import com.redhat.qute.commons.GenerateMissingJavaMemberParams.MemberType;
import com.redhat.qute.commons.ResolvedJavaTypeInfo;
import com.redhat.qute.ls.api.QuteTemplateGenerateMissingJavaMember;
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

	private static final String APPEND_TO_TEMPLATE_EXTENSIONS = "Create template extension `{0}()` in detected template extensions class.";

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
			QuteTemplateGenerateMissingJavaMember resolver = request.getResolver();
			SharedSettings sharedSettings = request.getSharedSettings();

			Part propertyPart = (Part) request.getCoveredNode();
			ResolvedJavaTypeInfo baseResolvedType = request.getJavaTypeOfCoveredNode(javaCache);

			if (baseResolvedType != null) {
				doCodeActionToCreateProperty(propertyPart, baseResolvedType, template, diagnostic, resolver,
						sharedSettings, codeActionResolveFutures, codeActions);
			}

		} catch (BadLocationException e) {
			LOGGER.log(Level.SEVERE, "Creation of unknown property code action failed", e);
		}
	}

	private static void doCodeActionToCreateProperty(Part propertyPart, ResolvedJavaTypeInfo baseResolvedType,
			Template template, Diagnostic diagnostic, QuteTemplateGenerateMissingJavaMember resolver,
			SharedSettings settings, List<CompletableFuture<Void>> registrations, List<CodeAction> codeActions) {

		String missingProperty = propertyPart.getPartName();
		String resolvedType = baseResolvedType.getSignature();
		String propertyCapitalized = missingProperty.substring(0, 1).toUpperCase() + missingProperty.substring(1);

		String projectUri = template.getProjectUri();

		if (baseResolvedType.isSource()) {
			GenerateMissingJavaMemberParams publicFieldParams = new GenerateMissingJavaMemberParams(MemberType.Field,
					missingProperty, resolvedType, projectUri);
			GenerateMissingJavaMemberParams getterParams = new GenerateMissingJavaMemberParams(MemberType.Getter,
					missingProperty, resolvedType, projectUri);
			CodeAction createPublicField = createCodeActionWithData(
					MessageFormat.format(CREATE_PUBLIC_FIELD, missingProperty, resolvedType), publicFieldParams,
					Collections.singletonList(diagnostic));
			CodeAction createGetter = createCodeActionWithData(
					MessageFormat.format(CREATE_GETTER, propertyCapitalized, resolvedType), getterParams,
					Collections.singletonList(diagnostic));
			codeActions.add(createPublicField);
			codeActions.add(createGetter);
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
		}

		GenerateMissingJavaMemberParams appendToTemplateExtensionsParams = new GenerateMissingJavaMemberParams(
				MemberType.AppendTemplateExtension, missingProperty, resolvedType, projectUri);
		CodeAction appendToTemplateExtensions = createCodeActionWithData(
				MessageFormat.format(APPEND_TO_TEMPLATE_EXTENSIONS, missingProperty), appendToTemplateExtensionsParams,
				Collections.singletonList(diagnostic));
		codeActions.add(appendToTemplateExtensions);
		registrations.add(resolver.generateMissingJavaMember(appendToTemplateExtensionsParams) //
				.thenAccept((workspaceEdit) -> {
					if (workspaceEdit == null) {
						return;
					}
					appendToTemplateExtensions.setEdit(workspaceEdit);
				}));

		GenerateMissingJavaMemberParams createTemplateExtensionsParams = new GenerateMissingJavaMemberParams(
				MemberType.CreateTemplateExtension, missingProperty, resolvedType, projectUri);
		CodeAction createTemplateExtensions = createCodeActionWithData(
				MessageFormat.format(CREATE_TEMPLATE_EXTENSIONS, missingProperty), createTemplateExtensionsParams,
				Collections.singletonList(diagnostic));
		codeActions.add(createTemplateExtensions);
		registrations.add(resolver.generateMissingJavaMember(createTemplateExtensionsParams) //
				.thenAccept((workspaceEdit) -> {
					if (workspaceEdit == null) {
						return;
					}
					createTemplateExtensions.setEdit(workspaceEdit);
				}));

	}

}

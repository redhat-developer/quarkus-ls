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
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Diagnostic;

import com.redhat.qute.commons.GenerateMissingJavaMemberParams;
import com.redhat.qute.commons.GenerateMissingJavaMemberParams.MemberType;
import com.redhat.qute.commons.ResolvedJavaTypeInfo;
import com.redhat.qute.ls.api.QuteTemplateGenerateMissingJavaMember;
import com.redhat.qute.ls.commons.BadLocationException;
import com.redhat.qute.parser.expression.Part;
import com.redhat.qute.parser.expression.PropertyPart;
import com.redhat.qute.parser.template.Node;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.project.datamodel.JavaDataModelCache;
import com.redhat.qute.services.diagnostics.QuteErrorCode;
import com.redhat.qute.services.nativemode.JavaTypeAccessibiltyRule;
import com.redhat.qute.services.nativemode.JavaTypeFilter;
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
			QuteTemplateGenerateMissingJavaMember resolver = request.getResolver();
			SharedSettings sharedSettings = request.getSharedSettings();
			Set<String> namespaces = this.javaCache.getAllTemplateExtensionsClasses(template.getProjectUri());

			Part part = (Part) node;
			ResolvedJavaTypeInfo baseResolvedType = request.getJavaTypeOfCoveredNode(javaCache);

			if (baseResolvedType != null) {

				String missingProperty = part.getPartName();
				String resolvedType = baseResolvedType.getSignature();
				String projectUri = template.getProjectUri();
				String propertyCapitalized = missingProperty.substring(0, 1).toUpperCase()
						+ missingProperty.substring(1);

				if (!baseResolvedType.isBinary()) {
					doCodeActionToCreateField(missingProperty, resolvedType, projectUri, propertyCapitalized,
							diagnostic,
							resolver, sharedSettings, codeActionResolveFutures, codeActions);
					doCodeActionToCreateGetter(missingProperty, resolvedType, projectUri, propertyCapitalized,
							diagnostic,
							resolver, sharedSettings, codeActionResolveFutures, codeActions);
				}

				doCodeActionToAddTemplateExtension(missingProperty, resolvedType, projectUri, diagnostic, resolver,
						namespaces, sharedSettings, codeActionResolveFutures, codeActions);
				doCodeActionToCreateTemplateExtensionsClass(missingProperty, resolvedType, projectUri,
						propertyCapitalized, diagnostic, resolver, sharedSettings, codeActionResolveFutures,
						codeActions);

				doCodeActionToCreateProperty(part, baseResolvedType, template, diagnostic, resolver, sharedSettings,
						codeActionResolveFutures, codeActions);

				doCodeActionsForSimilarValues((PropertyPart) part, template, diagnostic, baseResolvedType, codeActions);
			}

		} catch (BadLocationException e) {
			LOGGER.log(Level.SEVERE, "Creation of unknown property code action failed", e);
		}
	}

	private static void doCodeActionToCreateField(String missingProperty, String resolvedType, String projectUri,
			String propertyCapitalized, Diagnostic diagnostic, QuteTemplateGenerateMissingJavaMember resolver,
			SharedSettings settings, List<CompletableFuture<Void>> registrations, List<CodeAction> codeActions) {
		GenerateMissingJavaMemberParams publicFieldParams = new GenerateMissingJavaMemberParams(MemberType.Field,
				missingProperty, resolvedType, projectUri);
		CodeAction createPublicField = createCodeActionWithData(
				MessageFormat.format(CREATE_PUBLIC_FIELD, missingProperty, resolvedType), publicFieldParams,
				Collections.singletonList(diagnostic));
		codeActions.add(createPublicField);
		registrations.add(resolver.generateMissingJavaMember(publicFieldParams) //
				.thenAccept((workspaceEdit) -> {
					if (workspaceEdit == null) {
						return;
					}
					createPublicField.setEdit(workspaceEdit);
				}));
	}

	private static void doCodeActionToCreateGetter(String missingProperty, String resolvedType, String projectUri,
			String propertyCapitalized, Diagnostic diagnostic, QuteTemplateGenerateMissingJavaMember resolver,
			SharedSettings settings, List<CompletableFuture<Void>> registrations, List<CodeAction> codeActions) {
		GenerateMissingJavaMemberParams getterParams = new GenerateMissingJavaMemberParams(MemberType.Getter,
				missingProperty, resolvedType, projectUri);
		CodeAction createGetter = createCodeActionWithData(
				MessageFormat.format(CREATE_GETTER, propertyCapitalized, resolvedType), getterParams,
				Collections.singletonList(diagnostic));
		codeActions.add(createGetter);
		registrations.add(resolver.generateMissingJavaMember(getterParams) //
				.thenAccept((workspaceEdit) -> {
					if (workspaceEdit == null) {
						return;
					}
					createGetter.setEdit(workspaceEdit);
				}));
	}

	private static void doCodeActionToAddTemplateExtension(String missingProperty, String resolvedType,
			String projectUri, Diagnostic diagnostic, QuteTemplateGenerateMissingJavaMember resolver,
			Set<String> templateExtensionsClasses, SharedSettings sharedSettings,
			List<CompletableFuture<Void>> registrations, List<CodeAction> codeActions) {

		for (String templateExtensionsClass : templateExtensionsClasses) {

			GenerateMissingJavaMemberParams appendToTemplateExtensionsParams = new GenerateMissingJavaMemberParams(
					MemberType.AppendTemplateExtension, missingProperty, resolvedType, projectUri,
					templateExtensionsClass);
			CodeAction appendToTemplateExtensions = createCodeActionWithData(
					MessageFormat.format(APPEND_TO_TEMPLATE_EXTENSIONS, missingProperty, templateExtensionsClass),
					appendToTemplateExtensionsParams, Collections.singletonList(diagnostic));
			codeActions.add(appendToTemplateExtensions);
			registrations.add(resolver.generateMissingJavaMember(appendToTemplateExtensionsParams) //
					.thenAccept((workspaceEdit) -> {
						if (workspaceEdit == null) {
							return;
						}
						appendToTemplateExtensions.setEdit(workspaceEdit);
					}));
		}

	}

	private static void doCodeActionToCreateTemplateExtensionsClass(String missingProperty, String resolvedType,
			String projectUri, String propertyCapitalized, Diagnostic diagnostic,
			QuteTemplateGenerateMissingJavaMember resolver, SharedSettings settings,
			List<CompletableFuture<Void>> registrations, List<CodeAction> codeActions) {
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

	private void doCodeActionsForSimilarValues(PropertyPart part, Template template, Diagnostic diagnostic,
			ResolvedJavaTypeInfo baseResolvedType, List<CodeAction> codeActions) throws BadLocationException {
		Collection<String> availableValues = collectAvailableValuesForPropertyPart(part, template, baseResolvedType);
		doCodeActionsForSimilarValues(part, availableValues, template, diagnostic, codeActions);
	}

	private Collection<String> collectAvailableValuesForPropertyPart(PropertyPart node, Template template,
			ResolvedJavaTypeInfo baseResolvedType) {
		Collection<String> availableValues = new HashSet<>();
		String projectUri = template.getProjectUri();

		List<String> javaFieldNames = baseResolvedType.getFields().stream().map(x -> x.getName())
				.collect(Collectors.toList());
		for (String fieldName : javaFieldNames) {
			availableValues.add(fieldName);
		}

		JavaTypeFilter filter = javaCache.getJavaTypeFilter(projectUri, null);
		JavaTypeAccessibiltyRule javaTypeAccessibility = filter.getJavaTypeAccessibility(baseResolvedType,
				template.getJavaTypesSupportedInNativeMode());

		if (!isIgnoreSuperclasses(baseResolvedType, javaTypeAccessibility, filter)) {
			List<String> extendedTypes = baseResolvedType.getExtendedTypes();
			if (extendedTypes != null) {
				for (String extendedType : extendedTypes) {
					ResolvedJavaTypeInfo resolvedExtendedType = javaCache.resolveJavaType(extendedType, projectUri)
							.getNow(null);
					if (resolvedExtendedType != null) {
						collectAvailableValuesForPropertyPart(node, template, resolvedExtendedType);
					}
				}
			}
		}

		return availableValues;
	}

	private static boolean isIgnoreSuperclasses(ResolvedJavaTypeInfo baseType,
			JavaTypeAccessibiltyRule javaTypeAccessibility, JavaTypeFilter filter) {
		return filter != null && filter.isIgnoreSuperclasses(baseType, javaTypeAccessibility);
	}

}

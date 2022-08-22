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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Diagnostic;

import com.redhat.qute.commons.JavaMethodInfo;
import com.redhat.qute.commons.ResolvedJavaTypeInfo;
import com.redhat.qute.ls.commons.BadLocationException;
import com.redhat.qute.parser.expression.MethodPart;
import com.redhat.qute.parser.template.Node;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.project.datamodel.JavaDataModelCache;
import com.redhat.qute.project.datamodel.resolvers.MethodValueResolver;
import com.redhat.qute.services.diagnostics.QuteErrorCode;
import com.redhat.qute.services.nativemode.JavaTypeAccessibiltyRule;
import com.redhat.qute.services.nativemode.JavaTypeFilter;
import com.redhat.qute.settings.QuteNativeSettings;

/**
 * Code actions for {@link QuteErrorCode#UnknownMethod}.
 *
 * @author Angelo ZERR
 *
 */
public class QuteCodeActionForUnknownMethod extends AbstractQuteCodeAction {

	private static final Logger LOGGER = Logger.getLogger(QuteCodeActionForUnknownMethod.class.getName());

	public QuteCodeActionForUnknownMethod(JavaDataModelCache javaCache) {
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

			ResolvedJavaTypeInfo baseResolvedType = request.getJavaTypeOfCoveredNode(javaCache);
			if (baseResolvedType == null) {
				return;
			}

			MethodPart part = (MethodPart) node;
			Template template = request.getTemplate();
			Diagnostic diagnostic = request.getDiagnostic();
			QuteNativeSettings nativeImageSettings = request.getSharedSettings().getNativeSettings();

			// CodeAction(s) to replace text with similar suggestions for Java methods
			doCodeActionsForSimilarValues(part, template, diagnostic, baseResolvedType, nativeImageSettings,
					codeActions);

		} catch (BadLocationException e) {
			LOGGER.log(Level.SEVERE, "Creation of unknown method code action failed", e);
		}

	}

	/**
	 * Create CodeAction(s) for similar text suggestions for methods
	 *
	 * @param part                the method part
	 * @param template            the Qute template
	 * @param diagnostic          the UnknownMethod diagnostic
	 * @param baseResolvedType    the resolved Java type info
	 * @param nativeImageSettings the native image settings
	 * @param codeActions         list of CodeActions
	 *
	 * @throws BadLocationException
	 */
	private void doCodeActionsForSimilarValues(MethodPart part, Template template, Diagnostic diagnostic,
			ResolvedJavaTypeInfo baseResolvedType, QuteNativeSettings nativeImageSettings,
			List<CodeAction> codeActions) {

		String projectUri = template.getProjectUri();
		JavaTypeFilter filter = javaCache.getJavaTypeFilter(projectUri, nativeImageSettings);
		Set<String> existingProperties = new HashSet<>();

		// Collect similar code action for methods defined in Java type
		collectSimilarCodeActionsForJavaMethods(part, template, projectUri, baseResolvedType, filter,
				existingProperties, diagnostic, codeActions);

		// Collect similar code action for methods defined in built-in Qute value
		// resolvers
		List<MethodValueResolver> resolvers = javaCache.getResolversFor(baseResolvedType, template.getProjectUri());
		for (MethodValueResolver method : resolvers) {
			if (method.isValidName()) {
				doCodeActionsForSimilarValue(part, method.getMethodName(), template, existingProperties, diagnostic,
						codeActions);
			}
		}
	}

	private void collectSimilarCodeActionsForJavaMethods(MethodPart part, Template template, String projectUri,
			ResolvedJavaTypeInfo baseResolvedType, JavaTypeFilter filter, Set<String> existingProperties,
			Diagnostic diagnostic, List<CodeAction> codeActions) {
		// Java method similar code actions
		for (JavaMethodInfo method : baseResolvedType.getMethods()) {
			doCodeActionsForSimilarValue(part, method.getName(), template, existingProperties, diagnostic, codeActions);
		}

		// Java super method similar code actions
		JavaTypeAccessibiltyRule javaTypeAccessibility = filter.getJavaTypeAccessibility(baseResolvedType,
				template.getJavaTypesSupportedInNativeMode());
		if (!isIgnoreSuperclasses(baseResolvedType, javaTypeAccessibility, filter)) {
			List<String> extendedTypes = baseResolvedType.getExtendedTypes();
			if (extendedTypes != null) {
				for (String extendedType : extendedTypes) {
					ResolvedJavaTypeInfo resolvedExtendedType = javaCache.resolveJavaType(extendedType, projectUri)
							.getNow(null);
					if (resolvedExtendedType != null) {
						collectSimilarCodeActionsForJavaMethods(part, template, projectUri, resolvedExtendedType,
								filter, existingProperties, diagnostic, codeActions);
					}
				}
			}
		}
	}

}

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

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Diagnostic;

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

			doCodeActionsForSimilarValues(part, template, diagnostic, baseResolvedType, nativeImageSettings, codeActions);

		} catch (BadLocationException e) {
			LOGGER.log(Level.SEVERE, "Creation of unknown method code action failed", e);
		}

	}

	/**
	 * Create CodeAction(s) for similar text suggestions for methods
	 *
	 * @param part             the method part
	 * @param template         the Qute template
	 * @param diagnostic       the UndefinedVariable diagnostic
	 * @param baseResolvedType the resolved Java type info
	 * @param codeActions      list of CodeActions
	 *
	 * @throws BadLocationException
	 */
	private void doCodeActionsForSimilarValues(MethodPart part, Template template, Diagnostic diagnostic,
			ResolvedJavaTypeInfo baseResolvedType, QuteNativeSettings nativeImageSettings, List<CodeAction> codeActions) throws BadLocationException {
		Collection<String> availableValues = collectAvailableValuesForMethodPart(part, template, baseResolvedType, nativeImageSettings);
		doCodeActionsForSimilarValues(part, availableValues, template, diagnostic, codeActions);
	}

	/**
	 * Collect similar text suggestions for methods
	 *
	 * @param part             the method part
	 * @param template         the Qute template
	 * @param baseResolvedType the resolved Java type info
	 * @param nativeImageSettings   the Qute native settings
	 *
	 * @return a list of available methods for the resolved type
	 */
	private Collection<String> collectAvailableValuesForMethodPart(MethodPart part, Template template,
			ResolvedJavaTypeInfo baseResolvedType, QuteNativeSettings nativeImageSettings) {
		Collection<String> availableValues = new HashSet<>();
		String projectUri = template.getProjectUri();

		JavaTypeFilter filter = javaCache.getJavaTypeFilter(projectUri, nativeImageSettings);

		// Resolve methods defined in Java type
		Collection<String> availableMethodNames = collectJavaTypeMethodNames(part, template, projectUri, baseResolvedType, filter, nativeImageSettings);
		for (String methodName : availableMethodNames) {
			availableValues.add(methodName);
		}

		// Resolve methods defined in built-in Qute value resolvers
		List<MethodValueResolver> resolvers = javaCache.getResolversFor(baseResolvedType, template.getProjectUri());
		for (MethodValueResolver method : resolvers) {
			if (method.isValidName()) {
				availableValues.add(method.getMethodName());
			}
		}

		return availableValues;
	}

	private Collection<String> collectJavaTypeMethodNames(MethodPart part, Template template, String projectUri,
	ResolvedJavaTypeInfo baseResolvedType, JavaTypeFilter filter, QuteNativeSettings nativeImageSettings) {
		Collection<String> availableMethodNames = new HashSet<>();

		List<String> javaMethodNames = baseResolvedType.getMethods().stream().map(x -> x.getMethodName())
				.collect(Collectors.toList());
		for (String methodName : javaMethodNames) {
			availableMethodNames.add(methodName);
		}

		JavaTypeAccessibiltyRule javaTypeAccessibility = filter.getJavaTypeAccessibility(baseResolvedType,
				template.getJavaTypesSupportedInNativeMode());

		if (!isIgnoreSuperclasses(baseResolvedType, javaTypeAccessibility, filter)) {
			List<String> extendedTypes = baseResolvedType.getExtendedTypes();
			if (extendedTypes != null) {
				for (String extendedType : extendedTypes) {
					ResolvedJavaTypeInfo resolvedExtendedType = javaCache.resolveJavaType(extendedType, projectUri)
							.getNow(null);
					if (resolvedExtendedType != null) {
						availableMethodNames.addAll(collectJavaTypeMethodNames(part, template, projectUri, resolvedExtendedType, filter, nativeImageSettings));
					}
				}
			}
		}

		return availableMethodNames;
	}

	private static boolean isIgnoreSuperclasses(ResolvedJavaTypeInfo baseType,
	JavaTypeAccessibiltyRule javaTypeAccessibility, JavaTypeFilter filter) {
		return filter != null && javaTypeAccessibility != null && filter.isIgnoreSuperclasses(baseType, javaTypeAccessibility);
	}

}

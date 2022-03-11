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
package com.redhat.qute.services.inlayhint;

import static com.redhat.qute.services.ResolvingJavaTypeContext.RESOLVING_JAVA_TYPE;
import static com.redhat.qute.services.ResolvingJavaTypeContext.isResolvingJavaType;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import com.redhat.lsp4j.proposed.InlayHint;
import com.redhat.lsp4j.proposed.InlayHintKind;
import com.redhat.qute.commons.ResolvedJavaTypeInfo;
import com.redhat.qute.parser.template.ASTVisitor;
import com.redhat.qute.parser.template.Expression;
import com.redhat.qute.parser.template.Parameter;
import com.redhat.qute.parser.template.Section;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.parser.template.sections.CustomSection;
import com.redhat.qute.parser.template.sections.ForSection;
import com.redhat.qute.parser.template.sections.IfSection;
import com.redhat.qute.parser.template.sections.LetSection;
import com.redhat.qute.parser.template.sections.SetSection;
import com.redhat.qute.project.datamodel.JavaDataModelCache;
import com.redhat.qute.services.ResolvingJavaTypeContext;
import com.redhat.qute.settings.QuteInlayHintSettings;

/**
 * AST visitor used to show inferred Java type for section parameter as inlay
 * hint.
 *
 * @author Angelo ZERR
 *
 */
public class InlayHintASTVistor extends ASTVisitor {

	private static final Logger LOGGER = Logger.getLogger(InlayHintASTVistor.class.getName());

	private final JavaDataModelCache javaCache;

	private final int startOffset;

	private final int endOffset;
	private final QuteInlayHintSettings inlayHintSettings;

	private final ResolvingJavaTypeContext resolvingJavaTypeContext;

	private final List<InlayHint> inlayHints;

	public InlayHintASTVistor(JavaDataModelCache javaCache, int startOffset, int endOffset,
			QuteInlayHintSettings inlayHintSettings, ResolvingJavaTypeContext resolvingJavaTypeContext,
			CancelChecker cancelChecker) {
		this.javaCache = javaCache;
		this.startOffset = startOffset;
		this.endOffset = endOffset;
		this.inlayHintSettings = inlayHintSettings;
		this.resolvingJavaTypeContext = resolvingJavaTypeContext;
		this.inlayHints = new ArrayList<InlayHint>();
	}

	public List<InlayHint> getInlayHints() {
		return inlayHints;
	}

	@Override
	public boolean visit(ForSection node) {
		if (!isAfterStartParameterVisible(node)) {
			return false;
		}
		if (inlayHintSettings.isShowSectionParameterType()) {
			// {#for item[:Item] in items}
			Parameter aliasParameter = node.getAliasParameter();
			if (aliasParameter != null) {
				Parameter iterableParameter = node.getIterableParameter();
				if (iterableParameter != null) {
					Template template = node.getOwnerTemplate();
					String projectUri = template.getProjectUri();
					createJavaTypeInlayHint(aliasParameter, iterableParameter, template, projectUri);
				}
			}
		}
		return isAfterEndParameterVisible(node);
	}

	@Override
	public boolean visit(IfSection node) {
		if (!isAfterStartParameterVisible(node)) {
			return false;
		}
		if (inlayHintSettings.isShowSectionParameterType()) {
			List<Parameter> parameters = node.getParameters();
			for (Parameter parameter : parameters) {
				if (parameter.isOptional()) {
					// {#if foo??}
					Template template = node.getOwnerTemplate();
					String projectUri = template.getProjectUri();
					createJavaTypeInlayHint(parameter, template, projectUri);
				}
			}
		}
		return isAfterEndParameterVisible(node);
	}

	@Override
	public boolean visit(LetSection node) {
		if (!isAfterStartParameterVisible(node)) {
			return false;
		}
		// {#let user[:User]=item.owner }
		createInlayHintParametersSection(node);
		return isAfterEndParameterVisible(node);
	}

	public boolean visit(SetSection node) {
		if (!isAfterStartParameterVisible(node)) {
			return false;
		}
		// {#set user[:User]=item.owner }
		createInlayHintParametersSection(node);
		return isAfterEndParameterVisible(node);
	}

	public boolean visit(CustomSection node) {
		if (!isAfterStartParameterVisible(node)) {
			return false;
		}
		// {#form id[:String]=item.id }
		// {#form item.id[:String] }
		createInlayHintParametersSection(node);
		return isAfterEndParameterVisible(node);
	}

	private boolean isAfterStartParameterVisible(Section node) {
		return node.getStart() >= startOffset;
	}

	private boolean isAfterEndParameterVisible(Section node) {
		return node.getEndParametersOffset() < endOffset;
	}

	private void createInlayHintParametersSection(Section node) {
		if (!inlayHintSettings.isShowSectionParameterType()) {
			return;
		}
		// {#let user[:User]=item.owner }
		// {#set user[:User]=item.owner }
		// {#form id[:String]=item.id }
		// {#form item.id[:String] }
		Template template = node.getOwnerTemplate();
		String projectUri = template.getProjectUri();
		List<Parameter> parameters = node.getParameters();
		for (Parameter parameter : parameters) {
			createJavaTypeInlayHint(parameter, template, projectUri);
		}
	}

	private void createJavaTypeInlayHint(Parameter parameter, Template template, String projectUri) {
		createJavaTypeInlayHint(parameter, parameter, template, projectUri);
	}

	private void createJavaTypeInlayHint(Parameter parameter, Parameter javaTypeParameter, Template template,
			String projectUri) {
		CompletableFuture<ResolvedJavaTypeInfo> javaTypeFuture = getJavaType(javaTypeParameter, projectUri, javaCache);
		if (javaTypeFuture == null) {
			return;
		}
		ResolvedJavaTypeInfo javaType = javaTypeFuture.getNow(RESOLVING_JAVA_TYPE);
		if (isResolvingJavaType(javaType)) {
			resolvingJavaTypeContext.add(javaTypeFuture);
			return;
		}
		if ((!parameter.isOptional() && javaType == null)) {
			return;
		}
		createInlayHint(parameter, javaType, template);
	}

	private void createInlayHint(Parameter parameter, ResolvedJavaTypeInfo javaType, Template template) {
		try {
			InlayHint hint = new InlayHint();
			hint.setKind(InlayHintKind.Type);
			// The Java type is resolved, display it as inlay hint
			updateJavaType(hint, javaType);
			int end = parameter.hasValueAssigned() ? parameter.getEndName() : parameter.getEnd();
			Position position = template.positionAt(end);
			hint.setPosition(position);
			inlayHints.add(hint);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Error while creating inlay hint for Java parameter type", e);
		}
	}

	private static CompletableFuture<ResolvedJavaTypeInfo> getJavaType(Parameter parameter, String projectUri,
			JavaDataModelCache javaCache) {
		if (projectUri == null) {
			return null;
		}
		Expression expression = parameter.getJavaTypeExpression();
		if (expression == null) {
			return null;
		}

		CompletableFuture<ResolvedJavaTypeInfo> javaType = null;
		String literalJavaType = expression.getLiteralJavaType();
		if (literalJavaType != null) {
			javaType = javaCache.resolveJavaType(literalJavaType, projectUri);
		} else {
			javaType = javaCache.resolveJavaType(parameter, projectUri);
		}

		Section section = parameter.getOwnerSection();
		if (section.isIterable()) {
			return javaType.thenCompose(javaTypeIterable -> {
				if (javaTypeIterable == null) {
					return CompletableFuture.completedFuture(null);
				}
				if (javaTypeIterable.isIterable()) {
					return javaCache.resolveJavaType(javaTypeIterable.getIterableOf(), projectUri);
				}
				return CompletableFuture.completedFuture(javaTypeIterable);
			});
		}
		return javaType;
	}

	private static void updateJavaType(InlayHint hint, ResolvedJavaTypeInfo javaType) {
		String type = javaType != null ? javaType.getJavaElementSimpleType() : "?";
		hint.setLabel(Either.forLeft(":" + type));
	}
}

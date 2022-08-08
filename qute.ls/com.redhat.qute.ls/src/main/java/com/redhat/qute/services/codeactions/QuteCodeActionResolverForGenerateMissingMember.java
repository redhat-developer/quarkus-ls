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

import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.CodeAction;

import com.redhat.qute.commons.GenerateMissingJavaMemberParams;
import com.redhat.qute.ls.api.QuteTemplateJavaTextEditProvider;
import com.redhat.qute.utils.JSONUtility;

/**
 * Resolves generate missing member code actions.
 *
 * eg.
 * <code>
 * {
 *   edit: null,
 *   ...,
 *   data: {
 *     resolverKind: GenerateMissingMember,
 *     textDocumentUri: ...,
 *     resolverData: {
 *       memberType: ...,
 *       missingProperty: ...,
 *       javaType: ...,
 *       projectUri: ...,
 *       templateClass: ...
 *     }
 *   }
 * }
 * </code>
 *
 * @author datho7561
 */
public class QuteCodeActionResolverForGenerateMissingMember implements CodeActionResolver {

	private final QuteTemplateJavaTextEditProvider javaTextEditProvider;

	public QuteCodeActionResolverForGenerateMissingMember(QuteTemplateJavaTextEditProvider javaTextEditProvider) {
		this.javaTextEditProvider = javaTextEditProvider;
	}

	@Override
	public CompletableFuture<CodeAction> resolveCodeAction(CodeAction codeAction) {
		/*
		 * {
		 *   resolverKind: GenerateMissingMember,
		 *   textDocumentUri: ...,
		 *   resolverData: {
		 *     memberType: ...,
		 *     missingProperty: ...,
		 *     javaType: ...,
		 *     projectUri: ...,
		 *     templateClass: ...
		 *   }
		 * }
		 */
		CodeActionUnresolvedData unresolvedData = JSONUtility.toModel(codeAction.getData(),
				CodeActionUnresolvedData.class);
		/*
		 * {
		 *   memberType: ...,
		 *   missingProperty: ...,
		 *   javaType: ...,
		 *   projectUri: ...,
		 *   templateClass: ...
		 * }
		 */
		GenerateMissingJavaMemberParams params = JSONUtility.toModel(unresolvedData.getResolverData(),
				GenerateMissingJavaMemberParams.class);
		return javaTextEditProvider.generateMissingJavaMember(params).thenApply(we -> {
			codeAction.setEdit(we);
			return codeAction;
		});
	}

}

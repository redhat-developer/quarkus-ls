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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

import org.eclipse.lsp4j.CodeAction;

import com.redhat.qute.ls.api.QuteTemplateJavaTextEditProvider;
import com.redhat.qute.utils.JSONUtility;

/**
 * Represents a collections of code action resolvers that code action resolve
 * requests can be delegated to.
 *
 * @author datho7561
 */
public class QuteTemplateCodeActionResolvers {

	private static final Logger LOGGER = Logger.getLogger(QuteTemplateCodeActionResolvers.class.getName());

	private Map<CodeActionResolverKind, CodeActionResolver> resolvers;

	public QuteTemplateCodeActionResolvers() {
	}

	/**
	 * Returns the resolved code action as a completable future.
	 *
	 * @param unresolved      the unresolved code action
	 * @param javaTextEditProvider the provider which can resolve code action information
	 *                        using the associated java language server
	 * @return the resolved code action as a completable future
	 */
	public CompletableFuture<CodeAction> resolveCodeAction(CodeAction unresolved,
			QuteTemplateJavaTextEditProvider javaTextEditProvider) {

		if (resolvers == null) {
			initalizeResolvers(javaTextEditProvider);
		}

		CodeActionUnresolvedData unresolvedData = JSONUtility.toModel(unresolved.getData(),
				CodeActionUnresolvedData.class);
		CodeActionResolverKind resolverKind = unresolvedData.getResolverKind();
		CodeActionResolver resolver = resolvers.get(resolverKind);
		if (resolver != null) {
			return resolver.resolveCodeAction(unresolved);
		}

		LOGGER.severe(String.format("Cannot resolve CodeAction of kind %s", resolverKind));
		return null;
	}

	private synchronized void initalizeResolvers(QuteTemplateJavaTextEditProvider javaTextEditProvider) {
		if (resolvers == null) {
			resolvers = new HashMap<>();
			resolvers.put(CodeActionResolverKind.GenerateMissingMember,
					new QuteCodeActionResolverForGenerateMissingMember(javaTextEditProvider));
		}
	}

}

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

/**
 * Represents a class that can be used to resolve a code action
 * 
 * @author datho7561
 */
interface CodeActionResolver {

	/**
	 * Returns the resolved code action as a completable future.
	 * 
	 * @param unresolved
	 * @return the resolved code action as a completable future
	 */
	CompletableFuture<CodeAction> resolveCodeAction(CodeAction unresolved);

}

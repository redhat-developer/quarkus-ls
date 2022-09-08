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
package com.redhat.qute.ls.api;

import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;

import com.redhat.qute.commons.QuteJavadocParams;

/**
 * Represents an object that fetch the javadoc for a given Java class member.
 * 
 * @author datho7561
 */
public interface QuteJavadocProvider {

	@JsonRequest("qute/template/javadoc")
	default CompletableFuture<String> getJavadoc(QuteJavadocParams params) {
		return CompletableFuture.completedFuture(null);
	}

}

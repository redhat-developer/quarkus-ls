/*******************************************************************************
* Copyright (c) 2021 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.project;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.redhat.qute.commons.ResolvedJavaTypeInfo;
import com.redhat.qute.parser.expression.MethodPart;

/**
 * Resolve all Java type of arguments of a given {@link MethodPart} which
 * belongs to a given {@link QuteProject}.
 */
public class ResolvedMethodParameters {

	private final ResolvedJavaTypeInfo[] parameterTypesArr;
	private final CompletableFuture<Void>[] paramResolveFutures;

	public ResolvedMethodParameters(MethodPart methodPart, QuteProject project) {
		var args = methodPart.getParameters();
		parameterTypesArr = new ResolvedJavaTypeInfo[args.size()];
		paramResolveFutures = new CompletableFuture[args.size()];
		for (int i = 0; i < methodPart.getParameters().size(); i++) {
			final int index = i;
			CompletableFuture<Void> paramResolveFuture = project.resolveJavaType(methodPart.getParameters().get(i)) //
					.thenAccept(resolvedJavaType -> parameterTypesArr[index] = resolvedJavaType);
			paramResolveFutures[i] = paramResolveFuture;
		}
	}

	public CompletableFuture<List<ResolvedJavaTypeInfo>> resolve() {
		return CompletableFuture.allOf(paramResolveFutures) //
				.thenApply(unused -> Arrays.asList(parameterTypesArr));
	}
}

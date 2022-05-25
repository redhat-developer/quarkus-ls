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
package com.redhat.qute.services;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.redhat.qute.commons.JavaElementInfo;
import com.redhat.qute.commons.ResolvedJavaTypeInfo;
import com.redhat.qute.commons.datamodel.resolvers.NamespaceResolverInfo;
import com.redhat.qute.project.datamodel.ExtendedDataModelTemplate;
import com.redhat.qute.project.datamodel.resolvers.FieldValueResolver;
import com.redhat.qute.project.datamodel.resolvers.MethodValueResolver;
import com.redhat.qute.project.datamodel.resolvers.ValueResolver;

public class QuteCompletableFutures {

	public static final CompletableFuture<ResolvedJavaTypeInfo> RESOLVED_JAVA_CLASSINFO_NULL_FUTURE = CompletableFuture
			.completedFuture(null);

	public static final CompletableFuture<ExtendedDataModelTemplate> EXTENDED_TEMPLATE_DATAMODEL_NULL_FUTURE = CompletableFuture
			.completedFuture(null);

	public static final CompletableFuture<List<MethodValueResolver>> METHOD_VALUE_RESOLVERS_NULL_FUTURE = CompletableFuture
			.completedFuture(null);

	public static final CompletableFuture<List<FieldValueResolver>> FIELD_VALUE_RESOLVERS_NULL_FUTURE = CompletableFuture
			.completedFuture(null);

	public static final CompletableFuture<List<ValueResolver>> VALUE_RESOLVERS_NULL_FUTURE = CompletableFuture
			.completedFuture(null);

	public static final CompletableFuture<NamespaceResolverInfo> NAMESPACE_RESOLVER_INFO_NULL_FUTURE = CompletableFuture
			.completedFuture(null);

	public static final CompletableFuture<JavaElementInfo> JAVA_ELEMENT_INFO_NULL_FUTURE = CompletableFuture
			.completedFuture(null);

}

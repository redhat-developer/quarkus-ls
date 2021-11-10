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
package com.redhat.qute.services.commands;

import java.util.concurrent.CompletableFuture;

import com.redhat.qute.commons.ResolvedJavaTypeInfo;
import com.redhat.qute.commons.datamodel.DataModelParameter;

public class ResolvedDataModelParameter extends DataModelParameter {
	private final CompletableFuture<ResolvedJavaTypeInfo> future;

	public ResolvedDataModelParameter(DataModelParameter parameter,
			CompletableFuture<ResolvedJavaTypeInfo> resolvedJavaType) {
		super.setKey(parameter.getKey());
		super.setSourceType(parameter.getSourceType());
		this.future = resolvedJavaType;
	}

	public ResolvedJavaTypeInfo getResolvedJavaType() {
		return future.getNow(null);
	}

}

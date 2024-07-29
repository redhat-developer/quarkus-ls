/*******************************************************************************
* Copyright (c) 2024 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
* 
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.microprofile.jdt.internal.quarkus.builditems;

import org.eclipse.lsp4mp.jdt.core.java.diagnostics.IJavaErrorCode;

/**
 * Represents error codes for validation issues in classes inheriting
 * <code>io.quarkus.builder.item.BuildItem</code>.
 */
public enum QuarkusBuildItemErrorCode implements IJavaErrorCode {

	InvalidModifierBuildItem;

	@Override
	public String getCode() {
		return name();
	}
}

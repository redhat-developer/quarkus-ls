/*******************************************************************************
* Copyright (c) 2026 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.jdt.internal.java;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.lsp4j.CodeLens;

import com.redhat.qute.jdt.utils.IJDTUtils;

/**
 * Context to collect LSP codeLens for template links of a given Java file.
 */
public class QuteJavaCodeLensCollectorContext extends QuteTemplateLinkCollectorContext {

	private List<CodeLens> codeLenses;

	public QuteJavaCodeLensCollectorContext(ITypeRoot typeRoot, IJDTUtils utils) {
		super(typeRoot, utils);
		this.codeLenses = new ArrayList<>();
	}

	public List<CodeLens> getCodeLenses() {
		return codeLenses;
	}
}

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
import org.eclipse.lsp4j.DocumentLink;

import com.redhat.qute.jdt.utils.IJDTUtils;

/**
 * Context to collect LSP documentLink for template links of a given Java file.
 */
public class QuteJavaDocumentLinkCollectorContext extends QuteTemplateLinkCollectorContext {

	private List<DocumentLink> links;

	public QuteJavaDocumentLinkCollectorContext(ITypeRoot typeRoot, IJDTUtils utils) {
		super(typeRoot, utils);
		this.links = new ArrayList<>();
	}

	public List<DocumentLink> getLinks() {
		return links;
	}
}

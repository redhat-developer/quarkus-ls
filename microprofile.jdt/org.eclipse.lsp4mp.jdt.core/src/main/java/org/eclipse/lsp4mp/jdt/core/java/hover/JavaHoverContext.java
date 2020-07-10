/*******************************************************************************
* Copyright (c) 2020 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lsp4mp.jdt.core.java.hover;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4mp.commons.DocumentFormat;
import org.eclipse.lsp4mp.jdt.core.java.AbtractJavaContext;
import org.eclipse.lsp4mp.jdt.core.utils.IJDTUtils;

/**
 * Java hover context for a given compilation unit.
 * 
 * @author Angelo ZERR
 *
 */
public class JavaHoverContext extends AbtractJavaContext {

	private final Position hoverPosition;

	private final IJavaElement hoverElement;

	private final DocumentFormat documentFormat;

	public JavaHoverContext(String uri, ITypeRoot typeRoot, IJDTUtils utils, IJavaElement hoverElement,
			Position hoverPosition, DocumentFormat documentFormat) {
		super(uri, typeRoot, utils);
		this.hoverElement = hoverElement;
		this.hoverPosition = hoverPosition;
		this.documentFormat = documentFormat;
	}

	public DocumentFormat getDocumentFormat() {
		return documentFormat;
	}

	public IJavaElement getHoverElement() {
		return hoverElement;
	}

	public Position getHoverPosition() {
		return hoverPosition;
	}

}

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
package org.eclipse.lsp4mp.commons;

import org.eclipse.lsp4j.CodeActionContext;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentIdentifier;

/**
 * MicroProfile Java codeAction parameters.
 * 
 * @author Angelo ZERR
 *
 */
public class MicroProfileJavaCodeActionParams extends CodeActionParams {

	private boolean resourceOperationSupported;

	public MicroProfileJavaCodeActionParams() {
		super();
	}

	public MicroProfileJavaCodeActionParams(final TextDocumentIdentifier textDocument, final Range range,
			final CodeActionContext context) {
		super(textDocument, range, context);
	}

	public String getUri() {
		return getTextDocument().getUri();
	}

	public boolean isResourceOperationSupported() {
		return resourceOperationSupported;
	}

	public void setResourceOperationSupported(boolean resourceOperationSupported) {
		this.resourceOperationSupported = resourceOperationSupported;
	}

}

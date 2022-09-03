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
package com.redhat.qute.settings;

import org.eclipse.lsp4j.CompletionCapabilities;
import org.eclipse.lsp4j.InsertTextMode;

/**
 * A wrapper around LSP {@link CompletionCapabilities}.
 *
 */
public class QuteCompletionSettings {

	private CompletionCapabilities completionCapabilities;

	public void setCapabilities(CompletionCapabilities completionCapabilities) {
		this.completionCapabilities = completionCapabilities;
	}

	public CompletionCapabilities getCompletionCapabilities() {
		return completionCapabilities;
	}

	/**
	 * Returns <code>true</code> if the client support snippet and
	 * <code>false</code> otherwise.
	 * 
	 * @return <code>true</code> if the client support snippet and
	 *         <code>false</code> otherwise.
	 */
	public boolean isCompletionSnippetsSupported() {
		return completionCapabilities != null && completionCapabilities.getCompletionItem() != null
				&& completionCapabilities.getCompletionItem().getSnippetSupport() != null
				&& completionCapabilities.getCompletionItem().getSnippetSupport();
	}

	/**
	 * Returns <code>true</code> if the client support the given documentation
	 * format and <code>false</code> otherwise.
	 * 
	 * @return <code>true</code> if the client support the given documentation
	 *         format and <code>false</code> otherwise.
	 */
	public boolean isDocumentationFormatSupported(String documentationFormat) {
		return completionCapabilities != null && completionCapabilities.getCompletionItem() != null
				&& completionCapabilities.getCompletionItem().getDocumentationFormat() != null
				&& completionCapabilities.getCompletionItem().getDocumentationFormat().contains(documentationFormat);
	}

	public boolean canSupportMarkupKind(String kind) {
		return getCompletionCapabilities() != null && getCompletionCapabilities().getCompletionItem() != null
				&& getCompletionCapabilities().getCompletionItem().getDocumentationFormat() != null
				&& getCompletionCapabilities().getCompletionItem().getDocumentationFormat().contains(kind);
	}

	/**
	 * Returns <code>true</code> if the client support
	 * {@link InsertTextMode#AdjustIndentation} <code>false</code> otherwise.
	 * 
	 * @return <code>true</code> if the client support
	 *         {@link InsertTextMode#AdjustIndentation} <code>false</code>
	 *         otherwise.
	 */
	public boolean isInsertTextModeAdjustIndentationSupported() {
		return completionCapabilities != null && completionCapabilities.getCompletionItem() != null
				&& completionCapabilities.getCompletionItem().getInsertTextModeSupport() != null
				&& completionCapabilities.getCompletionItem().getInsertTextModeSupport().getValueSet() != null
				&& completionCapabilities.getCompletionItem().getInsertTextModeSupport().getValueSet()
						.contains(InsertTextMode.AdjustIndentation);
	}

	/**
	 * Returns the default {@link InsertTextMode} supported by the client.
	 * 
	 * @return the default {@link InsertTextMode} supported by the client.
	 */
	public InsertTextMode getDefaultInsertTextMode() {
		if (completionCapabilities != null && completionCapabilities.getInsertTextMode() != null) {
			return completionCapabilities.getInsertTextMode();
		}
		return InsertTextMode.AsIs;
	}
}

/*******************************************************************************
* Copyright (c) 2023 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.services.completions;

import java.util.Objects;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.xtext.xbase.lib.util.ToStringBuilder;

/**
 * Represents data that is attached to a {@link CompletionItem} that's required
 * in order to resolve it.
 * 
 * @author Angelo ZERR
 */
public class CompletionItemUnresolvedData {

	private String textDocumentUri;

	private CompletionItemResolverKind resolverKind;
	private Object resolverData;

	public CompletionItemUnresolvedData(String textDocumentUri, CompletionItemResolverKind resolverKind,
			Object resolverData) {
		this.textDocumentUri = textDocumentUri;
		this.resolverKind = resolverKind;
		this.resolverData = resolverData;
	}

	CompletionItemUnresolvedData() {
	}

	/**
	 * Returns the uri of the text document that the diagnostic associated with the
	 * {@link CompletionItem} is coming from.
	 * 
	 * @return the uri of the text document that the diagnostic associated with the
	 *         {@link CompletionItem} is coming from
	 */
	public String getTextDocumentUri() {
		return this.textDocumentUri;
	}

	/**
	 * Returns the kind of the resolver that is needed to resolve the
	 * {@link CompletionItem}.
	 * 
	 * @return the kind of the resolver that is needed to resolve the
	 *         {@link CompletionItem}
	 */
	public CompletionItemResolverKind getResolverKind() {
		return this.resolverKind;
	}

	/**
	 * Returns the data that is needed in order to resolve the
	 * {@link CompletionItem} that is
	 * specific to the resolver kind.
	 * 
	 * @return the data that is needed in order to resolve the
	 *         {@link CompletionItem} that is
	 *         specific to the resolver kind
	 */
	public Object getResolverData() {
		return this.resolverData;
	}

	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof CompletionItemUnresolvedData)) {
			return false;
		}
		CompletionItemUnresolvedData that = (CompletionItemUnresolvedData) other;
		return Objects.equals(this.textDocumentUri, that.textDocumentUri) //
				&& Objects.equals(resolverKind, resolverKind);
	}

	public String toString() {
		ToStringBuilder builder = new ToStringBuilder(this);
		builder.add("textDocumentUri", textDocumentUri);
		builder.add("resolverKind", resolverKind);
		builder.add("resolverData", resolverData);
		return builder.toString();
	}

}

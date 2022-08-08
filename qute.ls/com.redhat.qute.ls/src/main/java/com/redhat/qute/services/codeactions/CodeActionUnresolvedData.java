/*******************************************************************************
* Copyright (c) 2022 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.services.codeactions;

import java.util.Objects;

import org.eclipse.xtext.xbase.lib.util.ToStringBuilder;

/**
 * Represents data that is attached to a CodeAction that's required in order to
 * resolve it.
 * 
 * @author datho7561
 */
public class CodeActionUnresolvedData {

	private String textDocumentUri;
	private CodeActionResolverKind resolverKind;
	private Object resolverData;

	public CodeActionUnresolvedData(String textDocumentUri, CodeActionResolverKind resolverKind, Object resolverData) {
		this.textDocumentUri = textDocumentUri;
		this.resolverKind = resolverKind;
		this.resolverData = resolverData;
	}

	CodeActionUnresolvedData() {
	}

	/**
	 * Returns the kind of the resolver that is needed to resolve the code action.
	 * 
	 * @return the kind of the resolver that is needed to resolve the code action
	 */
	public CodeActionResolverKind getResolverKind() {
		return this.resolverKind;
	}

	/**
	 * Returns the uri of the text document that the diagnostic associated with the
	 * code action is coming from.
	 * 
	 * @return the uri of the text document that the diagnostic associated with the
	 *         code action is coming from
	 */
	public String getTextDocumentUri() {
		return this.textDocumentUri;
	}

	/**
	 * Returns the data that is needed in order to resolve the CodeAction that is
	 * specific to the resolver kind.
	 * 
	 * @return the data that is needed in order to resolve the CodeAction that is
	 *         specific to the resolver kind
	 */
	public Object getResolverData() {
		return this.resolverData;
	}

	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof CodeActionUnresolvedData)) {
			return false;
		}
		CodeActionUnresolvedData that = (CodeActionUnresolvedData) other;
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

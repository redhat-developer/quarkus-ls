/*******************************************************************************
* Copyright (c) 2023 Red Hat Inc. and others.
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License v. 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
* which is available at https://www.apache.org/licenses/LICENSE-2.0.
*
* SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.services.completions;

import java.util.Objects;

import org.eclipse.lsp4j.CompletionItem;

import com.redhat.qute.utils.JSONUtility;

/**
 * Represents data sent to for completionItem/resolve
 *
 * { 'data': { 'offset': {...}, 'uri': {...} } }
 */
public class CompletionData {

	private String uri;

	private Integer offset;

	public CompletionData(String uri, Integer offset) {
		setUri(uri);
		setOffset(offset);
	}

	public static CompletionData getCompletionData(CompletionItem unresolved) {
		return JSONUtility.toModel(unresolved.getData(), CompletionData.class);
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public Integer getOffset() {
		return offset;
	}

	public void setOffset(Integer offset) {
		this.offset = offset;
	}

	@Override
	public int hashCode() {
		return Objects.hash(offset, uri);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CompletionData other = (CompletionData) obj;
		return Objects.equals(offset, other.offset) && Objects.equals(uri, other.uri);
	}

}

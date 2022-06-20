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
package com.redhat.qute.ls.commons.snippets;

import java.util.List;

/**
 * Abstract snippet context used to retrieve the prefix.
 *
 * @param <T> the value type waited by the snippet context.
 */
public abstract class AbstractSnippetContext<T> implements ISnippetContext<T> {

	private List<String> prefixes;

	@Override
	public void setPrefixes(List<String> prefixes) {
		this.prefixes = prefixes;
	}

	@Override
	public List<String> getPrefixes() {
		return prefixes;
	}
}

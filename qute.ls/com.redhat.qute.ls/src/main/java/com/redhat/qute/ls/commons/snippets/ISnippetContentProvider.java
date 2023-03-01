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
package com.redhat.qute.ls.commons.snippets;

import java.util.Map;

/**
 * Snippet content provider API used to generate the insert text for a given
 * completion item based on an {@link Snippet}.
 * 
 * @author Angelo ZERR
 *
 */
public interface ISnippetContentProvider {

	/**
	 * Returns the insert text to use for the completion item based on the given
	 * <code>snippet</code>.
	 * 
	 * @param snippet           the snippet.
	 * @param model             the model.
	 * @param replace           true if body lines must be replaced by some
	 *                          values coming from the given model and false
	 *                          otherwise.
	 * @param lineDelimiter     the line delimiter.
	 * @param whitespacesIndent the whitespaces indent.
	 * 
	 * @return the insert text to use for the completion item based on the given
	 *         <code>snippet</code>.
	 */
	String getInsertText(Snippet snippet, Map<String, String> model, boolean replace,
			String lineDelimiter, String whitespacesIndent);
}
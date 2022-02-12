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
package com.redhat.qute.ls.commons.snippets;

/**
 * Snippet registry provider.
 * 
 * @author Angelo ZERR
 *
 * @param <T>
 */
public interface SnippetRegistryProvider<T extends Snippet> {

	/**
	 * Returns the snippet registry.
	 * 
	 * @return the snippet registry.
	 */
	SnippetRegistry<T> getSnippetRegistry();
}

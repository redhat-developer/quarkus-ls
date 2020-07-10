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
package org.eclipse.lsp4mp.snippets;

import org.eclipse.lsp4mp.ls.commons.snippets.ISnippetRegistryLoader;
import org.eclipse.lsp4mp.ls.commons.snippets.SnippetRegistry;

/**
 * Snippet loader for MicroProfile in properties files.
 * 
 * @author Angelo ZERR
 *
 */
public class MicroProfilePropertiesSnippetRegistryLoader implements ISnippetRegistryLoader {

	@Override
	public void load(SnippetRegistry registry) throws Exception {
		// No mp snippets for properties file for the moment...
	}

	@Override
	public String getLanguageId() {
		return LanguageId.properties.name();
	}

}

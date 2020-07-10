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
package com.redhat.quarkus.snippets;

import org.eclipse.lsp4mp.ls.commons.snippets.ISnippetRegistryLoader;
import org.eclipse.lsp4mp.ls.commons.snippets.SnippetRegistry;
import org.eclipse.lsp4mp.snippets.LanguageId;
import org.eclipse.lsp4mp.snippets.SnippetContextForProperties;

/**
 * Snippet loader for Quarkus in properties files.
 * 
 * @author Angelo ZERR
 *
 */
public class QuarkusPropertiesSnippetRegistryLoader implements ISnippetRegistryLoader {

	@Override
	public void load(SnippetRegistry registry) throws Exception {
		registry.registerSnippets(
				QuarkusPropertiesSnippetRegistryLoader.class.getResourceAsStream("quarkus-properties.json"),
				SnippetContextForProperties.TYPE_ADAPTER);
	}

	@Override
	public String getLanguageId() {
		return LanguageId.properties.name();
	}

}

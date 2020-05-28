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

import java.io.IOException;

import com.redhat.microprofile.ls.commons.snippets.ISnippetRegistryLoader;
import com.redhat.microprofile.ls.commons.snippets.SnippetRegistry;
import com.redhat.microprofile.snippets.LanguageId;
import com.redhat.microprofile.snippets.SnippetContextForJava;

/**
 * Snippet loader for Quarkus in java files.
 * 
 * @author Angelo ZERR
 *
 */
public class QuarkusJavaSnippetRegistryLoader implements ISnippetRegistryLoader {

	@Override
	public void load(SnippetRegistry registry) throws IOException {
		registry.registerSnippets(QuarkusJavaSnippetRegistryLoader.class.getResourceAsStream("quarkus-java.json"),
				SnippetContextForJava.TYPE_ADAPTER);
	}

	@Override
	public String getLanguageId() {
		return LanguageId.java.name();
	}

}

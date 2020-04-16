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
package com.redhat.microprofile.snippets;

import java.io.IOException;

import com.redhat.microprofile.ls.commons.snippets.ISnippetRegistryLoader;
import com.redhat.microprofile.ls.commons.snippets.SnippetRegistry;

/**
 * Snippet loader for MicroProfile in java files.
 * 
 * @author Angelo ZERR
 *
 */
public class MicroProfileJavaSnippetRegistryLoader implements ISnippetRegistryLoader {

	@Override
	public void load(SnippetRegistry registry) throws IOException {
		registry.registerSnippets(MicroProfileJavaSnippetRegistryLoader.class.getResourceAsStream("mp-metrics.json"),
				SnippetContextForJava.TYPE_ADAPTER);
		registry.registerSnippets(MicroProfileJavaSnippetRegistryLoader.class.getResourceAsStream("mp-openapi.json"),
				SnippetContextForJava.TYPE_ADAPTER);
		registry.registerSnippets(MicroProfileJavaSnippetRegistryLoader.class.getResourceAsStream("mp-faulttolerance.json"),
				SnippetContextForJava.TYPE_ADAPTER);
	}

	@Override
	public String getLanguageId() {
		return LanguageId.java.name();
	}

}

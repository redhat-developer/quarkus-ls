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
package com.redhat.qute.services.snippets;

import com.redhat.qute.ls.commons.snippets.ISnippetRegistryLoader;
import com.redhat.qute.ls.commons.snippets.SnippetRegistry;

/**
 * Load default Qute snippets.
 *
 */
public class QuteSnippetRegistryLoader implements ISnippetRegistryLoader {

	@Override
	public void load(SnippetRegistry registry) throws Exception {
		registry.registerSnippets(QuteSnippetRegistryLoader.class.getResourceAsStream("qute-snippets.json"),
				QuteSnippetContext.IN_TEXT);
	}

}

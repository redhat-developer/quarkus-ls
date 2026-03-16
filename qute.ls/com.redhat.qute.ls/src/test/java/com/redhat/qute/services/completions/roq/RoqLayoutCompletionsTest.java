/*******************************************************************************
* Copyright (c) 2026 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.services.completions.roq;

import static com.redhat.qute.QuteAssert.assertCompletion;
import static com.redhat.qute.QuteAssert.c;
import static com.redhat.qute.QuteAssert.r;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.eclipse.lsp4j.CompletionList;
import org.junit.jupiter.api.Test;

import com.redhat.qute.project.roq.RoqProjectQuteLanguageServer;

/**
 * Test completion with Roq Quarkus extension and data files.
 *
 * @author Angelo ZERR
 * 
 */
public class RoqLayoutCompletionsTest {

	@Test
	public void layout() throws Exception {

		// template/layouts/default.hml defines {#insert head /}
		String template = "---\r\n" + //
				"layout: default\r\n" + //
				"---\r\n" + //
				"{#head /}\r\n" + // <-- head coming from default.html
				"{#headXXXX /}"; // <-- UndefinedSectionTag

		RoqProjectQuteLanguageServer server = new RoqProjectQuteLanguageServer();
		// Open user tag -> name parameter has no type
		server.didOpenWithContent("test.html", template);
		CompletionList result = server.completionFile("test.html", 3, 2); // {#|
		assertNotNull(result);
		assertFalse(result.getItems().isEmpty());

		assertCompletion(result, null, false, //
				c("head", "{#head}{/head}", r(3, 0, 3, 8)));

	}

}
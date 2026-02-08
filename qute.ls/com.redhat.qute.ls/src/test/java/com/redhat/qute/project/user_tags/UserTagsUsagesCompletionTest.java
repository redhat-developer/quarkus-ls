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
package com.redhat.qute.project.user_tags;

import static com.redhat.qute.QuteAssert.assertCompletion;
import static com.redhat.qute.QuteAssert.c;
import static com.redhat.qute.QuteAssert.r;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.lsp4j.CompletionList;
import org.junit.jupiter.api.Test;

/**
 * Test completion on user tag parameters that infer their type based on how the
 * user tag is used.
 */
public class UserTagsUsagesCompletionTest {

	@Test
	public void completion() throws Exception {
		RenardeProjectQuteLanguageServer server = new RenardeProjectQuteLanguageServer();

		// Open user tag -> name parameter has no type
		server.didOpenWithContent("tags/input.html", "<input name=\"{name.");
		CompletionList result = server.completionFile("tags/input.html", 0, 19); // completion on <input name="{name.|
		assertNotNull(result);
		assertTrue(result.getItems().isEmpty());

		// Open a template which call user tag with Integer type -> name has Integer
		// type
		server.didOpenWithContent("main.html", "{#input name=10 }");
		result = server.completionFile("tags/input.html", 0, 19); // completion on <input name="{name.|
		assertNotNull(result);
		assertCompletion(result, null, false, //
				c("byteValue() : byte", "byteValue", r(0, 19, 0, 19)));

		// Update the template which call user tag with String type -> name has String
		// type
		server.didChangeFile("main.html", "\"FOO\"", 0, 13, 15); // --> {#input name="FOO" } 
		result = server.completionFile("tags/input.html", 0, 19); // completion on <input name="{name.|
		assertNotNull(result);
		assertCompletion(result, null, false, //
				c("codePointCount(beginIndex : int, endIndex : int) : int", "codePointCount(beginIndex, endIndex)",
						r(0, 19, 0, 19)));

		// Remove name parameter
		server.didChangeFile("main.html", "", 0, 13, 18); // --> {#input name= }
		result = server.completionFile("tags/input.html", 0, 19); // completion on <input name=\"{name.|
		assertNotNull(result);
		assertTrue(result.getItems().isEmpty());

		// Insert name parameter as Integer
		server.didChangeFile("main.html", "10", 0, 13, 13); // --> {#input name=10 } 
		result = server.completionFile("tags/input.html", 0, 19); // completion on <input name="{name.|
		assertNotNull(result);
		assertCompletion(result, null, false, //
				c("byteValue() : byte", "byteValue", r(0, 19, 0, 19)));

	}
}

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

import static com.redhat.qute.QuteAssert.assertHover;
import static com.redhat.qute.QuteAssert.r;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.eclipse.lsp4j.Hover;
import org.junit.jupiter.api.Test;

/**
 * Test hover on user tag parameters that infer their type based on how the user
 * tag is used.
 */
public class UserTagsUsagesHoverTest {

	@Test
	public void hover() throws Exception {
		RenardeProjectQuteLanguageServer server = new RenardeProjectQuteLanguageServer();

		// Open user tag -> name parameter has no type
		server.didOpenWithContent("tags/input.html", "<input name=\"{name}\" />");
		Hover hover = server.hoverFile("tags/input.html", 0, 15); // hover {n|ame}
		assertNull(hover);

		// Open a template which call user tag with Integer type -> name has Integer
		// type
		server.didOpenWithContent("main.html", "{#input name=10 }");
		hover = server.hoverFile("tags/input.html", 0, 15); // hover {n|ame}
		assertNotNull(hover);
		assertHover(hover, //
				"Integer", //
				r(0, 14, 0, 18));

		// Update the template which call user tag with String type -> name has String
		// type
		server.didChangeFile("main.html", "\"FOO\"", 0, 13, 15);
		hover = server.hoverFile("tags/input.html", 0, 15); // hover {n|ame}
		assertNotNull(hover);
		assertHover(hover, //
				"String", //
				r(0, 14, 0, 18));

		// Remove name parameter
		server.didChangeFile("main.html", "", 0, 13, 18); // --> {#input name= }
		hover = server.hoverFile("tags/input.html", 0, 15); // hover {n|ame}
		assertNull(hover);

		// Insert name parameter as Integer
		server.didChangeFile("main.html", "10", 0, 13, 13); // --> {#input name=10 }
		hover = server.hoverFile("tags/input.html", 0, 15); // hover {n|ame}
		assertNotNull(hover);
		assertHover(hover, //
				"Integer", //
				r(0, 14, 0, 18));
	}
}

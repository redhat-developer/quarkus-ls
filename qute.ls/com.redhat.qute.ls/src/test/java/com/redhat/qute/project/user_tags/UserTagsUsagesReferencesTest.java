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

import static com.redhat.qute.QuteAssert.assertLocations;
import static com.redhat.qute.QuteAssert.l;
import static com.redhat.qute.QuteAssert.r;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.eclipse.lsp4j.Location;
import org.junit.jupiter.api.Test;

/**
 * Test references on user tag parameters that infer their type based on how the
 * user tag is used.
 */
public class UserTagsUsagesReferencesTest {

	@Test
	public void references() throws Exception {
		RenardeProjectQuteLanguageServer server = new RenardeProjectQuteLanguageServer();

		// Open user tag -> name parameter has no type
		server.didOpenWithContent("tags/input.html", "<input name=\"{name}\" />");
		List<? extends Location> references = server.referencesFile("tags/input.html", 0, 15); // references {n|ame}
		assertTrue(references.isEmpty());

		// Open a template which call user tag with Integer type -> name has Integer
		// type
		String main1Uri = server.didOpenWithContent("main1.html", "{#input name=10 }").getUri();
		references = server.referencesFile("tags/input.html", 0, 15); // references {n|ame}
		assertLocations(references, //
				l(main1Uri, r(0, 8, 0, 15)));

		// Open a second template which call user tag with Integer type -> name has
		// Integer
		// type
		String main2Uri = server.didOpenWithContent("main2.html", "<p>{#input name=10 }</p>").getUri();
		references = server.referencesFile("tags/input.html", 0, 15); // references {n|ame}
		assertLocations(references, //
				l(main1Uri, r(0, 8, 0, 15)), //
				l(main2Uri, r(0, 11, 0, 18)));

	}
}

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
package com.redhat.qute.services.codelens.roq;

import static com.redhat.qute.QuteAssert.assertCodeLens;
import static com.redhat.qute.QuteAssert.cl;
import static com.redhat.qute.QuteAssert.r;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.eclipse.lsp4j.CodeLens;
import org.junit.jupiter.api.Test;

import com.redhat.qute.project.roq.RoqProjectQuteLanguageServer;

/**
 * Test codeLens with Roq Quarkus extension and layout.
 *
 * @author Angelo ZERR
 * 
 */
public class RoqLayoutCodeLensTest {

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
		List<? extends CodeLens> result = server.codeLensFile("test.html");
		assertNotNull(result);
		assertEquals(4, result.size());

		assertCodeLens(result, //
				cl(r(0, 0, 0, 0), "site : Site", ""), //
				cl(r(0, 0, 0, 0), "page : NormalPage", ""), //
				cl(r(0, 0, 0, 0), "Included by:", ""), //
				cl(r(0, 0, 0, 0), "default", ""));
	}

}
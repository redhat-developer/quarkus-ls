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
package com.redhat.qute.services.definition.roq;

import static com.redhat.qute.QuteAssert.assertLocations;
import static com.redhat.qute.QuteAssert.l;
import static com.redhat.qute.QuteAssert.r;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.LocationLink;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.junit.jupiter.api.Test;

import com.redhat.qute.project.roq.RoqProject;
import com.redhat.qute.project.roq.RoqProjectQuteLanguageServer;

/**
 * Test definition with Roq Quarkus extension and layout.
 *
 * @author Angelo ZERR
 * 
 */
public class RoqLayoutDefinitionTest {

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
		Either<List<? extends Location>, List<? extends LocationLink>> result = server.definitionFile("test.html", 3,
				4); // {#he|ad}
		assertNotNull(result);
		assertTrue(result.isLeft());

		String defaultUri = RoqProject.getFileUri("/templates/layouts/default.html");
		List<? extends Location> actual = result.getLeft();
		assertLocations(actual, l(defaultUri, r(3, 11, 3, 15)));
	}

}
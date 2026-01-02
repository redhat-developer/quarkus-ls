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
package com.redhat.qute.services.definition.renarde;

import static com.redhat.qute.QuteAssert.ll;
import static com.redhat.qute.QuteAssert.r;

import java.nio.file.Paths;

import org.eclipse.lsp4j.LocationLink;
import org.junit.jupiter.api.Test;

import com.redhat.qute.QuteAssert;
import com.redhat.qute.project.MockQuteProject;
import com.redhat.qute.project.renarde.RenardeProject;

/**
 * Test definition in Renarde.
 * 
 * @author Angelo ZERR
 *
 */
public class RenardeMessagesDefinitionTest {

	@Test
	public void definitionInUndefinedObject() throws Exception {
		String template = "{m:main.lo|ginXXX}";
		testDefinitionFor(template);
	}

	@Test
	public void definitionInDefinedObject() throws Exception {

		String messagesUri = getMessagesUri("messages.properties");
		String messagesFRUri = getMessagesUri("messages_fr.properties");

		String template = "{m|:main.login}";
		testDefinitionFor(template);

		template = "{m:ma|in.login}";
		testDefinitionFor(template, //
				ll(messagesUri, r(0, 1, 0, 13), r(0, 0, 0, 0)), //
				ll(messagesFRUri, r(0, 1, 0, 13), r(0, 0, 0, 0)));

		template = "{m:main.log|in}";
		testDefinitionFor(template, //
				ll(messagesUri, r(0, 1, 0, 13), r(0, 0, 0, 0)), //
				ll(messagesFRUri, r(0, 1, 0, 13), r(0, 0, 0, 0)));

	}

	private static String getMessagesUri(String fileName) {
		return Paths.get(MockQuteProject.getProjectPath(RenardeProject.PROJECT_URI) + "/src/main/resources/" + fileName)
				.toAbsolutePath().toUri().toASCIIString();
	}

	public static void testDefinitionFor(String value, LocationLink... expected) throws Exception {
		QuteAssert.testDefinitionFor(value, null, RenardeProject.PROJECT_URI, QuteAssert.TEMPLATE_BASE_DIR, expected);
	}
}

/*******************************************************************************
* Copyright (c) 2022 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.services.definition;

import static com.redhat.qute.QuteAssert.getFileUri;
import static com.redhat.qute.QuteAssert.ll;
import static com.redhat.qute.QuteAssert.r;
import static com.redhat.qute.QuteAssert.testDefinitionFor;

import org.junit.jupiter.api.Test;

/**
 * Test definition with section tag.
 * 
 * @author Angelo ZERR
 *
 */
public class QuteDefinitionInSectionTagTest {

	@Test
	public void definitionInStartTagSection() throws Exception {
		String template = "{|#each items}\r\n" + //
				"		{it.name}\r\n" + //
				"{/each}";
		testDefinitionFor(template, "test.qute", //
				ll("test.qute", r(0, 1, 0, 6), r(2, 1, 2, 6)));

		template = "{#ea|ch items}\r\n" + //
				"		{it.name}\r\n" + //
				"{/each}";
		testDefinitionFor(template, "test.qute", //
				ll("test.qute", r(0, 1, 0, 6), r(2, 1, 2, 6)));

		template = "{#each| items}\r\n" + //
				"		{it.name}\r\n" + //
				"{/each}";
		testDefinitionFor(template, "test.qute", //
				ll("test.qute", r(0, 1, 0, 6), r(2, 1, 2, 6)));

		template = "{#each |items}\r\n" + //
				"		{it.name}\r\n" + //
				"{/each}";
		testDefinitionFor(template);

	}

	@Test
	public void definitionInEndTagSection() throws Exception {
		String template = "{#each items}\r\n" + //
				"		{it.name}\r\n" + //
				"{|/each}";
		testDefinitionFor(template, "test.qute", //
				ll("test.qute", r(2, 1, 2, 6), r(0, 1, 0, 6)));

		template = "{#each items}\r\n" + //
				"		{it.name}\r\n" + //
				"{/e|ach}";
		testDefinitionFor(template, "test.qute", //
				ll("test.qute", r(2, 1, 2, 6), r(0, 1, 0, 6)));

		template = "{#each items}\r\n" + //
				"		{it.name}\r\n" + //
				"{/each|}";
		testDefinitionFor(template, "test.qute", //
				ll("test.qute", r(2, 1, 2, 6), r(0, 1, 0, 6)));

		template = "{#each items}\r\n" + //
				"		{it.name}\r\n" + //
				"|{/each}";
		testDefinitionFor(template);

		template = "{#each items}\r\n" + //
				"		{it.name}\r\n" + //
				"{/each}|";
		testDefinitionFor(template);

	}

	@Test
	public void noStartTag() throws Exception {
		String template = "{/|each}";
		testDefinitionFor(template);
	}

	@Test
	public void noEndTag() throws Exception {
		String template = "{#|each items}";
		testDefinitionFor(template);
	}

	@Test
	public void userTag() throws Exception {
		String userTagUri = getFileUri("/tags/formElement.html");
		String template = "{|#formElement /}";
		testDefinitionFor(template, "test.qute", //
				ll(userTagUri, r(0, 1, 0, 13), r(0, 0, 0, 0)));
	}
}

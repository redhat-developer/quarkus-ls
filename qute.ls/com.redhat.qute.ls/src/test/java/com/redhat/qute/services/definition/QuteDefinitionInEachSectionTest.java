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
package com.redhat.qute.services.definition;

import static com.redhat.qute.QuteAssert.ll;
import static com.redhat.qute.QuteAssert.r;
import static com.redhat.qute.QuteAssert.testDefinitionFor;

import org.junit.jupiter.api.Test;

/**
 * Test definition with #each section.
 * 
 * @author Angelo ZERR
 *
 */
public class QuteDefinitionInEachSectionTest {

	@Test
	public void undefinedObject() throws Exception {
		String template = "{#each ite|ms}\r\n" + //
				"		{it.name}\r\n" + //
				"{/each}";
		testDefinitionFor(template);
	}

	@Test
	public void definedObject() throws Exception {
		String template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				"{#each ite|ms}\r\n" + //
				"		{it.name}\r\n" + //
				"{/each}";
		testDefinitionFor(template, "test.qute", //
				ll("test.qute", r(1, 7, 1, 12), r(0, 32, 0, 37)));
	}

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
	public void definitionInDefaultAlias() throws Exception {
		String template = "{#each items}\r\n" + //
				"		{i|t.name}\r\n" + //
				"{/ea|ch}";
		testDefinitionFor(template);
	}
}

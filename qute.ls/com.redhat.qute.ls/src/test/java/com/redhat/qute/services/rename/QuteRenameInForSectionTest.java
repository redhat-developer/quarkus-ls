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
package com.redhat.qute.services.rename;

import static com.redhat.qute.QuteAssert.assertRename;
import static com.redhat.qute.QuteAssert.edits;
import static com.redhat.qute.QuteAssert.r;

import org.junit.jupiter.api.Test;

import com.redhat.qute.ls.commons.BadLocationException;

/**
 * Tests for Qute rename inside #for section.
 *
 */
public class QuteRenameInForSectionTest {

	@Test
	public void fromAlias() throws BadLocationException {
		String template = //
				"{#for it|em in items}\r\n" + //
				"	{item.name}\r\n" + //
				"{/for}";
		assertRename(template, "test", //
				edits("test", //
						r(0, 6, 0, 10), // {#for it|em in items}
						r(1, 2, 1, 6) // {item.name}
				));
	}

	@Test
	public void toAlias() throws BadLocationException {
		String template = //
				"{#for item in items}\r\n" + //
				"	{it|em.name}\r\n" + //
				"{/for}";
		assertRename(template, "test");
	}

	@Test
	public void unbalanced() throws BadLocationException {
		String template = //
				"{#for ite|m in items}\r\n" + //
				"{/each}";
		assertRename(template, "test");
	}

	@Test
	public void fromAliasInElseBlock() throws BadLocationException {
		String template = //
				"{#for it|em in items}\r\n" + //
				"{#else}" + //
				"	{item.name}\r\n" + //
				"{/for}";
		assertRename(template, "test");
	}

	@Test
	public void emptyParameterName() throws BadLocationException {
		String template = //
				"{#let |=3}\r\n" + //
				"{/let}";
		assertRename(template, "test");
	}
}

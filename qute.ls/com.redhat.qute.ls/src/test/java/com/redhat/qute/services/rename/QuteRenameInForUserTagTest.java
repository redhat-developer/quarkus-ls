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
 * Tests for Qute rename in user tag.
 *
 */
public class QuteRenameInForUserTagTest {

	@Test
	public void fromAlias() throws BadLocationException {
		String template = //
				"{#for it|em in items}\r\n" + //
				"	{item.name}\r\n" + //
				"	{#linkItem item name=item.name item /}\r\n" + //
				"{/for}";
		assertRename(template, "test", //
				edits("test", //
						r(0, 6, 0, 10), // {#for it|em,
						r(1, 2, 1, 6), // {it|em.name}
						r(2, 12, 2, 16), // {#linkItem it|em
						r(2, 22, 2, 26) // {#linkItem item name=it|em.name
				));
	}

	@Test
	public void toAliasIt() throws BadLocationException {
		String template = //
				"{#for item in items}\r\n" + //
				"	{item.name}\r\n" + //
				"	{#linkItem it|em name=item.name item /}\r\n" + //
				"{/for}";
		assertRename(template, "test");
	}

	@Test
	public void toAliasParam() throws BadLocationException {
		String template = //
				"{#for item in items}\r\n" + //
				"	{item.name}\r\n" + //
				"	{#linkItem item name=ite|m.name item /}\r\n" + //
				"{/for}";
		assertRename(template, "test");
	}

	@Test
	public void toAliasNoIt() throws BadLocationException {
		String template = //
				"{#for item in items}\r\n" + //
				"	{item.name}\r\n" + //
				"	{#linkItem item name=item.name it|em /}\r\n" + //
				"{/for}";
		assertRename(template, "test");
	}
}

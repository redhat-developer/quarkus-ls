/*******************************************************************************
* Copyright (c) 2023 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.services.commands;

import static com.redhat.qute.QuteAssert.assertSurroundWith;
import static com.redhat.qute.QuteAssert.te;

import org.junit.jupiter.api.Test;

import com.redhat.qute.services.commands.QuteSurroundWithCommandHandler.SurroundWithKind;

/**
 * Tests for Qute surround with command.
 *
 */
public class QuteSurroundWithCommandHandlerTest {

	@Test
	public void surroundWithComments() throws Exception {
		String template = "|{class}|";
		assertSurroundWith(template, SurroundWithKind.comments, true, //
				te(0, 0, 0, 0, "{!"), //
				te(0, 7, 0, 7, "!}"));
	}

	@Test
	public void surroundWithCommentsEmptySelection() throws Exception {
		String template = "|{class}";
		assertSurroundWith(template, SurroundWithKind.comments, true, //
				te(0, 0, 0, 0, "{!"), //
				te(0, 0, 0, 0, "!}"));
	}

	@Test
	public void surroundWithSectionSimple() throws Exception {
		String template = "|{class}|";
		assertSurroundWith(template, SurroundWithKind.section, true, //
				te(0, 0, 0, 0, "{#}"), //
				te(0, 7, 0, 7, "{/}"));
	}

	@Test
	public void surroundWithSection() throws Exception {
		String template = "|{#for item in items}\r\n" + //
				"  {item.name}\r\n" + //
				"{/for}|";
		assertSurroundWith(template, SurroundWithKind.section, true, //
				te(0, 0, 0, 0, "{#}"), //
				te(2, 6, 2, 6, "{/}"));
	}

	@Test
	public void surroundWithSectionAdjustSelection() throws Exception {
		String template = "{#f|or item in items}\r\n" + //
				"  {item.name}\r\n" + //
				"{/for}";
		assertSurroundWith(template, SurroundWithKind.section, true, //
				te(0, 0, 0, 0, "{#}"), //
				te(2, 6, 2, 6, "{/}"));
	}

}

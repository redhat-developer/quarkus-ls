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
 * Tests for Qute rename inside #if section.
 *
 */
public class QuteRenameInIfSectionTest {

	@Test
	public void noOptionalParameter() throws BadLocationException {
		String template = //
				"{#if fo|o}\r\n" + //
				"	{foo}\r\n" + //
				"{/for}";
		assertRename(template, null);
	}

	@Test
	public void optionalParameter() throws BadLocationException {
		String template = //
				"{#if fo|o??}\r\n" + //
				"	{foo}\r\n" + //
				"{/for}";
		assertRename(template, "text", //
				edits("text", //
						r(0, 5, 0, 8), // {#if fo|o??}
						r(1, 2, 1, 5) // {foo}
				));
	}
}

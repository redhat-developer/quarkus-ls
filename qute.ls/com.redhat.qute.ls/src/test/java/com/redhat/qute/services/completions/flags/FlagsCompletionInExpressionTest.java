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
package com.redhat.qute.services.completions.flags;

import static com.redhat.qute.QuteAssert.c;
import static com.redhat.qute.QuteAssert.r;

import org.eclipse.lsp4j.CompletionItem;
import org.junit.jupiter.api.Test;

import com.redhat.qute.QuteAssert;
import com.redhat.qute.project.flags.FlagsProject;

/**
 * Tests for Flags completion with flags namespace.
 *
 * @author Angelo ZERR
 *
 */
public class FlagsCompletionInExpressionTest {

	@Test
	public void flag() throws Exception {
		String template = "{flag:|}";
		testCompletionFor(template, //
				8, //
				c("flag:flags() : List<Flag>", "flags", r(0, 6, 0, 6)), //
				c("flag:bool(key : Object) : Boolean", "bool(${1:key})$0", r(0, 6, 0, 6)), //
				c("flag:enabled(key : Object) : Boolean", "enabled(${1:key})$0", r(0, 6, 0, 6)), //
				c("flag:disabled(key : Object) : Boolean", "disabled(${1:key})$0", r(0, 6, 0, 6)), //
				c("flag:string(key : Object) : String", "string(${1:key})$0", r(0, 6, 0, 6)), //
				c("flag:int(key : Object) : int", "int(${1:key})$0", r(0, 6, 0, 6)), //
				c("flag:meta(key : Object) : Map<String,String>", "meta(${1:key})$0", r(0, 6, 0, 6)), //
				c("flag:find(key : Object) : Flag", "find(${1:key})$0", r(0, 6, 0, 6)));
	}

	private static void testCompletionFor(String value, Integer expectedCount, CompletionItem... expectedItems)
			throws Exception {
		QuteAssert.testCompletionFor(value, true, QuteAssert.FILE_URI, FlagsProject.PROJECT_URI, null, expectedCount,
				expectedItems);
	}

}
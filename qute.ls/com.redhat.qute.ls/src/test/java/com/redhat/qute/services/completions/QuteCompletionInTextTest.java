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
package com.redhat.qute.services.completions;

import static com.redhat.qute.QuteAssert.c;
import static com.redhat.qute.QuteAssert.r;
import static com.redhat.qute.QuteAssert.testCompletionFor;

import org.junit.jupiter.api.Test;

/**
 * Tests for Qute completion in text node.
 * 
 * @author Angelo ZERR
 *
 */
public class QuteCompletionInTextTest {

	@Test
	public void completionInText() throws Exception {
		String template = "|";

		// Without snippet
		testCompletionFor(template, // n
				c("for", "{#for item in items}" + System.lineSeparator() + //
						"	{item.name}" + System.lineSeparator() + //
						"{/for}", //
						r(0, 0, 0, 0)));

		// With snippet support
		testCompletionFor(template, //
				true, // snippet support
				c("for", "{#for ${1:item} in ${2:items}}" + System.lineSeparator() + //
						"	{${1:item}.${3:name}}$0" + System.lineSeparator() + //
						"{/for}", //
						r(0, 0, 0, 0)));
	}

	@Test
	public void completionInsideSection() throws Exception {
		String template = "{#let name=value}\r\n" + //
				"	|\r\n" + //
				"{/let}";

		// Without snippet
		testCompletionFor(template, // n
				c("for", "{#for item in items}\r\n" + //
						"	{item.name}\r\n" + //
						"{/for}", //
						r(1, 1, 1, 1)));

		// With snippet support
		testCompletionFor(template, //
				true, // snippet support
				c("for", "{#for ${1:item} in ${2:items}}\r\n" + //
						"	{${1:item}.${3:name}}$0\r\n" + //
						"{/for}", //
						r(1, 1, 1, 1)));
	}

}
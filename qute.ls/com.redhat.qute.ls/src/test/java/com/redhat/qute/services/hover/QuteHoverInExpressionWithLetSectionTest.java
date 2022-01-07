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
package com.redhat.qute.services.hover;

import static com.redhat.qute.QuteAssert.assertHover;
import static com.redhat.qute.QuteAssert.r;

import org.junit.jupiter.api.Test;

/**
 * Tests for Qute hover in expression with #let section.
 *
 * @author Angelo ZERR
 *
 */
public class QuteHoverInExpressionWithLetSectionTest {

	@Test
	public void referencedParameter() throws Exception {
		String template = "{#set name=item.name age=10 long=10L negDouble=-10D isActive=true simpleQuote='abcd' doubleQuote=\"efgh\"}\r\n"
				+ //
				"  {true}\r\n" + //
				"  {name}\r\n" + //
				"  {neg|Double}\r\n" + //
				"  {isActive}\r\n" + //
				"  {simpleQuote}\r\n" + //
				"  {doubleQuote}\r\n" + //
				"{/set}\r\n" + //
				"";
		assertHover(template, "```java" + //
				System.lineSeparator() + //
				"Double" + //
				System.lineSeparator() + //
				"```", //
				r(3, 3, 3, 12));
	}

	@Test
	public void assignedString() throws Exception {
		String template = "{#let a=1 |b='abcd'}\r\n" + //
				"{/let}\r\n";
		assertHover(template, "```java" + //
				System.lineSeparator() + //
				"String" + //
				System.lineSeparator() + //
				"```", //
				r(0, 10, 0, 18));
	}
}

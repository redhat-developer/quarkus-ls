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
package com.redhat.qute.services.hover;

import static com.redhat.qute.QuteAssert.assertHover;
import static com.redhat.qute.QuteAssert.r;

import org.junit.jupiter.api.Test;

/**
 * Tests for Qute hover with infix notation.
 *
 * @author Angelo ZERR
 *
 */
public class QuteHoverInInfixNotationTest {

	@Test
	public void methodHoverWithInfixNotation() throws Exception {
		String template = "{@java.lang.String foo}\r\n" + //
				"{@java.lang.Integer index}\r\n" + //
				"\r\n" + //
				"{foo cha|rAt index}";
		assertHover(template, "```java" + //
				System.lineSeparator() + //
				"char java.lang.String.charAt(int index)" + //
				System.lineSeparator() + //
				"```", //
				r(3, 5, 3, 11));
	}

	@Test
	public void methodHoverNoInfixNotation() throws Exception {
		String template = "{@java.lang.String foo}\r\n" + //
				"{@java.lang.Integer index}\r\n" + //
				"\r\n" + //
				"{foo.cha|rAt(index)}";
		assertHover(template, "```java" + //
				System.lineSeparator() + //
				"char java.lang.String.charAt(int index)" + //
				System.lineSeparator() + //
				"```", //
				r(3, 5, 3, 11));
	}

	@Test
	public void parameterHoverNoInfixNotation() throws Exception {
		String template = "{@java.lang.String foo}\r\n" + //
				"{@java.lang.Integer index}\r\n" + //
				"\r\n" + //
				"{foo.charAt(in|dex)}";
		assertHover(template, "```java" + //
				System.lineSeparator() + //
				"Integer" + //
				System.lineSeparator() + //
				"```", //
				r(3, 12, 3, 17));
	}
}

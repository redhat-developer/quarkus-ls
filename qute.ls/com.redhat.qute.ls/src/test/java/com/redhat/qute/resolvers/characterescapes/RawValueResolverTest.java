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

package com.redhat.qute.resolvers.characterescapes;

import static com.redhat.qute.QuteAssert.assertHover;
import static com.redhat.qute.QuteAssert.c;
import static com.redhat.qute.QuteAssert.r;
import static com.redhat.qute.QuteAssert.testCompletionFor;
import static com.redhat.qute.QuteAssert.testDiagnosticsFor;

import org.junit.jupiter.api.Test;

/**
 * Tests for 'raw' value resolver.
 *
 * @see https://quarkus.io/guides/qute-reference#character-escapes
 *
 */
public class RawValueResolverTest {
	@Test
	public void diagnostics() throws Exception {
		String template = "{@org.acme.Item item}\r\n" + //
				"{item.name.raw}";
		testDiagnosticsFor(template);
		template = "{@org.acme.Item item}\r\n" + //
				"{item.name.raw.value}";
		testDiagnosticsFor(template);
	}

	@Test
	public void completion() throws Exception {
		String template = "{@org.acme.Item item}\r\n" + //
				"{item.name.|}";
		testCompletionFor(template, //
				c("raw(base : Object) : RawString", "raw", r(1, 11, 1, 11)));
		template = "{@org.acme.Item item}\r\n" + //
				"{item.name.raw.|}";
		testCompletionFor(template, //
				c("getValue() : String", "getValue", r(1, 15, 1, 15)), //
				c("toString() : String", "toString", r(1, 15, 1, 15)));
	}

	@Test
	public void hover() throws Exception {
		String template = "{@org.acme.Item item}\r\n" + //
				" \r\n" + //
				"{item.name.r|aw}";
		assertHover(template, "```java" + //
				System.lineSeparator() + //
				"RawString raw()" + //
				System.lineSeparator() + //
				"```" + //
				System.lineSeparator() + //
				"Marks the object so that character escape is not needed and can be rendered as is." + //
				System.lineSeparator() + //
				System.lineSeparator() + //
				"`Sample`:" + //
				System.lineSeparator() + //
				"```qute-html" + //
				System.lineSeparator() + //
				"{paragraph.raw}" + //
				System.lineSeparator() + //
				"```" + //
				System.lineSeparator() + //
				"See [here](https://quarkus.io/guides/qute-reference#character-escapes) for more informations.", //
				r(2, 11, 2, 14));
	}
}

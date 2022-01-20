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
public class SafeValueResolverTest {
	@Test
	public void diagnostics() throws Exception {
		String template = "{@org.acme.Item item}\r\n" + //
				"{item.name.safe}";
		testDiagnosticsFor(template);
		template = "{@org.acme.Item item}\r\n" + //
				"{item.name.safe.value}";
		testDiagnosticsFor(template);
	}

	@Test
	public void completion() throws Exception {
		String template = "{@org.acme.Item item}\r\n" + //
				"{item.name.|}";
		testCompletionFor(template, //
				c("safe(base : Object) : RawString", "safe", r(1, 11, 1, 11)));
		template = "{@org.acme.Item item}\r\n" + //
				"{item.name.safe.|}";
		testCompletionFor(template, //
				c("getValue() : String", "getValue", r(1, 16, 1, 16)), //
				c("toString() : String", "toString", r(1, 16, 1, 16)));
	}

	@Test
	public void hover() throws Exception {
		String template = "{@org.acme.Item item}\r\n" + //
				" \r\n" + //
				"{item.name.s|afe}";
		assertHover(template, "```java" + //
				System.lineSeparator() + //
				"RawString safe()" + //
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
				"{paragraph.safe}" + //
				System.lineSeparator() + //
				"```" + //
				System.lineSeparator() + //
				"See [here](https://quarkus.io/guides/qute-reference#character-escapes) for more informations.", //
				r(2, 11, 2, 15));
	}
}
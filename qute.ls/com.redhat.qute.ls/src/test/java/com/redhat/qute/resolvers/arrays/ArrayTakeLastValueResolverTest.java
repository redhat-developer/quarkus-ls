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
package com.redhat.qute.resolvers.arrays;

import static com.redhat.qute.QuteAssert.assertHover;
import static com.redhat.qute.QuteAssert.c;
import static com.redhat.qute.QuteAssert.d;
import static com.redhat.qute.QuteAssert.r;
import static com.redhat.qute.QuteAssert.testCompletionFor;
import static com.redhat.qute.QuteAssert.testDiagnosticsFor;

import org.eclipse.lsp4j.DiagnosticSeverity;
import org.junit.jupiter.api.Test;

import com.redhat.qute.services.diagnostics.UnknownPropertyData;
import com.redhat.qute.services.diagnostics.QuteErrorCode;

/**
 * Tests for Array 'takeLast' value resolver.
 *
 * @author Angelo ZERR
 *
 * @see https://quarkus.io/guides/qute-reference#arrays
 *
 */
public class ArrayTakeLastValueResolverTest {

	@Test
	public void diagnostics() {
		String template = "{@org.acme.Item[] items}\r\n" + //
				"{items.takeLast(0)}";
		testDiagnosticsFor(template);

		template = "{@org.acme.Item[] items}\r\n" + //
				"{items.takeLast(0).length}";
		testDiagnosticsFor(template);

		template = "{@org.acme.Item[] items}\r\n" + //
				"{items.takeLast(0).lengthXXX}";
		testDiagnosticsFor(template, //
				d(1, 19, 1, 28, QuteErrorCode.UnknownProperty,
						"`lengthXXX` cannot be resolved or is not a field of `org.acme.Item[]` Java type.",
						new UnknownPropertyData("org.acme.Item[]", "lengthXXX", false),
						DiagnosticSeverity.Error));
	}

	@Test
	public void completion() throws Exception {
		String template = "{@org.acme.Item[] items}\r\n" + //
				"Item: {items.|}";
		testCompletionFor(template, //
				c("takeLast(base : T[], n : int) : T[]", "takeLast(n)", r(1, 13, 1, 13)));

		template = "{@org.acme.Item[] items}\r\n" + //
				"Item: {items.takeLast(0).|}";
		testCompletionFor(template, //
				c("length(base : T[]) : int", "length", r(1, 25, 1, 25)), //
				c("get(base : T[], index : int) : T", "get(index)", r(1, 25, 1, 25)), //
				c("take(base : T[], n : int) : T[]", "take(n)", r(1, 25, 1, 25)), //
				c("takeLast(base : T[], n : int) : T[]", "takeLast(n)", r(1, 25, 1, 25)));
	}

	@Test
	public void hover() throws Exception {
		String template = "{@org.acme.Item[] items}\r\n" + //
				"Item: {items.ta|keLast(0)}";
		assertHover(template, "```java" + //
				System.lineSeparator() + //
				"T[] takeLast(int n)" + //
				System.lineSeparator() + //
				"```" + //
				System.lineSeparator() + //
				"Returns the last `n` elements from the given list; throws an `IndexOutOfBoundsException` if `n` is out of range."
				+ //
				System.lineSeparator() + //
				System.lineSeparator() + //
				"`Sample`:" + //
				System.lineSeparator() + //
				"```qute-html" + //
				System.lineSeparator() + //
				"{#for r in myArray.takeLast(3)}" + //
				System.lineSeparator() + //
				"```" + //
				System.lineSeparator() + //
				"See [here](https://quarkus.io/guides/qute-reference#arrays) for more informations.", //
				r(1, 13, 1, 21));
	}
}
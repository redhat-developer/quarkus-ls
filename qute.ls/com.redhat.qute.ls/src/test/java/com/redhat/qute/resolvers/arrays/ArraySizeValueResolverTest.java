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
 * Tests for Array 'size' value resolver.
 *
 * @author Angelo ZERR
 *
 * @see https://quarkus.io/guides/qute-reference#arrays
 *
 */
public class ArraySizeValueResolverTest {

	@Test
	public void diagnostics() {
		String template = "{@org.acme.Item[] items}\r\n" + //
				"{items.size}";
		testDiagnosticsFor(template);

		template = "{@org.acme.Item[] items}\r\n" + //
				"{items.sizeXXX}";
		testDiagnosticsFor(template, //
				d(1, 7, 1, 14, QuteErrorCode.UnknownProperty,
						"`sizeXXX` cannot be resolved or is not a field of `org.acme.Item[]` Java type.",
						new UnknownPropertyData("org.acme.Item[]", "sizeXXX"),
						DiagnosticSeverity.Error));

		template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				"{items.size}";
		testDiagnosticsFor(template);
	}

	@Test
	public void completion() throws Exception {
		String template = "{@org.acme.Item[] items}\r\n" + //
				"Item: {items.|}";
		testCompletionFor(template, //
				c("size(base : T[]) : int", "size", r(1, 13, 1, 13)));
	}

	@Test
	public void hover() throws Exception {
		String template = "{@org.acme.Item[] items}\r\n" + //
				"Item: {items.siz|e}";
		assertHover(template, "```java" + //
				System.lineSeparator() + //
				"int size()" + //
				System.lineSeparator() + //
				"```" + //
				System.lineSeparator() + //
				"The size of the array." + //
				System.lineSeparator() + //
				System.lineSeparator() + //
				"`Sample`:" + //
				System.lineSeparator() + //
				"```qute-html" + //
				System.lineSeparator() + //
				"{myArray.size}" + //
				System.lineSeparator() + //
				"```" + //
				System.lineSeparator() + //
				"See [here](https://quarkus.io/guides/qute-reference#arrays) for more informations.", //
				r(1, 13, 1, 17));
	}
}
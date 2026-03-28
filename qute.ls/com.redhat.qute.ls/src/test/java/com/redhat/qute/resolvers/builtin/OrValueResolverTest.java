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
package com.redhat.qute.resolvers.builtin;

import static com.redhat.qute.QuteAssert.assertHover;
import static com.redhat.qute.QuteAssert.c;
import static com.redhat.qute.QuteAssert.d;
import static com.redhat.qute.QuteAssert.r;
import static com.redhat.qute.QuteAssert.testCompletionFor;
import static com.redhat.qute.QuteAssert.testDiagnosticsFor;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.junit.jupiter.api.Test;

import com.redhat.qute.services.diagnostics.QuteErrorCode;

/**
 * Tests for 'or' value resolver.
 *
 * @see https://quarkus.io/guides/qute-reference#built-in-resolvers
 *
 */
public class OrValueResolverTest {

	@Test
	public void validDiagnostics() {
		String template = "{@org.acme.Item item}\r\n" + //
				"{item.name.or('John')}";
		testDiagnosticsFor(template);

		template = "{@org.acme.Item item}\r\n" + //
				" \r\n" + //
				"{item.name ?: 'John'}";
		testDiagnosticsFor(template);

		template = "{@org.acme.Item item}\r\n" + //
				" \r\n" + //
				"{item.name.or('John')}";
		testDiagnosticsFor(template);
	}

	@Test
	public void invalidDiagnostics() {
		String template = "{@org.acme.Item item}\r\n" + //
				"{item ?: bar}";
		Diagnostic d1 = d(1, 9, 1, 12, QuteErrorCode.UndefinedObject, "`bar` cannot be resolved to an object.",
				DiagnosticSeverity.Warning);
		testDiagnosticsFor(template, d1);

		template = "{@org.acme.Item item}\r\n" + //
				"{bar ?: item}";
		Diagnostic d2 = d(1, 1, 1, 4, QuteErrorCode.UndefinedObject, "`bar` cannot be resolved to an object.",
				DiagnosticSeverity.Warning);
		testDiagnosticsFor(template, d2);

		template = "{@org.acme.Item item}\r\n" + //
				"{foo ?: bar}";
		Diagnostic d3_1 = d(1, 1, 1, 4, QuteErrorCode.UndefinedObject, "`foo` cannot be resolved to an object.",
				DiagnosticSeverity.Warning);
		Diagnostic d3_2 = d(1, 8, 1, 11, QuteErrorCode.UndefinedObject, "`bar` cannot be resolved to an object.",
				DiagnosticSeverity.Warning);
		testDiagnosticsFor(template, d3_1, d3_2);
	}

	@Test
	public void completion() throws Exception {
		String template = "{@org.acme.Item item}\r\n" + //
				"{item.name.|}";
		testCompletionFor(template, //
				c("or(base : Object, arg : T) : T", "or(arg)", r(1, 11, 1, 11)));

		template = "{@org.acme.Item item}\r\n" + //
				"{item.or('abcd').|}";
		testCompletionFor(template, 13, //
				// - resolvers
				c("orEmpty(base : T) : List<T>", "orEmpty", r(1, 17, 1, 17)),
				c("ifTruthy(base : Object, arg : T) : T", "ifTruthy(${1:arg})$0", r(1, 17, 1, 17)),
				c("or(base : Object, arg : T) : T", "or(${1:arg})$0", r(1, 17, 1, 17)),
				// - String Java fields
				c("UTF16 : byte", "UTF16", r(1, 17, 1, 17)),
				// - String Java methods
				c("getBytes() : byte[]", "getBytes", r(1, 17, 1, 17)),
				c("getBytes(charsetName : String) : byte[]", "getBytes(${1:charsetName})$0", r(1, 17, 1, 17)),
				c("charAt(index : int) : char", "charAt(${1:index})$0", r(1, 17, 1, 17)));
	}

	@Test
	public void hover() throws Exception {
		String template = "{@org.acme.Item item}\r\n" + //
				" \r\n" + //
				"{item.o|r('John')}";
		assertHover(template, "```java" + //
				System.lineSeparator() + //
				"String or(String arg)" + //
				System.lineSeparator() + //
				"```" + //
				System.lineSeparator() + //
				"Outputs the default value if the previous part cannot be resolved or resolves to `null`." + //
				System.lineSeparator() + //
				System.lineSeparator() + //
				"`Sample`:" + //
				System.lineSeparator() + //
				"```qute-html" + //
				System.lineSeparator() + //
				"{person.name ?: 'John'}" + //
				System.lineSeparator() + //
				"{person.name or 'John'}" + //
				System.lineSeparator() + //
				"{person.name.or('John')}" + //
				System.lineSeparator() + //
				"```" + //
				System.lineSeparator() + //
				"See [here](https://quarkus.io/guides/qute-reference#built-in-resolvers) for more informations.", //
				r(2, 6, 2, 8));
	}

	@Test
	public void hoverWithInfixNotation() throws Exception {
		String template = "{@org.acme.Item item}\r\n" + //
				" \r\n" + //
				"{item.name o|r 'John'}";
		assertHover(template, "```java" + //
				System.lineSeparator() + //
				"String or(String arg)" + //
				System.lineSeparator() + //
				"```" + //
				System.lineSeparator() + //
				"Outputs the default value if the previous part cannot be resolved or resolves to `null`." + //
				System.lineSeparator() + //
				System.lineSeparator() + //
				"`Sample`:" + //
				System.lineSeparator() + //
				"```qute-html" + //
				System.lineSeparator() + //
				"{person.name ?: 'John'}" + //
				System.lineSeparator() + //
				"{person.name or 'John'}" + //
				System.lineSeparator() + //
				"{person.name.or('John')}" + //
				System.lineSeparator() + //
				"```" + //
				System.lineSeparator() + //
				"See [here](https://quarkus.io/guides/qute-reference#built-in-resolvers) for more informations.", //
				r(2, 11, 2, 13));
	}
}
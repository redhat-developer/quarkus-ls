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
package com.redhat.qute.resolvers;

import static com.redhat.qute.QuteAssert.assertHover;
import static com.redhat.qute.QuteAssert.c;
import static com.redhat.qute.QuteAssert.r;
import static com.redhat.qute.QuteAssert.testCompletionFor;
import static com.redhat.qute.QuteAssert.testDiagnosticsFor;

import org.junit.jupiter.api.Test;

/**
 * Tests for 'or' value resolver.
 *
 * @see https://quarkus.io/guides/qute-reference#built-in-resolvers
 *
 */
public class OrValueResolverTest {

	@Test
	public void diagnostics() {
		String template = "{@org.acme.Item item}\r\n" + //
				"{item.or}";
//		testDiagnosticsFor(template);

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
	public void completion() throws Exception {
		String template = "{@org.acme.Item item}\r\n" + //
				"{item.name.|}";
		testCompletionFor(template, //
				c("or(base : T, arg : Object) : T", "or(arg)", r(1, 11, 1, 11)));

		template = "{@org.acme.Item item}\r\n" + //
				"{item.or('abcd').|}";
		testCompletionFor(template, //
				c("name : String", "name", r(1, 17, 1, 17)), //
				c("price : BigInteger", "price", r(1, 17, 1, 17)), //
				c("review : Review", "review", r(1, 17, 1, 17)), //
				c("review2 : Review", "review2", r(1, 17, 1, 17)), //
				c("getReview2() : Review", "getReview2", r(1, 17, 1, 17)));
	}

	@Test
	public void hover() throws Exception {
		String template = "{@org.acme.Item item}\r\n" + //
				" \r\n" + //
				"{item.name.o|r('John')}";
		assertHover(template, "```java" + //
				System.lineSeparator() + //
				"T or(Object arg)" + //
				System.lineSeparator() + //
				"```" + //
				System.lineSeparator() + //
				"Outputs the default value if the previous part cannot be resolved or resolves to null." + //
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
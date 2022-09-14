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
import static com.redhat.qute.QuteAssert.r;
import static com.redhat.qute.QuteAssert.testCompletionFor;
import static com.redhat.qute.QuteAssert.testDiagnosticsFor;

import org.junit.jupiter.api.Test;

/**
 * Tests for 'ifTruthy' value resolver.
 *
 * @see https://quarkus.io/guides/qute-reference#built-in-resolvers
 *
 */
public class IfTruthyValueResolverTest {

	@Test
	public void diagnostics() throws Exception {
		String template = "{@org.acme.Item item}\r\n" + //
				"{item.ifTruthy}";
		testDiagnosticsFor(template);

		template = "{@org.acme.Item item}\r\n" + //
				"{item.ifTruthy('abcd').|}";
		testCompletionFor(template, //
				c("name : String", "name", r(1, 23, 1, 23)), //
				c("price : BigInteger", "price", r(1, 23, 1, 23)), //
				c("review : Review", "review", r(1, 23, 1, 23)), //
				c("review2 : Review", "review2", r(1, 23, 1, 23)), //
				c("getReview2() : Review", "getReview2", r(1, 23, 1, 23)));
	}

	@Test
	public void completion() throws Exception {
		String template = "{@org.acme.Item item}\r\n" + //
				"{item.name.|}";
		testCompletionFor(template, //
				c("ifTruthy(base : T, arg : Object) : T", "ifTruthy(arg)", r(1, 11, 1, 11)));
		template = "{@org.acme.Item item}\r\n" + //
				"{item.name.ifTruthy('abcd').|}";
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
				"{item.name.i|fTruthy(item)}";
		assertHover(template, "```java" + //
				System.lineSeparator() + //
				"String ifTruthy(Object arg)" + //
				System.lineSeparator() + //
				"```" + //
				System.lineSeparator() + //
				"Outputs the default value if the base object cannot be resolved or the base Object otherwise." + //
				System.lineSeparator() + //
				System.lineSeparator() + //
				"`Sample`:" + //
				System.lineSeparator() + //
				"```qute-html" + //
				System.lineSeparator() + //
				"{item.isActive.ifTruthy(item.name)}" + //
				System.lineSeparator() + //
				"```" + //
				System.lineSeparator() + //
				"See [here](https://quarkus.io/guides/qute-reference#built-in-resolvers) for more informations.", //
				r(2, 11, 2, 19));
	}
}
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
 * Tests for Qute hover in expression with namespace.
 *
 * @author Angelo ZERR
 *
 */
public class QuteHoverInExpressionWithNamespaceTest {

	@Test
	public void dataPropertyAfterNamespace() throws Exception {
		String template = "{@org.acme.Item item}\r\n" + //
				"{data:i|tem}";
		assertHover(template, "```java" + //
				System.lineSeparator() + //
				"Item" + //
				System.lineSeparator() + //
				"```", // ,
				r(1, 6, 1, 10));

		template = "{@org.acme.Item item}\r\n" + //
				"{data:i|temX}";
		assertHover(template);
	}

	@Test
	public void inject() throws Exception {
		String template = "{inj|ect:}";
		assertHover(template, //
				"Namespace: `inject`" + //
						System.lineSeparator() + //
						System.lineSeparator() + //
						"A CDI bean annotated with `@Named` can be referenced in any template through `cdi` and/or `inject` namespaces."
						+ //
						System.lineSeparator() + //
						System.lineSeparator() + //
						"See [here](https://quarkus.io/guides/qute-reference#injecting-beans-directly-in-templates) for more informations.", //
				r(0, 1, 0, 7));
	}

	@Test
	public void injectPropertyAfterNamespace() throws Exception {
		String template = "{inject:bea|n}";
		assertHover(template, "```java" + //
				System.lineSeparator() + //
				"String org.acme.Bean.bean" + //
				System.lineSeparator() + //
				"```", //
				r(0, 8, 0, 12));

		template = "{inject:bea|nX}";
		assertHover(template);
	}

	@Test
	public void cdi() throws Exception {
		String template = "{cd|i:}";
		assertHover(template, //
				"Namespace: `cdi`" + //
						System.lineSeparator() + //
						System.lineSeparator() + //
						"A CDI bean annotated with `@Named` can be referenced in any template through `cdi` and/or `inject` namespaces."
						+ //
						System.lineSeparator() + //
						System.lineSeparator() + //
						"See [here](https://quarkus.io/guides/qute-reference#injecting-beans-directly-in-templates) for more informations.", //
				r(0, 1, 0, 4));
	}

	@Test
	public void cdiPropertyAfterNamespace() throws Exception {
		String template = "{cdi:bea|n}";
		assertHover(template, "```java" + //
				System.lineSeparator() + //
				"String org.acme.Bean.bean" + //
				System.lineSeparator() + //
				"```", //
				r(0, 5, 0, 9));

		template = "{cdi:bea|nX}";
		assertHover(template);
	}

	@Test
	public void injectPropertyAfterBadNamespace() throws Exception {
		String template = "{X:bea|n}";
		assertHover(template);
	}
}

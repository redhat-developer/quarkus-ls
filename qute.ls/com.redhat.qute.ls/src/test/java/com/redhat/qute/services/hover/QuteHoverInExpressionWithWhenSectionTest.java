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
 * Tests for Qute hover in expression with #when section.
 *
 */
public class QuteHoverInExpressionWithWhenSectionTest {

	@Test
	public void onOperator() throws Exception {
		String template = "{@org.acme.Machine Machine}\r\n" + //
				"{#when Machine.status}\r\n" + //
				"  {#is i|n}\r\n" + //
				"{/when}";
		assertHover(template, "**Operator** for #case/#is section." + //
				System.lineSeparator() + //
				System.lineSeparator() + //
				"Is in." + //
				System.lineSeparator() + //
				System.lineSeparator() + //
				"`Sample`:" + //
				System.lineSeparator() + //
				"```qute-html" + //
				System.lineSeparator() + //
				"{#is in 'foo' 'bar' 'baz'}" + //
				System.lineSeparator() + //
				"```" + //
				System.lineSeparator() + //
				"See [here](https://quarkus.io/guides/qute-reference#when_operators) for more informations.", //
				r(2, 7, 2, 9));
	}

	@Test
	public void onOperatorAliasAtEnd() throws Exception {
		String template = "{@org.acme.Machine Machine}\r\n" + //
				"{#when Machine.status}\r\n" + //
				"  {#is <| ON}\r\n" + //
				"{/when}";
		assertHover(template, "**Operator** for #case/#is section." + //
				System.lineSeparator() + //
				System.lineSeparator() + //
				"Less than. (alias for `lt`)" + //
				System.lineSeparator() + //
				System.lineSeparator() + //
				"`Sample`:" + //
				System.lineSeparator() + //
				"```qute-html" + //
				System.lineSeparator() + //
				"{#case < 10}" + //
				System.lineSeparator() + //
				"```" + //
				System.lineSeparator() + //
				"See [here](https://quarkus.io/guides/qute-reference#when_operators) for more informations.", //
				r(2, 7, 2, 8));
	}

	@Test
	public void onOperatorAliasLessOrEqual() throws Exception {
		String template = "{@org.acme.Machine Machine}\r\n" + //
				"{#when Machine.status}\r\n" + //
				"  {#is <|= ON}\r\n" + //
				"{/when}";
		assertHover(template, "**Operator** for #case/#is section." + //
				System.lineSeparator() + //
				System.lineSeparator() + //
				"Less than or equal to. (alias for `le`)" + //
				System.lineSeparator() + //
				System.lineSeparator() + //
				"`Sample`:" + //
				System.lineSeparator() + //
				"```qute-html" + //
				System.lineSeparator() + //
				"{#case le 10}" + //
				System.lineSeparator() + //
				"```" + //
				System.lineSeparator() + //
				"See [here](https://quarkus.io/guides/qute-reference#when_operators) for more informations.", //
				r(2, 7, 2, 9));
	}

	@Test
	public void onOperatorAliasNotEqual() throws Exception {
		String template = "{@org.acme.Machine Machine}\r\n" + //
				"{#when Machine.status}\r\n" + //
				"  {#is !|= ON}\r\n" + //
				"{/when}";
		assertHover(template, "**Operator** for #case/#is section." + //
				System.lineSeparator() + //
				System.lineSeparator() + //
				"Not equal. (alias for `not`)" + //
				System.lineSeparator() + //
				System.lineSeparator() + //
				"`Sample`:" + //
				System.lineSeparator() + //
				"```qute-html" + //
				System.lineSeparator() + //
				"{#is not 10}" + //
				System.lineSeparator() + //
				"{#case != 10}" + //
				System.lineSeparator() + //
				"```" + //
				System.lineSeparator() + //
				"See [here](https://quarkus.io/guides/qute-reference#when_operators) for more informations.", //
				r(2, 7, 2, 9));
	}

	@Test
	public void onOperatorAsEnum() throws Exception {
		String template = "{@org.acme.Machine Machine}\r\n" + //
				"{#when Machine.status}\r\n" + //
				"  {#is in i|n}\r\n" + //
				"{/when}";
		assertHover(template);
	}
}

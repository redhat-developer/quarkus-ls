/*******************************************************************************
* Copyright (c) 2021 Red Hat Inc. and others.
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

import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

/**
 * Tests for Qute hover in tag section.
 *
 * @author Angelo ZERR
 *
 */
public class QuteHoverInTagTest {

	@Test
	public void noHover() throws Exception {
		String template = "{#|}";
		assertHover(template);
	}

	@Test
	public void coreForTag() throws Exception {
		String template = "{#fo|r}";
		assertHover(template, "**#for** section tag " + //
				System.lineSeparator() + //
				System.lineSeparator() + //
				"Loop section with alias" + //
				System.lineSeparator() + //
				System.lineSeparator() + //
				"See [Loop section](https://quarkus.io/guides/qute-reference#loop_section) for more information.", //
				r(0, 1, 0, 5));
	}

	@Test
	public void coreFragmentTag() throws Exception {
		String template = "{#fr|agment}";
		assertHover(template, "**#fragment** section tag " + //
				System.lineSeparator() + //
				System.lineSeparator() + //
				"A fragment represents a part of the template that can be treated as a separate template, i.e. rendered separately." + //
				System.lineSeparator() + //
				System.lineSeparator() + //
				"See for more information:" +  //
				System.lineSeparator() + //
				" * [Fragments](https://quarkus.io/guides/qute-reference#fragments)" +  //
				System.lineSeparator() + //
				" * [Type-safe Fragments](https://quarkus.io/guides/qute-reference#type_safe_fragments)", //
				r(0, 1, 0, 10));
	}
	
	@Test
	public void userTag() throws Exception {
		String userTagUri = Paths.get("src/test/resources/templates/tags/formElement.html").toUri().toString();
		String template = "{#for|mElement}";
		assertHover(template, "**#formElement** user tag " + //
				System.lineSeparator() + //
				System.lineSeparator() + //
				"Defined in [formElement.html](" + //
				userTagUri + //
				")", //
				r(0, 1, 0, 13));
	}
}

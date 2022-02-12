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
public class QuteHoverInTag {

	@Test
	public void noHover() throws Exception {
		String template = "{#|}";
		assertHover(template);
	}

	@Test
	public void coreTag() throws Exception {
		String template = "{#fo|r}";
		assertHover(template, "**#for** section tag " + //
				System.lineSeparator() + //
				System.lineSeparator() + //
				"Loop section with alias", //
				r(0, 1, 0, 5));
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

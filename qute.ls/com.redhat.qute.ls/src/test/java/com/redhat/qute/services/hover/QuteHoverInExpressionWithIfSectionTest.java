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
 * Tests for Qute hover in expression with #if section.
 *
 * @author Angelo ZERR
 *
 */
public class QuteHoverInExpressionWithIfSectionTest {

	@Test
	public void referencedParameter() throws Exception {
		String template = "{#let value=123}\r\n" + //
				"  {#if val|ue}";
		assertHover(template, "```java" + //
				System.lineSeparator() + //
				"java.lang.Integer" + //
				System.lineSeparator() + //
				"```", //
				r(1, 7, 1, 12));
	}

}

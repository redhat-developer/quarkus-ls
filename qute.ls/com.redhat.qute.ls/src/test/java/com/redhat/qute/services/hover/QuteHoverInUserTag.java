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
 * Tests for Qute hover in user tag section.
 *
 * @author Angelo ZERR
 *
 */
public class QuteHoverInUserTag {

	@Test
	public void parameterObjectPart() throws Exception {
		String template = "{@org.acme.Item item}\r\n" + //
				"{#user name=it|em /}";
		assertHover(template, "```java" + //
				System.lineSeparator() + //
				"Item" + //
				System.lineSeparator() + //
				"```", //
				r(1, 12, 1, 16));
	}

	@Test
	public void parameterPropertyPart() throws Exception {
		String template = "{@org.acme.Item item}\r\n" + //
				"{#user name=item.na|me /}";
		assertHover(template, "```java" + //
				System.lineSeparator() + //
				"String org.acme.Item.name" + //
				System.lineSeparator() + //
				"```", //
				r(1, 17, 1, 21));
	}

}

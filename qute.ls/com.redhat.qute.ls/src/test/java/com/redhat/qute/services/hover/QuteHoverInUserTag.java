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

	@Test
	public void itParameterObjectPart() throws Exception {
		String template = "{@org.acme.Item item}\r\n" + //
				"{#user it|em /}";
		assertHover(template, "```java" + //
				System.lineSeparator() + //
				"Item" + //
				System.lineSeparator() + //
				"```", //
				r(1, 7, 1, 11));
	}

	@Test
	public void itParameterPropertyPart() throws Exception {
		String template = "{@org.acme.Item item}\r\n" + //
				"{#user item.na|me /}";
		assertHover(template, "```java" + //
				System.lineSeparator() + //
				"String org.acme.Item.name" + //
				System.lineSeparator() + //
				"```", //
				r(1, 12, 1, 16));
	}

	@Test
	public void it() throws Exception {
		String template = "{i|t}";

		// In a qute template
		assertHover(template);

		// In a user tag
		assertHover(template, //
				"src/main/resources/templates/tags/form.html", //
				"tags/form", //
				"```java" + //
						System.lineSeparator() + //
						"Object" + //
						System.lineSeparator() + //
						"```" + //
						System.lineSeparator() + //
						"`it` is a special key that is replaced with the first unnamed parameter of the tag.", //
				r(0, 1, 0, 3));
	}

	@Test
	public void nestedContent() throws Exception {
		String template = "{nested-con|tent}";

		// In a qute template
		assertHover(template);

		// In a user tag
		assertHover(template, //
				"src/main/resources/templates/tags/form.html", //
				"tags/form", //
				"```java" + //
						System.lineSeparator() + //
						"Object" + //
						System.lineSeparator() + //
						"```" + //
						System.lineSeparator() + //
						"`nested-content` is a special key that will be replaced by the content of the tag", //
				r(0, 1, 0, 15));
	}
}

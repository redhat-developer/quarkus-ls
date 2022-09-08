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
 * Tests for getting the documentation when hovering a field or method of an
 * object.
 * 
 * @author datho7561
 */
public class QuteHoverMemberDocumentationTest {

	@Test
	public void testHoverItemField() throws Exception {
		String template = "{@org.acme.Item item}\n" + //
				"{item.na|me}";
		assertHover(template, "```java" + //
				System.lineSeparator() + //
				"String org.acme.Item.name" + //
				System.lineSeparator() + //
				"```" + System.lineSeparator() + //
				"---" + System.lineSeparator() + //
				"The name of the item", // ,
				r(1, 6, 1, 10));
	}

	@Test
	public void testHoverItemMethod() throws Exception {
		String template = "{@org.acme.Item item}\n" + //
				"{item.isAvail|able()}";
		assertHover(template, "```java" + //
				System.lineSeparator() + //
				"Boolean org.acme.Item.isAvailable()" + //
				System.lineSeparator() + //
				"```" + System.lineSeparator() + //
				"---" + System.lineSeparator() + //
				"Returns true if the item is available and false otherwise", // ,
				r(1, 6, 1, 17));
	}
	
	@Test
	public void testHoverItemMethodOverloadWithUndefinedParam() throws Exception {
		String template = "{@org.acme.Item item}\n" + //
				"{item.isAvail|able(e)}";
		assertHover(template, "```java" + //
				System.lineSeparator() + //
				"Boolean org.acme.Item.isAvailable(int index)" + //
				System.lineSeparator() + //
				"```" + System.lineSeparator() + //
				"---" + System.lineSeparator() + //
				"Returns true if the item at the given index is available and false otherwise", // ,
				r(1, 6, 1, 17));
	}
	
	@Test
	public void testHoverItemMethodOverloadWithParamOfWrongType() throws Exception {
		String template = "{@org.acme.Item item}\n" + //
				"{item.isAvail|able(\"asdf\")}";
		assertHover(template);
	}

}

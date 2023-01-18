/*******************************************************************************
* Copyright (c) 2023 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.services.codelens;

import static com.redhat.qute.QuteAssert.cl;
import static com.redhat.qute.QuteAssert.r;
import static com.redhat.qute.QuteAssert.testCodeLensFor;

import org.junit.jupiter.api.Test;

import com.redhat.qute.services.commands.QuteClientCommandConstants;

/**
 * Tests for Qute code lens and data model for fragment.
 * 
 * @author Angelo ZERR
 *
 */
public class QuteCodeLensForDataModelFragmentTest {

	@Test
	public void noCheckedTemplateMatching() throws Exception {
		String value = "{#fragment }\r\n"
				+ "	\r\n"
				+ "{/fragment}\r\n"
				+ "\r\n"
				+ "{#fragment id=id2 }\r\n"
				+ "	\r\n"
				+ "{/fragment}\r\n"
				+ "\r\n"
				+ "{#fragment id=id3 }\r\n"
				+ "	\r\n"
				+ "{/fragment}";
		testCodeLensFor(value, "src/main/resources/templates/ItemResourceWithFragments/XXXXXXXXXXX.qute.html");
		testCodeLensFor(value, "src/main/resources/templates/ItemResourceWithFragments/items.XXXXXXXXXXX.html");
	}

	@Test
	public void checkedTemplateMatching() throws Exception {
		// Display information about data model (classes and parameters) as codelens.
		String value = "{#fragment }\r\n"
				+ "	\r\n"
				+ "{/fragment}\r\n"
				+ "\r\n"
				+ "{#fragment id=id2 }\r\n"
				+ "	\r\n"
				+ "{/fragment}\r\n"
				+ "\r\n"
				+ "{#fragment id=id3 }\r\n"
				+ "	\r\n"
				+ "{/fragment}";
		testCodeLensFor(value, "src/main/resources/templates/ItemResourceWithFragments/items.qute.html", //
				cl(r(0, 0, 0, 0), "ItemResourceWithFragments$Templates#items(...)",
						QuteClientCommandConstants.COMMAND_JAVA_DEFINITION), //
				cl(r(0, 0, 0, 0), "items : List<Item>", QuteClientCommandConstants.COMMAND_JAVA_DEFINITION),
				cl(r(4, 1, 4, 10), "ItemResourceWithFragments$Templates#items$id2(...)",
						QuteClientCommandConstants.COMMAND_JAVA_DEFINITION), //
				cl(r(4, 1, 4, 10), "items : List<Item>", QuteClientCommandConstants.COMMAND_JAVA_DEFINITION));

		testCodeLensFor(value, "src/main/resources/templates/ItemResourceWithFragments/items", //
				cl(r(0, 0, 0, 0), "ItemResourceWithFragments$Templates#items(...)",
						QuteClientCommandConstants.COMMAND_JAVA_DEFINITION), //
				cl(r(0, 0, 0, 0), "items : List<Item>", QuteClientCommandConstants.COMMAND_JAVA_DEFINITION),
				cl(r(4, 1, 4, 10), "ItemResourceWithFragments$Templates#items$id2(...)",
						QuteClientCommandConstants.COMMAND_JAVA_DEFINITION), //
				cl(r(4, 1, 4, 10), "items : List<Item>", QuteClientCommandConstants.COMMAND_JAVA_DEFINITION));

		testCodeLensFor(value, "src/main/resources/templates/ItemResourceWithFragments/items.qute", //
				cl(r(0, 0, 0, 0), "ItemResourceWithFragments$Templates#items(...)",
						QuteClientCommandConstants.COMMAND_JAVA_DEFINITION), //
				cl(r(0, 0, 0, 0), "items : List<Item>", QuteClientCommandConstants.COMMAND_JAVA_DEFINITION),
				cl(r(4, 1, 4, 10), "ItemResourceWithFragments$Templates#items$id2(...)",
						QuteClientCommandConstants.COMMAND_JAVA_DEFINITION), //
				cl(r(4, 1, 4, 10), "items : List<Item>", QuteClientCommandConstants.COMMAND_JAVA_DEFINITION));

		testCodeLensFor(value, "src/main/resources/templates/ItemResourceWithFragments/items.html", //
				cl(r(0, 0, 0, 0), "ItemResourceWithFragments$Templates#items(...)",
						QuteClientCommandConstants.COMMAND_JAVA_DEFINITION), //
				cl(r(0, 0, 0, 0), "items : List<Item>", QuteClientCommandConstants.COMMAND_JAVA_DEFINITION),
				cl(r(4, 1, 4, 10), "ItemResourceWithFragments$Templates#items$id2(...)",
						QuteClientCommandConstants.COMMAND_JAVA_DEFINITION), //
				cl(r(4, 1, 4, 10), "items : List<Item>", QuteClientCommandConstants.COMMAND_JAVA_DEFINITION));
	}
}

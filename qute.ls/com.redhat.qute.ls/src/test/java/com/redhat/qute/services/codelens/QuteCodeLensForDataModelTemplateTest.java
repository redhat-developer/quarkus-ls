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
package com.redhat.qute.services.codelens;

import static com.redhat.qute.QuteAssert.cl;
import static com.redhat.qute.QuteAssert.r;
import static com.redhat.qute.QuteAssert.testCodeLensFor;

import org.junit.jupiter.api.Test;

import com.redhat.qute.services.commands.QuteClientCommandConstants;

/**
 * Tests for Qute code lens and data model for template.
 * 
 * @author Angelo ZERR
 *
 */
public class QuteCodeLensForDataModelTemplateTest {

	@Test
	public void noCheckedTemplateMatching() throws Exception {
		String value = "";
		testCodeLensFor(value, "src/main/resources/templates/ItemResource/XXXXXXXXXXX.qute.html");
		testCodeLensFor(value, "src/main/resources/templates/ItemResource/items.XXXXXXXXXXX.html");
	}

	@Test
	public void checkedTemplateMatching() throws Exception {
		// Display information about data model (classes and parameters) as codelens.
		String value = "";
		testCodeLensFor(value, "src/main/resources/templates/ItemResource/items.qute.html", //
				cl(r(0, 0, 0, 0), "ItemResource$Templates#items(...)", QuteClientCommandConstants.COMMAND_JAVA_DEFINITION), //
				cl(r(0, 0, 0, 0), "items : List<Item>", QuteClientCommandConstants.COMMAND_JAVA_DEFINITION));

		testCodeLensFor(value, "src/main/resources/templates/ItemResource/items", //
				cl(r(0, 0, 0, 0), "ItemResource$Templates#items(...)", QuteClientCommandConstants.COMMAND_JAVA_DEFINITION), //
				cl(r(0, 0, 0, 0), "items : List<Item>", QuteClientCommandConstants.COMMAND_JAVA_DEFINITION));

		testCodeLensFor(value, "src/main/resources/templates/ItemResource/items.qute", //
				cl(r(0, 0, 0, 0), "ItemResource$Templates#items(...)", QuteClientCommandConstants.COMMAND_JAVA_DEFINITION), //
				cl(r(0, 0, 0, 0), "items : List<Item>", QuteClientCommandConstants.COMMAND_JAVA_DEFINITION));

		testCodeLensFor(value, "src/main/resources/templates/ItemResource/items.html", //
				cl(r(0, 0, 0, 0), "ItemResource$Templates#items(...)", QuteClientCommandConstants.COMMAND_JAVA_DEFINITION), //
				cl(r(0, 0, 0, 0), "items : List<Item>", QuteClientCommandConstants.COMMAND_JAVA_DEFINITION));
	}
}

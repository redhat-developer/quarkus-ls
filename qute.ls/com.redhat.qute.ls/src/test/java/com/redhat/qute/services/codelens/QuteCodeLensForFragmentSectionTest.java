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
 * Tests for Qute code lens and {#insert name} section tags.
 * 
 * @author Angelo ZERR fragment
 */
public class QuteCodeLensForFragmentSectionTest {

	@Test
	public void fragments() throws Exception {
		String template = "<html>\r\n"
				// [device : String]
				+ "{#fragment id=menu}\r\n" + //
				"    {device}\r\n" + //
				"{/fragment}\r\n" + //
				// [foo : String | baz : Integer]
				"{#fragment id=menu2}\r\n" + // 
				"    {device}\r\n" + //
				"{/fragment}\r\n" + //
				"\r\n" + //
				"{#include $menu device='mobile' /}\r\n" + //
				"{#include $menu2 foo='bar' baz=10 /}";
		testCodeLensFor(template, //
				"src/main/resources/templates/test.html", //
				cl(r(1, 1, 1, 10), "device : String", ""), //
				cl(r(4, 1, 4, 10), "foo : String", ""), //
				cl(r(4, 1, 4, 10), "baz : Integer", ""));
	}

}

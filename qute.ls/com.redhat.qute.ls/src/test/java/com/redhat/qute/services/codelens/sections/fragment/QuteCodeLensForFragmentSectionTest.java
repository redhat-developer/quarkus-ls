/*******************************************************************************
* Copyright (c) 2026 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.services.codelens.sections.fragment;

import static com.redhat.qute.QuteAssert.cl;
import static com.redhat.qute.QuteAssert.r;
import static com.redhat.qute.QuteAssert.testCodeLensFor;

import org.junit.jupiter.api.Test;

/**
 * Tests for Qute code lens and {#fragment id=menu} section tags.
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
				"{#include $menu device='mobile' /}\r\n" + //
				//"{#include test$menu device_test='mobile' /}\r\n" + //
				"{#include test.html$menu device_test_html='mobile' /}\r\n" + //
				"{#include $menu2 foo='bar' baz=10 /}";
		testCodeLensFor(template, //
				"src/main/resources/templates/test.html", //
				"test.html", //
				// fragment menu
				cl(r(1, 1, 1, 10), "device : String", ""), //
				//cl(r(1, 1, 1, 10), "device_test : String", ""), //
				cl(r(1, 1, 1, 10), "device_test_html : String", ""), //
				// fragment menu2
				cl(r(4, 1, 4, 10), "foo : String", ""), //
				cl(r(4, 1, 4, 10), "baz : Integer", ""));
	}

}

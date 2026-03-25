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
package com.redhat.qute.services.completions;

import static com.redhat.qute.QuteAssert.c;
import static com.redhat.qute.QuteAssert.r;
import static com.redhat.qute.QuteAssert.testCompletionFor;

import org.junit.jupiter.api.Test;

/**
 * Tests for Qute completion in object part inside fragment.
 *
 * @author Angelo ZERR
 *
 */
public class QuteCompletionWithFragmentTest {

	@Test
	public void includeTemplateParameterFragment() throws Exception {
		String template = "{#fragment id=menu}\r\n" + //
				"    {device}\r\n" + //
				"{/fragment}\r\n" + //
				"\r\n" + //
				"{#include $| device='mobile' /}"; // <-- completion on $menu

		testCompletionFor(template, //
				"test.qute", //
				c("$menu", "$menu", r(4, 10, 4, 11)));
	}

	@Test
	public void paramFromIncludeInFragment() throws Exception {
		String template = "{#fragment id=menu}\r\n" + //
				"    {|}\r\n" + // <-- completion on device
				"{/fragment}\r\n" + //
				"\r\n" + //
				"{#include $menu device='mobile' /}";

		testCompletionFor(template, //
				c("device", "device", r(1, 5, 1, 5)));
	}

}
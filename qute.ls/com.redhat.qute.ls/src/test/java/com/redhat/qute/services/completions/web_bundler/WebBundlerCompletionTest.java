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
package com.redhat.qute.services.completions.web_bundler;

import static com.redhat.qute.QuteAssert.c;
import static com.redhat.qute.QuteAssert.r;

import org.eclipse.lsp4j.CompletionItem;
import org.junit.jupiter.api.Test;

import com.redhat.qute.QuteAssert;
import com.redhat.qute.project.web_bundler.WebBundlerProject;

/**
 * Tests for Qute completion with user tag which defines #insert
 *
 * @author Angelo ZERR
 *
 */
public class WebBundlerCompletionTest {

	@Test
	public void userTagFromComponent() throws Exception {
		String template = "{#permissionDialog\r\n" + //
				"    title = \"\"\r\n" + //
				"    selectedCode = \"\"\r\n" + //
				"}\r\n" + //
				"    {#|}";
		testCompletionFor(template, //
				c("trigger", "{#trigger}{/trigger}", r(4, 4, 4, 7))); // trigger comes from {#insert trigger} declared
																		// in permissionDialog.html
	}

	private static void testCompletionFor(String value, CompletionItem... expectedItems) throws Exception {
		QuteAssert.testCompletionFor(value, false, QuteAssert.FILE_URI, null, WebBundlerProject.PROJECT_URI,
				QuteAssert.TEMPLATE_BASE_DIR, null, expectedItems);
	}
}
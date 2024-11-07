/*******************************************************************************
* Copyright (c) 2024 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.services.completions.qute_web;

import static com.redhat.qute.QuteAssert.c;
import static com.redhat.qute.QuteAssert.r;

import org.eclipse.lsp4j.CompletionItem;
import org.junit.jupiter.api.Test;

import com.redhat.qute.QuteAssert;
import com.redhat.qute.project.qute_web.QuteWebProject;

/**
 * Test completion with Qute Web Quarkus extension.
 *
 * @author Angelo ZERR
 * 
 */
public class QuteWebCompletionsTest {

	@Test
	public void http_param() throws Exception {
		String template = "{#let name=http:param('name', 'Qute')}\r\n" + //
				"{name.|}";
		testCompletionFor(template, //
				// - resolvers
				c("orEmpty(base : T) : List<T>", "orEmpty", r(1, 6, 1, 6)),
				c("ifTruthy(base : T, arg : Object) : T", "ifTruthy(${1:arg})$0", r(1, 6, 1, 6)),
				c("or(base : T, arg : Object) : T", "or(${1:arg})$0", r(1, 6, 1, 6)),
				// - String Java fields
				c("UTF16 : byte", "UTF16", r(1, 6, 1, 6)),
				// - String Java methods
				c("getBytes() : byte[]", "getBytes", r(1, 6, 1, 6)),
				c("getBytes(charsetName : String) : byte[]", "getBytes(${1:charsetName})$0", r(1, 6, 1, 6)),
				c("charAt(index : int) : char", "charAt(${1:index})$0", r(1, 6, 1, 6)));
	}

	public static void testCompletionFor(String value,
			CompletionItem... expectedItems) throws Exception {
		QuteAssert.testCompletionFor(value, true, QuteAssert.FILE_URI, null, QuteWebProject.PROJECT_URI,
				QuteAssert.TEMPLATE_BASE_DIR,
				null,
				expectedItems);
	}

}
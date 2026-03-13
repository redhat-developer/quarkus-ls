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
package com.redhat.qute.services.definition;

import static com.redhat.qute.QuteAssert.ll;
import static com.redhat.qute.QuteAssert.r;
import static com.redhat.qute.QuteAssert.testDefinitionFor;

import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

/**
 * Test definition with include section.
 * 
 * @author Angelo ZERR
 *
 */
public class QuteDefinitionInIncludeSectionTest {

	@Test
	public void inputName() throws Exception {
		String booksUri = getFileUri("BookPage/books.html");
		String bookUri = getFileUri("BookPage/book.html");

		// origin parameter comes from book.html {#include base origin="book"}
		String template = "{ori|gin}";
		testDefinitionFor(template, "src/main/resources/templates/base.html", //
				ll(booksUri, r(0, 1, 0, 7), r(0, 15, 0, 21)), //
				ll(bookUri, r(0, 1, 0, 7), r(0, 15, 0, 21)));
	}

	private static String getFileUri(String name) {
		return Paths.get("src/test/resources/templates/" + name).toUri().toString();
	}

}
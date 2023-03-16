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
package com.redhat.qute.services.references;

import static com.redhat.qute.QuteAssert.l;
import static com.redhat.qute.QuteAssert.r;
import static com.redhat.qute.QuteAssert.testReferencesFor;

import org.junit.jupiter.api.Test;

import com.redhat.qute.ls.commons.BadLocationException;
import com.redhat.qute.utils.FileUtils;

/**
 * Qute reference with object part.
 * 
 * @author Angelo ZERR
 *
 */
public class QuteReferenceWithInsertSectionTest {

	@Test
	public void insert() throws BadLocationException {
		String booksUri = FileUtils.toUri(FileUtils.createPath("src/test/resources/templates/BookPage/books.html"));
		String bookUri = FileUtils.toUri(FileUtils.createPath("src/test/resources/templates/BookPage/book.html"));

		String template = "<!DOCTYPE html>\r\n"
				+ "<html lang=\"en\">\r\n"
				+ "<head>  \r\n"
				+ "  <title>{#insert includedTitle}Default Title{/}</title>\r\n"
				+ "</head>\r\n"
				+ "<body>\r\n"
				+ "{#each items}\r\n"
				+ "{/each}\r\n"
				// [2 references]
				+ "{#insert bo|dy}No body!{/}\r\n"
				+ "</body>\r\n"
				+ "</html>";
		testReferencesFor(template, //
				l(booksUri, r(2, 1, 2, 6)), //
				l(bookUri, r(2, 1, 2, 6)));
	}
}

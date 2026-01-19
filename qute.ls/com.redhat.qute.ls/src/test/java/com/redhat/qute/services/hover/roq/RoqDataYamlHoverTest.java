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
package com.redhat.qute.services.hover.roq;

import static com.redhat.qute.QuteAssert.r;
import static com.redhat.qute.project.roq.RoqProject.getDataFileUri;

import org.eclipse.lsp4j.Range;
import org.junit.jupiter.api.Test;

import com.redhat.qute.QuteAssert;
import com.redhat.qute.project.roq.RoqProject;

/**
 * Tests hover with Roq Quarkus extension and Yaml data files.
 *
 * @author Angelo ZERR
 *
 */
public class RoqDataYamlHoverTest {

	@Test
	public void books() throws Exception {
		String booksUri = getDataFileUri("books.yaml");
		String template = "{inject:bo|oks}";
		assertHover(template, //
				"Open [books.yaml](" + booksUri + ")", //
				r(0, 8, 0, 13));
	}

	@Test
	public void books_invalid() throws Exception {
		String template = "{inject:books.li|s}";
		assertHover(template, null, null);
	}

	@Test
	public void books_list() throws Exception {
		String booksUri = getDataFileUri("books.yaml");
		String template = "{inject:books.li|st}";
		assertHover(template, //
				"```java" + System.lineSeparator() + //
						"Collection<Object> list" + System.lineSeparator() + //
						"```" + System.lineSeparator() + //
						"Source: [books.yaml](" + booksUri + ")", //
				r(0, 14, 0, 18));
	}

	public static void assertHover(String value, String expectedHoverLabel, Range expectedHoverRange) throws Exception {
		QuteAssert.assertHover(value, QuteAssert.FILE_URI, null, RoqProject.PROJECT_URI, expectedHoverLabel,
				expectedHoverRange);
	}

}

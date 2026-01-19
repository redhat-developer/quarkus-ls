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
package com.redhat.qute.services.definition.roq;

import static com.redhat.qute.QuteAssert.ll;
import static com.redhat.qute.QuteAssert.r;
import static com.redhat.qute.project.roq.RoqProject.getDataFileUri;

import org.eclipse.lsp4j.LocationLink;
import org.junit.jupiter.api.Test;

import com.redhat.qute.QuteAssert;
import com.redhat.qute.project.roq.RoqProject;

/**
 * Test definition in Roq with data file.
 * 
 * @author Angelo ZERR
 *
 */
public class RoqDataYamlDefinitionTest {

	@Test
	public void books() throws Exception {
		String booksUri = getDataFileUri("books.yaml");
		String template = "{inject:bo|oks}";
		testDefinitionFor(template, //
				ll(booksUri, r(0, 8, 0, 13), r(0, 0, 0, 0)));
	}

	@Test
	public void books_list() throws Exception {
		String booksUri = getDataFileUri("books.yaml");

		String template = "{inj|ect:books.list}";
		testDefinitionFor(template);

		template = "{inject:books.li|st}";
		testDefinitionFor(template, //
				ll(booksUri, r(0, 14, 0, 18), r(0, 0, 0, 0)));
	}

	@Test
	public void books_list_title() throws Exception {
		String booksUri = getDataFileUri("books.yaml");

		String template = "{#for b in inject:books.list}\r\n" + //
				"    {b.ti|tle}\r\n" + //
				"{/for}";
		testDefinitionFor(template, //
				ll(booksUri, r(1, 7, 1, 12), r(0, 0, 0, 0)));
	}

	public static void testDefinitionFor(String value, LocationLink... expected) throws Exception {
		QuteAssert.testDefinitionFor(value, null, RoqProject.PROJECT_URI, QuteAssert.TEMPLATE_BASE_DIR, expected);
	}
}

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
package com.redhat.qute.services.definition.multiple;

import static com.redhat.qute.QuteAssert.ll;
import static com.redhat.qute.QuteAssert.r;

import org.eclipse.lsp4j.LocationLink;
import org.junit.jupiter.api.Test;

import com.redhat.qute.QuteAssert;
import com.redhat.qute.project.multiple.QuteProjectA;
import com.redhat.qute.project.multiple.QuteProjectB;

/**
 * Test definition with user tag section and project dependency.
 * 
 * @author Angelo ZERR
 *
 */
public class QuteDefinitionWithUserTagSectionTest {

	@Test
	public void goToUserTagAInProjectA() throws Exception {
		String tagAUri = QuteProjectA.getFileUri("/src/main/resources/templates/tags/tag-a.html");

		// Use tag-a which is defined in project A
		String template = "{#ta|g-a /}";
		testDefinitionFor(template, //
				"test.qute", //
				QuteProjectA.PROJECT_URI, //
				ll(tagAUri, r(0, 1, 0, 7), r(0, 0, 0, 0)));
	}

	@Test
	public void goToUserTagAInProjectB() throws Exception {
		String tagAUri = QuteProjectA.getFileUri("/src/main/resources/templates/tags/tag-a.html");

		// Use tag-a which is defined in project B which depends from project A
		String template = "{#ta|g-a /}";
		testDefinitionFor(template, //
				"test.qute", //
				QuteProjectB.PROJECT_URI, //
				ll(tagAUri, r(0, 1, 0, 7), r(0, 0, 0, 0)));
	}

	private static void testDefinitionFor(String value, String fileUri, String projectUri, LocationLink... expected)
			throws Exception {
		QuteAssert.testDefinitionFor(value, fileUri, projectUri, QuteAssert.TEMPLATE_BASE_DIR, expected);
	}

}
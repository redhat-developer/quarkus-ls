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
package com.redhat.qute.services.documentlink.multiple;

import static com.redhat.qute.QuteAssert.dl;
import static com.redhat.qute.QuteAssert.r;
import static com.redhat.qute.QuteAssert.testDocumentLinkFor;

import org.junit.jupiter.api.Test;

import com.redhat.qute.project.multiple.QuteProjectB;

/**
 * Tests for Qute document link and project dependencies.
 * 
 * @author Angelo ZERR
 *
 */
public class QuteDocumentLinkTest {

	@Test
	public void includeExistingTemplate() throws Exception {
		// "src/main/resources/templates/index.html" from project-b references
		// "src/main/resources/templates/root.html" from project-a
		String template = "{#include root }\r\n" +
				"{/include}";
		testDocumentLinkFor(template, "src/main/resources/index.html", //
				QuteProjectB.PROJECT_URI, "",
				dl(r(0, 10, 0, 14),
						"src/test/resources/projects/project-a/src/main/resources/templates/root.html"));
	}

}

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
package com.redhat.qute.services.completions.roq;

import static com.redhat.qute.QuteAssert.c;
import static com.redhat.qute.QuteAssert.r;

import org.eclipse.lsp4j.CompletionItem;
import org.junit.jupiter.api.Test;

import com.redhat.qute.QuteAssert;
import com.redhat.qute.project.roq.RoqProject;

/**
 * Test completion with Roq Quarkus extension and data files.
 *
 * @author Angelo ZERR
 * 
 */
public class RoqUserTagCompletionsTest {

	@Test
	public void userTags() throws Exception {
		String template = "{#|}";
		testCompletionFor(template, //
				// from src/main/resources/templates/tags folder
				c("roq-default/hero", "{#roq-default/hero /}", r(0, 0, 0, 3)),
				// from binary templates
				c("bundle", "{#bundle /}", r(0, 0, 0, 3)));
	}

	public static void testCompletionFor(String value, CompletionItem... expectedItems) throws Exception {
		QuteAssert.testCompletionFor(value, false, QuteAssert.FILE_URI, null, RoqProject.PROJECT_URI,
				QuteAssert.TEMPLATE_BASE_DIR, null, expectedItems);
	}

}
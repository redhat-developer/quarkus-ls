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
package com.redhat.qute.services.inlayhint.roq;

import org.eclipse.lsp4j.InlayHint;
import org.junit.jupiter.api.Test;

import com.redhat.qute.QuteAssert;
import com.redhat.qute.project.roq.RoqProject;
import com.redhat.qute.settings.QuteInlayHintSettings;

/**
 * Tests for Qute inlay hint with page Roq
 * 
 * @author Angelo ZERR
 *
 */
public class RoqPageInlayHintTest {

	@Test
	public void pageData() throws Exception {
		String template = "{@io.quarkiverse.roq.frontmatter.runtime.model.Page page}\r\n" + //
				"{#for item in page.data.toc}\r\n" + //
				"    {#for subsection in item.subsections} \r\n" + //
				"    {/}    \r\n" + //
				"{/}";
		testInlayHintFor(template);

	}

	public static void testInlayHintFor(String value, InlayHint... expected) throws Exception {
		testInlayHintFor(value, null, expected);
	}

	public static void testInlayHintFor(String value, QuteInlayHintSettings inlayHintSettings, InlayHint... expected)
			throws Exception {
		QuteAssert.testInlayHintFor(value, QuteAssert.FILE_URI, null, RoqProject.PROJECT_URI,
				QuteAssert.TEMPLATE_BASE_DIR, inlayHintSettings, expected);
	}
}

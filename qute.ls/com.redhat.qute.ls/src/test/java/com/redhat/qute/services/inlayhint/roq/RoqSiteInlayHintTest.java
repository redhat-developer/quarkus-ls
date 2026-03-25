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

import static com.redhat.qute.QuteAssert.ih;
import static com.redhat.qute.QuteAssert.ihLabel;
import static com.redhat.qute.QuteAssert.p;

import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.InlayHint;
import org.junit.jupiter.api.Test;

import com.redhat.qute.QuteAssert;
import com.redhat.qute.project.roq.RoqProject;
import com.redhat.qute.services.inlayhint.InlayHintASTVistor;
import com.redhat.qute.settings.QuteInlayHintSettings;

/**
 * Tests for Qute inlay hint with site Roq
 * 
 * @author Angelo ZERR
 *
 */
public class RoqSiteInlayHintTest {

	@Test
	public void collection() throws Exception {
		String template = "{@io.quarkiverse.roq.frontmatter.runtime.model.Site site}\r\n" + //
				"{#for c in site.collections.posts.subList(0,1)}\r\n" + //
				" {c}\r\n" + //
				"{/for}";
		testInlayHintFor(template, //
				ih(p(1, 7), ihLabel(":"),
						ihLabel("DocumentPage",
								"Open `io.quarkiverse.roq.frontmatter.runtime.model.DocumentPage` Java type.",
								cd("io.quarkiverse.roq.frontmatter.runtime.model.DocumentPage"))));

	}

	public static void testInlayHintFor(String value, InlayHint... expected) throws Exception {
		testInlayHintFor(value, null, expected);
	}

	public static void testInlayHintFor(String value, QuteInlayHintSettings inlayHintSettings, InlayHint... expected)
			throws Exception {
		QuteAssert.testInlayHintFor(value, QuteAssert.FILE_URI, null, RoqProject.PROJECT_URI,
				QuteAssert.TEMPLATE_BASE_DIR, inlayHintSettings, expected);
	}

	private static Command cd(String javaType) {
		return InlayHintASTVistor.createOpenJavaTypeCommand(javaType, RoqProject.PROJECT_URI);
	}
}

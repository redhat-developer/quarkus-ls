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
package com.redhat.qute.services.inlayhint.renarde;

import static com.redhat.qute.QuteAssert.ih;
import static com.redhat.qute.QuteAssert.p;

import org.eclipse.lsp4j.InlayHint;
import org.junit.jupiter.api.Test;

import com.redhat.qute.QuteAssert;
import com.redhat.qute.project.renarde.RenardeProject;
import com.redhat.qute.settings.QuteInlayHintSettings;

/**
 * Tests for Qute inlay hint with {m: } Renarde expression.
 * 
 * @author Angelo ZERR
 *
 */
public class RenardeInlayHintTest {

	@Test
	public void messagesNamespace() throws Exception {
		String template = "{m:main.login}";
		testInlayHintFor(template, //
				ih(p(0, 13), " =Login/Register"));

		// enabled=false
		QuteInlayHintSettings settings = new QuteInlayHintSettings();
		settings.setEnabled(false);
		testInlayHintFor(template, //
				settings);

		// showMessages=false
		settings = new QuteInlayHintSettings();
		settings.setShowMessages(false);
		testInlayHintFor(template, //
				settings);

	}

	@Test
	public void nullNamespace() throws Exception {
		String template = "{m:";
		testInlayHintFor(template);
	}

	public static void testInlayHintFor(String value, InlayHint... expected) throws Exception {
		testInlayHintFor(value, null, expected);
	}

	public static void testInlayHintFor(String value, QuteInlayHintSettings inlayHintSettings, InlayHint... expected)
			throws Exception {
		QuteAssert.testInlayHintFor(value, QuteAssert.FILE_URI, null, RenardeProject.PROJECT_URI,
				QuteAssert.TEMPLATE_BASE_DIR, inlayHintSettings, expected);
	}
}

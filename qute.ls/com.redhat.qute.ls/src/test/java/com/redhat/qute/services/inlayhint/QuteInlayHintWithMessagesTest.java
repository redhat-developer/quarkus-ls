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
package com.redhat.qute.services.inlayhint;

import static com.redhat.qute.QuteAssert.ih;
import static com.redhat.qute.QuteAssert.ihLabel;
import static com.redhat.qute.QuteAssert.p;
import static com.redhat.qute.QuteAssert.testInlayHintFor;

import org.eclipse.lsp4j.Command;
import org.junit.jupiter.api.Test;

import com.redhat.qute.project.QuteQuickStartProject;

/**
 * Tests for Qute inlay hint with Type-safe Message Bundles support.
 * 
 * @author Angelo ZERR
 * 
 * @see https://quarkus.io/guides/qute-reference#type-safe-message-bundles
 *
 */
public class QuteInlayHintWithMessagesTest {

	@Test
	public void hello_name() throws Exception {
		String template = "{msg:hello_name('Lucie')}"; // [Hello {name ?: 'Qute'}]
		testInlayHintFor(template, //
				ih(p(0, 25), ihLabel("Hello {name ?: 'Qute'}", "Edit `Hello {name ?: 'Qute'}` Java message.",
						cd("Hello {name ?: 'Qute'}", "org.acme.AppMessages", "hello_name"))));
	}

	@Test
	public void invalid() throws Exception {
		String template = "{msg:}";
		testInlayHintFor(template);

		template = "{msg:hello}";
		testInlayHintFor(template);

		template = "{msg:hello()}";
		testInlayHintFor(template);
	}

	@Test
	public void helloObjectPart() throws Exception {
		String template = "{msg2:hello}"; // [Hello!]
		testInlayHintFor(template, //
				ih(p(0, 12), ihLabel("Hello!", "Edit `Hello!` Java message.",
						cd("Hello!", "org.acme.App2Messages", "hello"))));
	}

	@Test
	public void helloMethodPart() throws Exception {
		String template = "{msg2:hello()}"; // [Hello!]
		testInlayHintFor(template, //
				ih(p(0, 14), ihLabel("Hello!", "Edit `Hello!` Java message.",
						cd("Hello!", "org.acme.App2Messages", "hello"))));
	}

	private static Command cd(String messageContent, String sourceType, String sourceMethod) {
		return InlayHintASTVistor.createEditJavaMessageCommand(messageContent, sourceType, sourceMethod,
				QuteQuickStartProject.PROJECT_URI);
	}
}

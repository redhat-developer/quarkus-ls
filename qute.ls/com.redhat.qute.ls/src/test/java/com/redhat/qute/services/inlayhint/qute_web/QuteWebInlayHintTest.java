/*******************************************************************************
* Copyright (c) 2024 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.services.inlayhint.qute_web;

import static com.redhat.qute.QuteAssert.ih;
import static com.redhat.qute.QuteAssert.ihLabel;
import static com.redhat.qute.QuteAssert.p;

import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.InlayHint;
import org.junit.jupiter.api.Test;

import com.redhat.qute.QuteAssert;
import com.redhat.qute.project.qute_web.QuteWebProject;
import com.redhat.qute.services.inlayhint.InlayHintASTVistor;

/**
 * Tests for Qute inlay hint in Qute Web project.
 * 
 * @author Angelo ZERR
 *
 */
public class QuteWebInlayHintTest {

	@Test
	public void http_request() throws Exception {
		// http:request
		String template = "{#let name=http:request}";
		testInlayHintFor(template, //
				ih(p(0, 10), ihLabel(":"),
						ihLabel("HttpServerRequest", "Open `io.vertx.core.http.HttpServerRequest` Java type.",
								cd("io.vertx.core.http.HttpServerRequest"))));

		// http:request()
		template = "{#let name=http:request()}";
		testInlayHintFor(template, //
				ih(p(0, 10), ihLabel(":"),
						ihLabel("HttpServerRequest", "Open `io.vertx.core.http.HttpServerRequest` Java type.",
								cd("io.vertx.core.http.HttpServerRequest"))));
	}

	@Test
	public void http_param() throws Exception {
		String template = "{#let name=http:param('name', 'Qute')}";
		testInlayHintFor(template, //
				ih(p(0, 10), ihLabel(":"), ihLabel("String", "Open `java.lang.String` Java type.", cd("java.lang.String"))));

	}

	public static void testInlayHintFor(String value, InlayHint... expected)
			throws Exception {
		QuteAssert.testInlayHintFor(value, QuteAssert.FILE_URI, null, QuteWebProject.PROJECT_URI,
				QuteAssert.TEMPLATE_BASE_DIR, null, expected);
	}

	private static Command cd(String javaType) {
		return InlayHintASTVistor.createOpenJavaTypeCommand(javaType, QuteWebProject.PROJECT_URI);
	}
}

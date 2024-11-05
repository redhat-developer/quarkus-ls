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
package com.redhat.qute.services.diagnostics.roq;

import org.eclipse.lsp4j.Diagnostic;
import org.junit.jupiter.api.Test;

import com.redhat.qute.QuteAssert;
import com.redhat.qute.project.roq.RoqProject;

/**
 * Test diagnostics with Roq Quarkus extension.
 *
 * @author Angelo ZERR
 *
 */
public class RoqDiagnosticsTest {
 
	@Test
	public void noErrorByUsingMatchNameAny() throws Exception {
		// Here posts is declared although collections doesn't declare posts
		// It uses a template extension collections.get('posts') with matchName=*
		String template = "{@io.quarkiverse.roq.frontmatter.runtime.model.Site site}\r\n" + //
				"{#for post in site.collections.posts.paginated(null)}\r\n" + //
				"{#if post.hidden}\r\n" + //
				"{/if}" + //
				"{/for}";
		testDiagnosticsFor(template);
	}

	@Test
	public void noErrorWithStringParameter() {
		String template = "{@io.quarkiverse.roq.frontmatter.runtime.model.DocumentPage page}\r\n" + //
				"{page.date.format('yyyy, MMM dd')}";
		testDiagnosticsFor(template);
	}

	@Test
	public void pageWithNoDeclaration() {		
		// As 'page' and 'site' are injected for all Qute templates for a Roq application
		// no need to declare {@io.quarkiverse.roq.frontmatter.runtime.model.DocumentPage page}
		String template = "{page.date.format('yyyy, MMM dd')}";
		testDiagnosticsFor(template);
	}
	
	private static void testDiagnosticsFor(String value, Diagnostic... expected) {
		QuteAssert.testDiagnosticsFor(value, QuteAssert.FILE_URI, null, RoqProject.PROJECT_URI,
				QuteAssert.TEMPLATE_BASE_DIR, true, null, expected);
	}

}

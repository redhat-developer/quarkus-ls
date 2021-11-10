/*******************************************************************************
* Copyright (c) 2021 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.services;

import static com.redhat.qute.QuteAssert.dl;
import static com.redhat.qute.QuteAssert.r;
import static com.redhat.qute.QuteAssert.testDocumentLinkFor;

import org.junit.jupiter.api.Test;

/**
 * Tests for Qute document link.
 * 
 * @author Angelo ZERR
 *
 */
public class QuteDocumentLinkTest {

	@Test
	public void includeExistingTemplate() throws Exception {
		String template = "{#include base} \r\n" + //
				"  {#title}My Title{/title} \r\n" + //
				"  <div> \r\n" + //
				"    My body.\r\n" + //
				"  </div>\r\n" + //
				"{/include}";
		testDocumentLinkFor(template, "src/test/resources/detail", //
				dl(r(0,10,0,14), "src/test/resources/templates/base.qute.html"));
	}
	
	@Test
	public void includeTemplateNotFound() throws Exception {
		String template = "{#include XXXX} \r\n" + //
				"  {#title}My Title{/title} \r\n" + //
				"  <div> \r\n" + //
				"    My body.\r\n" + //
				"  </div>\r\n" + //
				"{/include}";
		testDocumentLinkFor(template, "src/test/resources/detail", //
				dl(r(0,10,0,14), "src/test/resources/templates/XXXX.qute.html"));
	}
}

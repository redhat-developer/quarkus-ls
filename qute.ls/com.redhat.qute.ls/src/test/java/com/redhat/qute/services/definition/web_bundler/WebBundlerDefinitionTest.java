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
package com.redhat.qute.services.definition.web_bundler;

import static com.redhat.qute.QuteAssert.ll;
import static com.redhat.qute.QuteAssert.r;

import org.eclipse.lsp4j.LocationLink;
import org.junit.jupiter.api.Test;

import com.redhat.qute.QuteAssert;
import com.redhat.qute.project.web_bundler.WebBundlerProject;

/**
 * Test definition in Roq with data file.
 * 
 * @author Angelo ZERR
 *
 */
public class WebBundlerDefinitionTest {

	@Test
	public void userTagFromComponent() throws Exception {
		String permissionDialogUri = WebBundlerProject
				.getFileUri("/src/main/resources/web/components/dialog/permissionDialog.html");

		String template = "{#permissionDialog\r\n" + //
				"    title = \"\"\r\n" + //
				"    selectedCode = \"\"\r\n" + //
				"}\r\n" + //
				"    {#invalid-tag /}\r\n" + //
				"    {#trig|ger}\r\n" + // <-- trigger comes from {#insert trigger}
										// declared in
										// permissionDialog.html
				"        <button type=\"button\">Edit</button>\r\n" + //
				"    {/trigger}\r\n" + //
				"{/permissionDialog}";

		testDefinitionFor(template, //
				ll(permissionDialogUri, r(5, 5, 5, 13), r(6, 13, 6, 20)));
	}

	public static void testDefinitionFor(String value, LocationLink... expected) throws Exception {
		QuteAssert.testDefinitionFor(value, null, WebBundlerProject.PROJECT_URI, QuteAssert.TEMPLATE_BASE_DIR,
				expected);
	}
}

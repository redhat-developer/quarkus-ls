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
package com.redhat.qute.services.codelens.roq;

import static com.redhat.qute.QuteAssert.cl;
import static com.redhat.qute.QuteAssert.r;

import org.eclipse.lsp4j.CodeLens;
import org.junit.jupiter.api.Test;

import com.redhat.qute.QuteAssert;
import com.redhat.qute.project.roq.RoqProject;

/**
 * Tests for Qute code lens and data model for template which belongs to a Roq
 * application.
 * 
 * @author Angelo ZERR
 *
 */
public class RoqCodeLensTest {

	@Test
	public void pageAndDocument() throws Exception {
		String value = "";
		testCodeLensFor(value, "src/main/resources/templates/ItemResource/XXXXXXXXXXX.qute.html", //
				cl(r(0, 0, 0, 0), "site : Site", ""), //
				cl(r(0, 0, 0, 0), "page : Page", ""));
	}

	public static void testCodeLensFor(String value, String fileUri, CodeLens... expected) throws Exception {
		QuteAssert.testCodeLensFor(value, fileUri, null, RoqProject.PROJECT_URI, QuteAssert.TEMPLATE_BASE_DIR,
				expected);
	}
}

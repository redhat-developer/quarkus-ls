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
import static com.redhat.qute.project.roq.RoqProject.getFileUri;

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
	public void normalPage() throws Exception {
		String value = "";
		testCodeLensFor(value, getFileUri("/src/main/resources/templates/ItemResource/XXXXXXXXXXX.qute.html"), //
				cl(r(0, 0, 0, 0), "site : Site", ""), //
				cl(r(0, 0, 0, 0), "page : NormalPage", ""));
	}

	@Test
	public void noCollection() throws Exception {
		// content folder is not a collection --> NormalPage
		String value = "";
		String fileUri = getFileUri("/content/foo.html");
		testCodeLensFor(value, fileUri, //
				cl(r(0, 0, 0, 0), "site : Site", ""), //
				cl(r(0, 0, 0, 0), "page : NormalPage", ""));
		
		fileUri = getFileUri("/content/no-collecton/foo.html");
		testCodeLensFor(value, fileUri, //
				cl(r(0, 0, 0, 0), "site : Site", ""), //
				cl(r(0, 0, 0, 0), "page : NormalPage", ""));
	}

	@Test
	public void collection() throws Exception {
		// content/posts folder is a collection --> DocumentPage 
		String value = "";
		String fileUri = getFileUri("/content/posts/foo.html");
		testCodeLensFor(value, fileUri, //
				cl(r(0, 0, 0, 0), "site : Site", ""), //
				cl(r(0, 0, 0, 0), "page : DocumentPage", ""));
		
		// special case : index.html --> NormalPage
		fileUri = getFileUri("/content/posts/index.html");
		testCodeLensFor(value, fileUri, //
				cl(r(0, 0, 0, 0), "site : Site", ""), //
				cl(r(0, 0, 0, 0), "page : NormalPage", ""));
	}

	public static void testCodeLensFor(String value, String fileUri, CodeLens... expected) throws Exception {
		QuteAssert.testCodeLensFor(value, fileUri, null, RoqProject.PROJECT_URI, QuteAssert.TEMPLATE_BASE_DIR,
				expected);
	}
}

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
package com.redhat.qute.services.definition;

import static com.redhat.qute.QuteAssert.ll;
import static com.redhat.qute.QuteAssert.r;
import static com.redhat.qute.QuteAssert.testDefinitionFor;

import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

/**
 * Test definition with user tag.
 * 
 * @author Angelo ZERR
 *
 */
public class QuteDefinitionWithUserTagTest {

	@Test
	public void inputName() throws Exception {
		String inputUri = getUserTagUri("input");
		String template = "{#input na|me=\"name\"}";
		testDefinitionFor(template, "test.html", //
				ll(inputUri, r(0, 8, 0, 12), r(0, 14, 0, 18)), //
				ll(inputUri, r(0, 8, 0, 12), r(4, 31, 4, 35)), //
				ll(inputUri, r(0, 8, 0, 12), r(6, 26, 6, 30)));
	}

	@Test
	public void inputNoDef() throws Exception {
		String template = "{#input na|me2=\"name\"}";
		testDefinitionFor(template);
	}

	private static String getUserTagUri(String name) {
		return Paths.get("src/test/resources/templates/tags/" + name + ".html").toUri().toString();
	}

}
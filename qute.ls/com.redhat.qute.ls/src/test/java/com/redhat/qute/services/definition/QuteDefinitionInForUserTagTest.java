/*******************************************************************************
* Copyright (c) 2022 Red Hat Inc. and others.
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
public class QuteDefinitionInForUserTagTest {

	@Test
	public void param() throws Exception {
		String template = "{#for item in items}\r\n" + //
				"	{#linkItem item name=it|em.name item /}\r\n" + //
				"{/for}";
		testDefinitionFor(template, "test.qute", //
				ll("test.qute", r(1, 22, 1, 26), r(0, 6, 0, 10)));
	}

	@Test
	public void it() throws Exception {
		String template = "{#for item in items}\r\n" + //
				"	{#linkItem it|em name=item.name item /}\r\n" + //
				"{/for}";
		testDefinitionFor(template, "test.qute", //
				ll("test.qute", r(1, 12, 1, 16), r(0, 6, 0, 10)));
	}

	@Test
	public void noIt() throws Exception {
		String template = "{#for item in items}\r\n" + //
				"	{#linkItem item name=item.name ite|m /}\r\n" + //
				"{/for}";
		testDefinitionFor(template);
	}

	@Test
	public void inputName() throws Exception {
		String inputUri = getUserTagUri("input");
		String template = "{#input na|me=\"name\"}";
		testDefinitionFor(template, "test.html", //
				ll(inputUri, r(0, 8, 0, 12), r(0, 14, 0, 18)));
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

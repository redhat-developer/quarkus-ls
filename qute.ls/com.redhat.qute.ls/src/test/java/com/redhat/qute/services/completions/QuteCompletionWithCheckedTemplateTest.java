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
package com.redhat.qute.services.completions;

import static com.redhat.qute.QuteAssert.c;
import static com.redhat.qute.QuteAssert.r;
import static com.redhat.qute.QuteAssert.testCompletionFor;

import org.junit.jupiter.api.Test;

/**
 * Tests for Qute completion with @CkeckedTemplate.
 * 
 * @author Angelo ZERR
 *
 */
public class QuteCompletionWithCheckedTemplateTest {

	@Test
	public void noCheckedTemplateMatching() throws Exception {
		String template = "Item: {|";
		testCompletionFor(template);
	}

	@Test
	public void checkedTemplateMatching() throws Exception {
		String template = "Item: {|";
		testCompletionFor(template, //
				"src/main/resources/templates/ItemResource/items.qute.html", //
				c("items", "items", r(0, 7, 0, 7)));
	}

	@Test
	public void checkedTemplateWithOverlappingTemplateParam() throws Exception {
		String template = "{@java.lang.List<java.lang.String> items}\n" + //
				"Item: {|";
		testCompletionFor(template, //
				"src/main/resources/templates/ItemResource/items.qute.html", //
				"ItemResource/Items", 6, c("items", "items", r(1, 7, 1, 7)));
	}
}
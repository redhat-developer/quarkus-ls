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
package com.redhat.qute.services.completions;

import static com.redhat.qute.QuteAssert.c;
import static com.redhat.qute.QuteAssert.r;
import static com.redhat.qute.QuteAssert.testCompletionFor;

import org.junit.jupiter.api.Test;

/**
 * Tests for Qute completion in expression for #if section.
 *
 * @author Angelo ZERR
 *
 */
public class QuteCompletionInExpressionWithIfSectionTest {

	@Test
	public void objectPart() throws Exception {
		String template = "{@org.acme.Item item}\r\n" + //
				"{#if |}";
		testCompletionFor(template, //
				c("item", "item", r(1, 5, 1, 5)));
		
		template = "{@org.acme.Item item}\r\n" + //
				"{#if abcd != '' && i|t }";
		testCompletionFor(template, //
				c("item", "item", r(1, 19, 1, 21)));
	}
	
	@Test
	public void propertyPart() throws Exception {
		String template = "{@org.acme.Item item}\r\n" + //
				"{#if item.|}";
		testCompletionFor(template, //
				c("name : String", "name", r(1, 10, 1, 10)));
		
		template = "{@org.acme.Item item}\r\n" + //
				"{#if abcd != '' && item.| }";
		testCompletionFor(template, //
				c("name : String", "name", r(1, 24, 1, 24)));
	}}
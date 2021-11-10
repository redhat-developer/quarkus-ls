/*******************************************************************************
* Copyright (c) 2020 Red Hat Inc. and others.
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
 * Tests for Qute completion in parameter declaration.
 * 
 * @author Angelo ZERR
 *
 */
public class QuteCompletionInParameterDeclarationTest {

	// 
	@Test
	public void completionInParameterDeclarationForJavaClass() throws Exception {
		String template = "{@|}\r\n";

		// Without snippet
		testCompletionFor(template, //
				// Package completion
				c("org.acme", "org.acme", r(0, 2, 0, 2)), //
				// Class completion
				c("org.acme.Item", "org.acme.Item item", r(0, 2, 0, 2)), //
				c("org.acme.Review", "org.acme.Review review", r(0, 2, 0, 2)));

		// With snippet support
		testCompletionFor(template, //
				true, // snippet support
				// Package completion
				c("org.acme", "org.acme", r(0, 2, 0, 2)), //
				// Class completion
				c("org.acme.Item", "org.acme.Item ${1:item}$0", r(0, 2, 0, 2)), //
				c("org.acme.Review", "org.acme.Review ${1:review}$0", r(0, 2, 0, 2)));

	}

	@Test
	public void completionInParameterDeclarationForJavaClass2() throws Exception {
		String template = "{@|\r\n";

		// Without snippet
		testCompletionFor(template, //
				// Package completion
				c("org.acme", "org.acme", r(0, 2, 0, 2)), //
				// Class completion
				c("org.acme.Item", "org.acme.Item item", r(0, 2, 0, 2)), //
				c("org.acme.Review", "org.acme.Review review", r(0, 2, 0, 2)));

		// With snippet support
		testCompletionFor(template, //
				true, // snippet support
				// Package completion
				c("org.acme", "org.acme", r(0, 2, 0, 2)), //
				// Class completion
				c("org.acme.Item", "org.acme.Item ${1:item}$0", r(0, 2, 0, 2)), //
				c("org.acme.Review", "org.acme.Review ${1:review}$0", r(0, 2, 0, 2)));

	}

	@Test
	public void completionInParameterDeclarationForJavaClass3() throws Exception {
		String template = "{@I|t\r\n";

		// Without snippet
		testCompletionFor(template, //
				// Package completion
				c("org.acme", "org.acme", r(0, 2, 1, 0)), //
				// Class completion
				c("org.acme.Item", "org.acme.Item item", r(0, 2, 1, 0)), //
				c("org.acme.Review", "org.acme.Review review", r(0, 2, 1, 0)));

		// With snippet support
		testCompletionFor(template, //
				true, // snippet support
				// Package completion
				c("org.acme", "org.acme", r(0, 2, 1, 0)), //
				// Class completion
				c("org.acme.Item", "org.acme.Item ${1:item}$0", r(0, 2, 1, 0)), //
				c("org.acme.Review", "org.acme.Review ${1:review}$0", r(0, 2, 1, 0)));

	}

}
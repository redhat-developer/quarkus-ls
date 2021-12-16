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
 * Tests for Qute completion in expression for #with section.
 *
 * @author Angelo ZERR
 *
 */
public class QuteCompletionInExpressionWithWithSectionTest {

	@Test
	public void propertyAndMethodPart() throws Exception {
		String template = "{@org.acme.Item item}\r\n" + //
				"{#with item}\r\n" + //
				"  <h1>{|}</h1>\r\n" + // <-- completion here
				"{/with}";
		testCompletionFor(template, //
				c("item", "item", r(2, 7, 2, 7)), //
				c("name : String", "name", r(2, 7, 2, 7)), //
				c("price : BigInteger", "price", r(2, 7, 2, 7)), //
				c("review : Review", "review", r(2, 7, 2, 7)), //
				c("review2 : Review", "review2", r(2, 7, 2, 7)), //
				c("getReview2() : Review", "getReview2", r(2, 7, 2, 7)));
	}
}
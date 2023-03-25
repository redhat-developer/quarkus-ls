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
package com.redhat.qute.services.completions;

import static com.redhat.qute.QuteAssert.c;
import static com.redhat.qute.QuteAssert.r;
import static com.redhat.qute.QuteAssert.testCompletionFor;

import org.junit.jupiter.api.Test;

/**
 * Test completion with CompletableFuture and Uni as data model.
 *
 * @author Angelo ZERR
 *
 */
public class QuteCompletionInExpressionWithCompletionStageOrUniTest {

	@Test
	public void propertyPartWithCompletionStage() throws Exception {
		String template = "{@java.util.concurrent.CompletionStage<java.util.List<org.acme.Item>> items}\r\n" + //
				" \r\n" + //
				"{#for item in items}\r\n" + //
				"	{item.|}    \r\n" + //
				"{/for}";
		testCompletionFor(template, //
				c("name : String", "name", r(3, 7, 3, 7)), //
				c("price : BigInteger", "price", r(3, 7, 3, 7)), //
				c("review : Review", "review", r(3, 7, 3, 7)), //
				c("review2 : Review", "review2", r(3, 7, 3, 7)), //
				c("getReview2() : Review", "getReview2", r(3, 7, 3, 7)));
	}

	@Test
	public void propertyPartWithCompletableFuture() throws Exception {
		String template = "{@java.util.concurrent.CompletableFuture<java.util.List<org.acme.Item>> items}\r\n" + //
				" \r\n" + //
				"{#for item in items}\r\n" + //
				"	{item.|}    \r\n" + //
				"{/for}";
		testCompletionFor(template, //
				c("name : String", "name", r(3, 7, 3, 7)), //
				c("price : BigInteger", "price", r(3, 7, 3, 7)), //
				c("review : Review", "review", r(3, 7, 3, 7)), //
				c("review2 : Review", "review2", r(3, 7, 3, 7)), //
				c("getReview2() : Review", "getReview2", r(3, 7, 3, 7)));
	}

	@Test
	public void propertyPartWithUni() throws Exception {
		String template = "{@io.smallrye.mutiny.Uni<java.util.List<org.acme.Item>> items}\r\n" + //
				" \r\n" + //
				"{#for item in items}\r\n" + //
				"	{item.|}    \r\n" + //
				"{/for}";
		testCompletionFor(template, //
				c("name : String", "name", r(3, 7, 3, 7)), //
				c("price : BigInteger", "price", r(3, 7, 3, 7)), //
				c("review : Review", "review", r(3, 7, 3, 7)), //
				c("review2 : Review", "review2", r(3, 7, 3, 7)), //
				c("getReview2() : Review", "getReview2", r(3, 7, 3, 7)));
	}

	@Test
	public void propertyPartWitAsyncResultUni() throws Exception {
		String template = "{@io.smallrye.mutiny.vertx.AsyncResultUni<java.util.List<org.acme.Item>> items}\r\n" + //
				" \r\n" + //
				"{#for item in items}\r\n" + //
				"	{item.|}    \r\n" + //
				"{/for}";
		testCompletionFor(template, //
				c("name : String", "name", r(3, 7, 3, 7)), //
				c("price : BigInteger", "price", r(3, 7, 3, 7)), //
				c("review : Review", "review", r(3, 7, 3, 7)), //
				c("review2 : Review", "review2", r(3, 7, 3, 7)), //
				c("getReview2() : Review", "getReview2", r(3, 7, 3, 7)));
	}
}
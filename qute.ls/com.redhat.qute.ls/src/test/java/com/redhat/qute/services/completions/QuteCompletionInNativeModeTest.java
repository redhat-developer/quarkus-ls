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

import org.eclipse.lsp4j.CompletionItem;
import org.junit.jupiter.api.Test;

import com.redhat.qute.QuteAssert;
import com.redhat.qute.project.QuteQuickStartProject;
import com.redhat.qute.settings.QuteNativeSettings;

/**
 * Tests for Qute completion in native mode.
 *
 * @author Angelo ZERR
 *
 */
public class QuteCompletionInNativeModeTest {

	@Test
	public void noAnnotations() throws Exception {
		// public class Item

		String template = "{@org.acme.Item item}\r\n" + //
				"Item: {item.|}";

		testCompletionFor(template, 7 /* value resolvers */,
				c("orEmpty(base : T) : List<T>", "orEmpty", r(1, 12, 1, 12)),
				c("ifTruthy(base : T, arg : Object) : T", "ifTruthy(arg)", r(1, 12, 1, 12)),
				c("or(base : T, arg : Object) : T", "or(arg)", r(1, 12, 1, 12)), //
				c("discountedPrice(item : Item) : BigDecimal", "discountedPrice", r(1, 12, 1, 12)), //
				c("pretty(item : Item, elements : String...) : String", "pretty(elements)", r(1, 12, 1, 12)));
	}

	@Test
	public void templateData() throws Exception {
		// @TemplateData
		// public class ItemWithTemplateData

		String template = "{@org.acme.ItemWithTemplateData item}\r\n" + //
				"Item: {item.|}";
		testCompletionFor(template, 6 /* value resolvers */ + 8, //
				c("base : String", "base", r(1, 12, 1, 12)), // comes from BaseItem extended by ItemWithTemplateData
				c("getReviews() : List<Review>", "getReviews", r(1, 12, 1, 12)), // comes from BaseItem extended by
																					// ItemWithTemplateData
				c("name : String", "name", r(1, 12, 1, 12)), //
				c("price : BigInteger", "price", r(1, 12, 1, 12)), //
				c("review2 : Review", "review2", r(1, 12, 1, 12)), //
				c("getReview2() : Review", "getReview2", r(1, 12, 1, 12)));
	}

	@Test
	public void templateDataIgnoreSubClasses() throws Exception {
		// @TemplateData(ignoreSuperclasses = true)
		// public class ItemWithTemplateDataIgnoreSubClasses

		String template = "{@org.acme.ItemWithTemplateDataIgnoreSubClasses item}\r\n" + //
				"Item: {item.|}";
		testCompletionFor(template, 6 /* value resolvers */ + 5, //
//				c("base : String", "base", r(1, 12, 1, 12)), // comes from BaseItem extended by ItemWithTemplateDataIgnoreSubClasses
//				c("getReviews() : List<Review>", "getReviews", r(1, 12, 1, 12)), // comes from BaseItem extended by
				// ItemWithTemplateDataIgnoreSubClasses
				c("name : String", "name", r(1, 12, 1, 12)), //
				c("price : BigInteger", "price", r(1, 12, 1, 12)), //
				c("review2 : Review", "review2", r(1, 12, 1, 12)), //
				c("getReview2() : Review", "getReview2", r(1, 12, 1, 12)));
	}

	@Test
	public void itemWithRegisterForReflection() throws Exception {
		// @RegisterForReflection
		// public class ItemWithRegisterForReflection

		String template = "{@org.acme.ItemWithRegisterForReflection item}\r\n" + //
				"Item: {item.|}";
		testCompletionFor(template, 6 /* value resolvers */ + 8, //
				c("base : String", "base", r(1, 12, 1, 12)), // comes from BaseItem extended by ItemWithTemplateData
				c("getReviews() : List<Review>", "getReviews", r(1, 12, 1, 12)), // comes from BaseItem extended by
																					// ItemWithTemplateData
				c("name : String", "name", r(1, 12, 1, 12)), //
				c("price : BigInteger", "price", r(1, 12, 1, 12)), //
				c("review2 : Review", "review2", r(1, 12, 1, 12)), //
				c("getReview2() : Review", "getReview2", r(1, 12, 1, 12)));
	}

	@Test
	public void itemWithRegisterForReflectionNoFields() throws Exception {
		// @RegisterForReflection(fields = false)
		// public class ItemWithRegisterForReflection

		String template = "{@org.acme.ItemWithRegisterForReflectionNoFields item}\r\n" + //
				"Item: {item.|}";
		testCompletionFor(template, 6 /* value resolvers */ + 4 /* only methods */, //
				// c("base : String", "base", r(1, 12, 1, 12)), // comes from BaseItem extended
				// by ItemWithTemplateData
				c("getReviews() : List<Review>", "getReviews", r(1, 12, 1, 12)), // comes from BaseItem extended by
																					// ItemWithTemplateData
				// c("name : String", "name", r(1, 12, 1, 12)), //
				// c("price : BigInteger", "price", r(1, 12, 1, 12)), //
				c("review2 : Review", "review2", r(1, 12, 1, 12)), //
				c("getReview2() : Review", "getReview2", r(1, 12, 1, 12)));
	}

	@Test
	public void itemWithRegisterForReflectionNoMethods() throws Exception {
		// @RegisterForReflection(methods = false)
		// public class ItemWithRegisterForReflectionNoMethods

		String template = "{@org.acme.ItemWithRegisterForReflectionNoMethods item}\r\n" + //
				"Item: {item.|}";
		testCompletionFor(template, 6 /* value resolvers */ + 5 /* only fields */, //
				c("base : String", "base", r(1, 12, 1, 12)), // comes from BaseItem extended by ItemWithTemplateData
				// c("getReviews() : List<Review>", "getReviews", r(1, 12, 1, 12)), // comes
				// from BaseItem extended by
				// ItemWithTemplateData
				c("name : String", "name", r(1, 12, 1, 12)), //
				c("price : BigInteger", "price", r(1, 12, 1, 12)), //
				c("review2 : Review", "review2", r(1, 12, 1, 12)) //
		// c("getReview2() : Review", "getReview2", r(1, 12, 1, 12))
		);
	}

	private static void testCompletionFor(String template, Integer expectedCount, CompletionItem... expected)
			throws Exception {
		QuteNativeSettings nativeSettings = new QuteNativeSettings();
		nativeSettings.setEnabled(true);
		QuteAssert.testCompletionFor(template, false, "test.qute", null, QuteQuickStartProject.PROJECT_URI,
				QuteAssert.TEMPLATE_BASE_DIR, expectedCount, nativeSettings, expected);
	}

}
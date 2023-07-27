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
		// public class Review

		String template = // "{@org.acme.Review review}\r\n" + //
				"{review.|}";

		testCompletionFor(template, 5 /* value resolvers */, //
				c("orEmpty(base : T) : List<T>", "orEmpty", r(0, 8, 0, 8)),
				c("ifTruthy(base : T, arg : Object) : T", "ifTruthy(arg)", r(0, 8, 0, 8)),
				c("or(base : T, arg : Object) : T", "or(arg)", r(0, 8, 0, 8)));
	}

	@Test
	public void templateData() throws Exception {
		// @TemplateData
		// public class ItemWithTemplateData

		String template = "{@org.acme.ItemWithTemplateData item}\r\n" + //
				"Item: {item.|}";
		testCompletionFor(template, 6 /* value resolvers */ + 10, //
				c("base : String", "base", r(1, 12, 1, 12)), // comes from BaseItem extended by ItemWithTemplateData
				c("getReviews() : List<Review>", "getReviews", r(1, 12, 1, 12)), // comes from BaseItem extended by
																					// ItemWithTemplateData
				c("name : String", "name", r(1, 12, 1, 12)), //
				c("price : BigInteger", "price", r(1, 12, 1, 12)), //
				c("review2 : Review", "review2", r(1, 12, 1, 12)), //
				c("getReview2() : Review", "getReview2", r(1, 12, 1, 12)));

		template = "{@org.acme.ItemWithTemplateData item}\r\n" + //
				"Item: {item.name.|}";
		testCompletionFor(template, 5 /* value resolvers */);

		template = "{@org.acme.ItemWithTemplateData item}\r\n" + //
				"Item: {item.price.|}";
		testCompletionFor(template, 5 /* value resolvers */);

	}

	@Test
	public void templateDataWithTarget() throws Exception {
		// @TemplateData
		// @TemplateData(target = BigInteger.class)
		// public class ItemWithTemplateDataWithTarget

		String template = "{@org.acme.ItemWithTemplateDataWithTarget item}\r\n" + //
				"Item: {item.|}";
		testCompletionFor(template, 6 /* value resolvers */ + 10, //
				c("convert(item : AbstractItem) : int", "convert(item)", r(1, 12, 1, 12)),
				c("base : String", "base", r(1, 12, 1, 12)), // comes from BaseItem extended by ItemWithTemplateData
				c("getReviews() : List<Review>", "getReviews", r(1, 12, 1, 12)), // comes from BaseItem extended by
																					// ItemWithTemplateData
				c("name : String", "name", r(1, 12, 1, 12)), //
				c("price : BigInteger", "price", r(1, 12, 1, 12)), //
				c("review2 : Review", "review2", r(1, 12, 1, 12)), //
				c("getReview2() : Review", "getReview2", r(1, 12, 1, 12)));

		template = "{@org.acme.ItemWithTemplateDataWithTarget item}\r\n" + //
				"Item: {item.price.|}";
		testCompletionFor(template, 5 /* value resolvers */);

		template = "{@org.acme.ItemWithTemplateDataWithTarget item}\r\n" + //
				"Item: {item.name.|}";
		testCompletionFor(template, 5 /* value resolvers */ + 8 /* String fields and methods */,
				c("isEmpty() : boolean", "isEmpty", r(1, 17, 1, 17)));
	}

	@Test
	public void templateDataIgnoreWithProperties() throws Exception {
		// @TemplateData(properties = true)
		// public class ItemWithTemplateDataProperties

		String template = // "{@org.acme.ItemWithTemplateDataProperties itemWithTemplateDataProperties}\r\n" + //
				"{itemWithTemplateDataProperties.|}";
		testCompletionFor(template, 6 /* value resolvers */ + 9, //
//				c("convert(item : AbstractItem) : int", "convert(item)", r(0, 32, 0, 32)),
				c("base : String", "base", r(0, 32, 0, 32)), // comes from BaseItem extended by
																// ItemWithTemplateDataIgnoreSubClasses
				c("getReviews() : List<Review>", "getReviews", r(0, 32, 0, 32)), // comes from BaseItem extended by
				// ItemWithTemplateDataProperties
				c("name : String", "name", r(0, 32, 0, 32)), //
				c("price : BigInteger", "price", r(0, 32, 0, 32)), //
				c("review2 : Review", "review2", r(0, 32, 0, 32)), //
				c("getReview2() : Review", "getReview2", r(0, 32, 0, 32)));
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

		String template = // "{@org.acme.ItemWithRegisterForReflectionNoFields itemWithRegisterForReflectionNoFields}\r\n" + //
				"{itemWithRegisterForReflectionNoFields.|}";
		testCompletionFor(template, 6 /* value resolvers */ + 4 /* only methods */, //
				// c("base : String", "base", r(0, 39, 0, 39)), // comes from BaseItem extended
				// by ItemWithTemplateData
				c("getReviews() : List<Review>", "getReviews", r(0, 39, 0, 39)), // comes from BaseItem extended by
																					// ItemWithTemplateData
				// c("name : String", "name", r(0, 39, 0, 39)), //
				// c("price : BigInteger", "price", r(0, 39, 0, 39)), //
				c("review2 : Review", "review2", r(0, 39, 0, 39)), //
				c("getReview2() : Review", "getReview2", r(0, 39, 0, 39)));
	}

	@Test
	public void itemWithRegisterForReflectionNoMethods() throws Exception {
		// @RegisterForReflection(methods = false)
		// public class ItemWithRegisterForReflectionNoMethods

		String template = // "{@org.acme.ItemWithRegisterForReflectionNoMethods
							// itemWithRegisterForReflectionNoMethods}\r\n" + //
				"Item: {itemWithRegisterForReflectionNoMethods.|}";
		testCompletionFor(template, 6 /* value resolvers */ + 5 /* only fields */, //
				c("base : String", "base", r(0, 46, 0, 46)), // comes from BaseItem extended by ItemWithTemplateData
				// c("getReviews() : List<Review>", "getReviews", r(0, 46, 0, 46)), // comes
				// from BaseItem extended by
				// ItemWithTemplateData
				c("name : String", "name", r(0, 46, 0, 46)), //
				c("price : BigInteger", "price", r(0, 46, 0, 46)), //
				c("review2 : Review", "review2", r(0, 46, 0, 46)) //
		// c("getReview2() : Review", "getReview2", r(0, 46, 0, 46))
		);
	}

	private static void testCompletionFor(String template, Integer expectedCount, CompletionItem... expected)
			throws Exception {
		String templateUri = QuteQuickStartProject.NATIVEITEMRESOURCE_ITEMS_TEMPLATE_URI;

		QuteNativeSettings nativeImagesSettings = new QuteNativeSettings();
		nativeImagesSettings.setEnabled(true);

		QuteAssert.testCompletionFor(template, false, false, templateUri, null, QuteQuickStartProject.PROJECT_URI,
				QuteAssert.TEMPLATE_BASE_DIR, expectedCount, nativeImagesSettings, expected);
	}

}
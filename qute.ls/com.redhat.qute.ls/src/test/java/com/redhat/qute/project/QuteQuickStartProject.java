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
package com.redhat.qute.project;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.redhat.qute.commons.InvalidMethodReason;
import com.redhat.qute.commons.JavaTypeInfo;
import com.redhat.qute.commons.JavaTypeKind;
import com.redhat.qute.commons.ProjectInfo;
import com.redhat.qute.commons.ResolvedJavaTypeInfo;
import com.redhat.qute.commons.ValueResolver;
import com.redhat.qute.commons.datamodel.DataModelParameter;
import com.redhat.qute.commons.datamodel.DataModelTemplate;
import com.redhat.qute.ls.api.QuteDataModelProjectProvider;

/**
 * Qute quick start project.
 * 
 * @author Angelo ZERR
 *
 */
public class QuteQuickStartProject extends MockQuteProject {

	public final static String PROJECT_URI = "qute-quickstart";

	public QuteQuickStartProject(ProjectInfo projectInfo, QuteDataModelProjectProvider dataModelProvider) {
		super(projectInfo, dataModelProvider);
	}

	@Override
	protected List<ResolvedJavaTypeInfo> createResolvedTypes() {
		List<ResolvedJavaTypeInfo> cache = new ArrayList<>();

		createResolvedJavaTypeInfo("org.acme", cache).setKind(JavaTypeKind.Package);

		ResolvedJavaTypeInfo string = createResolvedJavaTypeInfo("java.lang.String", cache);
		registerField("UTF16 : byte", string);
		registerMethod("isEmpty() : boolean", string);
		registerMethod("codePointCount(beginIndex : int,endIndex : int) : int", string);
		string.setInvalidMethod("getChars", InvalidMethodReason.VoidReturn); // void getChars(int srcBegin, int srcEnd,
																				// char dst[], int dstBegin)
		registerMethod("getBytes(charsetName : java.lang.String) : byte[]", string);

		createResolvedJavaTypeInfo("java.lang.Boolean", cache);
		createResolvedJavaTypeInfo("java.lang.Integer", cache);
		createResolvedJavaTypeInfo("java.lang.Double", cache);
		createResolvedJavaTypeInfo("java.lang.Long", cache);
		createResolvedJavaTypeInfo("java.lang.Float", cache);
		createResolvedJavaTypeInfo("java.math.BigInteger", cache);

		ResolvedJavaTypeInfo review = createResolvedJavaTypeInfo("org.acme.Review", cache);
		registerField("name : java.lang.String", review);
		registerField("average : java.lang.Integer", review);

		ResolvedJavaTypeInfo baseItem = createResolvedJavaTypeInfo("org.acme.Basetem", cache);
		registerField("base : java.lang.String", baseItem);
		registerField("name : java.lang.String", baseItem);
		registerMethod("getReviews() : java.util.List<org.acme.Review>", baseItem);

		ResolvedJavaTypeInfo item = createResolvedJavaTypeInfo("org.acme.Item", cache, baseItem);
		// Override BaseItem#name
		registerField("name : java.lang.String", item);
		registerField("price : java.math.BigInteger", item);
		registerField("review : org.acme.Review", item);
		registerMethod("isAvailable() : java.lang.Boolean", item);
		registerMethod("getReview2() : org.acme.Review", item);
		// Override BaseItem#getReviews()
		registerMethod("getReviews() : java.util.List<org.acme.Review>", item);
		createResolvedJavaTypeInfo("java.util.List<org.acme.Review>", "java.util.List", "org.acme.Review", cache);
		registerField("derivedItems : java.util.List<org.acme.Item>", item);
		registerField("derivedItemArray : org.acme.Item[]", item);
		item.setInvalidMethod("staticMethod", InvalidMethodReason.Static); // public static BigDecimal
																			// staticMethod(Item item)

		createResolvedJavaTypeInfo("java.util.List<org.acme.Item>", "java.util.List", "org.acme.Item", cache);
		createResolvedJavaTypeInfo("java.lang.Iterable<org.acme.Item>", "java.lang.Iterable", "org.acme.Item", cache);
		createResolvedJavaTypeInfo("org.acme.Item[]", null, "org.acme.Item", cache);

		ResolvedJavaTypeInfo iterable = createResolvedJavaTypeInfo("java.lang.Iterable<T>", "java.lang.Iterable", "T",
				cache);

		ResolvedJavaTypeInfo list = createResolvedJavaTypeInfo("java.util.List<E>", "java.util.List", "E", cache);
		list.setExtendedTypes(Arrays.asList("java.lang.Iterable"));
		registerMethod("size() : int", list);
		registerMethod("get(index : int) : E", list);

		ResolvedJavaTypeInfo map = createResolvedJavaTypeInfo("java.util.Map", cache);

		ResolvedJavaTypeInfo itemResource = createResolvedJavaTypeInfo("org.acme.ItemResource", cache);
		registerMethod("discountedPrice(item : org.acme.Item) : java.math.BigDecimal", itemResource);

		return cache;
	}

	@Override
	protected List<DataModelTemplate<DataModelParameter>> createTemplates() {
		List<DataModelTemplate<DataModelParameter>> templates = new ArrayList<>();
		DataModelTemplate<DataModelParameter> template = new DataModelTemplate<DataModelParameter>();
		template.setTemplateUri("src/main/resources/templates/ItemResource/items");
		template.setSourceType("org.acme.qute.ItemResource$Templates");
		template.setSourceMethod("items");
		templates.add(template);

		DataModelParameter parameter = new DataModelParameter();
		parameter.setKey("items");
		parameter.setSourceType("java.util.List<org.acme.Item>");
		template.addParameter(parameter);

		return templates;
	}

	@Override
	protected List<ValueResolver> createValueResolvers() {
		List<ValueResolver> resolvers = new ArrayList<>();
		resolvers.add(createValueResolver("discountedPrice(item : org.acme.Item) : java.math.BigDecimal",
				"org.acme.ItemResource"));
		resolvers.add(createValueResolver("get(list : java.util.List<T>, index : int) : T",
				"io.quarkus.qute.runtime.extensions.CollectionTemplateExtensions"));
		return resolvers;
	}

	@Override
	protected List<JavaTypeInfo> createTypes() {
		List<JavaTypeInfo> cache = new ArrayList<>();
		createJavaTypeInfo("java.util.List<E>", JavaTypeKind.Interface, cache);
		createJavaTypeInfo("java.util.Map<K,V>", JavaTypeKind.Interface, cache);
		return cache;
	}
}

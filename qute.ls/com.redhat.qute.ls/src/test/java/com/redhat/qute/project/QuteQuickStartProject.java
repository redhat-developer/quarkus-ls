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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.redhat.qute.commons.InvalidMethodReason;
import com.redhat.qute.commons.JavaTypeInfo;
import com.redhat.qute.commons.JavaTypeKind;
import com.redhat.qute.commons.ProjectInfo;
import com.redhat.qute.commons.ResolvedJavaTypeInfo;
import com.redhat.qute.commons.datamodel.DataModelParameter;
import com.redhat.qute.commons.datamodel.DataModelTemplate;
import com.redhat.qute.commons.datamodel.resolvers.NamespaceResolverInfo;
import com.redhat.qute.commons.datamodel.resolvers.ValueResolverInfo;
import com.redhat.qute.ls.api.QuteDataModelProjectProvider;
import com.redhat.qute.ls.api.QuteUserTagProvider;

/**
 * Qute quick start project.
 *
 * @author Angelo ZERR
 *
 */
public class QuteQuickStartProject extends MockQuteProject {

	public final static String PROJECT_URI = "qute-quickstart";

	public QuteQuickStartProject(ProjectInfo projectInfo, QuteDataModelProjectProvider dataModelProvider,
			QuteUserTagProvider tagProvider) {
		super(projectInfo, dataModelProvider, tagProvider);
	}

	@Override
	protected List<ResolvedJavaTypeInfo> createResolvedTypes() {
		List<ResolvedJavaTypeInfo> cache = new ArrayList<>();

		createResolvedJavaTypeInfo("org.acme", cache).setKind(JavaTypeKind.Package);

		createResolvedJavaTypeInfo("java.lang.Object", cache);

		ResolvedJavaTypeInfo string = createResolvedJavaTypeInfo("java.lang.String", cache);
		registerField("UTF16 : byte", string);
		registerMethod("isEmpty() : boolean", string);
		registerMethod("codePointCount(beginIndex : int,endIndex : int) : int", string);
		string.setInvalidMethod("getChars", InvalidMethodReason.VoidReturn); // void getChars(int srcBegin, int srcEnd,
																				// char dst[], int dstBegin)
		registerMethod("getBytes(charsetName : java.lang.String) : byte[]", string);
		registerMethod("getBytes() : byte[]", string);

		createResolvedJavaTypeInfo("java.lang.Boolean", cache);
		createResolvedJavaTypeInfo("java.lang.Integer", cache);
		createResolvedJavaTypeInfo("java.lang.Double", cache);
		createResolvedJavaTypeInfo("java.lang.Long", cache);
		createResolvedJavaTypeInfo("java.lang.Float", cache);
		createResolvedJavaTypeInfo("java.math.BigInteger", cache);

		ResolvedJavaTypeInfo bean = createResolvedJavaTypeInfo("org.acme.Bean", cache);
		registerField("bean : java.lang.String", bean);

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

		// RawString for raw and safe resolver tests
		ResolvedJavaTypeInfo rawString = createResolvedJavaTypeInfo("io.quarkus.qute.RawString", cache);
		registerMethod("getValue() : java.lang.String", rawString);
		registerMethod("toString() : java.lang.String", rawString);

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
	protected List<ValueResolverInfo> createValueResolvers() {
		List<ValueResolverInfo> resolvers = new ArrayList<>();

		// Type value resolvers
		resolvers.add(createValueResolver("inject", "plexux",
				"org.eclipse.aether.internal.transport.wagon.PlexusWagonConfigurator",
				"org.eclipse.aether.internal.transport.wagon.PlexusWagonConfigurator"));
		resolvers.add(createValueResolver("inject", "plexux",
				"org.eclipse.aether.internal.transport.wagon.PlexusWagonProvider",
				"org.eclipse.aether.internal.transport.wagon.PlexusWagonProvider"));

		// Method value resolvers
		// No namespace
		resolvers.add(createValueResolver(null, null, "org.acme.ItemResource",
				"discountedPrice(item : org.acme.Item) : java.math.BigDecimal"));
		resolvers.add(createValueResolver(null, null, "io.quarkus.qute.runtime.extensions.CollectionTemplateExtensions",
				"getByIndex(list : java.util.List<T>, index : int) : T"));
		// 'config' namespace
		resolvers.add(createValueResolver("config", null, "io.quarkus.qute.runtime.extensions.ConfigTemplateExtensions",
				"getConfigProperty(propertyName : java.lang.String) : java.lang.Object"));

		// Field value resolvers
		resolvers.add(createValueResolver("inject", "bean", "org.acme.Bean", "bean : java.lang.String"));

		return resolvers;
	}

	@Override
	protected List<JavaTypeInfo> createTypes() {
		List<JavaTypeInfo> cache = new ArrayList<>();
		createJavaTypeInfo("java.util.List<E>", JavaTypeKind.Interface, cache);
		createJavaTypeInfo("java.util.Map<K,V>", JavaTypeKind.Interface, cache);
		return cache;
	}

	@Override
	protected Map<String, NamespaceResolverInfo> createNamespaceResolverInfos() {
		Map<String, NamespaceResolverInfo> infos = new HashMap<>();
		NamespaceResolverInfo inject = new NamespaceResolverInfo();
		inject.setNamespaces(Arrays.asList("inject", "cdi"));
		inject.setDescription(
				"A CDI bean annotated with `@Named` can be referenced in any template through `cdi` and/or `inject` namespaces.");
		inject.setUrl("https://quarkus.io/guides/qute-reference#injecting-beans-directly-in-templates");
		infos.put("inject", inject);
		return infos;
	}
}
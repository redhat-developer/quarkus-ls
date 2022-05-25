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
import com.redhat.qute.commons.RegisterForReflectionAnnotation;
import com.redhat.qute.commons.ResolvedJavaTypeInfo;
import com.redhat.qute.commons.TemplateDataAnnotation;
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
		registerMethod("charAt(index : int) : char", string);
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

		// Item <- BaseItem <- AbstractItem
		ResolvedJavaTypeInfo abstractItem = createResolvedJavaTypeInfo("org.acme.AbstractItem", cache);
		registerField("abstractName : java.lang.String", abstractItem);
		registerMethod("convert(item : org.acme.AbstractItem) : int", abstractItem);

		ResolvedJavaTypeInfo baseItem = createResolvedJavaTypeInfo("org.acme.BaseItem", cache, abstractItem);
		registerField("base : java.lang.String", baseItem);
		registerField("name : java.lang.String", baseItem);
		registerMethod("getReviews() : java.util.List<org.acme.Review>", baseItem);

		// org.acme.Item
		ResolvedJavaTypeInfo item = createResolvedJavaTypeInfo("org.acme.Item", cache, baseItem);
		registerField("name : java.lang.String", item); // Override BaseItem#name
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

		// @TemplateData
		// public class ItemWithTemplateData
		ResolvedJavaTypeInfo itemWithTemplateData = createResolvedJavaTypeInfo("org.acme.ItemWithTemplateData", cache,
				baseItem);
		registerField("name : java.lang.String", itemWithTemplateData); // Override BaseItem#name
		registerField("price : java.math.BigInteger", itemWithTemplateData);
		registerMethod("getReview2() : org.acme.Review", itemWithTemplateData);
		TemplateDataAnnotation annotation = new TemplateDataAnnotation();
		itemWithTemplateData.setTemplateDataAnnotations(Arrays.asList(annotation));

		// @TemplateData(ignoreSuperclasses = true)
		// public class ItemWithTemplateDataIgnoreSubClasses
		ResolvedJavaTypeInfo itemWithTemplateDataIgnoreSubClasses = createResolvedJavaTypeInfo(
				"org.acme.ItemWithTemplateDataIgnoreSubClasses", cache, baseItem);
		registerField("name : java.lang.String", itemWithTemplateDataIgnoreSubClasses); // Override BaseItem#name
		registerField("price : java.math.BigInteger", itemWithTemplateDataIgnoreSubClasses);
		registerMethod("getReview2() : org.acme.Review", itemWithTemplateDataIgnoreSubClasses);
		registerMethod("getSubClasses() : int", itemWithTemplateDataIgnoreSubClasses);

		annotation = new TemplateDataAnnotation();
		annotation.setIgnoreSuperclasses(true);
		itemWithTemplateDataIgnoreSubClasses.setTemplateDataAnnotations(Arrays.asList(annotation));

		// @RegisterForReflection
		// public class ItemWithRegisterForReflection
		ResolvedJavaTypeInfo itemWithRegisterForReflection = createResolvedJavaTypeInfo(
				"org.acme.ItemWithRegisterForReflection", cache, baseItem);
		registerField("name : java.lang.String", itemWithRegisterForReflection); // Override BaseItem#name
		registerField("price : java.math.BigInteger", itemWithRegisterForReflection);
		registerMethod("getReview2() : org.acme.Review", itemWithRegisterForReflection);
		RegisterForReflectionAnnotation registerForReflectionAnnotation = new RegisterForReflectionAnnotation();
		itemWithRegisterForReflection.setRegisterForReflectionAnnotation(registerForReflectionAnnotation);

		// @RegisterForReflection(fields = false)
		// public class ItemWithRegisterForReflectionNoFields
		ResolvedJavaTypeInfo itemWithRegisterForReflectionNoFields = createResolvedJavaTypeInfo(
				"org.acme.ItemWithRegisterForReflectionNoFields", cache, baseItem);
		registerField("name : java.lang.String", itemWithRegisterForReflectionNoFields); // Override BaseItem#name
		registerField("price : java.math.BigInteger", itemWithRegisterForReflectionNoFields);
		registerMethod("getReview2() : org.acme.Review", itemWithRegisterForReflectionNoFields);
		registerForReflectionAnnotation = new RegisterForReflectionAnnotation();
		registerForReflectionAnnotation.setFields(false);
		itemWithRegisterForReflectionNoFields.setRegisterForReflectionAnnotation(registerForReflectionAnnotation);

		// @RegisterForReflection(methods = false)
		// public class ItemWithRegisterForReflectionNoMethods
		ResolvedJavaTypeInfo itemWithRegisterForReflectionNoMethods = createResolvedJavaTypeInfo(
				"org.acme.ItemWithRegisterForReflectionNoMethods", cache, baseItem);
		registerField("name : java.lang.String", itemWithRegisterForReflectionNoMethods); // Override BaseItem#name
		registerField("price : java.math.BigInteger", itemWithRegisterForReflectionNoMethods);
		registerMethod("getReview2() : org.acme.Review", itemWithRegisterForReflectionNoMethods);
		registerForReflectionAnnotation = new RegisterForReflectionAnnotation();
		registerForReflectionAnnotation.setMethods(false);
		itemWithRegisterForReflectionNoMethods.setRegisterForReflectionAnnotation(registerForReflectionAnnotation);

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
		resolvers.add(createValueResolver("inject", "plexux", null,
				"org.eclipse.aether.internal.transport.wagon.PlexusWagonConfigurator",
				"org.eclipse.aether.internal.transport.wagon.PlexusWagonConfigurator"));
		resolvers.add(createValueResolver("inject", "plexux", null,
				"org.eclipse.aether.internal.transport.wagon.PlexusWagonProvider",
				"org.eclipse.aether.internal.transport.wagon.PlexusWagonProvider"));

		// Method value resolvers
		// No namespace
		resolvers.add(createValueResolver(null, null, null, "org.acme.ItemResource",
				"discountedPrice(item : org.acme.Item) : java.math.BigDecimal"));
		resolvers.add(
				createValueResolver(null, null, null, "io.quarkus.qute.runtime.extensions.CollectionTemplateExtensions",
						"getByIndex(list : java.util.List<T>, index : int) : T"));
		// 'config' namespace
		resolvers.add(
				createValueResolver("config", null, "*", "io.quarkus.qute.runtime.extensions.ConfigTemplateExtensions",
						"getConfigProperty(propertyName : java.lang.String) : java.lang.Object"));
		resolvers.add(
				createValueResolver("config", null, null, "io.quarkus.qute.runtime.extensions.ConfigTemplateExtensions",
						"property(propertyName : java.lang.String) : java.lang.Object"));

		// Field value resolvers
		resolvers.add(createValueResolver("inject", "bean", null, "org.acme.Bean", "bean : java.lang.String"));

		// Static field value resolvers
		resolvers.add(createValueResolver(null, "GLOBAL", null, "org.acme.Bean", "bean : java.lang.String", true));
				
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
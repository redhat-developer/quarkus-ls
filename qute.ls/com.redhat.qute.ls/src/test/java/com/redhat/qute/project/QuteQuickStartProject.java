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
import com.redhat.qute.commons.annotations.RegisterForReflectionAnnotation;
import com.redhat.qute.commons.annotations.TemplateDataAnnotation;
import com.redhat.qute.commons.datamodel.DataModelParameter;
import com.redhat.qute.commons.datamodel.DataModelTemplate;
import com.redhat.qute.commons.datamodel.resolvers.NamespaceResolverInfo;
import com.redhat.qute.commons.datamodel.resolvers.ValueResolverKind;
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

	public static final String ITEMRESOURCE_ITEMS_TEMPLATE_URI = "src/main/resources/templates/ItemResource/items";
	public static final String NATIVEITEMRESOURCE_ITEMS_TEMPLATE_URI = "src/main/resources/templates/NativeItemResource/items";

	public QuteQuickStartProject(ProjectInfo projectInfo, QuteDataModelProjectProvider dataModelProvider,
			QuteUserTagProvider tagProvider) {
		super(projectInfo, dataModelProvider, tagProvider);
	}

	@Override
	protected List<ResolvedJavaTypeInfo> createResolvedTypes() {
		List<ResolvedJavaTypeInfo> cache = new ArrayList<>();

		createResolvedJavaTypeInfo("org.acme", cache, true).setJavaTypeKind(JavaTypeKind.Package);

		createResolvedJavaTypeInfo("java.lang.Object", cache, true);

		ResolvedJavaTypeInfo string = createResolvedJavaTypeInfo("java.lang.String", cache, true);
		registerField("UTF16 : byte", string);
		registerMethod("isEmpty() : boolean", string);
		registerMethod("codePointCount(beginIndex : int,endIndex : int) : int", string);
		string.setInvalidMethod("getChars", InvalidMethodReason.VoidReturn); // void getChars(int srcBegin, int srcEnd,
																				// char dst[], int dstBegin)
		registerMethod("charAt(index : int) : char", string);
		registerMethod("getBytes(charsetName : java.lang.String) : byte[]", string);
		registerMethod("getBytes() : byte[]", string);

		createResolvedJavaTypeInfo("java.lang.Boolean", cache, true);
		createResolvedJavaTypeInfo("java.lang.Integer", cache, true);
		createResolvedJavaTypeInfo("java.lang.Double", cache, true);
		createResolvedJavaTypeInfo("java.lang.Long", cache, true);
		createResolvedJavaTypeInfo("java.lang.Float", cache, true);
		createResolvedJavaTypeInfo("java.math.BigDecimal", cache, true);

		ResolvedJavaTypeInfo bigInteger = createResolvedJavaTypeInfo("java.math.BigInteger", cache, true);
		registerMethod("divide(val : java.math.BigInteger) : java.math.BigInteger", bigInteger);

		ResolvedJavaTypeInfo bean = createResolvedJavaTypeInfo("org.acme.Bean", cache, false);
		registerField("bean : java.lang.String", bean);

		ResolvedJavaTypeInfo review = createResolvedJavaTypeInfo("org.acme.Review", cache, false);
		registerField("name : java.lang.String", review);
		registerField("average : java.lang.Integer", review);
		registerMethod("getReviews() : java.util.List<org.acme.Review>", review);

		// Item <- BaseItem <- AbstractItem
		ResolvedJavaTypeInfo abstractItem = createResolvedJavaTypeInfo("org.acme.AbstractItem", cache, false);
		registerField("abstractName : java.lang.String", abstractItem);
		registerMethod("convert(item : org.acme.AbstractItem) : int", abstractItem);

		ResolvedJavaTypeInfo baseItem = createResolvedJavaTypeInfo("org.acme.BaseItem", cache, false, abstractItem.getSignature());
		registerField("base : java.lang.String", baseItem);
		registerField("name : java.lang.String", baseItem);
		registerMethod("getReviews() : java.util.List<org.acme.Review>", baseItem);

		// org.acme.Item
		ResolvedJavaTypeInfo item = createResolvedJavaTypeInfo("org.acme.Item", cache, false, baseItem.getSignature());
		registerField("name : java.lang.String", item); // Override BaseItem#name
		registerField("price : java.math.BigInteger", item);
		registerField("review : org.acme.Review", item);
		registerMethod("isAvailable() : java.lang.Boolean", item);
		registerMethod("getReview2() : org.acme.Review", item);
		// Override BaseItem#getReviews()
		registerMethod("getReviews() : java.util.List<org.acme.Review>", item);
		createResolvedJavaTypeInfo("java.util.List<org.acme.Review>", "java.util.List", "org.acme.Review", cache, true, null);
		registerField("derivedItems : java.util.List<org.acme.Item>", item);
		registerField("derivedItemArray : org.acme.Item[]", item);
		item.setInvalidMethod("staticMethod", InvalidMethodReason.Static); // public static BigDecimal
																			// staticMethod(Item item)

		createResolvedJavaTypeInfo("java.util.List<org.acme.Item>", "java.util.List", "org.acme.Item", cache, true);
		createResolvedJavaTypeInfo("java.lang.Iterable<org.acme.Item>", "java.lang.Iterable", "org.acme.Item", cache, true);
		createResolvedJavaTypeInfo("org.acme.Item[]", null, "org.acme.Item", cache, true);

		ResolvedJavaTypeInfo classA = createResolvedJavaTypeInfo("org.acme.qute.cyclic.ClassA", cache, false, "org.acme.qute.cyclic.ClassC");
		createResolvedJavaTypeInfo("org.acme.qute.cyclic.ClassB", cache, false, "org.acme.qute.cyclic.ClassA");
		createResolvedJavaTypeInfo("org.acme.qute.cyclic.ClassC", cache, false, "org.acme.qute.cyclic.ClassB");
		registerMethod("convert() : java.lang.String", classA);
		registerField("name : java.lang.String", classA);

		// org.acme.MachineStatus
		ResolvedJavaTypeInfo machineStatus = createResolvedJavaTypeInfo("org.acme.MachineStatus", cache, false);
		machineStatus.setJavaTypeKind(JavaTypeKind.Enum);
		registerField("ON : org.acme.MachineStatus", machineStatus);
		registerField("OFF : org.acme.MachineStatus", machineStatus);
		registerField("BROKEN : org.acme.MachineStatus", machineStatus);
		registerField("in : org.acme.MachineStatus", machineStatus);

		// org.acme.Machine
		ResolvedJavaTypeInfo machine = createResolvedJavaTypeInfo("org.acme.Machine", cache, false);
		registerField("status : org.acme.MachineStatus", machine);
		registerMethod("getMachine() : org.acme.MachineStatus", machine);
		registerMethod("getCount() : java.lang.Integer", machine);

		// @TemplateData
		// public class ItemWithTemplateData
		ResolvedJavaTypeInfo itemWithTemplateData = createResolvedJavaTypeInfo("org.acme.ItemWithTemplateData", cache,
				false, baseItem.getSignature());
		registerField("name : java.lang.String", itemWithTemplateData); // Override BaseItem#name
		registerField("price : java.math.BigInteger", itemWithTemplateData);
		registerMethod("getReview2() : org.acme.Review", itemWithTemplateData);
		registerMethod("getSubClasses() : int", itemWithTemplateData);
		itemWithTemplateData.setTemplateDataAnnotations(Arrays.asList(new TemplateDataAnnotation()));

		// @TemplateData
		// @TemplateData(target = BigInteger.class)
		// public class ItemWithTemplateDataWithTarget
		ResolvedJavaTypeInfo itemWithTemplateDataWithTarget = createResolvedJavaTypeInfo(
				"org.acme.ItemWithTemplateDataWithTarget", cache, false, baseItem.getSignature());
		registerField("name : java.lang.String", itemWithTemplateDataWithTarget); // Override BaseItem#name
		registerField("price : java.math.BigInteger", itemWithTemplateDataWithTarget);
		registerMethod("getReview2() : org.acme.Review", itemWithTemplateDataWithTarget);
		registerMethod("getSubClasses() : int", itemWithTemplateDataWithTarget);
		TemplateDataAnnotation templateDataAnnotationWithTarget = new TemplateDataAnnotation();
		templateDataAnnotationWithTarget.setTarget("java.lang.String");
		itemWithTemplateDataWithTarget.setTemplateDataAnnotations(
				Arrays.asList(new TemplateDataAnnotation(), templateDataAnnotationWithTarget));

		// @TemplateData(properties = true)
		// public class ItemWithTemplateDataProperties
		ResolvedJavaTypeInfo itemWithTemplateDataProperties = createResolvedJavaTypeInfo(
				"org.acme.ItemWithTemplateDataProperties", cache, false, baseItem.getSignature());
		registerField("name : java.lang.String", itemWithTemplateDataProperties); // Override BaseItem#name
		registerField("price : java.math.BigInteger", itemWithTemplateDataProperties);
		registerMethod("getReview2() : org.acme.Review", itemWithTemplateDataProperties);
		registerMethod("getSubClasses() : int", itemWithTemplateDataProperties);
		TemplateDataAnnotation propertiesTemplateDataAnnotation = new TemplateDataAnnotation();
		propertiesTemplateDataAnnotation.setProperties(true);
		itemWithTemplateDataProperties.setTemplateDataAnnotations(Arrays.asList(propertiesTemplateDataAnnotation));

		// @TemplateData(ignoreSuperclasses = true)
		// public class ItemWithTemplateDataIgnoreSubClasses
		ResolvedJavaTypeInfo itemWithTemplateDataIgnoreSubClasses = createResolvedJavaTypeInfo(
				"org.acme.ItemWithTemplateDataIgnoreSubClasses", cache, false, baseItem.getSignature());
		registerField("name : java.lang.String", itemWithTemplateDataIgnoreSubClasses); // Override BaseItem#name
		registerField("price : java.math.BigInteger", itemWithTemplateDataIgnoreSubClasses);
		registerMethod("getReview2() : org.acme.Review", itemWithTemplateDataIgnoreSubClasses);
		registerMethod("getSubClasses() : int", itemWithTemplateDataIgnoreSubClasses);
		TemplateDataAnnotation templateDataAnnotation = new TemplateDataAnnotation();
		templateDataAnnotation.setIgnoreSuperclasses(true);
		itemWithTemplateDataIgnoreSubClasses.setTemplateDataAnnotations(Arrays.asList(templateDataAnnotation));

		// @RegisterForReflection
		// public class ItemWithRegisterForReflection
		ResolvedJavaTypeInfo itemWithRegisterForReflection = createResolvedJavaTypeInfo(
				"org.acme.ItemWithRegisterForReflection", cache, false, baseItem.getSignature());
		registerField("name : java.lang.String", itemWithRegisterForReflection); // Override BaseItem#name
		registerField("price : java.math.BigInteger", itemWithRegisterForReflection);
		registerMethod("getReview2() : org.acme.Review", itemWithRegisterForReflection);
		RegisterForReflectionAnnotation registerForReflectionAnnotation = new RegisterForReflectionAnnotation();
		itemWithRegisterForReflection.setRegisterForReflectionAnnotation(registerForReflectionAnnotation);

		// @RegisterForReflection(fields = false)
		// public class ItemWithRegisterForReflectionNoFields
		ResolvedJavaTypeInfo itemWithRegisterForReflectionNoFields = createResolvedJavaTypeInfo(
				"org.acme.ItemWithRegisterForReflectionNoFields", cache, false, baseItem.getSignature());
		registerField("name : java.lang.String", itemWithRegisterForReflectionNoFields); // Override BaseItem#name
		registerField("price : java.math.BigInteger", itemWithRegisterForReflectionNoFields);
		registerMethod("getReview2() : org.acme.Review", itemWithRegisterForReflectionNoFields);
		registerForReflectionAnnotation = new RegisterForReflectionAnnotation();
		registerForReflectionAnnotation.setFields(false);
		itemWithRegisterForReflectionNoFields.setRegisterForReflectionAnnotation(registerForReflectionAnnotation);

		// @RegisterForReflection(methods = false)
		// public class ItemWithRegisterForReflectionNoMethods
		ResolvedJavaTypeInfo itemWithRegisterForReflectionNoMethods = createResolvedJavaTypeInfo(
				"org.acme.ItemWithRegisterForReflectionNoMethods", cache, false, baseItem.getSignature());
		registerField("name : java.lang.String", itemWithRegisterForReflectionNoMethods); // Override BaseItem#name
		registerField("price : java.math.BigInteger", itemWithRegisterForReflectionNoMethods);
		registerMethod("getReview2() : org.acme.Review", itemWithRegisterForReflectionNoMethods);
		registerForReflectionAnnotation = new RegisterForReflectionAnnotation();
		registerForReflectionAnnotation.setMethods(false);
		itemWithRegisterForReflectionNoMethods.setRegisterForReflectionAnnotation(registerForReflectionAnnotation);

		ResolvedJavaTypeInfo iterable = createResolvedJavaTypeInfo("java.lang.Iterable<T>", "java.lang.Iterable", "T",
				cache, true);

		ResolvedJavaTypeInfo list = createResolvedJavaTypeInfo("java.util.List<E>", "java.util.List", "E", cache, true);
		list.setExtendedTypes(Arrays.asList("java.lang.Iterable"));
		registerMethod("size() : int", list);
		registerMethod("get(index : int) : E", list);

		ResolvedJavaTypeInfo map = createResolvedJavaTypeInfo("java.util.Map", cache, true);

		// RawString for raw and safe resolver tests
		ResolvedJavaTypeInfo rawString = createResolvedJavaTypeInfo("io.quarkus.qute.RawString", cache, true);
		registerMethod("getValue() : java.lang.String", rawString);
		registerMethod("toString() : java.lang.String", rawString);

		return cache;
	}

	@Override
	protected List<DataModelTemplate<DataModelParameter>> createTemplates() {
		List<DataModelTemplate<DataModelParameter>> templates = new ArrayList<>();
		createItemsTemplate(templates);
		createItemsNativeTemplate(templates);
		return templates;
	}

	private static void createItemsTemplate(List<DataModelTemplate<DataModelParameter>> templates) {
		DataModelTemplate<DataModelParameter> template = new DataModelTemplate<DataModelParameter>();
		template.setTemplateUri(ITEMRESOURCE_ITEMS_TEMPLATE_URI);
		template.setSourceType("org.acme.qute.ItemResource$Templates");
		template.setSourceMethod("items");
		templates.add(template);

		// ItemResource$Templates#items(Item item)
		DataModelParameter parameter = new DataModelParameter();
		parameter.setKey("items");
		parameter.setSourceType("java.util.List<org.acme.Item>");
		template.addParameter(parameter);
	}

	private static void createItemsNativeTemplate(List<DataModelTemplate<DataModelParameter>> templates) {
		DataModelTemplate<DataModelParameter> template = new DataModelTemplate<DataModelParameter>();
		template.setTemplateUri(NATIVEITEMRESOURCE_ITEMS_TEMPLATE_URI);
		template.setSourceType("org.acme.qute.NativeItemResource$Templates");
		template.setSourceMethod("items");
		templates.add(template);

		// template.data("item", ...)
		DataModelParameter parameter = new DataModelParameter();
		parameter.setDataMethodInvocation(true);
		parameter.setKey("review");
		parameter.setSourceType("org.acme.Review");
		template.addParameter(parameter);

		parameter = new DataModelParameter();
		parameter.setDataMethodInvocation(true);
		parameter.setKey("itemWithTemplateData");
		parameter.setSourceType("org.acme.ItemWithTemplateData");
		template.addParameter(parameter);

		parameter = new DataModelParameter();
		parameter.setDataMethodInvocation(true);
		parameter.setKey("itemWithTemplateDataWithTarget");
		parameter.setSourceType("org.acme.ItemWithTemplateDataWithTarget");
		template.addParameter(parameter);

		parameter = new DataModelParameter();
		parameter.setDataMethodInvocation(true);
		parameter.setKey("itemWithTemplateDataIgnoreSubClasses");
		parameter.setSourceType("org.acme.ItemWithTemplateDataIgnoreSubClasses");
		template.addParameter(parameter);

		parameter = new DataModelParameter();
		parameter.setDataMethodInvocation(true);
		parameter.setKey("itemWithRegisterForReflectionNoFields");
		parameter.setSourceType("org.acme.ItemWithRegisterForReflectionNoFields");
		template.addParameter(parameter);

		parameter = new DataModelParameter();
		parameter.setDataMethodInvocation(true);
		parameter.setKey("itemWithRegisterForReflectionNoMethods");
		parameter.setSourceType("org.acme.ItemWithRegisterForReflectionNoMethods");
		template.addParameter(parameter);

		parameter = new DataModelParameter();
		parameter.setDataMethodInvocation(true);
		parameter.setKey("itemWithRegisterForReflection");
		parameter.setSourceType("org.acme.ItemWithRegisterForReflection");
		template.addParameter(parameter);

		parameter = new DataModelParameter();
		parameter.setDataMethodInvocation(true);
		parameter.setKey("itemWithTemplateDataProperties");
		parameter.setSourceType("org.acme.ItemWithTemplateDataProperties");
		template.addParameter(parameter);
	}

	@Override
	protected List<ValueResolverInfo> createValueResolvers() {
		List<ValueResolverInfo> resolvers = new ArrayList<>();

		// Type value resolvers
		resolvers.add(createValueResolver("inject", "plexux", null,
				"org.eclipse.aether.internal.transport.wagon.PlexusWagonConfigurator",
				"org.eclipse.aether.internal.transport.wagon.PlexusWagonConfigurator", ValueResolverKind.InjectedBean, false, true));
		resolvers.add(createValueResolver("inject", "plexux", null,
				"org.eclipse.aether.internal.transport.wagon.PlexusWagonProvider",
				"org.eclipse.aether.internal.transport.wagon.PlexusWagonProvider", ValueResolverKind.InjectedBean, false, true));

		// Method value resolvers
		// No namespace
		resolvers.add(createValueResolver(null, null, null, "org.acme.ItemResource",
				"discountedPrice(item : org.acme.Item) : java.math.BigDecimal", ValueResolverKind.TemplateExtensionOnMethod, false, false));
		resolvers.add(
				createValueResolver(null, null, null, "io.quarkus.qute.runtime.extensions.CollectionTemplateExtensions",
						"getByIndex(list : java.util.List<T>, index : int) : T", ValueResolverKind.TemplateExtensionOnClass, false, true));
		resolvers.add(createValueResolver(null, null, null, "org.acme.ItemResource",
				"pretty(item : org.acme.Item, elements : java.lang.String...) : java.lang.String", ValueResolverKind.TemplateExtensionOnMethod, false, false));

		// @TemplateExtension
		// org.acme.TemplateExtensions
		resolvers.add(createValueResolver(null, null, null, "org.acme.TemplateExtensions", "", ValueResolverKind.TemplateExtensionOnClass, false, false));
		// @TemplateExtension
		// org.acme.foo.TemplateExtensions
		resolvers.add(createValueResolver(null, null, null, "org.acme.foo.TemplateExtensions", "", ValueResolverKind.TemplateExtensionOnClass, false, false));

		// 'config' namespace
		resolvers.add(
				createValueResolver("config", null, "*", "io.quarkus.qute.runtime.extensions.ConfigTemplateExtensions",
						"getConfigProperty(propertyName : java.lang.String) : java.lang.Object", ValueResolverKind.TemplateExtensionOnMethod, false, true));
		resolvers.add(
				createValueResolver("config", null, null, "io.quarkus.qute.runtime.extensions.ConfigTemplateExtensions",
						"property(propertyName : java.lang.String) : java.lang.Object", ValueResolverKind.TemplateExtensionOnMethod, false, true));

		// Field value resolvers
		resolvers.add(createValueResolver("inject", "bean", null, "org.acme.Bean", "bean : java.lang.String", ValueResolverKind.InjectedBean));

		// Static field value resolvers
		resolvers.add(createValueResolver(null, "GLOBAL", null, "org.acme.Bean", "bean : java.lang.String", ValueResolverKind.TemplateGlobal, true));

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
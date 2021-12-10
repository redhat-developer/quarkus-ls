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
package com.redhat.qute.jdt.template;

import static com.redhat.qute.jdt.internal.QuteProjectTest.getJDTUtils;
import static com.redhat.qute.jdt.internal.QuteProjectTest.loadMavenProject;

import java.util.List;
import java.util.Optional;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.Assert;
import org.junit.Test;

import com.redhat.qute.commons.ValueResolver;
import com.redhat.qute.commons.datamodel.DataModelParameter;
import com.redhat.qute.commons.datamodel.DataModelProject;
import com.redhat.qute.commons.datamodel.DataModelTemplate;
import com.redhat.qute.commons.datamodel.QuteDataModelProjectParams;
import com.redhat.qute.jdt.QuteSupportForTemplate;
import com.redhat.qute.jdt.internal.QuteProjectTest.QuteMavenProjectName;

/**
 * Tests for
 * {@link QuteSupportForTemplate#getDataModelProject(QuteDataModelProjectParams, com.redhat.qute.jdt.utils.IJDTUtils, org.eclipse.core.runtime.IProgressMonitor)}
 *
 * @author Angelo ZERR
 *
 */
public class TemplateGetDataModelProjectTest {

	@Test
	public void quteQuickStart() throws Exception {

		loadMavenProject(QuteMavenProjectName.qute_quickstart);

		QuteDataModelProjectParams params = new QuteDataModelProjectParams(QuteMavenProjectName.qute_quickstart);
		DataModelProject<DataModelTemplate<DataModelParameter>> project = QuteSupportForTemplate.getInstance()
				.getDataModelProject(params, getJDTUtils(), new NullProgressMonitor());
		Assert.assertNotNull(project);

		// Test templates
		testTemplates(project);
		// Test value resolvers
		testValueResolvers(project);
	}

	private static void testTemplates(DataModelProject<DataModelTemplate<DataModelParameter>> project) {
		List<DataModelTemplate<DataModelParameter>> templates = project.getTemplates();
		Assert.assertNotNull(templates);
		Assert.assertFalse(templates.isEmpty());

		templateField(project);
		checkedTemplateInnerClass(project);
		checkedTemplate(project);
	}

	private static void templateField(DataModelProject<DataModelTemplate<DataModelParameter>> project) {
		// Template hello;

		DataModelTemplate<DataModelParameter> helloTemplate = project
				.findDataModelTemplate("src/main/resources/templates/hello");
		Assert.assertNotNull(helloTemplate);
		Assert.assertEquals("src/main/resources/templates/hello", helloTemplate.getTemplateUri());
		Assert.assertEquals("org.acme.qute.HelloResource", helloTemplate.getSourceType());
		Assert.assertEquals("hello", helloTemplate.getSourceField());

		List<DataModelParameter> parameters = helloTemplate.getParameters();
		Assert.assertNotNull(parameters);

		// hello.data("age", 12);
		// hello.data("height", 1.50, "weight", 50.5);
		// return hello.data("name", name);

		Assert.assertEquals(4, parameters.size());
		assertParameter("age", "int", true, parameters, 0);
		assertParameter("height", "double", true, parameters, 1);
		assertParameter("weight", "long", true, parameters, 2);
		assertParameter("name", "java.lang.String", true, parameters, 3);

		// Template goodbye;

		DataModelTemplate<DataModelParameter> goodbyeTemplate = project
				.findDataModelTemplate("src/main/resources/templates/goodbye");
		Assert.assertNotNull(goodbyeTemplate);
		Assert.assertEquals("src/main/resources/templates/goodbye", goodbyeTemplate.getTemplateUri());
		Assert.assertEquals("org.acme.qute.HelloResource", goodbyeTemplate.getSourceType());
		Assert.assertEquals("goodbye", goodbyeTemplate.getSourceField());

		List<DataModelParameter> parameters2 = goodbyeTemplate.getParameters();
		Assert.assertNotNull(parameters2);

		// goodbye.data("age2", 12);
		// return goodbye.data("name2", name);

		Assert.assertEquals(2, parameters2.size());
		assertParameter("age2", "int", true, parameters2, 0);
		assertParameter("name2", "java.lang.String", true, parameters2, 1);
	}

	private static void checkedTemplateInnerClass(DataModelProject<DataModelTemplate<DataModelParameter>> project) {
		DataModelTemplate<DataModelParameter> itemResourceTemplate = project
				.findDataModelTemplate("src/main/resources/templates/ItemResource/items");
		Assert.assertNotNull(itemResourceTemplate);
		Assert.assertEquals("src/main/resources/templates/ItemResource/items", itemResourceTemplate.getTemplateUri());
		Assert.assertEquals("org.acme.qute.ItemResource$Templates", itemResourceTemplate.getSourceType());
		Assert.assertEquals("items", itemResourceTemplate.getSourceMethod());

		List<DataModelParameter> parameters = itemResourceTemplate.getParameters();
		Assert.assertNotNull(parameters);

		// static native TemplateInstance items(List<Item> items);

		Assert.assertEquals(1, parameters.size());
		assertParameter("items", "java.util.List<org.acme.qute.Item>", false, parameters, 0);
	}

	private static void checkedTemplate(DataModelProject<DataModelTemplate<DataModelParameter>> project) {
		// hello2
		DataModelTemplate<DataModelParameter> hello2Template = project
				.findDataModelTemplate("src/main/resources/templates/hello2");
		Assert.assertNotNull(hello2Template);
		Assert.assertEquals("src/main/resources/templates/hello2", hello2Template.getTemplateUri());
		Assert.assertEquals("org.acme.qute.Templates", hello2Template.getSourceType());
		Assert.assertEquals("hello2", hello2Template.getSourceMethod());

		List<DataModelParameter> hello2Parameters = hello2Template.getParameters();
		Assert.assertNotNull(hello2Parameters);

		// public static native TemplateInstance hello2(String name);

		Assert.assertEquals(1, hello2Parameters.size());
		assertParameter("name", "java.lang.String", false, hello2Parameters, 0);

		// hello3
		DataModelTemplate<DataModelParameter> hello3Template = project
				.findDataModelTemplate("src/main/resources/templates/hello3");
		Assert.assertNotNull(hello3Template);
		Assert.assertEquals("src/main/resources/templates/hello3", hello3Template.getTemplateUri());
		Assert.assertEquals("org.acme.qute.Templates", hello3Template.getSourceType());
		Assert.assertEquals("hello3", hello3Template.getSourceMethod());

		List<DataModelParameter> hello3Parameters = hello3Template.getParameters();
		Assert.assertNotNull(hello3Parameters);

		// public static native TemplateInstance hello3(String name);

		Assert.assertEquals(1, hello3Parameters.size());
		assertParameter("name", "java.lang.String", false, hello3Parameters, 0);
	}

	private static void testValueResolvers(DataModelProject<DataModelTemplate<DataModelParameter>> project) {
		List<ValueResolver> resolvers = project.getValueResolvers();
		Assert.assertNotNull(resolvers);
		Assert.assertFalse(resolvers.isEmpty());

		// Resolver from Java sources
		assertValueResolver(null, "discountedPrice(item : org.acme.qute.Item) : java.math.BigDecimal",
				"org.acme.qute.ItemResource", resolvers);
		// Resolver from Java binaries
		// from io.quarkus.qute.runtime.extensions.CollectionTemplateExtensions
		assertValueResolver(null, "get(list : java.util.List<T>, index : int) : T",
				"io.quarkus.qute.runtime.extensions.CollectionTemplateExtensions", resolvers);
		assertValueResolver(null, "getByIndex(list : java.util.List<T>, index : java.lang.String) : T",
				"io.quarkus.qute.runtime.extensions.CollectionTemplateExtensions", resolvers);

		// from io.quarkus.qute.runtime.extensions.ConfigTemplateExtensions
		assertValueResolver("config", "config:getConfigProperty(propertyName : java.lang.String) : java.lang.Object",
				"io.quarkus.qute.runtime.extensions.ConfigTemplateExtensions", resolvers);

		// from io.quarkus.qute.runtime.extensions.MapTemplateExtensions
		assertValueResolver(null, "map(arg0 : java.util.Map, arg1 : java.lang.String) : java.lang.Object",
				"io.quarkus.qute.runtime.extensions.MapTemplateExtensions", resolvers);
		assertValueResolver(null, "get(map : java.util.Map<?,V>, key : java.lang.Object) : V",
				"io.quarkus.qute.runtime.extensions.MapTemplateExtensions", resolvers);

		// from io.quarkus.qute.runtime.extensions.StringTemplateExtensions
		assertValueResolver(null, "mod(number : java.lang.Integer, mod : java.lang.Integer) : java.lang.Integer",
				"io.quarkus.qute.runtime.extensions.NumberTemplateExtensions", resolvers);

		// from io.quarkus.qute.runtime.extensions.StringTemplateExtensions
		assertValueResolver(null,
				"fmtInstance(format : java.lang.String, ignoredPropertyName : java.lang.String, args : java.lang.Object[]) : java.lang.String",
				"io.quarkus.qute.runtime.extensions.StringTemplateExtensions", resolvers);
		assertValueResolver("str",
				"str:fmt(ignoredPropertyName : java.lang.String, format : java.lang.String, args : java.lang.Object[]) : java.lang.String",
				"io.quarkus.qute.runtime.extensions.StringTemplateExtensions", resolvers);

		// from io.quarkus.qute.runtime.extensions.TimeTemplateExtensions
		assertValueResolver(null,
				"format(temporal : java.time.temporal.TemporalAccessor, pattern : java.lang.String) : java.lang.String",
				"io.quarkus.qute.runtime.extensions.TimeTemplateExtensions", resolvers);
		assertValueResolver("time",
				"time:format(dateTimeObject : java.lang.Object, pattern : java.lang.String) : java.lang.String",
				"io.quarkus.qute.runtime.extensions.TimeTemplateExtensions", resolvers);

	}

	private static void assertValueResolver(String namespace, String signature, String sourceType,
			List<ValueResolver> resolvers) {
		Optional<ValueResolver> result = resolvers.stream().filter(r -> signature.equals(r.getSignature())).findFirst();
		Assert.assertFalse("Find '" + signature + "' value resolver.", result.isEmpty());
		ValueResolver resolver = result.get();
		Assert.assertEquals(namespace, resolver.getNamespace());
		Assert.assertEquals(signature, resolver.getSignature());
		Assert.assertEquals(sourceType, resolver.getSourceType());
	}

	private static void assertParameter(String key, String sourceType, boolean dataMethodInvocation,
			List<DataModelParameter> parameters, int index) {
		DataModelParameter parameter = parameters.get(index);
		Assert.assertEquals(key, parameter.getKey());
		Assert.assertEquals(sourceType, parameter.getSourceType());
		Assert.assertEquals(dataMethodInvocation, parameter.isDataMethodInvocation());
	}

}

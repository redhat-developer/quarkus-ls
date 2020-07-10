/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lsp4mp.commons;

import static org.eclipse.lsp4mp.services.MicroProfileAssert.getDefaultMicroProfileProjectInfo;
import static org.eclipse.lsp4mp.utils.MicroProfilePropertiesUtils.formatPropertyForCompletion;
import static org.eclipse.lsp4mp.utils.MicroProfilePropertiesUtils.formatPropertyForMarkdown;

import org.eclipse.lsp4mp.commons.MicroProfileProjectInfo;
import org.eclipse.lsp4mp.model.PropertiesModel;
import org.eclipse.lsp4mp.model.Property;
import org.eclipse.lsp4mp.model.PropertyKey;
import org.eclipse.lsp4mp.utils.MicroProfilePropertiesUtils;
import org.eclipse.lsp4mp.utils.MicroProfilePropertiesUtils.FormattedPropertyResult;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Test for {@link MicroProfileProjectInfo}.
 * 
 * @author Angelo ZERR
 *
 */
public class MicroProfileProjectInfoTest {

	@Test
	public void getSimpleProperty() {
		MicroProfileProjectInfo info = getDefaultMicroProfileProjectInfo();
		PropertyInfo property = getProperty("quarkus.thread-pool.core-threads", info);
		Assert.assertNotNull(property);
		Assert.assertNull(property.getProfile());
		Assert.assertNotNull(property.getProperty());
		Assert.assertEquals("quarkus.thread-pool.core-threads", property.getProperty().getName());
	}

	@Test
	public void getPropertyWithOnlyProfile() {
		MicroProfileProjectInfo info = getDefaultMicroProfileProjectInfo();
		PropertyInfo property = getProperty("%dev", info);

		Assert.assertNotNull(property);
		Assert.assertEquals("dev", property.getProfile());
		Assert.assertNull(property.getProperty());

		property = getProperty("%dev.", info);

		Assert.assertNotNull(property);
		Assert.assertEquals("dev", property.getProfile());
		Assert.assertNull(property.getProperty());
	}

	@Test
	public void getPropertyWithProfile() {
		MicroProfileProjectInfo info = getDefaultMicroProfileProjectInfo();
		PropertyInfo property = getProperty("%dev.quarkus.thread-pool.core-threads", info);

		Assert.assertNotNull(property);
		Assert.assertEquals("dev", property.getProfile());
		Assert.assertNotNull(property.getProperty());
		Assert.assertEquals("quarkus.thread-pool.core-threads", property.getProperty().getName());
	}

	@Test
	public void getPropertyMapWithOneKey() {
		MicroProfileProjectInfo info = getDefaultMicroProfileProjectInfo();
		PropertyInfo property = getProperty("quarkus.log.category.\"com.lordofthejars\".level", info);

		Assert.assertNotNull(property);
		Assert.assertNotNull(property.getProperty());
		Assert.assertEquals("quarkus.log.category.{*}.level", property.getProperty().getName());

		property = getProperty("quarkus.log.category.com.level", info);

		Assert.assertNotNull(property);
		Assert.assertNotNull(property.getProperty());
		Assert.assertEquals("quarkus.log.category.{*}.level", property.getProperty().getName());

		property = getProperty("quarkus.log.category.com\\\\.lordofthejars.level", info);

		Assert.assertNotNull(property);
		Assert.assertNotNull(property.getProperty());
		Assert.assertEquals("quarkus.log.category.{*}.level", property.getProperty().getName());
	}

	@Test
	@Ignore("Ignore this test since quarkus.keycloak.policy-enforcer.claim-information-point.{*}.{*}.{*} no longer exists")
	public void getPropertyMapWithThreeKeys() {
		MicroProfileProjectInfo info = getDefaultMicroProfileProjectInfo();
		PropertyInfo property = getProperty("quarkus.keycloak.policy-enforcer.claim-information-point.foo.bar.zoo",
				info);

		Assert.assertNotNull(property);
		Assert.assertNotNull(property.getProperty());
		Assert.assertEquals("quarkus.keycloak.policy-enforcer.claim-information-point.{*}.{*}.{*}",
				property.getProperty().getName());

		property = getProperty("quarkus.keycloak.policy-enforcer.claim-information-point.foo.bar", info);
		Assert.assertNotNull(property);

		property = getProperty("quarkus.keycloak.policy-enforcer.paths.a.claim-information-point.b.c", info);

		Assert.assertNotNull(property);
		Assert.assertNotNull(property.getProperty());
		Assert.assertEquals("quarkus.keycloak.policy-enforcer.paths.{*}.claim-information-point.{*}.{*}",
				property.getProperty().getName());
	}

	@Test
	public void simpleFormatPropertyForMarkdown() {
		String actual = formatPropertyForMarkdown("quarkus.thread-pool.core-threads");
		Assert.assertEquals("quarkus.thread-pool.core-threads", actual);
	}

	@Test
	public void mapFormatPropertyForMarkdown() {
		String actual = formatPropertyForMarkdown("quarkus.log.category.{*}.level");
		Assert.assertEquals("quarkus.log.category.\\{\\*\\}.level", actual);

		actual = formatPropertyForMarkdown("quarkus.keycloak.credentials.jwt.{*}");
		Assert.assertEquals("quarkus.keycloak.credentials.jwt.\\{\\*\\}", actual);

		actual = formatPropertyForMarkdown("quarkus.keycloak.policy-enforcer.claim-information-point.{*}.{*}.{*}");
		Assert.assertEquals("quarkus.keycloak.policy-enforcer.claim-information-point.\\{\\*\\}.\\{\\*\\}.\\{\\*\\}",
				actual);

		actual = formatPropertyForMarkdown(
				"quarkus.keycloak.policy-enforcer.paths.{*}.claim-information-point.{*}.{*}");
		Assert.assertEquals(
				"quarkus.keycloak.policy-enforcer.paths.\\{\\*\\}.claim-information-point.\\{\\*\\}.\\{\\*\\}", actual);
	}

	@Test
	public void indexArrayFormatPropertyForMarkdown() {
		String actual = formatPropertyForMarkdown("kubernetes.labels[*].key");
		Assert.assertEquals("kubernetes.labels[\\*\\].key", actual);
	}

	@Test
	public void simpleFormatPropertyForCompletion() {
		FormattedPropertyResult actual = formatPropertyForCompletion("quarkus.thread-pool.core-threads");
		Assert.assertEquals("quarkus.thread-pool.core-threads", actual.getPropertyName());
		Assert.assertEquals(0, actual.getParameterCount());
	}

	@Test
	public void mapFormatPropertyForCompletion() {
		FormattedPropertyResult actual = formatPropertyForCompletion("quarkus.log.category.{*}.level");
		Assert.assertEquals("quarkus.log.category.${1:key}.level", actual.getPropertyName());
		Assert.assertEquals(1, actual.getParameterCount());

		actual = formatPropertyForCompletion("quarkus.keycloak.credentials.jwt.{*}");
		Assert.assertEquals("quarkus.keycloak.credentials.jwt.${1:key}", actual.getPropertyName());
		Assert.assertEquals(1, actual.getParameterCount());

		actual = formatPropertyForCompletion("quarkus.keycloak.policy-enforcer.claim-information-point.{*}.{*}.{*}");
		Assert.assertEquals("quarkus.keycloak.policy-enforcer.claim-information-point.${1:key}.${2:key}.${3:key}",
				actual.getPropertyName());
		Assert.assertEquals(3, actual.getParameterCount());

		actual = formatPropertyForCompletion(
				"quarkus.keycloak.policy-enforcer.paths.{*}.claim-information-point.{*}.{*}");
		Assert.assertEquals("quarkus.keycloak.policy-enforcer.paths.${1:key}.claim-information-point.${2:key}.${3:key}",
				actual.getPropertyName());
		Assert.assertEquals(3, actual.getParameterCount());
	}

	@Test
	public void indexArrayFormatPropertyForCompletion() {
		FormattedPropertyResult actual = formatPropertyForCompletion("kubernetes.labels[*].key");
		Assert.assertEquals("kubernetes.labels[${1:0}].key", actual.getPropertyName());
		Assert.assertEquals(1, actual.getParameterCount());
	}

	private static PropertyInfo getProperty(String text, MicroProfileProjectInfo info) {
		PropertiesModel model = PropertiesModel.parse(text, "application.properties");
		PropertyKey key = (PropertyKey) ((Property) model.getChildren().get(0)).getKey();
		return new PropertyInfo(MicroProfilePropertiesUtils.getProperty(key.getPropertyName(), info), key.getProfile());
	}

}

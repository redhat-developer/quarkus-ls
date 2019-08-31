/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.quarkus.commons;

import static com.redhat.quarkus.services.QuarkusAssert.getDefaultQuarkusProjectInfo;
import static com.redhat.quarkus.utils.QuarkusPropertiesUtils.formatPropertyForCompletion;
import static com.redhat.quarkus.utils.QuarkusPropertiesUtils.formatPropertyForMarkdown;

import org.junit.Assert;
import org.junit.Test;

import com.redhat.quarkus.model.PropertiesModel;
import com.redhat.quarkus.model.Property;
import com.redhat.quarkus.model.PropertyKey;
import com.redhat.quarkus.utils.QuarkusPropertiesUtils;

/**
 * Test for {@link QuarkusProjectInfo}.
 * 
 * @author Angelo ZERR
 *
 */
public class QuarkusProjectInfoTest {

	@Test
	public void getSimpleProperty() {
		QuarkusProjectInfo info = getDefaultQuarkusProjectInfo();
		PropertyInfo property = getProperty("quarkus.thread-pool.core-threads", info);
		Assert.assertNotNull(property);
		Assert.assertNull(property.getProfile());
		Assert.assertNotNull(property.getProperty());
		Assert.assertEquals("quarkus.thread-pool.core-threads", property.getProperty().getPropertyName());
	}

	@Test
	public void getPropertyWithOnlyProfile() {
		QuarkusProjectInfo info = getDefaultQuarkusProjectInfo();
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
		QuarkusProjectInfo info = getDefaultQuarkusProjectInfo();
		PropertyInfo property = getProperty("%dev.quarkus.thread-pool.core-threads", info);

		Assert.assertNotNull(property);
		Assert.assertEquals("dev", property.getProfile());
		Assert.assertNotNull(property.getProperty());
		Assert.assertEquals("quarkus.thread-pool.core-threads", property.getProperty().getPropertyName());
	}

	@Test
	public void getPropertyMapWithOneKey() {
		QuarkusProjectInfo info = getDefaultQuarkusProjectInfo();
		PropertyInfo property = getProperty("quarkus.log.category.\"com.lordofthejars\".level", info);

		Assert.assertNotNull(property);
		Assert.assertNotNull(property.getProperty());
		Assert.assertEquals("quarkus.log.category.{*}.level", property.getProperty().getPropertyName());

		property = getProperty("quarkus.log.category.com.level", info);

		Assert.assertNotNull(property);
		Assert.assertNotNull(property.getProperty());
		Assert.assertEquals("quarkus.log.category.{*}.level", property.getProperty().getPropertyName());

		property = getProperty("quarkus.log.category.com\\\\.lordofthejars.level", info);

		Assert.assertNotNull(property);
		Assert.assertNotNull(property.getProperty());
		Assert.assertEquals("quarkus.log.category.{*}.level", property.getProperty().getPropertyName());
	}

	@Test
	public void getPropertyMapWithThreeKeys() {
		QuarkusProjectInfo info = getDefaultQuarkusProjectInfo();
		PropertyInfo property = getProperty("quarkus.keycloak.policy-enforcer.claim-information-point.foo.bar.zoo",
				info);

		Assert.assertNotNull(property);
		Assert.assertNotNull(property.getProperty());
		Assert.assertEquals("quarkus.keycloak.policy-enforcer.claim-information-point.{*}.{*}.{*}",
				property.getProperty().getPropertyName());

		property = getProperty("quarkus.keycloak.policy-enforcer.claim-information-point.foo.bar", info);
		Assert.assertNotNull(property);

		property = getProperty("quarkus.keycloak.policy-enforcer.paths.a.claim-information-point.b.c", info);

		Assert.assertNotNull(property);
		Assert.assertNotNull(property.getProperty());
		Assert.assertEquals("quarkus.keycloak.policy-enforcer.paths.{*}.claim-information-point.{*}.{*}",
				property.getProperty().getPropertyName());
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
	public void simpleFormatPropertyForCompletion() {
		String actual = formatPropertyForCompletion("quarkus.thread-pool.core-threads");
		Assert.assertEquals("quarkus.thread-pool.core-threads", actual);
	}

	@Test
	public void mapFormatPropertyForCompletion() {
		String actual = formatPropertyForCompletion("quarkus.log.category.{*}.level");
		Assert.assertEquals("quarkus.log.category.${1:key}.level", actual);

		actual = formatPropertyForCompletion("quarkus.keycloak.credentials.jwt.{*}");
		Assert.assertEquals("quarkus.keycloak.credentials.jwt.${1:key}", actual);

		actual = formatPropertyForCompletion("quarkus.keycloak.policy-enforcer.claim-information-point.{*}.{*}.{*}");
		Assert.assertEquals("quarkus.keycloak.policy-enforcer.claim-information-point.${1:key}.${2:key}.${3:key}",
				actual);

		actual = formatPropertyForCompletion(
				"quarkus.keycloak.policy-enforcer.paths.{*}.claim-information-point.{*}.{*}");
		Assert.assertEquals("quarkus.keycloak.policy-enforcer.paths.${1:key}.claim-information-point.${2:key}.${3:key}",
				actual);
	}

	private static PropertyInfo getProperty(String text, QuarkusProjectInfo info) {
		PropertiesModel model = PropertiesModel.parse(text, "application.properties");
		PropertyKey key = (PropertyKey) ((Property) model.getChildren().get(0)).getKey();
		return new PropertyInfo(QuarkusPropertiesUtils.getProperty(key.getPropertyName(), info), key.getProfile());
	}

}

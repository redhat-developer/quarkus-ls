/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.quarkus.jdt.core;

import static com.redhat.quarkus.commons.ExtendedConfigDescriptionBuildItem.CONFIG_PHASE_BUILD_TIME;
import static com.redhat.quarkus.commons.ExtendedConfigDescriptionBuildItem.CONFIG_PHASE_RUN_TIME;
import static com.redhat.quarkus.jdt.internal.core.QuarkusAssert.assertProperties;
import static com.redhat.quarkus.jdt.internal.core.QuarkusAssert.p;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

import com.redhat.quarkus.commons.QuarkusProjectInfo;
import com.redhat.quarkus.jdt.internal.core.utils.DependencyUtil;

/**
 * Test to download and use in classpath deployment JARs declared in //
 * META-INF/quarkus-extension.properties of quarkus-hibernate-orm.jar as
 * property:
 * <code>deployment-artifact=io.quarkus\:quarkus-hibernate-orm-deployment\:0.21.1</code>
 * 
 * @author Angelo ZERR
 *
 */
public class JDTQuarkusManagerConfigRootTest extends BaseJDTQuarkusManagerTest {

	@Test
	public void hibernateOrmResteasy() throws Exception {
		QuarkusProjectInfo info = getQuarkusProjectInfoFromMavenProject("hibernate-orm-resteasy");

		File f = DependencyUtil.getArtifact("io.quarkus", "quarkus-hibernate-orm-deployment", "0.19.1", null);
		Assert.assertNotNull("Test existing of quarkus-hibernate-orm-deployment*.jar", f);

		assertProperties(info,

				// io.quarkus.hibernate.orm.deployment.HibernateOrmConfig
				p("quarkus-hibernate-orm", "quarkus.hibernate-orm.dialect", "java.util.Optional<java.lang.String>",
						"The hibernate ORM dialect class name", f.getAbsolutePath(),
						"io.quarkus.hibernate.orm.deployment.HibernateOrmConfig#dialect", CONFIG_PHASE_BUILD_TIME,
						null));
	}

	@Test
	public void allQuarkusExtensions() throws Exception {
		QuarkusProjectInfo info = getQuarkusProjectInfoFromMavenProject("all-quarkus-extensions");

		File keycloakJARFile = DependencyUtil.getArtifact("io.quarkus", "quarkus-keycloak-deployment", "0.21.1", null);
		Assert.assertNotNull("Test existing of quarkus-keycloak-deployment*.jar", keycloakJARFile);
		File hibernateJARFile = DependencyUtil.getArtifact("io.quarkus", "quarkus-hibernate-orm-deployment", "0.21.1",
				null);
		Assert.assertNotNull("Test existing of quarkus-hibernate-orm-deployment*.jar", hibernateJARFile);
		File undertowJARFile = DependencyUtil.getArtifact("io.quarkus", "quarkus-undertow", "0.21.1", null);
		Assert.assertNotNull("Test existing of quarkus-undertow*.jar", undertowJARFile);
		File mongoJARFile = DependencyUtil.getArtifact("io.quarkus", "quarkus-mongodb-client", "0.21.1", null);
		Assert.assertNotNull("Test existing of quarkus-mongodb-client*.jar", mongoJARFile);

		assertProperties(info,

				// Test with Map<String, String>
				// https://github.com/quarkusio/quarkus/blob/0.21/extensions/keycloak/deployment/src/main/java/io/quarkus/keycloak/KeycloakConfig.java#L308
				p("quarkus-keycloak", "quarkus.keycloak.credentials.jwt.{*}", "java.lang.String",
						"The settings for client authentication with signed JWT", keycloakJARFile.getAbsolutePath(),
						"io.quarkus.keycloak.KeycloakConfig$KeycloakConfigCredentials#jwt", CONFIG_PHASE_BUILD_TIME,
						null),

				// Test with Map<String, Map<String, Map<String, String>>>
				// https://github.com/quarkusio/quarkus/blob/0.21/extensions/keycloak/deployment/src/main/java/io/quarkus/keycloak/KeycloakConfig.java#L469
				p("quarkus-keycloak", "quarkus.keycloak.policy-enforcer.paths.{*}.claim-information-point.{*}.{*}.{*}",
						"java.lang.String", "", keycloakJARFile.getAbsolutePath(),
						"io.quarkus.keycloak.KeycloakConfig$KeycloakConfigPolicyEnforcer$ClaimInformationPointConfig#complexConfig",
						CONFIG_PHASE_BUILD_TIME, null),

				// io.quarkus.hibernate.orm.deployment.HibernateOrmConfig
				p("quarkus-hibernate-orm", "quarkus.hibernate-orm.dialect", "java.util.Optional<java.lang.String>",
						"The hibernate ORM dialect class name", hibernateJARFile.getAbsolutePath(),
						"io.quarkus.hibernate.orm.deployment.HibernateOrmConfig#dialect", CONFIG_PHASE_BUILD_TIME,
						null),

				// test with extension name
				p("quarkus-undertow", "quarkus.http.ssl.certificate.file", "java.util.Optional<java.nio.file.Path>",
						"The file path to a server certificate or certificate chain in PEM format.",
						undertowJARFile.getAbsolutePath(),
						"io.quarkus.runtime.configuration.ssl.CertificateConfig#file", CONFIG_PHASE_RUN_TIME, null),

				p("quarkus-mongodb-client", "quarkus.mongodb.credentials.auth-mechanism-properties.{*}",
						"java.lang.String", "Allows passing authentication mechanism properties.",
						mongoJARFile.getAbsolutePath(),
						"io.quarkus.mongodb.runtime.CredentialConfig#authMechanismProperties", CONFIG_PHASE_RUN_TIME,
						null));

	}

}

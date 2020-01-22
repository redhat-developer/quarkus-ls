/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.microprofile.jdt.core;

import static com.redhat.microprofile.commons.metadata.ItemMetadata.CONFIG_PHASE_BUILD_TIME;
import static com.redhat.microprofile.commons.metadata.ItemMetadata.CONFIG_PHASE_RUN_TIME;
import static com.redhat.microprofile.jdt.internal.core.MicroProfileAssert.assertHints;
import static com.redhat.microprofile.jdt.internal.core.MicroProfileAssert.assertHintsDuplicate;
import static com.redhat.microprofile.jdt.internal.core.MicroProfileAssert.assertProperties;
import static com.redhat.microprofile.jdt.internal.core.MicroProfileAssert.h;
import static com.redhat.microprofile.jdt.internal.core.MicroProfileAssert.p;
import static com.redhat.microprofile.jdt.internal.core.MicroProfileAssert.vh;

import java.io.File;
import java.util.Optional;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.Assert;
import org.junit.Test;

import com.redhat.microprofile.commons.MicroProfileProjectInfo;
import com.redhat.microprofile.commons.metadata.ItemHint;
import com.redhat.microprofile.commons.metadata.ItemMetadata;
import com.redhat.microprofile.jdt.internal.core.utils.DependencyUtil;

/**
 * Test to download and use in classpath deployment JARs declared in //
 * META-INF/quarkus-extension.properties of quarkus-hibernate-orm.jar as
 * property:
 * <code>deployment-artifact=io.quarkus\:quarkus-hibernate-orm-deployment\:0.21.1</code>
 * 
 * @author Angelo ZERR
 *
 */
public class QuarkusConfigRootTest extends BasePropertiesManagerTest {

	@Test
	public void hibernateOrmResteasy() throws Exception {
		MicroProfileProjectInfo info = getMicroProfileProjectInfoFromMavenProject(
				MavenProjectName.hibernate_orm_resteasy);

		File f = DependencyUtil.getArtifact("io.quarkus", "quarkus-hibernate-orm-deployment", "0.19.1", null,
				new NullProgressMonitor());
		Assert.assertNotNull("Test existing of quarkus-hibernate-orm-deployment*.jar", f);

		assertProperties(info,

				// io.quarkus.hibernate.orm.deployment.HibernateOrmConfig
				p("quarkus-hibernate-orm", "quarkus.hibernate-orm.dialect", "java.util.Optional<java.lang.String>",
						"The hibernate ORM dialect class name", true,
						"io.quarkus.hibernate.orm.deployment.HibernateOrmConfig", "dialect", null,
						CONFIG_PHASE_BUILD_TIME, null));
	}

	@Test
	public void allQuarkusExtensions() throws Exception {
		MicroProfileProjectInfo info = getMicroProfileProjectInfoFromMavenProject(
				MavenProjectName.all_quarkus_extensions);

		File keycloakJARFile = DependencyUtil.getArtifact("io.quarkus", "quarkus-keycloak-deployment", "0.21.1", null,
				new NullProgressMonitor());
		Assert.assertNotNull("Test existing of quarkus-keycloak-deployment*.jar", keycloakJARFile);
		File hibernateJARFile = DependencyUtil.getArtifact("io.quarkus", "quarkus-hibernate-orm-deployment", "0.21.1",
				null, new NullProgressMonitor());
		Assert.assertNotNull("Test existing of quarkus-hibernate-orm-deployment*.jar", hibernateJARFile);
		File undertowJARFile = DependencyUtil.getArtifact("io.quarkus", "quarkus-undertow", "0.21.1", null,
				new NullProgressMonitor());
		Assert.assertNotNull("Test existing of quarkus-undertow*.jar", undertowJARFile);
		File mongoJARFile = DependencyUtil.getArtifact("io.quarkus", "quarkus-mongodb-client", "0.21.1", null,
				new NullProgressMonitor());
		Assert.assertNotNull("Test existing of quarkus-mongodb-client*.jar", mongoJARFile);

		assertProperties(info,

				// Test with Map<String, String>
				// https://github.com/quarkusio/quarkus/blob/0.21/extensions/keycloak/deployment/src/main/java/io/quarkus/keycloak/KeycloakConfig.java#L308
				p("quarkus-keycloak", "quarkus.keycloak.credentials.jwt.{*}", "java.lang.String",
						"The settings for client authentication with signed JWT", true,
						"io.quarkus.keycloak.KeycloakConfig.KeycloakConfigCredentials", "jwt", null,
						CONFIG_PHASE_BUILD_TIME, null),

				// Test with Map<String, Map<String, Map<String, String>>>
				// https://github.com/quarkusio/quarkus/blob/0.21/extensions/keycloak/deployment/src/main/java/io/quarkus/keycloak/KeycloakConfig.java#L469
				p("quarkus-keycloak", "quarkus.keycloak.policy-enforcer.paths.{*}.claim-information-point.{*}.{*}.{*}",
						"java.lang.String", "", true,
						"io.quarkus.keycloak.KeycloakConfig.KeycloakConfigPolicyEnforcer.ClaimInformationPointConfig",
						"complexConfig", null, CONFIG_PHASE_BUILD_TIME, null),

				// io.quarkus.hibernate.orm.deployment.HibernateOrmConfig
				p("quarkus-hibernate-orm", "quarkus.hibernate-orm.dialect", "java.util.Optional<java.lang.String>",
						"The hibernate ORM dialect class name", true,
						"io.quarkus.hibernate.orm.deployment.HibernateOrmConfig", "dialect", null,
						CONFIG_PHASE_BUILD_TIME, null),

				// test with extension name
				p("quarkus-undertow", "quarkus.http.ssl.certificate.file", "java.util.Optional<java.nio.file.Path>",
						"The file path to a server certificate or certificate chain in PEM format.", true,
						"io.quarkus.runtime.configuration.ssl.CertificateConfig", "file", null, CONFIG_PHASE_RUN_TIME,
						null),

				p("quarkus-mongodb-client", "quarkus.mongodb.credentials.auth-mechanism-properties.{*}",
						"java.lang.String", "Allows passing authentication mechanism properties.", true,
						"io.quarkus.mongodb.runtime.CredentialConfig", "authMechanismProperties", null,
						CONFIG_PHASE_RUN_TIME, null),

				// test with java.util.Optional enumeration
				p("quarkus-agroal", "quarkus.datasource.transaction-isolation-level",
						"java.util.Optional<io.agroal.api.configuration.AgroalConnectionFactoryConfiguration.TransactionIsolation>",
						"The transaction isolation level.", true, "io.quarkus.agroal.runtime.DataSourceRuntimeConfig",
						"transactionIsolationLevel", null, CONFIG_PHASE_RUN_TIME, null),

				// test with enumeration
				p("quarkus-core", "quarkus.log.console.async.overflow",
						"org.jboss.logmanager.handlers.AsyncHandler.OverflowAction",
						"Determine whether to block the publisher (rather than drop the message) when the queue is full",
						true, "io.quarkus.runtime.logging.AsyncConfig", "overflow", null, CONFIG_PHASE_RUN_TIME,
						"block") //
		);

		// assertPropertiesDuplicate(info);

		assertHints(info,
				h("io.agroal.api.configuration.AgroalConnectionFactoryConfiguration.TransactionIsolation", null, true,
						"io.agroal.api.configuration.AgroalConnectionFactoryConfiguration.TransactionIsolation", //
						vh("UNDEFINED", null, null), //
						vh("NONE", null, null), //
						vh("READ_UNCOMMITTED", null, null), //
						vh("READ_COMMITTED", null, null), //
						vh("REPEATABLE_READ", null, null), //
						vh("SERIALIZABLE", null, null)), //

				h("org.jboss.logmanager.handlers.AsyncHandler.OverflowAction", null, true,
						"org.jboss.logmanager.handlers.AsyncHandler.OverflowAction", //
						vh("BLOCK", null, null), //
						vh("DISCARD", null, null)) //
		);

		assertHintsDuplicate(info);

		// Check get enum values from project info

		// for Optional Java enum
		Optional<ItemMetadata> metadata = getItemMetadata("quarkus.datasource.transaction-isolation-level", info);
		Assert.assertTrue("Check existing of quarkus.datasource.transaction-isolation-level", metadata.isPresent());
		ItemHint hint = info.getHint(metadata.get());
		Assert.assertNotNull("Check existing of hint for quarkus.datasource.transaction-isolation-level", hint);
		Assert.assertNotNull("Check existing of values hint for quarkus.datasource.transaction-isolation-level",
				hint.getValues());
		Assert.assertFalse("Check has values hint for quarkus.datasource.transaction-isolation-level",
				hint.getValues().isEmpty());

		// for Java enum
		metadata = getItemMetadata("quarkus.log.console.async.overflow", info);
		Assert.assertTrue("Check existing of quarkus.log.console.async.overflow", metadata.isPresent());
		hint = info.getHint(metadata.get());
		Assert.assertNotNull("Check existing of hint for quarkus.log.console.async.overflow", hint);
		Assert.assertNotNull("Check existing of values hint for quarkus.log.console.async.overflow", hint.getValues());
		Assert.assertFalse("Check has values hint for quarkus.log.console.async.overflow", hint.getValues().isEmpty());

	}

	private static Optional<ItemMetadata> getItemMetadata(String propertyName, MicroProfileProjectInfo info) {
		return info.getProperties().stream().filter(completion -> {
			return propertyName.equals(completion.getName());
		}).findFirst();

	}

}

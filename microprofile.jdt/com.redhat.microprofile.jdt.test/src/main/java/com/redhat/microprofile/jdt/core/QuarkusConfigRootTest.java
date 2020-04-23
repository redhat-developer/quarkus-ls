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

import static com.redhat.microprofile.commons.metadata.ItemMetadata.CONFIG_PHASE_BUILD_AND_RUN_TIME_FIXED;
import static com.redhat.microprofile.commons.metadata.ItemMetadata.CONFIG_PHASE_BUILD_TIME;
import static com.redhat.microprofile.commons.metadata.ItemMetadata.CONFIG_PHASE_RUN_TIME;
import static com.redhat.microprofile.jdt.internal.core.MicroProfileAssert.assertHints;
import static com.redhat.microprofile.jdt.internal.core.MicroProfileAssert.assertHintsDuplicate;
import static com.redhat.microprofile.jdt.internal.core.MicroProfileAssert.assertProperties;
import static com.redhat.microprofile.jdt.internal.core.MicroProfileAssert.h;
import static com.redhat.microprofile.jdt.internal.core.MicroProfileAssert.p;
import static com.redhat.microprofile.jdt.internal.core.MicroProfileAssert.vh;

import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;

import com.redhat.microprofile.commons.MicroProfileProjectInfo;
import com.redhat.microprofile.commons.MicroProfilePropertiesScope;
import com.redhat.microprofile.commons.metadata.ItemHint;
import com.redhat.microprofile.commons.metadata.ItemMetadata;

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
		assertProperties(info,

				// io.quarkus.hibernate.orm.deployment.HibernateOrmConfig
				p("quarkus-hibernate-orm", "quarkus.hibernate-orm.dialect", "java.util.Optional<java.lang.String>",
						"Class name of the Hibernate ORM dialect. The complete list of bundled dialects is available in the\n" //
								+ "https://docs.jboss.org/hibernate/stable/orm/javadocs/org/hibernate/dialect/package-summary.html[Hibernate ORM JavaDoc].\n" //
								+ "\n" + //
								"[NOTE]\n" + //
								"====\n" //
								+ "Not all the dialects are supported in GraalVM native executables: we currently provide driver extensions for PostgreSQL,\n" //
								+ "MariaDB, Microsoft SQL Server and H2.\n" + //
								"====\n" + //
								"\n" + //
								"@asciidoclet",
						true, "io.quarkus.hibernate.orm.deployment.HibernateOrmConfig", "dialect", null,
						CONFIG_PHASE_BUILD_TIME, null));
	}

	@Test
	public void allQuarkusExtensions() throws Exception {
		MicroProfileProjectInfo info = getMicroProfileProjectInfoFromMavenProject(
				MavenProjectName.all_quarkus_extensions);

		assertProperties(info,

				p("quarkus-keycloak-authorization", "quarkus.keycloak.policy-enforcer.paths.{*}.name",
						"java.util.Optional<java.lang.String>",
						"The name of a resource on the server that is to be associated with a given path", true,
						"io.quarkus.keycloak.pep.runtime.KeycloakPolicyEnforcerConfig.KeycloakConfigPolicyEnforcer.PathConfig",
						"name", null, CONFIG_PHASE_BUILD_AND_RUN_TIME_FIXED, null),

				p("quarkus-keycloak-authorization", "quarkus.keycloak.policy-enforcer.paths.{*}.methods.{*}.method",
						"java.lang.String", "The name of the HTTP method", true,
						"io.quarkus.keycloak.pep.runtime.KeycloakPolicyEnforcerConfig.KeycloakConfigPolicyEnforcer.MethodConfig",
						"method", null, CONFIG_PHASE_BUILD_AND_RUN_TIME_FIXED, null),

				p("quarkus-hibernate-orm", "quarkus.hibernate-orm.dialect", "java.util.Optional<java.lang.String>",
						"Class name of the Hibernate ORM dialect. The complete list of bundled dialects is available in the\n" //
								+ "https://docs.jboss.org/hibernate/stable/orm/javadocs/org/hibernate/dialect/package-summary.html[Hibernate ORM JavaDoc].\n" //
								+ "\n" + //
								"[NOTE]\n" + //
								"====\n" //
								+ "Not all the dialects are supported in GraalVM native executables: we currently provide driver extensions for PostgreSQL,\n" //
								+ "MariaDB, Microsoft SQL Server and H2.\n" + //
								"====\n" + //
								"\n" + //
								"@asciidoclet",
						true, "io.quarkus.hibernate.orm.deployment.HibernateOrmConfig", "dialect", null,
						CONFIG_PHASE_BUILD_TIME, null),

				p("quarkus-vertx-http", "quarkus.http.ssl.certificate.file", "java.util.Optional<java.nio.file.Path>",
						"The file path to a server certificate or certificate chain in PEM format.", true,
						"io.quarkus.vertx.http.runtime.CertificateConfig", "file", null, CONFIG_PHASE_RUN_TIME, null),

				p("quarkus-mongodb-client", "quarkus.mongodb.credentials.auth-mechanism-properties.{*}",
						"java.lang.String", "Allows passing authentication mechanism properties.", true,
						"io.quarkus.mongodb.runtime.CredentialConfig", "authMechanismProperties", null,
						CONFIG_PHASE_RUN_TIME, null),

				// test with java.util.Optional enumeration
				p("quarkus-agroal", "quarkus.datasource.jdbc.transaction-isolation-level",
						"java.util.Optional<io.agroal.api.configuration.AgroalConnectionFactoryConfiguration.TransactionIsolation>",
						"The transaction isolation level.", true, "io.quarkus.agroal.runtime.DataSourceJdbcRuntimeConfig",
						"transactionIsolationLevel", null, CONFIG_PHASE_RUN_TIME, null),

				// test with enumeration
				p("quarkus-core", "quarkus.log.console.async.overflow",
						"org.jboss.logmanager.handlers.AsyncHandler.OverflowAction",
						"Determine whether to block the publisher (rather than drop the message) when the queue is full",
						true, "io.quarkus.runtime.logging.AsyncConfig", "overflow", null, CONFIG_PHASE_RUN_TIME,
						"block"), //

				// test with quarkus.smallrye-openapi.path (download dependencies from
				// deployment artifact quarkus.smallrye-openapi)
				p("quarkus-smallrye-openapi-common", "quarkus.smallrye-openapi.path", "java.lang.String",
						"The path at which to register the OpenAPI Servlet.", true,
						"io.quarkus.smallrye.openapi.common.deployment.SmallRyeOpenApiConfig", "path", null,
						CONFIG_PHASE_BUILD_TIME, "/openapi") //

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

	@Test
	public void configQuickStart() throws Exception {
		// Test for https://github.com/redhat-developer/vscode-quarkus/issues/249

		MicroProfileProjectInfo info = getMicroProfileProjectInfoFromMavenProject(MavenProjectName.config_quickstart,
				MicroProfilePropertiesScope.SOURCES_AND_DEPENDENCIES);

		assertProperties(info,

				// io.quarkus.deployment.ApplicationConfig
				p("quarkus-core", "quarkus.application.name", "java.util.Optional<java.lang.String>",
						"The name of the application.\nIf not set, defaults to the name of the project (except for tests where it is not set at all).",
						true, "io.quarkus.runtime.ApplicationConfig", "name", null,
						CONFIG_PHASE_BUILD_AND_RUN_TIME_FIXED, null),

				p("quarkus-core", "quarkus.application.version", "java.util.Optional<java.lang.String>",
						"The version of the application.\nIf not set, defaults to the version of the project (except for tests where it is not set at all).",
						true, "io.quarkus.runtime.ApplicationConfig", "version", null,
						CONFIG_PHASE_BUILD_AND_RUN_TIME_FIXED, null),

				p("quarkus-core", "quarkus.banner.enabled", "boolean", "Whether or not the banner will be displayed",
						true, "io.quarkus.runtime.BannerRuntimeConfig", "enabled", null, CONFIG_PHASE_RUN_TIME, "true"),

				p("quarkus-core", "quarkus.locales", "java.util.Set<java.util.Locale>",
						"The set of supported locales that can be consumed by the extensions.\n<p>\nThe locales must be specified in the IETF BCP 47 format e.g. en-US or fr-FR.\n<p>\nFor instance, the Hibernate Validator extension makes use of it.",
						true, "io.quarkus.runtime.LocalesBuildTimeConfig", "locales", null,
						CONFIG_PHASE_BUILD_AND_RUN_TIME_FIXED, "${user.language:en}-${user.country:}"),

				p("quarkus-core", "quarkus.default-locale", "java.util.Locale",
						"Default locale that can be consumed by the extensions.\n<p>\nThe locales must be specified in the IETF BCP 47 format e.g. en-US or fr-FR.\n<p>\nFor instance, the Hibernate Validator extension makes use of it.",
						true, "io.quarkus.runtime.LocalesBuildTimeConfig", "defaultLocale", null,
						CONFIG_PHASE_BUILD_AND_RUN_TIME_FIXED, "${user.language:en}-${user.country:}")

		);
	}

	private static Optional<ItemMetadata> getItemMetadata(String propertyName, MicroProfileProjectInfo info) {
		return info.getProperties().stream().filter(completion -> {
			return propertyName.equals(completion.getName());
		}).findFirst();
	}

}

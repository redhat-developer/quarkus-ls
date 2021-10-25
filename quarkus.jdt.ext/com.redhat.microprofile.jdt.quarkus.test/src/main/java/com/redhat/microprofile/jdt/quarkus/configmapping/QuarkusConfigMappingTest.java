/*******************************************************************************
* Copyright (c) 2021 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.microprofile.jdt.quarkus.configmapping;

import static org.eclipse.lsp4mp.jdt.core.MicroProfileAssert.assertProperties;
import static org.eclipse.lsp4mp.jdt.core.MicroProfileAssert.assertPropertiesDuplicate;
import static org.eclipse.lsp4mp.jdt.core.MicroProfileAssert.p;

import org.eclipse.lsp4mp.commons.MicroProfileProjectInfo;
import org.eclipse.lsp4mp.commons.MicroProfilePropertiesScope;
import org.eclipse.lsp4mp.jdt.core.BasePropertiesManagerTest;
import org.junit.Test;

import com.redhat.microprofile.jdt.quarkus.QuarkusMavenProjectName;

/**
 * Test collection of Quarkus properties from @ConfigMapping
 * 
 * @author Angelo ZERR
 *
 */
public class QuarkusConfigMappingTest extends BasePropertiesManagerTest {

	@Test
	public void configMapping() throws Exception {

		MicroProfileProjectInfo infoFromJavaSources = getMicroProfileProjectInfoFromMavenProject(
				QuarkusMavenProjectName.config_mapping, MicroProfilePropertiesScope.ONLY_SOURCES);

		assertProperties(infoFromJavaSources,

				// 1) collections

				// @ConfigMapping(prefix = "server.collections")
				// public interface ServerCollections {
				// Set<Environment> environments();
				// interface Environment

				p(null, "server.collections.environments[*].name", "java.lang.String", null, false,
						"org.acme.collections.ServerCollections.Environment", null, "name()QString;", 0, null),
				p(null, "server.collections.environments[*].apps[*].name", "java.lang.String", null, false,
						"org.acme.collections.ServerCollections.Environment.App", null, "name()QString;", 0, null),
				p(null, "server.collections.environments[*].apps[*].services[*]", "java.util.List", null, false,
						"org.acme.collections.ServerCollections.Environment.App", null, "services()QList<QString;>;", 0,
						null),
				p(null, "server.collections.environments[*].apps[*].databases[*]", "java.util.List", null, false,
						"org.acme.collections.ServerCollections.Environment.App", null,
						"databases()QOptional<QList<QString;>;>;", 0, null),

				// 2) map

				// @ConfigMapping(prefix = "server.map")
				// public interface Server {
				// ...
				// Map<String, String> form();

				p(null, "server.map.host", "java.lang.String", null, false, "org.acme.map.Server", null,
						"host()QString;", 0, null),
				p(null, "server.map.port", "int", null, false, "org.acme.map.Server", null, "port()I", 0, null),
				p(null, "server.map.form.{*}", "java.util.Map", null, false, "org.acme.map.Server", null,
						"form()QMap<QString;QString;>;", 0, null),

				// 3) Naming strategy

				// @ConfigMapping(prefix = "server.kebab", namingStrategy = KEBAB_CASE)
				// public interface ServerKebabCaseNamingStrategy {
				// String theHost();
				// int thePort();
				p(null, "server.kebab.the-host", "java.lang.String", null, false,
						"org.acme.namingstrategy.ServerKebabCaseNamingStrategy", null, "theHost()QString;", 0, null),
				p(null, "server.kebab.the-port", "int", null, false,
						"org.acme.namingstrategy.ServerKebabCaseNamingStrategy", null, "thePort()I", 0, null),

				// @ConfigMapping(prefix = "server.snake", namingStrategy = SNAKE_CASE)
				// public interface ServerSnakeCaseNamingStrategy {
				// String theHost();
				// int thePort();
				p(null, "server.snake.the_host", "java.lang.String", null, false,
						"org.acme.namingstrategy.ServerSnakeCaseNamingStrategy", null, "theHost()QString;", 0, null),
				p(null, "server.snake.the_port", "int", null, false,
						"org.acme.namingstrategy.ServerSnakeCaseNamingStrategy", null, "thePort()I", 0, null),

				// @ConfigMapping(prefix = "server.verbatim", namingStrategy = VERBATIM)
				// public interface ServerVerbatimNamingStrategy {
				// String theHost();
				// int thePort();
				p(null, "server.verbatim.theHost", "java.lang.String", null, false,
						"org.acme.namingstrategy.ServerVerbatimNamingStrategy", null, "theHost()QString;", 0, null),
				p(null, "server.verbatim.thePort", "int", null, false,
						"org.acme.namingstrategy.ServerVerbatimNamingStrategy", null, "thePort()I", 0, null),

				// 4) Nested group

				// @ConfigMapping(prefix = "server.nestedgroup")
				// public interface Server {
				// ...
				// Log log();
				//
				// interface Log {
				// ...
				p(null, "server.nestedgroup.host", "java.lang.String", null, false, "org.acme.nestedgroup.Server", null,
						"host()QString;", 0, null),
				p(null, "server.nestedgroup.port", "int", null, false, "org.acme.nestedgroup.Server", null, "port()I",
						0, null),
				p(null, "server.nestedgroup.log.enabled", "boolean", null, false, "org.acme.nestedgroup.Server.Log",
						null, "enabled()Z", 0, null),
				p(null, "server.nestedgroup.log.suffix", "java.lang.String", null, false,
						"org.acme.nestedgroup.Server.Log", null, "suffix()QString;", 0, null),
				p(null, "server.nestedgroup.log.rotate", "boolean", null, false, "org.acme.nestedgroup.Server.Log",
						null, "rotate()Z", 0, null),

				// 5) Simple

				// @ConfigMapping(prefix = "server.simple")
				// interface Server {
				// String host();
				// int port();
				p(null, "server.simple.host", "java.lang.String", null, false, "org.acme.simple.Server", null,
						"host()QString;", 0, null),
				p(null, "server.simple.port", "int", null, false, "org.acme.simple.Server", null, "port()I", 0, null),

				// 6) WithName

				// @ConfigMapping(prefix = "server.withname")
				// interface Server {
				// @WithName("name")
				// String host();

				p(null, "server.withname.name", "java.lang.String", null, false, "org.acme.withname.Server", null,
						"host()QString;", 0, null),
				p(null, "server.withname.port", "int", null, false, "org.acme.withname.Server", null, "port()I", 0,
						null),

				// 7) @WithParentName

				// @ConfigMapping(prefix = "server.withparentname")
				// public interface Server {
				// @WithParentName
				// ServerHostAndPort hostAndPort();

				p(null, "server.withparentname.host", "java.lang.String", null, false,
						"org.acme.withparentname.ServerHostAndPort", null, "host()QString;", 0, null),
				p(null, "server.withparentname.port", "int", null, false, "org.acme.withparentname.ServerHostAndPort",
						null, "port()I", 0, null),
				p(null, "server.withparentname.name", "java.lang.String", null, false,
						"org.acme.withparentname.ServerInfo", null, "name()QString;", 0, null)

		);

		assertPropertiesDuplicate(infoFromJavaSources);
	}
}
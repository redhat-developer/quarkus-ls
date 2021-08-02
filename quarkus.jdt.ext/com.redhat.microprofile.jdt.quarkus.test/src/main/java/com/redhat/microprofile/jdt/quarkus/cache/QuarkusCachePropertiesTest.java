/*******************************************************************************
* Copyright (c) 2021 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.microprofile.jdt.quarkus.cache;

import static org.eclipse.lsp4mp.jdt.core.MicroProfileAssert.assertHints;
import static org.eclipse.lsp4mp.jdt.core.MicroProfileAssert.assertHintsDuplicate;
import static org.eclipse.lsp4mp.jdt.core.MicroProfileAssert.assertProperties;
import static org.eclipse.lsp4mp.jdt.core.MicroProfileAssert.assertPropertiesDuplicate;
import static org.eclipse.lsp4mp.jdt.core.MicroProfileAssert.h;
import static org.eclipse.lsp4mp.jdt.core.MicroProfileAssert.p;
import static org.eclipse.lsp4mp.jdt.core.MicroProfileAssert.vh;

import org.eclipse.lsp4mp.commons.MicroProfileProjectInfo;
import org.eclipse.lsp4mp.commons.MicroProfilePropertiesScope;
import org.eclipse.lsp4mp.jdt.core.BasePropertiesManagerTest;
import org.junit.Test;

import com.redhat.microprofile.jdt.quarkus.QuarkusMavenProjectName;

/**
 * Test collection of Quarkus properties from @CacheResult
 */
public class QuarkusCachePropertiesTest extends BasePropertiesManagerTest {

	@Test
	public void cacheQuickstartFromClasspath() throws Exception {

		MicroProfileProjectInfo infoFromClasspath = getMicroProfileProjectInfoFromMavenProject(
				QuarkusMavenProjectName.cache_quickstart, MicroProfilePropertiesScope.SOURCES_AND_DEPENDENCIES);

		assertProperties(infoFromClasspath,
				// WeatherForecastService
				// @CacheResult(cacheName = "weather-cache")
				// public String getDailyForecast(LocalDate date, String city) {
				p("quarkus-cache", "quarkus.cache.enabled", "boolean", "Whether or not the cache extension is enabled.",
						true, "io.quarkus.cache.deployment.CacheConfig", "enabled", null, 1, "true"),
				p("quarkus-cache", "quarkus.cache.caffeine.${quarkus.cache.name}.initial-capacity",
						"java.util.OptionalInt",
						"Minimum total size for the internal data structures. Providing a large enough estimate at construction time\navoids the need for expensive resizing operations later, but setting this value unnecessarily high wastes memory.",
						true, "io.quarkus.cache.deployment.CacheConfig.CaffeineConfig.CaffeineNamespaceConfig",
						"initialCapacity", null, 1, null));

		assertPropertiesDuplicate(infoFromClasspath);
		
		assertHints(infoFromClasspath, h("${quarkus.cache.name}", null, false, null, //
				vh("weather-cache", null, null)) //
		);

		assertHintsDuplicate(infoFromClasspath);

	}

}
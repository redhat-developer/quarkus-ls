/*******************************************************************************
* Copyright (c) 2020 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.microprofile.jdt.quarkus;

import static org.eclipse.lsp4mp.jdt.core.MicroProfileAssert.assertProperties;
import static org.eclipse.lsp4mp.jdt.core.MicroProfileAssert.assertPropertiesDuplicate;
import static org.eclipse.lsp4mp.jdt.core.MicroProfileAssert.p;

import org.eclipse.lsp4mp.commons.MicroProfileProjectInfo;
import org.eclipse.lsp4mp.commons.MicroProfilePropertiesScope;
import org.eclipse.lsp4mp.jdt.core.BasePropertiesManagerTest;
import org.junit.Test;

/**
 * Test collection of Quarkus properties from @Scheduled
 */
public class QuarkusScheduledPropertiesTest extends BasePropertiesManagerTest {

	@Test
	public void configQuickstartFromClasspath() throws Exception {

		MicroProfileProjectInfo infoFromClasspath = getMicroProfileProjectInfoFromMavenProject(
				QuarkusMavenProjectName.scheduler_quickstart, MicroProfilePropertiesScope.SOURCES_AND_DEPENDENCIES);

		assertProperties(infoFromClasspath,
				// CounterBean
				// @Scheduled(cron = "{cron.expr}")
				// void cronJobWithExpressionInConfig()
				p(null, "cron.expr", "java.lang.String", null, false, null,
						null, "cronJobWithExpressionInConfig()V", 0, null));

		assertPropertiesDuplicate(infoFromClasspath);
	}

	@Test
	public void configQuickstartFromJavaSources() throws Exception {

		MicroProfileProjectInfo infoFromJavaSources = getMicroProfileProjectInfoFromMavenProject(
				QuarkusMavenProjectName.scheduler_quickstart, MicroProfilePropertiesScope.ONLY_SOURCES);

		assertProperties(infoFromJavaSources,
				// CounterBean
				// @Scheduled(cron = "{cron.expr}")
				// void cronJobWithExpressionInConfig()
				p(null, "cron.expr", "java.lang.String", null, false, null,
						null, "cronJobWithExpressionInConfig()V", 0, null));

		assertPropertiesDuplicate(infoFromJavaSources);
	}
}
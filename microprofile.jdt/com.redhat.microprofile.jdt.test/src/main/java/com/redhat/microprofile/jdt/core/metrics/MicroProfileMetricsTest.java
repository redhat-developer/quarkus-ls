/*******************************************************************************
* Copyright (c) 2020 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.microprofile.jdt.core.metrics;

import static com.redhat.microprofile.jdt.internal.core.MicroProfileAssert.assertProperties;
import static com.redhat.microprofile.jdt.internal.core.MicroProfileAssert.assertPropertiesDuplicate;
import static com.redhat.microprofile.jdt.internal.core.MicroProfileAssert.p;

import org.junit.Test;

import com.redhat.microprofile.commons.MicroProfileProjectInfo;
import com.redhat.microprofile.commons.MicroProfilePropertiesScope;
import com.redhat.microprofile.jdt.core.BasePropertiesManagerTest;

/**
 * Test the availability of the MicroProfile Metrics properties
 * 
 * @author David Kwon
 *
 */
public class MicroProfileMetricsTest extends BasePropertiesManagerTest {

	@Test
	public void microprofileMetricsPropertiesTest() throws Exception {

		MicroProfileProjectInfo infoFromClasspath = getMicroProfileProjectInfoFromMavenProject(
				MavenProjectName.microprofile_metrics, MicroProfilePropertiesScope.SOURCES_AND_DEPENDENCIES);

		assertProperties(infoFromClasspath,

				p("microprofile-metrics-api", "mp.metrics.tags", "java.lang.String", 
						"List of tag values.\r\n"
						+ "Tag values set through `mp.metrics.tags` MUST escape equal symbols `=` and commas `,` with a backslash `\\`.",
						true, null, null, null, 0, null),

				p("microprofile-metrics-api", "mp.metrics.appName", "java.lang.String",
						"The app name.",
						true, null, null, null, 0, null)

		);

		assertPropertiesDuplicate(infoFromClasspath);
	}

}

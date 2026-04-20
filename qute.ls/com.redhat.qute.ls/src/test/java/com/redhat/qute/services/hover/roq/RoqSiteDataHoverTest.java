/*******************************************************************************
* Copyright (c) 2026 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.services.hover.roq;

import static com.redhat.qute.QuteAssert.r;

import java.util.Collections;

import org.eclipse.lsp4j.Range;
import org.junit.jupiter.api.Test;

import com.redhat.qute.HoverParameters;
import com.redhat.qute.QuteAssert;
import com.redhat.qute.project.extensions.roq.frontmatter.YamlFrontMatterDetector;
import com.redhat.qute.project.roq.RoqProject;

/**
 * Tests hover on site.data properties augmented with YAML front matter from content/index.html.
 *
 * @author Angelo ZERR
 */
public class RoqSiteDataHoverTest {

	@Test
	public void siteDataTitleProperty() throws Exception {
		String indexHtmlUri = RoqProject.getFileUri("/content/index.html");
		String template = "{site.data.tit|le}";
		assertHover(template, //
				"```java" + //
						System.lineSeparator() + //
						"title : String" + //
						System.lineSeparator() + //
						"```" + //
						System.lineSeparator() + //
						"---" + //
						System.lineSeparator() + //
						"Defined in YAML front matter: [index.html:2](" + indexHtmlUri + ")", //
				r(0, 11, 0, 16));
	}

	@Test
	public void siteDataDescriptionProperty() throws Exception {
		String indexHtmlUri = RoqProject.getFileUri("/content/index.html");
		String template = "{site.data.descri|ption}";
		assertHover(template, //
				"```java" + //
						System.lineSeparator() + //
						"description : String" + //
						System.lineSeparator() + //
						"```" + //
						System.lineSeparator() + //
						"---" + //
						System.lineSeparator() + //
						"Defined in YAML front matter: [index.html:3](" + indexHtmlUri + ")", //
				r(0, 11, 0, 22));
	}

	@Test
	public void siteDataPropertyNotInFrontMatter() throws Exception {
		String template = "{site.data.unkno|wn}";
		// Unknown property falls back to JsonObject.get() value resolver
		assertHover(template, //
				"```java" + //
						System.lineSeparator() + //
						"Object get()" + //
						System.lineSeparator() + //
						"```" + //
						System.lineSeparator() + //
						"See [here](https://quarkus.io/guides/qute-reference#vertx_integration) for more informations.", //
				r(0, 11, 0, 18));
	}

	public static void assertHover(String value, String expectedHoverLabel, Range expectedHoverRange) throws Exception {
		HoverParameters p = new HoverParameters();
		p.setProjectUri(RoqProject.PROJECT_URI);
		p.setInjectionDetectors(Collections.singletonList(new YamlFrontMatterDetector()));
		QuteAssert.assertHover(value, p, expectedHoverLabel, expectedHoverRange);
	}

}

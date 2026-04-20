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
 * Tests hover on page.data properties augmented with YAML front matter.
 *
 * @author Angelo ZERR
 */
public class RoqPageDataHoverTest {

	@Test
	public void pageDataLayoutProperty() throws Exception {
		String template = "---\r\n" + //
				"layout: main\r\n" + //
				"title: My title\r\n" + //
				"---\r\n" + //
				"{page.data.lay|out}";
		assertHover(template, //
				"```java" + //
						System.lineSeparator() + //
						"layout : String" + //
						System.lineSeparator() + //
						"```" + //
						System.lineSeparator() + //
						"---" + //
						System.lineSeparator() + //
						"Defined in YAML front matter: [test.qute:2](" + QuteAssert.FILE_URI + ")", //
				r(4, 11, 4, 17));
	}

	@Test
	public void pageDataTitleProperty() throws Exception {
		String template = "---\r\n" + //
				"layout: main\r\n" + //
				"title: My title\r\n" + //
				"---\r\n" + //
				"{page.data.tit|le}";
		assertHover(template, //
				"```java" + //
						System.lineSeparator() + //
						"title : String" + //
						System.lineSeparator() + //
						"```" + //
						System.lineSeparator() + //
						"---" + //
						System.lineSeparator() + //
						"Defined in YAML front matter: [test.qute:3](" + QuteAssert.FILE_URI + ")", //
				r(4, 11, 4, 16));
	}

	@Test
	public void pageDataNoFrontMatter() throws Exception {
		// No YAML front matter - standard JsonObject properties only
		String template = "{page.data.lay|out}";
		// No yaml frontmatter, falls back to JsonObject.get() value resolver
		assertHover(template, //
				"```java" + //
						System.lineSeparator() + //
						"Object get()" + //
						System.lineSeparator() + //
						"```" + //
						System.lineSeparator() + //
						"See [here](https://quarkus.io/guides/qute-reference#vertx_integration) for more informations.", //
				r(0, 11, 0, 17));
	}

	@Test
	public void pageDataPropertyNotInFrontMatter() throws Exception {
		String template = "---\r\n" + //
				"layout: main\r\n" + //
				"title: My title\r\n" + //
				"---\r\n" + //
				"{page.data.unkno|wn}";
		// Unknown property falls back to JsonObject.get() value resolver
		assertHover(template, //
				"```java" + //
						System.lineSeparator() + //
						"Object get()" + //
						System.lineSeparator() + //
						"```" + //
						System.lineSeparator() + //
						"See [here](https://quarkus.io/guides/qute-reference#vertx_integration) for more informations.", //
				r(4, 11, 4, 18));
	}

	public static void assertHover(String value, String expectedHoverLabel, Range expectedHoverRange) throws Exception {
		HoverParameters p = new HoverParameters();
		p.setProjectUri(RoqProject.PROJECT_URI);
		p.setInjectionDetectors(Collections.singletonList(new YamlFrontMatterDetector()));
		QuteAssert.assertHover(value, p, expectedHoverLabel, expectedHoverRange);
	}

}

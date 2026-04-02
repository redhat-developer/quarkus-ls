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
 * Tests hover with Roq Quarkus extension and yaml frontmatter.
 *
 * @author Angelo ZERR
 *
 */
public class RoqFrontMatterHoverTest {

	// --------------- layout

	@Test
	public void layoutKey() throws Exception {
		String template = "---\r\n" + //
				"lay|out: default\r\n" + //
				"title: My title\r\n" + //
				"---\r\n" + "";
		assertHover(template, //
				"```" + //
						System.lineSeparator() + //
						"layout: string" + //
						System.lineSeparator() + //
						"```" + //
						System.lineSeparator() + //
						"Qute layout template to wrap this page. Resolves local first, then theme fallback (e.g. `page`, `post`). Use `theme-layout:` to explicitly target a theme layout"
						+ //
						System.lineSeparator() + //
						System.lineSeparator() + //
						"```qute-html" + //
						System.lineSeparator() + //
						":theme/post" + //
						System.lineSeparator() + //
						"```" + //
						System.lineSeparator() + //
						"```qute-html" + //
						System.lineSeparator() + //
						":theme/page" + //
						System.lineSeparator() + //
						"```" + //
						System.lineSeparator() + //
						"```qute-html" + //
						System.lineSeparator() + //
						":theme/main" + //
						System.lineSeparator() + //
						"```" + //
						System.lineSeparator() + //
						"```qute-html" + //
						System.lineSeparator() + //
						"custom-layout" + //
						System.lineSeparator() + //
						"```" + //
						System.lineSeparator() + //
						System.lineSeparator() + //
						"See [here](https://iamroq.dev/docs/basics/#templates) for more informations.", //
				r(1, 0, 1, 6));
	}

	@Test
	public void sourceLayoutValue() throws Exception {
		String layoutUri = RoqProject.getFileUri("/templates/layouts/default.html");
		String template = "---\r\n" + //
				"layout: def|ault\r\n" + //
				"title: My title\r\n" + //
				"---\r\n" + "";
		assertHover(template, //
				"Layout `default`" + //
						System.lineSeparator() + //
						" * Template id: `default`" + //
						System.lineSeparator() + //
						" * Path: [templates/layouts/default.html](" + layoutUri + ")", //
				r(1, 0, 1, 6));
	}

	@Test
	public void binaryLayoutValue() throws Exception {
		String template = "---\r\n" + //
				"layout: mai|n\r\n" + //
				"title: My title\r\n" + //
				"---\r\n" + "";
		assertHover(template, //
				"Layout `main`" + //
						System.lineSeparator() + //
						" * Template id: `theme-layouts/roq-default/main`" + //
						System.lineSeparator() + //
						" * Path: [theme-layouts/roq-default/main.html](jdt://jarentry/templates/theme-layouts/roq-default/main.html?=roq-blog/C:%5C/Users%5C/AngeloZerr%5C/.m2%5C/repository%5C/io%5C/quarkiverse%5C/roq%5C/quarkus-roq-theme-default%5C/2.1.0.BETA2%5C/quarkus-roq-theme-default-2.1.0.BETA2.jar=/maven.pomderived=/true=/=/maven.groupId=/io.quarkiverse.roq=/=/maven.artifactId=/quarkus-roq-theme-default=/=/maven.version=/2.1.0.BETA2=/=/maven.scope=/compile=/=/maven.pomderived=/true=/)"
						+ //
						System.lineSeparator() + //
						" * Origin: `quarkus-roq-theme-default-2.1.0.BETA2.jar`", //
				r(1, 0, 1, 6));
	}

	@Test
	public void binaryThemeLayoutValue() throws Exception {
		String template = "---\r\n" + //
				"theme-layout: mai|n\r\n" + //
				"title: My title\r\n" + //
				"---\r\n" + "";
		assertHover(template, //
				"Layout `main`" + //
						System.lineSeparator() + //
						" * Template id: `theme-layouts/roq-default/main`" + //
						System.lineSeparator() + //
						" * Path: [theme-layouts/roq-default/main.html](jdt://jarentry/templates/theme-layouts/roq-default/main.html?=roq-blog/C:%5C/Users%5C/AngeloZerr%5C/.m2%5C/repository%5C/io%5C/quarkiverse%5C/roq%5C/quarkus-roq-theme-default%5C/2.1.0.BETA2%5C/quarkus-roq-theme-default-2.1.0.BETA2.jar=/maven.pomderived=/true=/=/maven.groupId=/io.quarkiverse.roq=/=/maven.artifactId=/quarkus-roq-theme-default=/=/maven.version=/2.1.0.BETA2=/=/maven.scope=/compile=/=/maven.pomderived=/true=/)"
						+ //
						System.lineSeparator() + //
						" * Origin: `quarkus-roq-theme-default-2.1.0.BETA2.jar`", //
				r(1, 0, 1, 12));
	}

	@Test
	public void noHover() throws Exception {
		String template = "---\r\n" + //
				"fo|o: default\r\n" + //
				"title: My title\r\n" + //
				"---\r\n" + "";
		assertHover(template, null, null);
	}

	// --------------- image

	@Test
	public void imageValue() throws Exception {
		String imageUri = RoqProject.getFileUri("/public/images/ico.png");
		String template = "---\r\n" + //
				"image: ic|o.png\r\n" + //
				"title: My title\r\n" + //
				"---\r\n" + "";
		assertHover(template, //
				"![image](" + imageUri + ")" + //
						System.lineSeparator(), //
				r(1, 0, 1, 5));
	}

	@Test
	public void invalidImageValue() throws Exception {
		String template = "---\r\n" + //
				"image: inv|alid.png\r\n" + //
				"title: My title\r\n" + //
				"---\r\n" + "";
		assertHover(template, null, null);
	}

	public static void assertHover(String value, String expectedHoverLabel, Range expectedHoverRange) throws Exception {
		HoverParameters p = new HoverParameters();
		p.setProjectUri(RoqProject.PROJECT_URI);
		p.setInjectionDetectors(Collections.singletonList(new YamlFrontMatterDetector()));
		QuteAssert.assertHover(value, p, expectedHoverLabel, expectedHoverRange);
	}

}

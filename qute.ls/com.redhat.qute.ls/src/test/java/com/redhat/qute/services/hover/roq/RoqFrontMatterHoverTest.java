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

	@Test
	public void layout() throws Exception {
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
						"The layout template to use for rendering this page. Use ':theme/' prefix to reference theme layouts (e.g., ':theme/post', ':theme/page')"
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
						System.lineSeparator(), //
				r(1, 0, 1, 6));
	}

	@Test
	public void noHover() throws Exception {
		String template = "---\r\n" + //
				"fo|o: default\r\n" + //
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

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
package com.redhat.qute.services.definition.roq;

import static com.redhat.qute.QuteAssert.ll;
import static com.redhat.qute.QuteAssert.r;

import java.util.Collections;

import org.eclipse.lsp4j.LocationLink;
import org.junit.jupiter.api.Test;

import com.redhat.qute.DefinitionParameters;
import com.redhat.qute.QuteAssert;
import com.redhat.qute.project.extensions.roq.frontmatter.YamlFrontMatterDetector;
import com.redhat.qute.project.roq.RoqProject;

/**
 * Tests definition/navigation for page.data properties augmented with YAML front matter.
 *
 * @author Angelo ZERR
 */
public class RoqPageDataDefinitionTest {

	@Test
	public void pageDataLayoutProperty() throws Exception {
		String template = "---\r\n" + //
				"layout: main\r\n" + //
				"title: My title\r\n" + //
				"---\r\n" + //
				"{page.data.lay|out}";
		// Should navigate to the "layout" key in the YAML front matter
		testDefinitionFor(template, //
				ll(QuteAssert.FILE_URI, r(4, 11, 4, 17), r(1, 0, 1, 6)));
	}

	@Test
	public void pageDataTitleProperty() throws Exception {
		String template = "---\r\n" + //
				"layout: main\r\n" + //
				"title: My title\r\n" + //
				"---\r\n" + //
				"{page.data.tit|le}";
		// Should navigate to the "title" key in the YAML front matter
		testDefinitionFor(template, //
				ll(QuteAssert.FILE_URI, r(4, 11, 4, 16), r(2, 0, 2, 5)));
	}

	@Test
	public void pageDataNoFrontMatter() throws Exception {
		// No YAML front matter - no definition
		String template = "{page.data.lay|out}";
		testDefinitionFor(template);
	}

	@Test
	public void pageDataPropertyNotInFrontMatter() throws Exception {
		String template = "---\r\n" + //
				"layout: main\r\n" + //
				"title: My title\r\n" + //
				"---\r\n" + //
				"{page.data.unkno|wn}";
		// Unknown property should have no definition
		testDefinitionFor(template);
	}

	public static void testDefinitionFor(String value, LocationLink... expected) throws Exception {
		DefinitionParameters p = new DefinitionParameters();
		p.setProjectUri(RoqProject.PROJECT_URI);
		p.setInjectionDetectors(Collections.singletonList(new YamlFrontMatterDetector()));
		QuteAssert.testDefinitionFor(value, p, expected);
	}
}

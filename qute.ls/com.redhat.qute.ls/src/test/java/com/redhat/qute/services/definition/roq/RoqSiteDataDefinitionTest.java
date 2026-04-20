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
 * Tests definition/navigation for site.data properties augmented with YAML
 * front matter from content/index.html.
 *
 * @author Angelo ZERR
 */
public class RoqSiteDataDefinitionTest {

	@Test
	public void siteDataTitleProperty() throws Exception {
		String indexHtmlUri = RoqProject.getFileUri("/content/index.html");
		String template = "{site.data.tit|le}";
		// Should navigate to the "title" key in the YAML front matter of
		// content/index.html
		testDefinitionFor(template, //
				ll(indexHtmlUri, r(0, 11, 0, 16), r(1, 0, 1, 5)));
	}

	@Test
	public void siteDataDescriptionProperty() throws Exception {
		String indexHtmlUri = RoqProject.getFileUri("/content/index.html");
		String template = "{site.data.descri|ption}";
		// Should navigate to the "description" key in the YAML front matter of
		// content/index.html
		testDefinitionFor(template, //
				ll(indexHtmlUri, r(0, 11, 0, 22), r(2, 0, 2, 11)));
	}

	@Test
	public void siteDataPropertyNotInFrontMatter() throws Exception {
		String template = "{site.data.unkno|wn}";
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

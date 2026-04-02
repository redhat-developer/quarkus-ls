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
package com.redhat.qute.services.codelens.roq;

import static com.redhat.qute.QuteAssert.cl;
import static com.redhat.qute.QuteAssert.r;

import java.util.Collections;

import org.eclipse.lsp4j.CodeLens;
import org.junit.jupiter.api.Test;

import com.redhat.qute.CodeLensParameters;
import com.redhat.qute.QuteAssert;
import com.redhat.qute.project.extensions.roq.frontmatter.YamlFrontMatterDetector;
import com.redhat.qute.project.roq.RoqProject;

/**
 * Test codeLens with Roq Quarkus extension and layout.
 *
 * @author Angelo ZERR
 * 
 */
public class RoqLayoutCodeLensTest {

	// Layout

	@Test
	public void layoutFromSource() throws Exception {
		// /roq/src/main/resources/templates/layouts/default.html
		String template = "---\r\n" + //
				"layout: default\r\n" + //
				"---\r\n";
		testCodeLensFor(template, //
				cl(r(0, 0, 0, 0), "site : Site", ""), //
				cl(r(0, 0, 0, 0), "page : NormalPage", ""), //
				cl(r(0, 0, 0, 0), "Included by:", ""), //
				cl(r(0, 0, 0, 0), "default", ""));
	}

	@Test
	public void layoutWithThemeFullPath() throws Exception {
		// quarkus-roq-theme-default-2.1.0.BETA2.jar!theme-layouts/roq-default/default.html
		String template = "---\r\n" + //
				"layout: theme-layouts/roq-default/default\r\n" + //
				"---\r\n";
		testCodeLensFor(template, //
				cl(r(0, 0, 0, 0), "site : Site", ""), //
				cl(r(0, 0, 0, 0), "page : NormalPage", ""), //
				cl(r(0, 0, 0, 0), "Included by:", ""), //
				cl(r(0, 0, 0, 0), "theme-layouts/roq-default/default", ""));
	}

	@Test
	public void layoutWithThemeSyntax() throws Exception {
		// quarkus-roq-theme-default-2.1.0.BETA2.jar!theme-layouts/roq-default/default.html
		String template = "---\r\n" + //
				"layout: :theme/default\r\n" + //
				"---\r\n";
		testCodeLensFor(template, //
				cl(r(0, 0, 0, 0), "site : Site", ""), //
				cl(r(0, 0, 0, 0), "page : NormalPage", ""), //
				cl(r(0, 0, 0, 0), "Included by:", ""), //
				cl(r(0, 0, 0, 0), "theme-layouts/roq-default/default", ""));
	}

	// Theme layout

	@Test
	public void themeLayout() throws Exception {
		// quarkus-roq-theme-default-2.1.0.BETA2.jar!theme-layouts/roq-default/default.html
		String template = "---\r\n" + //
				"theme-layout: default\r\n" + //
				"---\r\n";
		testCodeLensFor(template, //
				cl(r(0, 0, 0, 0), "site : Site", ""), //
				cl(r(0, 0, 0, 0), "page : NormalPage", ""), //
				cl(r(0, 0, 0, 0), "Included by:", ""), //
				cl(r(0, 0, 0, 0), "theme-layouts/roq-default/default", ""));
	}

	public static void testCodeLensFor(String value, CodeLens... expected) throws Exception {
		CodeLensParameters p = new CodeLensParameters();
		p.setFileUri(QuteAssert.FILE_URI);
		p.setProjectUri(RoqProject.PROJECT_URI);
		p.setInjectionDetectors(Collections.singletonList(new YamlFrontMatterDetector()));
		QuteAssert.testCodeLensFor(value, p, expected);
	}

}
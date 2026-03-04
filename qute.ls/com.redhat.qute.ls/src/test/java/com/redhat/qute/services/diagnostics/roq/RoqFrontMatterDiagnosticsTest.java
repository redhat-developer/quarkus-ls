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
package com.redhat.qute.services.diagnostics.roq;

import static com.redhat.qute.QuteAssert.d;

import java.util.Collections;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import com.redhat.qute.DiagnosticsParameters;
import com.redhat.qute.QuteAssert;
import com.redhat.qute.project.extensions.roq.frontmatter.YamlFrontMatterDetector;
import com.redhat.qute.project.extensions.roq.frontmatter.YamlFrontMatterErrorCode;
import com.redhat.qute.project.roq.RoqProject;

/**
 * Test diagnostics with Roq Quarkus extension and data files.
 *
 * @author Angelo ZERR
 *
 */
public class RoqFrontMatterDiagnosticsTest {

	// Layout validation

	@Test
	public void validLayout() throws Exception {
		String template = "---\r\n" + //
				"layout: default\r\n" + //
				"title: My title\r\n" + //
				"---";
		testDiagnosticsFor(template);
	}

	@Test
	public void layoutNotFound() throws Exception {
		String template = "---\r\n" + //
				"layout: defaultXXXX\r\n" + //
				"title: My title\r\n" + //
				"---";
		testDiagnosticsFor(template, //
				d(1, 8, 1, 19, YamlFrontMatterErrorCode.LayoutNotFound, "Layout not found: `defaultXXXX`.", //
						"qute", DiagnosticSeverity.Warning));
	}

	@Test
	public void validLayoutTheme() throws Exception {
		String template = "---\r\n" + //
				"layout: :theme/default\r\n" + //
				"title: My title\r\n" + //
				"---";
		testDiagnosticsFor(template);
	}

	@Test
	public void layoutThemeNotFound() throws Exception {
		String template = "---\r\n" + //
				"layout: :theme/defaultXXXX\r\n" + //
				"title: My title\r\n" + //
				"---";
		testDiagnosticsFor(template, //
				d(1, 8, 1, 26, YamlFrontMatterErrorCode.LayoutNotFound, "Layout not found: `:theme/defaultXXXX`.", //
						"qute", DiagnosticSeverity.Warning));
	}

	// Image validation

	@Test
	public void validImage() throws Exception {
		String template = "---\r\n" + //
				"image: ico.png\r\n" + //
				"title: My title\r\n" + //
				"---";
		testDiagnosticsFor(template);
	}

	@Test
	public void imageNotFound() throws Exception {
		String template = "---\r\n" + //
				"image: invalid.png\r\n" + //
				"title: My title\r\n" + //
				"---";
		testDiagnosticsFor(template, //
				d(1, 7, 1, 18, YamlFrontMatterErrorCode.ImageNotFound, "Image not found: `invalid.png`.", //
						"qute", DiagnosticSeverity.Warning));
	}

	@Test
	@EnabledOnOs({ OS.WINDOWS })
	public void invalidImagePath() throws Exception {
		String template = "---\r\n" + //
				"image: inva:lid.png\r\n" + //
				"title: My title\r\n" + //
				"---";
		testDiagnosticsFor(template, //
				d(1, 7, 1, 19, YamlFrontMatterErrorCode.InvalidImagePath, //
						"Invalid image path `inva:lid.png: `Illegal char <:> at index 4: inva:lid.png`.", //
						"qute", DiagnosticSeverity.Warning));
	}

	private static void testDiagnosticsFor(String value, Diagnostic... expected) {
		DiagnosticsParameters p = new DiagnosticsParameters();
		p.setProjectUri(RoqProject.PROJECT_URI);
		p.setInjectionDetectors(Collections.singletonList(new YamlFrontMatterDetector()));
		p.setFilter(false);
		QuteAssert.testDiagnosticsFor(value, p, expected);
	}

}

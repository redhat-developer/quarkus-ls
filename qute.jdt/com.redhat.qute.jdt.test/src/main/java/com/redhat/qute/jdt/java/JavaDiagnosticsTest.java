/*******************************************************************************
* Copyright (c) 2021 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.jdt.java;

import static com.redhat.qute.jdt.QuteProjectTest.getJDTUtils;
import static com.redhat.qute.jdt.QuteProjectTest.loadMavenProject;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.Range;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.redhat.qute.commons.QuteJavaDiagnosticsParams;
import com.redhat.qute.jdt.QuteSupportForJava;
import com.redhat.qute.jdt.QuteProjectTest.QuteMavenProjectName;
import com.redhat.qute.jdt.internal.java.QuteErrorCode;

/**
 * Tests for Qute @CheckedTemplate support validation inside Java files.
 * 
 * @author Angelo ZERR
 *
 */
public class JavaDiagnosticsTest {

	private static final Logger LOGGER = Logger.getLogger(JavaDiagnosticsTest.class.getSimpleName());
	private static Level oldLevel;

	@BeforeClass
	public static void setUp() {
		oldLevel = LOGGER.getLevel();
		LOGGER.setLevel(Level.INFO);
	}

	@AfterClass
	public static void tearDown() {
		LOGGER.setLevel(oldLevel);
	}

	@Test
	public void templateField() throws CoreException, Exception {
		// public class HelloResource {
		// Template hello;
		//
		// Template goodbye;
		//
		// @Location("detail/items2_v1.html")
		// Template hallo;

		IJavaProject javaProject = loadMavenProject(QuteMavenProjectName.qute_quickstart);

		QuteJavaDiagnosticsParams params = new QuteJavaDiagnosticsParams();
		IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/org/acme/qute/HelloResource.java"));
		params.setUris(Arrays.asList(javaFile.getLocation().toFile().toURI().toString()));

		List<PublishDiagnosticsParams> publishDiagnostics = QuteSupportForJava.getInstance().diagnostics(params,
				getJDTUtils(), new NullProgressMonitor());
		assertEquals(1, publishDiagnostics.size());

		List<Diagnostic> diagnostics = publishDiagnostics.get(0).getDiagnostics();
		assertEquals(2, diagnostics.size());

		assertDiagnostic(diagnostics, //
				new Diagnostic(r(20, 10, 20, 17),
						"No template matching the path goodbye could be found for: org.acme.qute.HelloResource",
						DiagnosticSeverity.Error, "qute", QuteErrorCode.NoMatchingTemplate.name()), //
				new Diagnostic(r(24, 10, 24, 15),
						"No template matching the path detail/items2_v1.html could be found for: org.acme.qute.HelloResource",
						DiagnosticSeverity.Error, "qute", QuteErrorCode.NoMatchingTemplate.name()));
	}

	@Test
	public void checkedTemplate() throws CoreException, Exception {
		// @CheckedTemplate
		// public class Templates {
		//
		// public static native TemplateInstance hello2(String name);
		//
		// public static native TemplateInstance hello3(String name);
		IJavaProject javaProject = loadMavenProject(QuteMavenProjectName.qute_quickstart);

		QuteJavaDiagnosticsParams params = new QuteJavaDiagnosticsParams();
		IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/org/acme/qute/Templates.java"));
		params.setUris(Arrays.asList(javaFile.getLocation().toFile().toURI().toString()));

		List<PublishDiagnosticsParams> publishDiagnostics = QuteSupportForJava.getInstance().diagnostics(params,
				getJDTUtils(), new NullProgressMonitor());
		assertEquals(1, publishDiagnostics.size());

		List<Diagnostic> diagnostics = publishDiagnostics.get(0).getDiagnostics();
		assertEquals(1, diagnostics.size());

		assertDiagnostic(diagnostics, //
				new Diagnostic(r(9, 42, 9, 48),
						"No template matching the path hello3 could be found for: org.acme.qute.Templates",
						DiagnosticSeverity.Error, "qute", QuteErrorCode.NoMatchingTemplate.name()));
	}

	@Test
	public void checkedTemplateInInnerClass() throws CoreException, Exception {
		// public class ItemResource {
		// @CheckedTemplate
		// static class Templates {
		// [Open `src/main/resources/templates/ItemResource/items.qute.html`]
		// static native TemplateInstance items(List<Item> items);

		IJavaProject javaProject = loadMavenProject(QuteMavenProjectName.qute_quickstart);

		QuteJavaDiagnosticsParams params = new QuteJavaDiagnosticsParams();
		IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/org/acme/qute/ItemResource.java"));
		params.setUris(Arrays.asList(javaFile.getLocation().toFile().toURI().toString()));

		List<PublishDiagnosticsParams> publishDiagnostics = QuteSupportForJava.getInstance().diagnostics(params,
				getJDTUtils(), new NullProgressMonitor());
		assertEquals(1, publishDiagnostics.size());

		List<Diagnostic> diagnostics = publishDiagnostics.get(0).getDiagnostics();
		assertEquals(1, diagnostics.size());

		assertDiagnostic(diagnostics, //
				new Diagnostic(r(25, 33, 25, 39),
						"No template matching the path ItemResource/items2 could be found for: org.acme.qute.ItemResource$Templates2",
						DiagnosticSeverity.Error, "qute", QuteErrorCode.NoMatchingTemplate.name()));
	}

	public static Range r(int line, int startChar, int endChar) {
		return r(line, startChar, line, endChar);
	}

	public static Range r(int startLine, int startChar, int endLine, int endChar) {
		Position start = new Position(startLine, startChar);
		Position end = new Position(endLine, endChar);
		return new Range(start, end);
	}

	public static Diagnostic d(Range range, String message, String source, DiagnosticSeverity severity) {
		return new Diagnostic(range, message, severity, "qute");
	}

	public static void assertDiagnostic(List<? extends Diagnostic> actual, Diagnostic... expected) {
		assertEquals(expected.length, actual.size());
		for (int i = 0; i < expected.length; i++) {
			assertEquals(expected[i].getRange(), actual.get(i).getRange());
			assertEquals(expected[i].getMessage(), actual.get(i).getMessage());
			assertEquals(expected[i].getData(), actual.get(i).getData());
		}
	}

}

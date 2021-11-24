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

import static com.redhat.qute.jdt.internal.QuteProjectTest.getJDTUtils;
import static com.redhat.qute.jdt.internal.QuteProjectTest.loadMavenProject;
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
import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.redhat.qute.commons.QuteJavaCodeLensParams;
import com.redhat.qute.jdt.QuteSupportForJava;
import com.redhat.qute.jdt.internal.QuteProjectTest.QuteMavenProjectName;

/**
 * Tests for Qute @CheckedTemplate support code lens inside Java files.
 *  
 * @author Angelo ZERR
 *
 */
public class JavaCodeLensTest {

	private static final Logger LOGGER = Logger.getLogger(JavaCodeLensTest.class.getSimpleName());
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
		// [Open `src/main/resources/templates/hello.qute.html`]
		// Template hello;
		IJavaProject javaProject = loadMavenProject(QuteMavenProjectName.qute_quickstart);

		QuteJavaCodeLensParams params = new QuteJavaCodeLensParams();
		IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/org/acme/qute/HelloResource.java"));
		params.setUri(javaFile.getLocation().toFile().toURI().toString());

		List<? extends CodeLens> lenses = QuteSupportForJava.getInstance().codeLens(params, getJDTUtils(),
				new NullProgressMonitor());
		assertEquals(1, lenses.size());

		String templateFileUri = javaProject.getProject().getFile("src/main/resources/templates/hello.qute.html")
				.getLocationURI().toString();

		assertCodeLens(lenses, //
				cl(r(15, 4, 16, 19), //
						"Open `src/main/resources/templates/hello.qute.html`", //
						"qute.command.open.uri", Arrays.asList(templateFileUri)));
	}

	@Test
	public void checkedTemplate() throws CoreException, Exception {
		// @CheckedTemplate
		// public class Templates {
		// [Open `src/main/resources/templates/hello2.qute.html`]
		// public static native TemplateInstance hello2(String name);
		// [Open `src/main/resources/templates/hello3.qute.html`]
		// public static native TemplateInstance hello3(String name);
		IJavaProject javaProject = loadMavenProject(QuteMavenProjectName.qute_quickstart);

		QuteJavaCodeLensParams params = new QuteJavaCodeLensParams();
		IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/org/acme/qute/Templates.java"));
		params.setUri(javaFile.getLocation().toFile().toURI().toString());

		List<? extends CodeLens> lenses = QuteSupportForJava.getInstance().codeLens(params, getJDTUtils(),
				new NullProgressMonitor());
		assertEquals(2, lenses.size());

		String hello2FileUri = javaProject.getProject().getFile("src/main/resources/templates/hello2.qute.html")
				.getLocationURI().toString();
		String hello3FileUri1 = javaProject.getProject().getFile("src/main/resources/templates/hello3.qute.html")
				.getLocationURI().toString();

		assertCodeLens(lenses, //
				cl(r(8, 1, 8, 59), //
						"Open `src/main/resources/templates/hello2.qute.html`", //
						"qute.command.open.uri", Arrays.asList(hello2FileUri)), //
				cl(r(9, 4, 9, 62), //
						"Create `src/main/resources/templates/hello3.qute.html`", //
						"qute.command.generate.template.file", Arrays.asList(hello3FileUri1)));
	}

	@Test
	public void checkedTemplateInInnerClass() throws CoreException, Exception {
		// public class ItemResource {
		// @CheckedTemplate
		// static class Templates {
		// [Open `src/main/resources/templates/ItemResource/items.qute.html`]
		// static native TemplateInstance items(List<Item> items);

		// static class Templates2 {
		// [Create `src/main/resources/templates/ItemResource/items2.qute.html`]
		// static native TemplateInstance items2(List<Item> items);

		IJavaProject javaProject = loadMavenProject(QuteMavenProjectName.qute_quickstart);

		QuteJavaCodeLensParams params = new QuteJavaCodeLensParams();
		IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/org/acme/qute/ItemResource.java"));
		params.setUri(javaFile.getLocation().toFile().toURI().toString());

		List<? extends CodeLens> lenses = QuteSupportForJava.getInstance().codeLens(params, getJDTUtils(),
				new NullProgressMonitor());
		assertEquals(2, lenses.size());

		String templateFileUri = javaProject.getProject()
				.getFile("src/main/resources/templates/ItemResource/items.qute.html").getLocationURI().toString();

		assertCodeLens(lenses, //
				cl(r(20, 2, 20, 57), //
						"Open `src/main/resources/templates/ItemResource/items.qute.html`", //
						"qute.command.open.uri", Arrays.asList(templateFileUri)), //
				cl(r(25, 2, 25, 58), //
						"Create `src/main/resources/templates/ItemResource/items2.qute.html`", //
						"qute.command.generate.template.file", Arrays.asList(templateFileUri)));
	}

	public static Range r(int line, int startChar, int endChar) {
		return r(line, startChar, line, endChar);
	}

	public static Range r(int startLine, int startChar, int endLine, int endChar) {
		Position start = new Position(startLine, startChar);
		Position end = new Position(endLine, endChar);
		return new Range(start, end);
	}

	public static CodeLens cl(Range range, String title, String command, List<Object> arguments) {
		return new CodeLens(range, new Command(title, command, arguments), null);
	}

	public static void assertCodeLens(List<? extends CodeLens> actual, CodeLens... expected) {
		assertEquals(expected.length, actual.size());
		for (int i = 0; i < expected.length; i++) {
			assertEquals(expected[i].getRange(), actual.get(i).getRange());
			Command expectedCommand = expected[i].getCommand();
			Command actualCommand = actual.get(i).getCommand();
			if (expectedCommand != null && actualCommand != null) {
				assertEquals(expectedCommand.getTitle(), actualCommand.getTitle());
				assertEquals(expectedCommand.getCommand(), actualCommand.getCommand());
			}
			assertEquals(expected[i].getData(), actual.get(i).getData());
		}
	}

}

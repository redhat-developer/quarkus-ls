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
import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.redhat.qute.commons.QuteJavaCodeLensParams;
import com.redhat.qute.jdt.QuteProjectTest.QuteMavenProjectName;
import com.redhat.qute.jdt.QuteSupportForJava;

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

		// [Create `src/main/resources/templates/goodbye.qute.html`]
		// Template goodbye;

		// [Create `src/main/resources/templates/detail/items2_v1.html`]
		// @Location("detail/items2_v1.html")
		// Template hallo;
		//
		// [Open `src/main/resources/templates/detail/page1.html`]
		// Template bonjour;
		//
		// [Create `src/main/resources/templates/detail/page2.html`]
		// Template aurevoir;
		//
		// public HelloResource(@Location("detail/page1.html") Template page1,
		// @Location("detail/page2.html") Template page2) {
		// this.bonjour = page1;
		// this.aurevoir = requireNonNull(page2, "page is required");
		// }

		IJavaProject javaProject = loadMavenProject(QuteMavenProjectName.qute_quickstart);

		QuteJavaCodeLensParams params = new QuteJavaCodeLensParams();
		IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/org/acme/qute/HelloResource.java"));
		params.setUri(javaFile.getLocation().toFile().toURI().toString());

		List<? extends CodeLens> lenses = QuteSupportForJava.getInstance().codeLens(params, getJDTUtils(),
				new NullProgressMonitor());
		assertEquals(5, lenses.size());

		String helloTemplateFileUri = javaProject.getProject().getFile("src/main/resources/templates/hello.qute.html")
				.getLocationURI().toString();
		String goodbyeTemplateFileUri = javaProject.getProject()
				.getFile("src/main/resources/templates/goodbye.qute.html").getLocationURI().toString();
		String halloTemplateFileUri = javaProject.getProject()
				.getFile("src/main/resources/templates/detail/items2_v1.html").getLocationURI().toString();
		String bonjourTemplateFileUri = javaProject.getProject()
				.getFile("src/main/resources/templates/detail/page1.html").getLocationURI().toString();
		String aurevoirTemplateFileUri = javaProject.getProject()
				.getFile("src/main/resources/templates/detail/page2.html").getLocationURI().toString();

		assertCodeLens(lenses, //
				cl(r(16, 1, 17, 16), //
						"Open `src/main/resources/templates/hello.qute.html`", //
						"qute.command.open.uri", Arrays.asList(helloTemplateFileUri)), //
				cl(r(19, 1, 20, 18), //
						"Create `src/main/resources/templates/goodbye.html`", //
						"qute.command.generate.template.file", Arrays.asList(goodbyeTemplateFileUri)), //
				cl(r(22, 1, 24, 16), //
						"Create `src/main/resources/templates/detail/items2_v1.html`", //
						"qute.command.generate.template.file", Arrays.asList(halloTemplateFileUri)), //
				cl(r(26, 1, 27, 18), //
						"Open `src/main/resources/templates/detail/page1.html`", //
						"qute.command.open.uri", Arrays.asList(bonjourTemplateFileUri)), //
				cl(r(29, 1, 30, 19), //
						"Create `src/main/resources/templates/detail/page2.html`", //
						"qute.command.generate.template.file", Arrays.asList(aurevoirTemplateFileUri)));
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

		String goodbyeFileUri = javaProject.getProject().getFile("src/main/resources/templates/hello2.qute.html")
				.getLocationURI().toString();
		String hello3FileUri1 = javaProject.getProject().getFile("src/main/resources/templates/hello3.qute.html")
				.getLocationURI().toString();

		assertCodeLens(lenses, //
				cl(r(8, 1, 8, 59), //
						"Open `src/main/resources/templates/hello2.qute.html`", //
						"qute.command.open.uri", Arrays.asList(goodbyeFileUri)), //
				cl(r(9, 4, 9, 62), //
						"Create `src/main/resources/templates/hello3.html`", //
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
		assertEquals(3, lenses.size());

		String itemsUri = javaProject.getProject().getFile("src/main/resources/templates/ItemResource/items.qute.html")
				.getLocationURI().toString();
		String mapUri = javaProject.getProject().getFile("src/main/resources/templates/ItemResource/map.html")
				.getLocationURI().toString();
		String items2Uri = javaProject.getProject().getFile("src/main/resources/templates/ItemResource/items2.html")
				.getLocationURI().toString();

		assertCodeLens(lenses, //
				cl(r(21, 2, 21, 57), //
						"Open `src/main/resources/templates/ItemResource/items.qute.html`", //
						"qute.command.open.uri", Arrays.asList(itemsUri)), //
				cl(r(23, 2, 23, 102), //
						"Create `src/main/resources/templates/ItemResource/map.html`", //
						"qute.command.generate.template.file", Arrays.asList(mapUri)), //
				cl(r(28, 2, 28, 58), //
						"Create `src/main/resources/templates/ItemResource/items2.html`", //
						"qute.command.generate.template.file", Arrays.asList(items2Uri)));
	}

	@Test
	public void checkedTemplateWithCustomBasePath() throws Exception {

		// @CheckedTemplate(basePath="ItemResourceWithFragment")
		// public class ItemTemplatesCustomBasePath {
		//
		// static native TemplateInstance items(List<Item> items);
		// static native TemplateInstance items$id1(List<Item> items);
		// static native TemplateInstance items3$id2(List<Item> items);
		// static native TemplateInstance items3$(List<Item> items);
		// }

		IJavaProject javaProject = loadMavenProject(QuteMavenProjectName.qute_quickstart);

		QuteJavaCodeLensParams params = new QuteJavaCodeLensParams();
		IFile javaFile = javaProject.getProject()
				.getFile(new Path("src/main/java/org/acme/qute/ItemTemplatesCustomBasePath.java"));
		params.setUri(javaFile.getLocation().toFile().toURI().toString());

		List<? extends CodeLens> lenses = QuteSupportForJava.getInstance().codeLens(params, getJDTUtils(),
				new NullProgressMonitor());
		assertEquals(3, lenses.size());

		String itemsUri = javaProject.getProject()
				.getFile("src/main/resources/templates/ItemResourceWithFragment/items.html").getLocationURI()
				.toString();
		String items3Uri = javaProject.getProject()
				.getFile("src/main/resources/templates/ItemResourceWithFragment/items3.html").getLocationURI()
				.toString();

		assertCodeLens(lenses, //
				cl(r(9, 1, 9, 56), //
						"Open `src/main/resources/templates/ItemResourceWithFragment/items.html`", //
						"qute.command.open.uri", Arrays.asList(itemsUri)), //
				cl(r(10, 1, 10, 60), //
						"Open `id1` fragment of `src/main/resources/templates/ItemResourceWithFragment/items.html`", //
						"qute.command.open.uri", Arrays.asList(itemsUri, "id1")), //
				cl(r(11, 1, 11, 61), //
						"Create `src/main/resources/templates/ItemResourceWithFragment/items3.html`", //
						"qute.command.generate.template.file", Arrays.asList(items3Uri)));

	}

	@Test
	public void checkedTemplateInInnerClassWithCustomBasePath() throws Exception {

		// @CheckedTemplate(basePath="ItemResourceWithFragment")
		// public class ItemTemplatesCustomBasePath {
		//
		// static native TemplateInstance items(List<Item> items);
		// static native TemplateInstance items$id1(List<Item> items);
		// static native TemplateInstance items3$id2(List<Item> items);
		// static native TemplateInstance items3$(List<Item> items);
		// }

		IJavaProject javaProject = loadMavenProject(QuteMavenProjectName.qute_quickstart);

		QuteJavaCodeLensParams params = new QuteJavaCodeLensParams();
		IFile javaFile = javaProject.getProject()
				.getFile(new Path("src/main/java/org/acme/qute/ItemTemplatesCustomBasePath.java"));
		params.setUri(javaFile.getLocation().toFile().toURI().toString());

		List<? extends CodeLens> lenses = QuteSupportForJava.getInstance().codeLens(params, getJDTUtils(),
				new NullProgressMonitor());
		assertEquals(3, lenses.size());

		String itemsUri = javaProject.getProject()
				.getFile("src/main/resources/templates/ItemResourceWithFragment/items.html").getLocationURI()
				.toString();
		String items3Uri = javaProject.getProject()
				.getFile("src/main/resources/templates/ItemResourceWithFragment/items3.html").getLocationURI()
				.toString();

		assertCodeLens(lenses, //
				cl(r(9, 1, 9, 56), //
						"Open `src/main/resources/templates/ItemResourceWithFragment/items.html`", //
						"qute.command.open.uri", Arrays.asList(itemsUri)), //
				cl(r(10, 1, 10, 60), //
						"Open `id1` fragment of `src/main/resources/templates/ItemResourceWithFragment/items.html`", //
						"qute.command.open.uri", Arrays.asList(itemsUri, "id1")), //
				cl(r(11, 1, 11, 61), //
						"Create `src/main/resources/templates/ItemResourceWithFragment/items3.html`", //
						"qute.command.generate.template.file", Arrays.asList(items3Uri)));

	}

	@Test
	public void checkedTemplateWithFragment() throws CoreException, Exception {

		IJavaProject javaProject = loadMavenProject(QuteMavenProjectName.qute_quickstart);

		QuteJavaCodeLensParams params = new QuteJavaCodeLensParams();
		IFile javaFile = javaProject.getProject()
				.getFile(new Path("src/main/java/org/acme/qute/ItemResourceWithFragment.java"));
		params.setUri(javaFile.getLocation().toFile().toURI().toString());

		List<? extends CodeLens> lenses = QuteSupportForJava.getInstance().codeLens(params, getJDTUtils(),
				new NullProgressMonitor());
		assertEquals(6, lenses.size());

		String itemsUri = javaProject.getProject()
				.getFile("src/main/resources/templates/ItemResourceWithFragment/items.html").getLocationURI()
				.toString();
		String items3Uri = javaProject.getProject()
				.getFile("src/main/resources/templates/ItemResourceWithFragment/items3.html").getLocationURI()
				.toString();
		String items2Uri = javaProject.getProject()
				.getFile("src/main/resources/templates/ItemResourceWithFragment/items2.html").getLocationURI()
				.toString();
		String items2Uri_id1 = javaProject.getProject()
				.getFile("src/main/resources/templates/ItemResourceWithFragment/items2$id1.html").getLocationURI()
				.toString();
		String items2Uri_id2 = javaProject.getProject()
				.getFile("src/main/resources/templates/ItemResourceWithFragment/items2$id2.html").getLocationURI()
				.toString();

		assertCodeLens(lenses, //
				cl(r(21, 2, 21, 57), //
						"Open `src/main/resources/templates/ItemResourceWithFragment/items.html`", //
						"qute.command.open.uri", Arrays.asList(itemsUri)), //
				cl(r(22, 2, 22, 61), //
						"Open `id1` fragment of `src/main/resources/templates/ItemResourceWithFragment/items.html`", //
						"qute.command.open.uri", Arrays.asList(itemsUri, "id1")), //
				cl(r(23, 2, 23, 62), //
						"Create `src/main/resources/templates/ItemResourceWithFragment/items3.html`", //
						"qute.command.generate.template.file", Arrays.asList(items3Uri)), //

				cl(r(29, 2, 29, 58), //
						"Open `src/main/resources/templates/ItemResourceWithFragment/items2.html`", //
						"qute.command.open.uri", Arrays.asList(items2Uri)), //
				cl(r(30, 2, 30, 62), //
						"Open `src/main/resources/templates/ItemResourceWithFragment/items2$id1.html`", //
						"qute.command.open.uri", Arrays.asList(items2Uri_id1)), //
				cl(r(31, 2, 31, 62), //
						"Create `src/main/resources/templates/ItemResourceWithFragment/items2$id2.html`", //
						"qute.command.generate.template.file", Arrays.asList(items2Uri_id2)));
	}

	@Test
	public void templateRecord() throws CoreException, Exception {
		// public class HelloResource {

		// record Hello(String name) implements TemplateInstance {}

		// record Bonjour(String name) implements TemplateInstance {}

		// record Status() {}

		IJavaProject javaProject = loadMavenProject(QuteMavenProjectName.qute_record);

		QuteJavaCodeLensParams params = new QuteJavaCodeLensParams();
		IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/org/acme/sample/HelloResource.java"));
		params.setUri(javaFile.getLocation().toFile().toURI().toString());

		List<? extends CodeLens> lenses = QuteSupportForJava.getInstance().codeLens(params, getJDTUtils(),
				new NullProgressMonitor());
		assertEquals(2, lenses.size());

		String helloFileUri = javaProject.getProject().getFile("src/main/resources/templates/Hello.html")
				.getLocationURI().toString();
		String boujourFileUri1 = javaProject.getProject().getFile("src/main/resources/templates/Bonjour.html")
				.getLocationURI().toString();

		assertCodeLens(lenses, //
				cl(r(14, 4, 14, 60), //
						"Open `src/main/resources/templates/Hello.html`", //
						"qute.command.open.uri", Arrays.asList(helloFileUri)), //
				cl(r(16, 4, 16, 62), //
						"Create `src/main/resources/templates/Bonjour.html`", //
						"qute.command.generate.template.file", Arrays.asList(boujourFileUri1)));
	}

	@Test
	public void checkedTemplateWithDefaultName() throws CoreException, Exception {
		// @CheckedTemplate(defaultName=CheckedTemplate.HYPHENATED_ELEMENT_NAME)
		// static class Templates {
		// static native TemplateInstance HelloWorld(String name);

		IJavaProject javaProject = loadMavenProject(QuteMavenProjectName.qute_record);

		QuteJavaCodeLensParams params = new QuteJavaCodeLensParams();
		IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/org/acme/sample/ItemResource.java"));
		params.setUri(javaFile.getLocation().toFile().toURI().toString());

		List<? extends CodeLens> lenses = QuteSupportForJava.getInstance().codeLens(params, getJDTUtils(),
				new NullProgressMonitor());
		assertEquals(3, lenses.size());

		String helloFileUri = javaProject.getProject()
				.getFile("src/main/resources/templates/ItemResource/HelloWorld.html").getLocationURI().toString();
		String hello2FileUri = javaProject.getProject()
				.getFile("src/main/resources/templates/ItemResource/hello-world.html").getLocationURI().toString();
		String hello3FileUri = javaProject.getProject()
				.getFile("src/main/resources/templates/ItemResource/hello_world.html").getLocationURI().toString();

		assertCodeLens(lenses, //
				cl(r(19, 2, 19, 57), //
						"Create `src/main/resources/templates/ItemResource/HelloWorld.html`", //
						"qute.command.generate.template.file", Arrays.asList(helloFileUri)), //
				cl(r(25, 2, 25, 57), //
						"Create `src/main/resources/templates/ItemResource/hello-world.html`", //
						"qute.command.generate.template.file", Arrays.asList(hello2FileUri)), //
				cl(r(31, 2, 31, 57), //
						"Create `src/main/resources/templates/ItemResource/hello_world.html`", //
						"qute.command.generate.template.file", Arrays.asList(hello3FileUri)));
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

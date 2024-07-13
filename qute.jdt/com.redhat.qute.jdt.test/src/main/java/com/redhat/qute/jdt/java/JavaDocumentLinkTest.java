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

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.lsp4j.DocumentLink;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.redhat.qute.commons.QuteJavaDocumentLinkParams;
import com.redhat.qute.jdt.QuteProjectTest.QuteMavenProjectName;
import com.redhat.qute.jdt.QuteSupportForJava;

/**
 * Tests for Qute @CheckedTemplate support document link inside Java files.
 * 
 * @author Angelo ZERR
 *
 */
public class JavaDocumentLinkTest {

	private static final Logger LOGGER = Logger.getLogger(JavaDocumentLinkTest.class.getSimpleName());
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

		QuteJavaDocumentLinkParams params = new QuteJavaDocumentLinkParams();
		IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/org/acme/qute/HelloResource.java"));
		params.setUri(javaFile.getLocation().toFile().toURI().toString());

		List<DocumentLink> links = QuteSupportForJava.getInstance().documentLink(params, getJDTUtils(),
				new NullProgressMonitor());
		assertEquals(5, links.size());

		String helloTemplateUri = javaProject.getProject().getFile("src/main/resources/templates/hello.qute.html")
				.getLocationURI().toString();
		String goodbyeTemplateUri = javaProject.getProject().getFile("src/main/resources/templates/hello2.qute.html")
				.getLocationURI().toString();
		String halloTemplateUri = javaProject.getProject().getFile("src/main/resources/templates/detail/items2_v1.html")
				.getLocationURI().toString();
		String bonjourTemplateFileUri = javaProject.getProject()
				.getFile("src/main/resources/templates/detail/page1.html").getLocationURI().toString();
		String aurevoirTemplateFileUri = javaProject.getProject()
				.getFile("src/main/resources/templates/detail/page2.html").getLocationURI().toString();

		assertDocumentLink(links, //
				dl(r(17, 10, 17, 15), //
						helloTemplateUri, "Open `src/main/resources/templates/hello.qute.html`"),
				dl(r(20, 10, 20, 17), //
						goodbyeTemplateUri, "Open `src/main/resources/templates/goodbye.qute.html`"), //
				dl(r(22, 11, 22, 34), //
						halloTemplateUri, "Open `src/main/resources/templates/detail/items2_v1.html`"), //
				dl(r(32, 32, 32, 51), //
						bonjourTemplateFileUri, "Open `src/main/resources/templates/detail/page1.html`"), //
				dl(r(32, 79, 32, 98), //
						aurevoirTemplateFileUri, "Create `src/main/resources/templates/detail/page2.html`"));
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

		QuteJavaDocumentLinkParams params = new QuteJavaDocumentLinkParams();
		IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/org/acme/qute/Templates.java"));
		params.setUri(javaFile.getLocation().toFile().toURI().toString());

		List<DocumentLink> links = QuteSupportForJava.getInstance().documentLink(params, getJDTUtils(),
				new NullProgressMonitor());
		assertEquals(2, links.size());

		String hello2FileUri = javaProject.getProject().getFile("src/main/resources/templates/hello2.qute.html")
				.getLocationURI().toString();
		String hello3FileUri1 = javaProject.getProject().getFile("src/main/resources/templates/hello3.qute.html")
				.getLocationURI().toString();

		assertDocumentLink(links, //
				dl(r(8, 39, 8, 45), //
						hello2FileUri, "Open `src/main/resources/templates/hello2.qute.html`"), //
				dl(r(9, 42, 9, 48), //
						hello3FileUri1, "Create `src/main/resources/templates/hello3.qute.html`"));
	}

	@Test
	public void checkedTemplateInInnerClass() throws CoreException, Exception {
		// public class ItemResource {
		// @CheckedTemplate
		// static class Templates {
		//
		// static native TemplateInstance items(List<Item> items);

		// static class Templates2 {
		//
		// static native TemplateInstance items2(List<Item> items);

		IJavaProject javaProject = loadMavenProject(QuteMavenProjectName.qute_quickstart);

		QuteJavaDocumentLinkParams params = new QuteJavaDocumentLinkParams();
		IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/org/acme/qute/ItemResource.java"));
		params.setUri(javaFile.getLocation().toFile().toURI().toString());

		List<DocumentLink> links = QuteSupportForJava.getInstance().documentLink(params, getJDTUtils(),
				new NullProgressMonitor());
		assertEquals(3, links.size());

		String templateFileUri = javaProject.getProject()
				.getFile("src/main/resources/templates/ItemResource/items.qute.html").getLocationURI().toString();

		assertDocumentLink(links, //
				dl(r(21, 33, 21, 38), //
						templateFileUri, "Open `src/main/resources/templates/ItemResource/items.qute.html`"), //
				dl(r(23, 33, 23, 36), //
						templateFileUri, "Create `src/main/resources/templates/ItemResource/map.qute.html`"), //
				dl(r(28, 33, 28, 39), //
						templateFileUri, "Create `src/main/resources/templates/ItemResource/items2.qute.html`"));
	}

	@Test
	public void checkedTemplateWithFragment() throws CoreException, Exception {

		IJavaProject javaProject = loadMavenProject(QuteMavenProjectName.qute_quickstart);

		QuteJavaDocumentLinkParams params = new QuteJavaDocumentLinkParams();
		IFile javaFile = javaProject.getProject()
				.getFile(new Path("src/main/java/org/acme/qute/ItemResourceWithFragment.java"));
		params.setUri(javaFile.getLocation().toFile().toURI().toString());

		List<DocumentLink> links = QuteSupportForJava.getInstance().documentLink(params, getJDTUtils(),
				new NullProgressMonitor());
		assertEquals(6, links.size());

		String templateFileUri = javaProject.getProject()
				.getFile("src/main/resources/templates/ItemResourceWithFragment/items.html").getLocationURI()
				.toString();

		assertDocumentLink(links, //
				dl(r(21, 33, 21, 38), //
						templateFileUri, "Open `src/main/resources/templates/ItemResourceWithFragment/items.html`"), //
				dl(r(22, 33, 22, 42), //
						templateFileUri, "Open `src/main/resources/templates/ItemResourceWithFragment/items.html`"), //
				dl(r(23, 33, 23, 43), //
						templateFileUri, "Create `src/main/resources/templates/ItemResourceWithFragment/items3.html`"), //
				dl(r(29, 33, 29, 39), //
						templateFileUri, "Open `src/main/resources/templates/ItemResourceWithFragment/items2.html`"), //
				dl(r(30, 33, 30, 43), //
						templateFileUri,
						"Open `src/main/resources/templates/ItemResourceWithFragment/items2$id1.html`"), //
				dl(r(31, 33, 31, 43), //
						templateFileUri,
						"Open `src/main/resources/templates/ItemResourceWithFragment/items2$id2.html`"));
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

		QuteJavaDocumentLinkParams params = new QuteJavaDocumentLinkParams();
		IFile javaFile = javaProject.getProject()
				.getFile(new Path("src/main/java/org/acme/qute/ItemTemplatesCustomBasePath.java"));
		params.setUri(javaFile.getLocation().toFile().toURI().toString());

		List<DocumentLink> links = QuteSupportForJava.getInstance().documentLink(params, getJDTUtils(),
				new NullProgressMonitor());
		assertEquals(3, links.size());

		String templateFileUri = javaProject.getProject()
				.getFile("src/main/resources/templates/ItemResourceWithFragment/items.html").getLocationURI()
				.toString();

		assertDocumentLink(links, //
				dl(r(9, 32, 9, 37), //
						templateFileUri, "Open `src/main/resources/templates/ItemResourceWithFragment/items.html`"), //
				dl(r(10, 32, 10, 41), //
						templateFileUri, "Open `src/main/resources/templates/ItemResourceWithFragment/items.html`"), //
				dl(r(11, 32, 11, 42), //
						templateFileUri, "Create `src/main/resources/templates/ItemResourceWithFragment/items3.html`"));
	}

	@Test
	public void templateRecord() throws Exception {

		// public class HelloResource {

		// record Hello(String name) implements TemplateInstance {}

		// record Bonjour(String name) implements TemplateInstance {}

		// record Status() {}

		IJavaProject javaProject = loadMavenProject(QuteMavenProjectName.qute_record);

		QuteJavaDocumentLinkParams params = new QuteJavaDocumentLinkParams();
		IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/org/acme/sample/HelloResource.java"));
		params.setUri(javaFile.getLocation().toFile().toURI().toString());

		List<DocumentLink> links = QuteSupportForJava.getInstance().documentLink(params, getJDTUtils(),
				new NullProgressMonitor());
		assertEquals(2, links.size());

		String templateFileUri = javaProject.getProject().getFile("src/main/resources/templates/Hello.html")
				.getLocationURI().toString();

		assertDocumentLink(links, //
				dl(r(14, 11, 14, 16), //
						templateFileUri, "Open `src/main/resources/templates/Hello.html`"), //
				dl(r(16, 11, 16, 18), //
						templateFileUri, "Create `src/main/resources/templates/Bonjour.html`"));
	}

	@Test
	public void checkedTemplateWithDefaultName() throws Exception {

		// @CheckedTemplate(defaultName=CheckedTemplate.HYPHENATED_ELEMENT_NAME)
		// static class Templates {
		// static native TemplateInstance HelloWorld(String name);

		IJavaProject javaProject = loadMavenProject(QuteMavenProjectName.qute_record);

		QuteJavaDocumentLinkParams params = new QuteJavaDocumentLinkParams();
		IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/org/acme/sample/ItemResource.java"));
		params.setUri(javaFile.getLocation().toFile().toURI().toString());

		List<DocumentLink> links = QuteSupportForJava.getInstance().documentLink(params, getJDTUtils(),
				new NullProgressMonitor());
		assertEquals(3, links.size());

		String helloFileUri = javaProject.getProject().getFile("src/main/resources/templates/ItemResource/HelloWorld.html")
				.getLocationURI().toString();
		String hello2FileUri = javaProject.getProject().getFile("src/main/resources/templates/ItemResource/hello-world.html")
				.getLocationURI().toString();
		String hello3FileUri = javaProject.getProject().getFile("src/main/resources/templates/ItemResource/hello_world.html")
				.getLocationURI().toString();

		assertDocumentLink(links, //
				dl(r(19, 33, 19, 43), //
						helloFileUri, "Create `src/main/resources/templates/ItemResource/HelloWorld.html`"), //
				dl(r(25, 33, 25, 43), //
						hello2FileUri, "Create `src/main/resources/templates/ItemResource/hello-world.html`"), //
				dl(r(31, 33, 31, 43), //
						hello3FileUri, "Create `src/main/resources/templates/ItemResource/hello_world.html`"));
	}

	public static Range r(int line, int startChar, int endChar) {
		return r(line, startChar, line, endChar);
	}

	public static Range r(int startLine, int startChar, int endLine, int endChar) {
		Position start = new Position(startLine, startChar);
		Position end = new Position(endLine, endChar);
		return new Range(start, end);
	}

	public static DocumentLink dl(Range range, String target, String tooltip) {
		return new DocumentLink(range, target, null, tooltip);
	}

	public static void assertDocumentLink(List<DocumentLink> actual, DocumentLink... expected) {
		assertEquals(expected.length, actual.size());
		for (int i = 0; i < expected.length; i++) {
			assertEquals(expected[i].getRange(), actual.get(i).getRange());
			assertEquals(expected[i].getData(), actual.get(i).getData());
		}
	}

}

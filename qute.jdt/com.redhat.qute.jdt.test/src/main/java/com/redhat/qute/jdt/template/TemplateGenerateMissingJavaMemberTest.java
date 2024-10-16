/*******************************************************************************
* Copyright (c) 2022 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.jdt.template;

import static com.redhat.qute.jdt.QuteProjectTest.getJDTUtils;
import static com.redhat.qute.jdt.QuteProjectTest.loadMavenProject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.ls.core.internal.JavaLanguageServerPlugin;
import org.eclipse.lsp4j.ClientCapabilities;
import org.eclipse.lsp4j.CreateFile;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.ResourceOperation;
import org.eclipse.lsp4j.ResourceOperationKind;
import org.eclipse.lsp4j.TextDocumentEdit;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.WorkspaceClientCapabilities;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.WorkspaceEditCapabilities;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.redhat.qute.commons.GenerateMissingJavaMemberParams;
import com.redhat.qute.jdt.QuteProjectTest.QuteMavenProjectName;
import com.redhat.qute.jdt.QuteSupportForTemplate;

/**
 * Integration tests for QuteSupportForTemplateGenerateMissingJavaMember
 *
 * Invokes the code in JDT to generate the WorkspaceEdit for the CodeActions
 * directly instead of going through the Qute language server.
 *
 * @author datho7561
 */
public class TemplateGenerateMissingJavaMemberTest {

	@BeforeClass
	public static void setupLS() {
		JavaLanguageServerPlugin.getPreferencesManager().initialize();
		ClientCapabilities clientCapabilities = new ClientCapabilities();
		WorkspaceClientCapabilities workspaceClientCapabilities = new WorkspaceClientCapabilities();
		WorkspaceEditCapabilities wEdit = new WorkspaceEditCapabilities();
		wEdit.setResourceOperations(Arrays.asList(ResourceOperationKind.Create, ResourceOperationKind.Delete,
				ResourceOperationKind.Rename));
		workspaceClientCapabilities.setWorkspaceEdit(wEdit);
		clientCapabilities.setWorkspace(workspaceClientCapabilities);
		JavaLanguageServerPlugin.getPreferencesManager().updateClientPrefences(clientCapabilities, new HashMap<>());
	}

	@Test
	public void generateField() throws Exception {

		IJavaProject project = loadMavenProject(QuteMavenProjectName.qute_quickstart);
		GenerateMissingJavaMemberParams params = new GenerateMissingJavaMemberParams(
				GenerateMissingJavaMemberParams.MemberType.Field, "asdf", "org.acme.qute.Item",
				QuteMavenProjectName.qute_quickstart);
		WorkspaceEdit actual = QuteSupportForTemplate.getInstance().generateMissingJavaMember(params, getJDTUtils(),
				new NullProgressMonitor());
		WorkspaceEdit expected = we(Either.forLeft(tde(project, "src/main/java/org/acme/qute/Item.java",
				te(15, 23, 15, 23, "\r\n\r\n\tpublic String asdf;"))));
		assertWorkspaceEdit(expected, actual);

	}

	@Test
	public void updateVisibilityOfFieldSimple() throws Exception {

		IJavaProject project = loadMavenProject(QuteMavenProjectName.qute_quickstart);
		GenerateMissingJavaMemberParams params = new GenerateMissingJavaMemberParams(
				GenerateMissingJavaMemberParams.MemberType.Field, "volume", "org.acme.qute.Item",
				QuteMavenProjectName.qute_quickstart);
		WorkspaceEdit actual = QuteSupportForTemplate.getInstance().generateMissingJavaMember(params, getJDTUtils(),
				new NullProgressMonitor());
		WorkspaceEdit expected = we(
				Either.forLeft(tde(project, "src/main/java/org/acme/qute/Item.java", te(15, 1, 15, 8, "public"))));
		assertWorkspaceEdit(expected, actual);

	}

	@Test
	public void updateVisibilityOfFieldComplex() throws Exception {

		IJavaProject project = loadMavenProject(QuteMavenProjectName.qute_quickstart);
		GenerateMissingJavaMemberParams params = new GenerateMissingJavaMemberParams(
				GenerateMissingJavaMemberParams.MemberType.Field, "identifier", "org.acme.qute.Item",
				QuteMavenProjectName.qute_quickstart);
		WorkspaceEdit actual = QuteSupportForTemplate.getInstance().generateMissingJavaMember(params, getJDTUtils(),
				new NullProgressMonitor());
		WorkspaceEdit expected = we(Either.forLeft(tde(project, "src/main/java/org/acme/qute/Item.java",
				te(13, 15, 15, 23, //
						"int version = 1;\r\n" //
								+ "\r\n" //
								+ "\tprivate double volume;\r\n" //
								+ "\r\n" //
								+ "\tpublic int identifier = 0;"))));
		assertWorkspaceEdit(expected, actual);

	}

	@Test
	public void generateGetterWithNoMatchingField() throws Exception {

		IJavaProject project = loadMavenProject(QuteMavenProjectName.qute_quickstart);
		GenerateMissingJavaMemberParams params = new GenerateMissingJavaMemberParams(
				GenerateMissingJavaMemberParams.MemberType.Getter, "asdf", "org.acme.qute.Item",
				QuteMavenProjectName.qute_quickstart);
		WorkspaceEdit actual = QuteSupportForTemplate.getInstance().generateMissingJavaMember(params, getJDTUtils(),
				new NullProgressMonitor());
		WorkspaceEdit expected = we(Either.forLeft(tde(project, "src/main/java/org/acme/qute/Item.java",
				te(35, 1, 35, 1, "public String getAsdf() {\r\n\t\treturn null;\r\n\t}\r\n\r\n\t"))));
		assertWorkspaceEdit(expected, actual);

	}

	@Test
	public void generateGetterWithMatchingField() throws Exception {

		IJavaProject project = loadMavenProject(QuteMavenProjectName.qute_quickstart);
		GenerateMissingJavaMemberParams params = new GenerateMissingJavaMemberParams(
				GenerateMissingJavaMemberParams.MemberType.Getter, "identifier", "org.acme.qute.Item",
				QuteMavenProjectName.qute_quickstart);
		WorkspaceEdit actual = QuteSupportForTemplate.getInstance().generateMissingJavaMember(params, getJDTUtils(),
				new NullProgressMonitor());
		WorkspaceEdit expected = we(Either.forLeft(tde(project, "src/main/java/org/acme/qute/Item.java",
				te(13, 15, 35, 1, //
						"int identifier = 0, version = 1;\r\n" //
								+ "\r\n" //
								+ "\tprivate double volume;\r\n" //
								+ "\r\n" //
								+ "\tpublic Item(BigDecimal price, String name) {\r\n" //
								+ "\t\tthis.price = price;\r\n" //
								+ "\t\tthis.name = name;\r\n" //
								+ "\t}\r\n" //
								+ "\r\n" //
								+ "\t/**\r\n" //
								+ "\t * Returns the derived items.\r\n"
								+ "\t * \r\n"
								+ "\t * @return the derived items\r\n"
								+ "\t */\r\n" //
								+ "\tpublic Item[] getDerivedItems() {\r\n" //
								+ "\t\treturn null;\r\n" + "\t}\r\n" //
								+ "\r\n" //
								+ "\tpublic String varArgsMethod(int index, String... elements) {\r\n" //
								+ "\t\treturn null;\r\n" //
								+ "\t}\r\n" //
								+ "\r\n" //
								+ "\tpublic int getIdentifier() {\r\n" //
								+ "\t\treturn this.identifier;\r\n" //
								+ "\t}\r\n\r\n\t"))));
		assertWorkspaceEdit(expected, actual);

	}

	@Test
	public void generateTemplateExtensionInNewClass() throws Exception {

		String sep = System.lineSeparator();

		IJavaProject project = loadMavenProject(QuteMavenProjectName.qute_quickstart);
		GenerateMissingJavaMemberParams params = new GenerateMissingJavaMemberParams(
				GenerateMissingJavaMemberParams.MemberType.CreateTemplateExtension, "asdf", "org.acme.qute.Item",
				QuteMavenProjectName.qute_quickstart);
		WorkspaceEdit actual = QuteSupportForTemplate.getInstance().generateMissingJavaMember(params, getJDTUtils(),
				new NullProgressMonitor());
		WorkspaceEdit expected = we(Either.forRight(createOp(project, "src/main/java/TemplateExtensions.java")),
				Either.forLeft(tde(project, "src/main/java/TemplateExtensions.java", te(0, 0, 0, 0, //
						sep //
								+ "/**" + sep //
								+ " * java" + sep //
								+ " */" + sep //
								+ sep //
								+ "@io.quarkus.qute.TemplateExtension" + sep //
								+ "public class TemplateExtensions {" + sep //
								+ "\tpublic static String asdf(org.acme.qute.Item item) {" + sep //
								+ "\t\treturn null;" + sep //
								+ "\t}" + sep //
								+ "}" + sep))));
		assertWorkspaceEdit(expected, actual);
	}

	@Test
	public void generateTemplateExtensionInExistingClass() throws Exception {

		IJavaProject project = loadMavenProject(QuteMavenProjectName.qute_quickstart);
		GenerateMissingJavaMemberParams params = new GenerateMissingJavaMemberParams(
				GenerateMissingJavaMemberParams.MemberType.AppendTemplateExtension, "asdf", "org.acme.qute.Item",
				QuteMavenProjectName.qute_quickstart, "org.acme.qute.MyTemplateExtensions");
		WorkspaceEdit actual = QuteSupportForTemplate.getInstance().generateMissingJavaMember(params, getJDTUtils(),
				new NullProgressMonitor());
		WorkspaceEdit expected = we(Either.forLeft(tde(project, "src/main/java/org/acme/qute/MyTemplateExtensions.java",
				te(9, 2, 9, 2, //
						"\n\n\tpublic static String asdf(org.acme.qute.Item item) {\n" //
								+ "\t\treturn null;\n" //
								+ "\t}"))));
		assertWorkspaceEdit(expected, actual);
	}

	@Test
	public void generateTemplateExtensionInNewClassWithExisting() throws Exception {

		String sep = System.lineSeparator();
		IJavaProject project = loadMavenProject(QuteMavenProjectName.qute_quickstart);
		IPackageFragmentRoot root = project.findPackageFragmentRoot(project.getPath().append("src/main/java"));
		IPackageFragment defaultPackageFragment = root.createPackageFragment("", false, null);
		ICompilationUnit existingTemplateExtensions = defaultPackageFragment.createCompilationUnit("TemplateExtensions.java", "public class TemplateExtensions {}\n", false, null);

		try {
			GenerateMissingJavaMemberParams params = new GenerateMissingJavaMemberParams(
					GenerateMissingJavaMemberParams.MemberType.CreateTemplateExtension, "asdf", "org.acme.qute.Item",
					QuteMavenProjectName.qute_quickstart);
			WorkspaceEdit actual = QuteSupportForTemplate.getInstance().generateMissingJavaMember(params, getJDTUtils(),
					new NullProgressMonitor());
			WorkspaceEdit expected = we(Either.forRight(createOp(project, "src/main/java/TemplateExtensions1.java")),
					Either.forLeft(tde(project, "src/main/java/TemplateExtensions1.java", te(0, 0, 0, 0, //
							sep //
							+ "/**" + sep //
							+ " * java" + sep //
							+ " */" + sep //
							+ sep //
							+ "@io.quarkus.qute.TemplateExtension" + sep //
							+ "public class TemplateExtensions1 {" + sep //
							+ "\tpublic static String asdf(org.acme.qute.Item item) {" + sep //
							+ "\t\treturn null;" + sep //
							+ "\t}" + sep //
							+ "}" + sep))));
			assertWorkspaceEdit(expected, actual);

			ICompilationUnit anotherTemplateExtensions = defaultPackageFragment.createCompilationUnit("TemplateExtensions1.java", "public class TemplateExtensions1 {}\n", false, null);

			try {
				params = new GenerateMissingJavaMemberParams(
						GenerateMissingJavaMemberParams.MemberType.CreateTemplateExtension, "asdf", "org.acme.qute.Item",
						QuteMavenProjectName.qute_quickstart);
				actual = QuteSupportForTemplate.getInstance().generateMissingJavaMember(params, getJDTUtils(),
						new NullProgressMonitor());
				expected = we(Either.forRight(createOp(project, "src/main/java/TemplateExtensions2.java")),
						Either.forLeft(tde(project, "src/main/java/TemplateExtensions2.java", te(0, 0, 0, 0, //
								sep //
								+ "/**" + sep //
								+ " * java" + sep //
								+ " */" + sep //
								+ sep //
								+ "@io.quarkus.qute.TemplateExtension" + sep //
								+ "public class TemplateExtensions2 {" + sep //
								+ "\tpublic static String asdf(org.acme.qute.Item item) {" + sep //
								+ "\t\treturn null;" + sep //
								+ "\t}" + sep //
								+ "}" + sep))));
				assertWorkspaceEdit(expected, actual);
			} finally {
				anotherTemplateExtensions.delete(true, null);
			}
		} finally {
			existingTemplateExtensions.delete(true, null);
		}
	}

	// ------------------- WorkspaceEdit assert

	public static void assertWorkspaceEdit(WorkspaceEdit expected, WorkspaceEdit actual) {
		if (expected == null) {
			Assert.assertNull(actual);
			return;
		} else {
			Assert.assertNotNull(actual);
		}
		Assert.assertEquals(expected.getDocumentChanges().size(), actual.getDocumentChanges().size());
		for (int i = 0; i < expected.getDocumentChanges().size(); i++) {
			Assert.assertEquals(expected.getDocumentChanges().get(i), actual.getDocumentChanges().get(i));
		}
	}

	public static WorkspaceEdit we(Either<TextDocumentEdit, ResourceOperation>... documentChanges) {
		return new WorkspaceEdit(Arrays.asList(documentChanges));
	}

	// ------------------- edits constants

	private static Pattern FILE_PREFIX_PATTERN = Pattern.compile("file:/(?!//)");

	// ------------------- ResourceOperation assert

	private static ResourceOperation createOp(IJavaProject project, String projectFile) throws CoreException {
		String brokenLocationUri = project.getUnderlyingResource().getLocationURI().toString();
		Matcher m = FILE_PREFIX_PATTERN.matcher(brokenLocationUri);
		String fixedLocationUri = m.replaceFirst("file:///");
		return new CreateFile(fixedLocationUri + "/" + projectFile);
	}

	// ------------------- TextDocumentEdit assert

	public static TextDocumentEdit tde(IJavaProject project, String projectFile, TextEdit... te) throws CoreException {
		String brokenLocationUri = project.getUnderlyingResource().getLocationURI().toString();
		Matcher m = FILE_PREFIX_PATTERN.matcher(brokenLocationUri);
		String fixedLocationUri = m.replaceFirst("file:///");
		return tde(fixedLocationUri + "/" + projectFile, 0, te);
	}

	public static TextDocumentEdit tde(String uri, int version, TextEdit... te) {
		VersionedTextDocumentIdentifier versionedTextDocumentIdentifier = new VersionedTextDocumentIdentifier(uri,
				version);
		return new TextDocumentEdit(versionedTextDocumentIdentifier, Arrays.asList(te));
	}

	// ------------------- TextEdit assert

	public static TextEdit te(int startLine, int startCharacter, int endLine, int endCharacter, String newText) {
		TextEdit textEdit = new TextEdit();
		textEdit.setNewText(newText);
		textEdit.setRange(r(startLine, startCharacter, endLine, endCharacter));
		return textEdit;
	}

	// ------------------- range utils
	public static Range r(int startLine, int startCharacter, int endLine, int endCharacter) {
		return new Range(new Position(startLine, startCharacter), new Position(endLine, endCharacter));
	}
}

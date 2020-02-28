/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.microprofile.jdt.core;

import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ls.core.internal.JavaLanguageServerPlugin;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.redhat.microprofile.commons.DocumentFormat;
import com.redhat.microprofile.commons.MicroProfileJavaHoverParams;
import com.redhat.microprofile.jdt.core.project.JDTMicroProfileProject;
import com.redhat.microprofile.jdt.core.utils.IJDTUtils;
import com.redhat.microprofile.jdt.internal.config.java.MicroProfileConfigHoverParticipant;
import com.redhat.microprofile.jdt.internal.core.ls.JDTUtilsLSImpl;

/**
 * JDT Quarkus manager test for hover in Java file.
 * 
 *
 */
public class JavaHoverTest extends BasePropertiesManagerTest {

	private static IJavaProject javaProject;
	private static IJDTUtils utils;
	private static String javaFileUri;

	@BeforeClass
	public static void setup() throws Exception {
		JavaLanguageServerPlugin.getProjectsManager().setAutoBuilding(true);

		javaProject = loadMavenProject(MavenProjectName.config_hover);
		IProject project = javaProject.getProject();
		project.build(IncrementalProjectBuilder.FULL_BUILD, new NullProgressMonitor());

		IFile javaFile = project.getFile(new Path("src/main/java/org/acme/config/GreetingResource.java"));
		javaFileUri = javaFile.getLocation().toFile().toURI().toString();

		utils = JDTUtilsLSImpl.getInstance();
	}

	@Before
	@After
	public void cleanup() throws JavaModelException, IOException {
		deleteFile(JDTMicroProfileProject.APPLICATION_YAML_FILE, javaProject);
		deleteFile(JDTMicroProfileProject.APPLICATION_PROPERTIES_FILE, javaProject);
	}

	@Test
	public void configPropertyNameHover() throws Exception {
		saveFile(JDTMicroProfileProject.APPLICATION_PROPERTIES_FILE,
				"greeting.message = hello\r\n" + "greeting.name = quarkus\r\n" + "greeting.number = 100", javaProject);
		// Position(14, 40) is the character after the | symbol:
		// @ConfigProperty(name = "greeting.mes|sage")
		Hover info = getActualHover(new Position(14, 40));
		assertHover("greeting.message", "hello", 14, 28, 44, info);

		// Test left edge
		// Position(14, 28) is the character after the | symbol:
		// @ConfigProperty(name = "|greeting.message")
		info = getActualHover(new Position(14, 28));
		assertHover("greeting.message", "hello", 14, 28, 44, info);

		// Test right edge
		// Position(14, 43) is the character after the | symbol:
		// @ConfigProperty(name = "greeting.messag|e")
		info = getActualHover(new Position(14, 43));
		assertHover("greeting.message", "hello", 14, 28, 44, info);

		// Test no hover
		// Position(14, 27) is the character after the | symbol:
		// @ConfigProperty(name = |"greeting.message")
		info = getActualHover(new Position(14, 27));
		Assert.assertNull(info);

		// Test no hover 2
		// Position(14, 44) is the character after the | symbol:
		// @ConfigProperty(name = "greeting.message|")
		info = getActualHover(new Position(14, 44));
		Assert.assertNull(info);

		// Hover default value
		// Position(17, 33) is the character after the | symbol:
		// @ConfigProperty(name = "greet|ing.suffix", defaultValue="!")
		info = getActualHover(new Position(17, 33));
		assertHover("greeting.suffix", "!", 17, 28, 43, info);

		// Hover override default value
		// Position(26, 33) is the character after the | symbol:
		// @ConfigProperty(name = "greet|ing.number", defaultValue="0")
		info = getActualHover(new Position(26, 33));
		assertHover("greeting.number", "100", 26, 28, 43, info);

		// Hover when no value
		// Position(23, 33) is the character after the | symbol:
		// @ConfigProperty(name = "greet|ing.missing")
		info = getActualHover(new Position(23, 33));
		assertHover("greeting.missing", null, 23, 28, 44, info);
	}

	@Test
	public void configPropertyNameYaml() throws Exception {
		saveFile(JDTMicroProfileProject.APPLICATION_YAML_FILE,
				"greeting:\n" + "  message: message from yaml\n" + "  number: 2001", javaProject);

		saveFile(JDTMicroProfileProject.APPLICATION_PROPERTIES_FILE,
				"greeting.message = hello\r\n" + "greeting.name = quarkus\r\n" + "greeting.number = 100", javaProject);

		// Position(14, 40) is the character after the | symbol:
		// @ConfigProperty(name = "greeting.mes|sage")
		Hover info = getActualHover(new Position(14, 40));
		assertHover("greeting.message", "message from yaml", 14, 28, 44, info);

		// Position(26, 33) is the character after the | symbol:
		// @ConfigProperty(name = "greet|ing.number", defaultValue="0")
		info = getActualHover(new Position(26, 33));
		assertHover("greeting.number", "2001", 26, 28, 43, info);

		saveFile(JDTMicroProfileProject.APPLICATION_YAML_FILE, "greeting:\n" + "  message: message from yaml",
				javaProject);

		// fallback to application.properties
		info = getActualHover(new Position(26, 33));
		assertHover("greeting.number", "100", 26, 28, 43, info);
	}

	private Hover getActualHover(Position hoverPosition) throws JavaModelException {
		MicroProfileJavaHoverParams params = new MicroProfileJavaHoverParams();
		params.setDocumentFormat(DocumentFormat.Markdown);
		params.setPosition(hoverPosition);
		params.setUri(javaFileUri);

		return PropertiesManagerForJava.getInstance().hover(params, utils, new NullProgressMonitor());
	}

	private void assertHover(String expectedKey, String expectedValue, int expectedLine, int expectedStartOffset,
			int expectedEndOffset, Hover actualInfo) {
		Assert.assertNotNull(actualInfo);

		Position expectedStart = new Position(expectedLine, expectedStartOffset);
		Position expectedEnd = new Position(expectedLine, expectedEndOffset);
		Range expectedRange = new Range(expectedStart, expectedEnd);

		MarkupContent expectedContent = MicroProfileConfigHoverParticipant.getDocumentation(expectedKey, expectedValue,
				DocumentFormat.Markdown, true);

		Assert.assertEquals(expectedContent, actualInfo.getContents().getRight());
		Assert.assertEquals(expectedRange, actualInfo.getRange());
	}

}
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
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import com.redhat.microprofile.commons.DocumentFormat;
import com.redhat.microprofile.commons.MicroProfileJavaHoverParams;
import com.redhat.microprofile.jdt.core.project.JDTMicroProfileProject;
import com.redhat.microprofile.jdt.internal.config.java.MicroProfileConfigHoverParticipant;

/**
 * JDT Quarkus manager test for hover in Java file.
 * 
 *
 */
public class JavaHoverTest extends BasePropertiesManagerTest {

	private static IJavaProject javaProject;

	@After
	public void cleanup() throws JavaModelException, IOException {
		deleteFile(JDTMicroProfileProject.APPLICATION_YAML_FILE, javaProject);
		deleteFile(JDTMicroProfileProject.APPLICATION_PROPERTIES_FILE, javaProject);
	}

	@Test
	public void configPropertyNameHover() throws Exception {
		
		javaProject = loadMavenProject(MavenProjectName.config_hover);
		IProject project = javaProject.getProject();
		IFile javaFile = project.getFile(new Path("src/main/java/org/acme/config/GreetingResource.java"));
		String javaFileUri = javaFile.getLocation().toFile().toURI().toString();
		
		saveFile(JDTMicroProfileProject.APPLICATION_PROPERTIES_FILE,
				"greeting.message = hello\r\n" + "greeting.name = quarkus\r\n" + "greeting.number = 100", javaProject);
		// Position(14, 40) is the character after the | symbol:
		// @ConfigProperty(name = "greeting.mes|sage")
		Hover info = getActualHover(new Position(14, 40), javaFileUri);
		assertHover("greeting.message", "hello", 14, 28, 44, info);

		// Test left edge
		// Position(14, 28) is the character after the | symbol:
		// @ConfigProperty(name = "|greeting.message")
		info = getActualHover(new Position(14, 28), javaFileUri);
		assertHover("greeting.message", "hello", 14, 28, 44, info);

		// Test right edge
		// Position(14, 43) is the character after the | symbol:
		// @ConfigProperty(name = "greeting.messag|e")
		info = getActualHover(new Position(14, 43), javaFileUri);
		assertHover("greeting.message", "hello", 14, 28, 44, info);

		// Test no hover
		// Position(14, 27) is the character after the | symbol:
		// @ConfigProperty(name = |"greeting.message")
		info = getActualHover(new Position(14, 27), javaFileUri);
		Assert.assertNull(info);

		// Test no hover 2
		// Position(14, 44) is the character after the | symbol:
		// @ConfigProperty(name = "greeting.message|")
		info = getActualHover(new Position(14, 44), javaFileUri);
		Assert.assertNull(info);

		// Hover default value
		// Position(17, 33) is the character after the | symbol:
		// @ConfigProperty(name = "greet|ing.suffix", defaultValue="!")
		info = getActualHover(new Position(17, 33), javaFileUri);
		assertHover("greeting.suffix", "!", 17, 28, 43, info);

		// Hover override default value
		// Position(26, 33) is the character after the | symbol:
		// @ConfigProperty(name = "greet|ing.number", defaultValue="0")
		info = getActualHover(new Position(26, 33), javaFileUri);
		assertHover("greeting.number", "100", 26, 28, 43, info);

		// Hover when no value
		// Position(23, 33) is the character after the | symbol:
		// @ConfigProperty(name = "greet|ing.missing")
		info = getActualHover(new Position(23, 33), javaFileUri);
		assertHover("greeting.missing", null, 23, 28, 44, info);
	}

	@Test
	public void configPropertyNameYaml() throws Exception {
		
		javaProject = loadMavenProject(MavenProjectName.config_hover);
		IProject project = javaProject.getProject();
		IFile javaFile = project.getFile(new Path("src/main/java/org/acme/config/GreetingResource.java"));
		String javaFileUri = javaFile.getLocation().toFile().toURI().toString();
		
		saveFile(JDTMicroProfileProject.APPLICATION_YAML_FILE,
				"greeting:\n" + "  message: message from yaml\n" + "  number: 2001", javaProject);

		saveFile(JDTMicroProfileProject.APPLICATION_PROPERTIES_FILE,
				"greeting.message = hello\r\n" + "greeting.name = quarkus\r\n" + "greeting.number = 100", javaProject);

		// Position(14, 40) is the character after the | symbol:
		// @ConfigProperty(name = "greeting.mes|sage")
		Hover info = getActualHover(new Position(14, 40), javaFileUri);
		assertHover("greeting.message", "message from yaml", 14, 28, 44, info);

		// Position(26, 33) is the character after the | symbol:
		// @ConfigProperty(name = "greet|ing.number", defaultValue="0")
		info = getActualHover(new Position(26, 33), javaFileUri);
		assertHover("greeting.number", "2001", 26, 28, 43, info);

		saveFile(JDTMicroProfileProject.APPLICATION_YAML_FILE, "greeting:\n" + "  message: message from yaml",
				javaProject);

		// fallback to application.properties
		info = getActualHover(new Position(26, 33), javaFileUri);
		assertHover("greeting.number", "100", 26, 28, 43, info);
	}
	
	@Test
	public void configPropertyNameMethod() throws Exception {
		
		javaProject = loadMavenProject(MavenProjectName.config_quickstart);
		IProject project = javaProject.getProject();
		IFile javaFile = project.getFile(new Path("src/main/java/org/acme/config/GreetingMethodResource.java"));
		String javaFileUri = javaFile.getLocation().toFile().toURI().toString();

		saveFile(JDTMicroProfileProject.APPLICATION_PROPERTIES_FILE,
				"greeting.method.message = hello", javaProject);

		// Position(22, 61) is the character after the | symbol:
		// @ConfigProperty(name = "greeting.m|ethod.message")
		Hover info = getActualHover(new Position(22, 61), javaFileUri);
		assertHover("greeting.method.message", "hello", 22, 51, 74, info);

		// Position(27, 60) is the character after the | symbol:
		// @ConfigProperty(name = "greeting.m|ethod.suffix" , defaultValue="!")
		info = getActualHover(new Position(27, 60), javaFileUri);
		assertHover("greeting.method.suffix", "!", 27, 50, 72, info);

		// Position(32, 58) is the character after the | symbol:
		// @ConfigProperty(name = "greeting.method.name")
		info = getActualHover(new Position(32, 58), javaFileUri);
		assertHover("greeting.method.name", null, 32, 48, 68, info);
	}
	
	@Test
	public void configPropertyNameConstructor() throws Exception {
		
		javaProject = loadMavenProject(MavenProjectName.config_quickstart);
		IProject project = javaProject.getProject();
		IFile javaFile = project.getFile(new Path("src/main/java/org/acme/config/GreetingConstructorResource.java"));
		String javaFileUri = javaFile.getLocation().toFile().toURI().toString();

		saveFile(JDTMicroProfileProject.APPLICATION_PROPERTIES_FILE,
				"greeting.constructor.message = hello", javaProject);

		// Position(23, 48) is the character after the | symbol:
		// @ConfigProperty(name = "greeting.con|structor.message")
		Hover info = getActualHover(new Position(23, 48), javaFileUri);
		assertHover("greeting.constructor.message", "hello", 23, 36, 64, info);

		// Position(24, 48) is the character after the | symbol:
		// @ConfigProperty(name = "greeting.con|structor.suffix" , defaultValue="!")
		info = getActualHover(new Position(24, 48), javaFileUri);
		assertHover("greeting.constructor.suffix", "!", 24, 36, 63, info);

		// Position(25, 48) is the character after the | symbol:
		// @ConfigProperty(name = "greeting.con|structor.name")
		info = getActualHover(new Position(25, 48), javaFileUri);
		assertHover("greeting.constructor.name", null, 25, 36, 61, info);
	}


	private Hover getActualHover(Position hoverPosition,String javaFileUri) throws JavaModelException {
		MicroProfileJavaHoverParams params = new MicroProfileJavaHoverParams();
		params.setDocumentFormat(DocumentFormat.Markdown);
		params.setPosition(hoverPosition);
		params.setUri(javaFileUri);

		return PropertiesManagerForJava.getInstance().hover(params, JDT_UTILS, new NullProgressMonitor());
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
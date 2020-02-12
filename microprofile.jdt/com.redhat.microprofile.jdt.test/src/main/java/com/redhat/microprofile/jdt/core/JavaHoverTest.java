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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.ls.core.internal.JavaLanguageServerPlugin;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.redhat.microprofile.commons.MicroProfileJavaHoverInfo;
import com.redhat.microprofile.commons.MicroProfileJavaHoverParams;
import com.redhat.microprofile.jdt.core.project.JDTMicroProfileProject;
import com.redhat.microprofile.jdt.core.utils.IJDTUtils;
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
	
	@Before @After
	public void cleanup() throws JavaModelException, IOException {
		deleteFile(JDTMicroProfileProject.APPLICATION_YAML_FILE, javaProject);
		deleteFile(JDTMicroProfileProject.APPLICATION_PROPERTIES_FILE, javaProject);
	}

	@Test
	public void configPropertyNameHover() throws Exception {
		saveFile(JDTMicroProfileProject.APPLICATION_PROPERTIES_FILE,
				"greeting.message = hello\r\n" + 
				"greeting.name = quarkus\r\n" + 
				"greeting.number = 100", javaProject);
		// Position(14, 40) is the character after the | symbol:
		// @ConfigProperty(name = "greeting.mes|sage")
		MicroProfileJavaHoverInfo info = getActualHoverInfo(new Position(14, 40));
		assertHoverInfo("greeting.message", "hello", 14, 28, 44, info);
		
		// Test left edge
		// Position(14, 28) is the character after the | symbol:
		// @ConfigProperty(name = "|greeting.message")
		info = getActualHoverInfo(new Position(14, 28));
		assertHoverInfo("greeting.message", "hello", 14, 28, 44, info);
		
		// Test right edge
		// Position(14, 43) is the character after the | symbol:
		// @ConfigProperty(name = "greeting.messag|e")
		info = getActualHoverInfo(new Position(14, 43));
		assertHoverInfo("greeting.message", "hello", 14, 28, 44, info);
		
		// Test no hover
		// Position(14, 27) is the character after the | symbol:
		// @ConfigProperty(name = |"greeting.message")
		info = getActualHoverInfo(new Position(14, 27));
		Assert.assertNull(info);
		
        // Test no hover 2
		// Position(14, 44) is the character after the | symbol:
		// @ConfigProperty(name = "greeting.message|")
		info = getActualHoverInfo(new Position(14, 44));
		Assert.assertNull(info);
		
		// Hover default value
		// Position(17, 33) is the character after the | symbol:
		// @ConfigProperty(name = "greet|ing.suffix", defaultValue="!")
		info = getActualHoverInfo(new Position(17, 33));
		assertHoverInfo("greeting.suffix", "!", 17, 28, 43, info);
		
		// Hover override default value
		// Position(26, 33) is the character after the | symbol:
		// @ConfigProperty(name = "greet|ing.number", defaultValue="0")
		info = getActualHoverInfo(new Position(26, 33));
		assertHoverInfo("greeting.number", "100", 26, 28, 43, info);
		
		// Hover when no value
		// Position(23, 33) is the character after the | symbol:
		// @ConfigProperty(name = "greet|ing.missing")
		info = getActualHoverInfo(new Position(23, 33));
		assertHoverInfo("greeting.missing", null, 23, 28, 44, info);
	}

	
	@Test
	public void configPropertyNameYaml() throws Exception {
		saveFile(JDTMicroProfileProject.APPLICATION_YAML_FILE,
				"greeting:\n" + 
				"  message: message from yaml\n" + 
				"  number: 2001", javaProject);
		
		saveFile(JDTMicroProfileProject.APPLICATION_PROPERTIES_FILE,
				"greeting.message = hello\r\n" + 
				"greeting.name = quarkus\r\n" + 
				"greeting.number = 100", javaProject);

		// Position(14, 40) is the character after the | symbol:
		// @ConfigProperty(name = "greeting.mes|sage")
		MicroProfileJavaHoverInfo info = getActualHoverInfo(new Position(14, 40));
		assertHoverInfo("greeting.message", "message from yaml", 14, 28, 44, info);

		// Position(26, 33) is the character after the | symbol:
		// @ConfigProperty(name = "greet|ing.number", defaultValue="0")
		info = getActualHoverInfo(new Position(26, 33));
		assertHoverInfo("greeting.number", "2001", 26, 28, 43, info);
		
		saveFile(JDTMicroProfileProject.APPLICATION_YAML_FILE,
				"greeting:\n" + 
				"  message: message from yaml", javaProject);
		
		// fallback to application.properties
		info = getActualHoverInfo(new Position(26, 33));
		assertHoverInfo("greeting.number", "100", 26, 28, 43, info);
	}
	
	private MicroProfileJavaHoverInfo getActualHoverInfo(Position hoverPosition) throws JavaModelException {
		MicroProfileJavaHoverParams params = new MicroProfileJavaHoverParams();

		params.setPosition(hoverPosition);
		params.setUri(javaFileUri);

		return PropertiesManagerForJava.getInstance().hover(params, utils,
				new NullProgressMonitor());
	}
	
	private void assertHoverInfo(String expectedKey, String expectedValue, int expectedLine,
			int expectedStartOffset, int expectedEndOffset, MicroProfileJavaHoverInfo actualInfo) {
		Assert.assertNotNull(actualInfo);

		Position expectedStart = new Position(expectedLine, expectedStartOffset);
		Position expectedEnd = new Position(expectedLine, expectedEndOffset);
		Range expectedRange = new Range(expectedStart, expectedEnd);
		
		
		Assert.assertEquals(expectedKey, actualInfo.getPropertyKey());
		if (expectedValue == null) {
			Assert.assertNull(expectedValue);
		} else {
			Assert.assertEquals(expectedValue, actualInfo.getPropertyValue());	
		}
		Assert.assertEquals(expectedRange, actualInfo.getRange());
	}

	private static List<File> getApplicationPropertiesFile(IJavaProject javaProject) {
		try {
			List<IPath> outputs = Stream.of(((JavaProject) javaProject).getResolvedClasspath(true)) //
					.filter(entry -> !entry.isTest()) //
					.filter(entry -> entry.getEntryKind() == IClasspathEntry.CPE_SOURCE) //
					.map(entry -> entry.getOutputLocation()) //
					.filter(output -> output != null) //
					.distinct() //
					.collect(Collectors.toList());
			List<File> files = new ArrayList<>();
			for (IPath output : outputs) {
				File file = javaProject.getProject().getLocation().append(output.removeFirstSegments(1))
						.append("application.properties").toFile();
				files.add(file);
			}
			return files;
		} catch (JavaModelException e) {
			return null;
		}
	}
}
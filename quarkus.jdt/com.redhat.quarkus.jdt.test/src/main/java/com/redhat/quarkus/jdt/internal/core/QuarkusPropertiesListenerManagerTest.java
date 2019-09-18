/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.quarkus.jdt.internal.core;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.io.MoreFiles;
import com.google.common.io.RecursiveDeleteOption;
import com.redhat.quarkus.jdt.core.IQuarkusPropertiesChangedListener;
import com.redhat.quarkus.jdt.internal.core.utils.JDTQuarkusUtils;

/**
 * Test with {@link QuarkusPropertiesListenerManager}
 * 
 * @author Angelo ZERR
 *
 */
public class QuarkusPropertiesListenerManagerTest {

	private final static Set<String> projects = new HashSet<>();
	private final static IQuarkusPropertiesChangedListener listener = p -> {
		projects.addAll(p.getProjectURIs());
	};

	@BeforeClass
	public static void init() {
		cleanWorkinkingDir();
		projects.clear();
		QuarkusPropertiesListenerManager manager = QuarkusPropertiesListenerManager.getInstance();
		manager.addQuarkusPropertiesChangedListener(listener);
	}

	private static void cleanWorkinkingDir() {
		try {
			File dir = JavaUtils.getWorkingProjectDirectory();
			if (dir.exists()) {
				MoreFiles.deleteRecursively(dir.toPath(), RecursiveDeleteOption.ALLOW_INSECURE);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@AfterClass
	public static void destroy() {
		cleanWorkinkingDir();
		QuarkusPropertiesListenerManager manager = QuarkusPropertiesListenerManager.getInstance();
		manager.removeQuarkusPropertiesChangedListener(listener);
	}

	@Test
	public void classpathChanged() throws Exception {
		projects.clear();
		Assert.assertEquals(0, projects.size());

		// Create a Java project -> classpath changed
		IJavaProject project = JavaUtils.createJavaProject("test-classpath-changed", new String[] { "/test.jar" });
		Assert.assertEquals(1, projects.size());
		Assert.assertEquals(JDTQuarkusUtils.getProjectURI(project), projects.iterator().next());

		projects.clear();
		Assert.assertEquals(0, projects.size());

		// Update classpath -> classpath changed
		project.setRawClasspath(new IClasspathEntry[] {}, new NullProgressMonitor());
		Assert.assertEquals(1, projects.size());
		Assert.assertEquals(JDTQuarkusUtils.getProjectURI(project), projects.iterator().next());
	}

	@Test
	public void javaSourcesChanged() throws Exception {
		projects.clear();
		Assert.assertEquals(0, projects.size());

		// Create a Java project -> classpath changed
		IJavaProject javaProject = JavaUtils.createJavaProject("test-java-sources-changed", new String[] {});
		Assert.assertEquals(1, projects.size());
		Assert.assertEquals(JDTQuarkusUtils.getProjectURI(javaProject), projects.iterator().next());

		projects.clear();
		Assert.assertEquals(0, projects.size());

		// create folder by using resources package
		IFolder folder = javaProject.getProject().getFolder("src");
		Assert.assertEquals(0, projects.size());

		// Add folder to Java element
		IPackageFragmentRoot srcFolder = javaProject.getPackageFragmentRoot(folder);
		Assert.assertEquals(0, projects.size());

		// create package fragment
		IPackageFragment fragment = srcFolder.createPackageFragment("org.acme.config", true, null);
		Assert.assertEquals(0, projects.size());

		// Test with create of Java file
		// --> init code string and create compilation unit
		String str = "package org.acme.config;\r\n" + //
				"\r\n" + //
				"import java.util.Optional;\r\n" + //
				"\r\n" + //
				"import javax.ws.rs.GET;\r\n" + //
				"import javax.ws.rs.Path;\r\n" + //
				"import javax.ws.rs.Produces;\r\n" + //
				"import javax.ws.rs.core.MediaType;\r\n" + //
				"\r\n" + //
				"import org.eclipse.microprofile.config.inject.ConfigProperty;\r\n" + //
				"     \r\n" + //
				"@Path(\"/greeting\") \r\n" + //
				"public class GreetingResource {\r\n" + //
				"  \r\n" + //
				"    @ConfigProperty(name = \"greeting.message\")\r\n" + //
				"    String message;\r\n" + //
				"   \r\n" + //
				"    @ConfigProperty(name = \"greeting.suffix\" , \r\n" + //
				"                    defaultValue=\"!\")\r\n" + //
				"    String suffix;\r\n" + //
				"\r\n" + //
				"    @ConfigProperty(name = \"greeting.name\")\r\n" + //
				"    Optional<String> name;\r\n" + //
				"\r\n" + //
				"\r\n" + //
				"    @GET\r\n" + //
				"    @Produces(MediaType.TEXT_PLAIN)\r\n" + //
				"    public String hello() {\r\n" + //
				"        return message + //\" \" + //name.orElse(\"world\") + //suffix;\r\n" + //
				"    }\r\n" + //
				"}\r\n" + //
				"";
		ICompilationUnit cu = fragment.createCompilationUnit("GreetingResource.java", str, false, null);

		// The java file has been modified
		Thread.sleep(200); // wait a moment since IQuarkusPropertiesChangedListener are fired in async mode
		Assert.assertEquals(1, projects.size());
		Assert.assertEquals(JDTQuarkusUtils.getProjectURI(javaProject), projects.iterator().next());

		// Test with update of Java file
		// -> create a field
		projects.clear();
		IType type = cu.getType("Test");
		type.createField("private String age;", null, true, null);

		// The java file has been modified
		Thread.sleep(200); // wait a moment since IQuarkusPropertiesChangedListener are fired in async mode
		Assert.assertEquals(1, projects.size());
		Assert.assertEquals(JDTQuarkusUtils.getProjectURI(javaProject), projects.iterator().next());

	}
}

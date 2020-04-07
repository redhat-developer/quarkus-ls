/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.microprofile.jdt.internal.core;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.google.common.io.MoreFiles;
import com.google.common.io.RecursiveDeleteOption;
import com.redhat.microprofile.commons.MicroProfilePropertiesChangeEvent;
import com.redhat.microprofile.jdt.core.IMicroProfilePropertiesChangedListener;
import com.redhat.microprofile.jdt.core.utils.JDTMicroProfileUtils;

/**
 * Test with {@link MicroProfilePropertiesListenerManager}
 * 
 * @author Angelo ZERR
 *
 */
public class MicroProfilePropertiesListenerManagerTest {

	private static final String TEST_CLASSPATH_CHANGED_PROJECT = "test-classpath-changed";
	private static final String TEST_JAVA_SOURCES_CHANGED_PROJECT = "test-java-sources-changed";

	static class ProjectTracker implements IMicroProfilePropertiesChangedListener {

		final Set<String> projects = new HashSet<>();

		@Override
		public void propertiesChanged(MicroProfilePropertiesChangeEvent event) {
			projects.addAll(event.getProjectURIs());
		}

		public Set<String> getProjects() {
			return projects;
		}
	}

	private ProjectTracker projectTracker;

	@Before
	public void init() {
		cleanWorkinkingDir();
		projectTracker = new ProjectTracker();
		MicroProfilePropertiesListenerManager manager = MicroProfilePropertiesListenerManager.getInstance();
		manager.addMicroProfilePropertiesChangedListener(projectTracker);
	}

	private void cleanWorkinkingDir() {
		try {
			IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
			for (IProject project : projects) {
				if (TEST_CLASSPATH_CHANGED_PROJECT.equals(project.getName())
						|| TEST_JAVA_SOURCES_CHANGED_PROJECT.equals(project.getName())) {
					project.delete(true, null);
				}
			}
			File dir = JavaUtils.getWorkingProjectDirectory();
			if (dir.exists()) {
				MoreFiles.deleteRecursively(dir.toPath(), RecursiveDeleteOption.ALLOW_INSECURE);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@After
	public void destroy() {
		MicroProfilePropertiesListenerManager manager = MicroProfilePropertiesListenerManager.getInstance();
		manager.removeMicroProfilePropertiesChangedListener(projectTracker);
		cleanWorkinkingDir();
	}

	@Ignore
	@Test
	public void classpathChanged() throws Exception {
		Assert.assertEquals(0, projectTracker.getProjects().size());

		// Create a Java project -> classpath changed
		IJavaProject project = JavaUtils.createJavaProject(TEST_CLASSPATH_CHANGED_PROJECT,
				new String[] { "/test.jar" });
		JobHelpers.waitForJobsToComplete();
		Assert.assertEquals(1, projectTracker.getProjects().size());
		Assert.assertEquals(JDTMicroProfileUtils.getProjectURI(project),
				projectTracker.getProjects().iterator().next());

		projectTracker.getProjects().clear();
		Assert.assertEquals(0, projectTracker.getProjects().size());

		// Update classpath -> classpath changed
		project.setRawClasspath(new IClasspathEntry[] {}, new NullProgressMonitor());
		JobHelpers.waitForJobsToComplete();
		Assert.assertEquals(1, projectTracker.getProjects().size());
		Assert.assertEquals(JDTMicroProfileUtils.getProjectURI(project),
				projectTracker.getProjects().iterator().next());
	}

	@Ignore
	@Test
	public void javaSourcesChanged() throws Exception {
		Assert.assertEquals(0, projectTracker.getProjects().size());

		// Create a Java project -> classpath changed
		IJavaProject javaProject = JavaUtils.createJavaProject(TEST_JAVA_SOURCES_CHANGED_PROJECT, new String[] {});
		JobHelpers.waitForJobsToComplete();
		Assert.assertEquals(1, projectTracker.getProjects().size());
		Assert.assertEquals(JDTMicroProfileUtils.getProjectURI(javaProject),
				projectTracker.getProjects().iterator().next());

		projectTracker.getProjects().clear();
		Assert.assertEquals(0, projectTracker.getProjects().size());

		// create folder by using resources package
		IFolder folder = javaProject.getProject().getFolder("src");
		Assert.assertEquals(0, projectTracker.getProjects().size());

		// Add folder to Java element
		IPackageFragmentRoot srcFolder = javaProject.getPackageFragmentRoot(folder);
		Assert.assertEquals(0, projectTracker.getProjects().size());

		// create package fragment
		IPackageFragment fragment = srcFolder.createPackageFragment("org.acme.config", true, null);
		Assert.assertEquals(0, projectTracker.getProjects().size());

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
		JobHelpers.waitForJobsToComplete();
		Assert.assertEquals(1, projectTracker.getProjects().size());
		Assert.assertEquals(JDTMicroProfileUtils.getProjectURI(javaProject),
				projectTracker.getProjects().iterator().next());

		// Test with update of Java file
		// -> create a field
		projectTracker.getProjects().clear();
		IType type = cu.getType("Test");
		type.createField("private String age;", null, true, null);

		// The java file has been modified
		JobHelpers.waitForJobsToComplete();
		Assert.assertEquals(1, projectTracker.getProjects().size());
		Assert.assertEquals(JDTMicroProfileUtils.getProjectURI(javaProject),
				projectTracker.getProjects().iterator().next());

	}
}

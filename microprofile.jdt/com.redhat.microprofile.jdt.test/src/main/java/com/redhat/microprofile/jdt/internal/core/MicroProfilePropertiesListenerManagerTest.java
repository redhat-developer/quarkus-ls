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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.internal.core.CreateCompilationUnitOperation;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.google.common.io.MoreFiles;
import com.google.common.io.RecursiveDeleteOption;
import com.redhat.microprofile.commons.MicroProfilePropertiesChangeEvent;
import com.redhat.microprofile.commons.MicroProfilePropertiesScope;
import com.redhat.microprofile.jdt.core.IMicroProfilePropertiesChangedListener;
import com.redhat.microprofile.jdt.core.utils.JDTMicroProfileUtils;

/**
 * Test with {@link MicroProfilePropertiesListenerManager}
 * 
 * @author Angelo ZERR
 *
 */
public class MicroProfilePropertiesListenerManagerTest {

	static class ProjectTracker implements IMicroProfilePropertiesChangedListener {

		final List<MicroProfilePropertiesChangeEvent> events = new ArrayList<>();

		@Override
		public void propertiesChanged(MicroProfilePropertiesChangeEvent event) {
			events.add(event);
		}

		public List<MicroProfilePropertiesChangeEvent> getEvents() {
			return events;
		}
	}

	private ProjectTracker projectTracker;
	private MicroProfilePropertiesListenerManager manager;

	@Before
	public void init() {
		cleanWorkinkingDir();
		projectTracker = new ProjectTracker();
		manager = new MicroProfilePropertiesListenerManager(false);
		manager.initialize();
		manager.addMicroProfilePropertiesChangedListener(projectTracker);
	}

	private void cleanWorkinkingDir() {
		try {
			File dir = JavaUtils.getWorkingProjectDirectory();
			if (dir.exists()) {
				MoreFiles.deleteRecursively(dir.toPath(), RecursiveDeleteOption.ALLOW_INSECURE);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@After
	public void destroy() {
		manager.removeMicroProfilePropertiesChangedListener(projectTracker);
		manager.destroy();
		manager = null;
		cleanWorkinkingDir();
	}

	@Ignore
	@Test
	public void classpathChanged() throws Exception {

		IJavaProject javaProject = null;
		try {
			// Create a Java project -> classpath changed
			javaProject = JavaUtils.createJavaProject("test-classpath-changed", new String[] { "/test.jar" });

			// Event 3 (sources and dependencies) :
			// Update classpath -> classpath changed
			javaProject.setRawClasspath(new IClasspathEntry[] {}, new NullProgressMonitor());

			assertWaitForEvents(projectTracker, 3);
			// Event 1 (sources and dependencies) -> coming from IProject#setDescription
			// used in JavaUtils.createJavaProject
			Assert.assertEquals(createEvent(JDTMicroProfileUtils.getProjectURI(javaProject),
					MicroProfilePropertiesScope.SOURCES_AND_DEPENDENCIES), projectTracker.getEvents().get(0));
			// Event 2 (sources and dependencies) -> coming from
			// IJavaProject#setRawClasspath
			// used in JavaUtils.createJavaProject
			Assert.assertEquals(createEvent(JDTMicroProfileUtils.getProjectURI(javaProject),
					MicroProfilePropertiesScope.SOURCES_AND_DEPENDENCIES), projectTracker.getEvents().get(1));
			// Event 3 (sources and dependencies) :
			Assert.assertEquals(createEvent(JDTMicroProfileUtils.getProjectURI(javaProject),
					MicroProfilePropertiesScope.ONLY_SOURCES), projectTracker.getEvents().get(2));
		} finally {
			if (javaProject != null) {
				javaProject.getProject().close(new NullProgressMonitor());
			}
		}
	}

	@Ignore
	@Test
	public void javaSourcesChanged() throws Exception {

		IJavaProject javaProject = null;
		try {
			// Create a Java project -> classpath changed
			javaProject = JavaUtils.createJavaProject("test-java-sources-changed", new String[] {});

			// create folder by using resources package
			IFolder folder = javaProject.getProject().getFolder("src");

			// Add folder to Java element
			IPackageFragmentRoot srcFolder = javaProject.getPackageFragmentRoot(folder);

			// create package fragment
			IPackageFragment pkg = srcFolder.createPackageFragment("org.acme.config", true, null);

			// Test with create of Java file
			// --> init code string and create compilation unit
			String source = "package org.acme.config;\r\n" + //
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
			CreateCompilationUnitOperation op = new CreateCompilationUnitOperation(pkg, "GreetingResource.java", source,
					true);
			op.runOperation(new NullProgressMonitor());

//		// Event 6 (sources) :
//		// Test with update of Java file
//		// -> create a field
//		IType type = cu.getType("Test");
//		type.createField("private String age;", null, true, null);
//		cu.getUnderlyingResource().refreshLocal(IResource.DEPTH_ZERO, new NullProgressMonitor());

			assertWaitForEvents(projectTracker, 3);
			// Event 1 (sources and dependencies) -> coming from IProject#setDescription
			// used in JavaUtils.createJavaProject
			Assert.assertEquals(createEvent(JDTMicroProfileUtils.getProjectURI(javaProject),
					MicroProfilePropertiesScope.SOURCES_AND_DEPENDENCIES), projectTracker.getEvents().get(0));
			// Event 2 (sources and dependencies) -> coming from
			// IJavaProject#setRawClasspath
			// used in JavaUtils.createJavaProject
			Assert.assertEquals(createEvent(JDTMicroProfileUtils.getProjectURI(javaProject),
					MicroProfilePropertiesScope.SOURCES_AND_DEPENDENCIES), projectTracker.getEvents().get(1));
			// Event 3 (sources) :
			Assert.assertEquals(createEvent(JDTMicroProfileUtils.getProjectURI(javaProject),
					MicroProfilePropertiesScope.ONLY_SOURCES), projectTracker.getEvents().get(2));
		} finally {
			if (javaProject != null) {
				javaProject.getProject().close(new NullProgressMonitor());
			}
		}

	}

	private static MicroProfilePropertiesChangeEvent createEvent(String projectURI,
			List<MicroProfilePropertiesScope> type) {
		MicroProfilePropertiesChangeEvent event = new MicroProfilePropertiesChangeEvent();
		event.setProjectURIs(new HashSet<String>(Arrays.asList(projectURI)));
		event.setType(type);
		return event;
	}

	private static void assertWaitForEvents(ProjectTracker projectTracker, int nbEvents) throws InterruptedException {
		int nbTry = 0;
		Object lock = new Object();
		synchronized (lock) {
			while (projectTracker.getEvents().size() != nbEvents && nbTry < 200) {
				lock.wait(500);
				nbTry++;
			}
		}
		Assert.assertEquals("Test events size", nbEvents, projectTracker.getEvents().size());
	}
}

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
import java.nio.file.Files;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.redhat.microprofile.commons.ClasspathKind;
import com.redhat.microprofile.commons.DocumentFormat;
import com.redhat.microprofile.commons.MicroProfileProjectInfo;
import com.redhat.microprofile.commons.MicroProfilePropertiesScope;
import com.redhat.microprofile.jdt.core.utils.IJDTUtils;
import com.redhat.microprofile.jdt.internal.core.JavaUtils;
import com.redhat.microprofile.jdt.internal.core.JobHelpers;
import com.redhat.microprofile.jdt.internal.core.ls.JDTUtilsLSImpl;

/**
 * Base class for testing {@link PropertiesManager}.
 * 
 * @author Angelo ZERR
 *
 */
public class BasePropertiesManagerTest {

	private static final Logger LOGGER = Logger.getLogger(BasePropertiesManagerTest.class.getSimpleName());
	private static Level oldLevel;
	
	protected static IJDTUtils JDT_UTILS = JDTUtilsLSImpl.getInstance();

	public enum MavenProjectName {

		all_quarkus_extensions("all-quarkus-extensions"), //
		config_hover("config-hover"), //
		config_properties("config-properties"), //
		config_quickstart("config-quickstart"), //
		config_quickstart_test("config-quickstart-test"), //
		empty_maven_project("empty-maven-project"), //
		hibernate_orm_resteasy("hibernate-orm-resteasy"), //
		hibernate_orm_resteasy_yaml("hibernate-orm-resteasy-yaml"), //
		kubernetes("kubernetes"), //
		microprofile_fault_tolerance("microprofile-fault-tolerance"), //
		microprofile_health_quickstart("microprofile-health-quickstart"), //
		microprofile_lra("microprofile-lra"), //
		microprofile_context_propagation("microprofile-context-propagation"), //
		microprofile_metrics("microprofile-metrics"), //
		microprofile_opentracing("microprofile-opentracing"), //
		microprofile_openapi("microprofile-openapi"), //
		rest_client_quickstart("rest-client-quickstart"), //
		using_vertx("using-vertx");

		private final String name;

		private MavenProjectName(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}
	
	public enum GradleProjectName {

		empty_gradle_project("empty-gradle-project"), //
		quarkus_gradle_project("quarkus-gradle-project");

		private final String name;

		private GradleProjectName(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}

	@BeforeClass
	public static void setUp() {
		oldLevel = LOGGER.getLevel();
		LOGGER.setLevel(Level.INFO);
	}

	@AfterClass
	public static void tearDown() {
		LOGGER.setLevel(oldLevel);
	}
	
	protected static void setJDTUtils(IJDTUtils newUtils) {
		JDT_UTILS = newUtils;
	}

	protected static MicroProfileProjectInfo getMicroProfileProjectInfoFromMavenProject(MavenProjectName mavenProject)
			throws CoreException, Exception, JavaModelException {
		return getMicroProfileProjectInfoFromMavenProject(mavenProject,
				MicroProfilePropertiesScope.SOURCES_AND_DEPENDENCIES);
	}

	protected static MicroProfileProjectInfo getMicroProfileProjectInfoFromMavenProject(MavenProjectName mavenProject,
			List<MicroProfilePropertiesScope> scopes) throws CoreException, Exception, JavaModelException {
		IJavaProject javaProject = loadMavenProject(mavenProject);
		return PropertiesManager.getInstance().getMicroProfileProjectInfo(javaProject, scopes, ClasspathKind.SRC,
				JDT_UTILS, DocumentFormat.Markdown, new NullProgressMonitor());
	}

	public static IJavaProject loadMavenProject(MavenProjectName mavenProject) throws CoreException, Exception {
		// Load existing "hibernate-orm-resteasy" maven project
		return loadJavaProject(mavenProject.getName(), "maven");
	}
	
	public static IJavaProject loadGradleProject(GradleProjectName gradleProject) throws CoreException, Exception {
		return loadJavaProject(gradleProject.getName(), "gradle");
	}
	
	private static IJavaProject loadJavaProject(String projectName, String parentDirName) throws CoreException, Exception {
		// Move project to working directory
		File projectFolder = copyProjectToWorkingDirectory(projectName, parentDirName);

		IPath path = new Path(new File(projectFolder, "/.project").getAbsolutePath());
		IProjectDescription description = ResourcesPlugin.getWorkspace().loadProjectDescription(path);
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(description.getName());
		if (!project.exists()) {
			project.create(description, null);
			project.open(null);

			// We need to call waitForBackgroundJobs with a Job which does nothing to have a
			// resolved classpath (IJavaProject#getResolvedClasspath) when search is done.
			IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
				@Override
				public void run(IProgressMonitor monitor) throws CoreException {
					monitor.done();

				}
			};
			IProgressMonitor monitor = new NullProgressMonitor();
			JavaCore.run(runnable, null, monitor);
			waitForBackgroundJobs(monitor);
		}
		// Collect Quarkus properties from the "hibernate-orm-resteasy" project. It
		// should collect Quarkus properties from given JAR:

		// 1) quarkus-hibernate-orm.jar which is declared in the dependencies of the
		// pom.xml
		// <dependency>
		// <groupId>io.quarkus</groupId>
		// <artifactId>quarkus-hibernate-orm</artifactId>
		// </dependency>

		// 2) quarkus-hibernate-orm-deployment.jar which is declared in
		// META-INF/quarkus-extension.properties of quarkus-hibernate-orm.jar as
		// property:
		// deployment-artifact=io.quarkus\:quarkus-hibernate-orm-deployment\:0.21.1

		IJavaProject javaProject = JavaModelManager.getJavaModelManager().getJavaModel().getJavaProject(projectName);
		return javaProject;
	}

	private static File copyProjectToWorkingDirectory(String projectName,  String parentDirName) throws IOException {
		File from = new File("projects/" + parentDirName+ "/" + projectName);
		File to = new File(JavaUtils.getWorkingProjectDirectory(), parentDirName + "/" + projectName);

		if (to.exists()) {
			FileUtils.forceDelete(to);
		}

		if (from.isDirectory()) {
			FileUtils.copyDirectory(from, to);
		} else {
			FileUtils.copyFile(from, to);
		}

		return to;
	}

	private static void waitForBackgroundJobs(IProgressMonitor monitor) throws Exception {
		JobHelpers.waitForJobsToComplete(monitor);
	}

	protected static void createFile(File file, String content) throws IOException {
		Files.createDirectories(file.getParentFile().toPath());
		Files.write(file.toPath(), content.getBytes());
	}

	protected static void updateFile(File file, String content) throws IOException {
		// For Mac OS, Linux OS, the call of Files.getLastModifiedTime is working for 1
		// second.
		// Here we wait for > 1s to be sure that call of Files.getLastModifiedTime will
		// work.
		try {
			Thread.sleep(1050);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		createFile(file, content);
	}
	
	protected static void saveFile(String configFileName, String content, IJavaProject javaProject)
			throws JavaModelException, IOException {
		IPath output = javaProject.getOutputLocation();
		File file = javaProject.getProject().getLocation().append(output.removeFirstSegments(1)).append(configFileName)
				.toFile();
		updateFile(file, content);
	}
	
	protected static void deleteFile(String configFileName, IJavaProject javaProject)
			throws JavaModelException, IOException {
		IPath output = javaProject.getOutputLocation();
		File file = javaProject.getProject().getLocation().append(output.removeFirstSegments(1)).append(configFileName)
				.toFile();
		file.delete();
	}
}

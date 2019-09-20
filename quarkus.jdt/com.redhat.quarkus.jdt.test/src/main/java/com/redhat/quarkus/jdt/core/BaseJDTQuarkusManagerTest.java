/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.quarkus.jdt.core;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

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

import com.redhat.quarkus.commons.ClasspathKind;
import com.redhat.quarkus.commons.QuarkusProjectInfo;
import com.redhat.quarkus.commons.QuarkusPropertiesScope;
import com.redhat.quarkus.jdt.internal.core.JobHelpers;

/**
 * Base class for testing {@link JDTQuarkusManager} to initialize logs.
 * 
 * @author Angelo ZERR
 *
 */
public class BaseJDTQuarkusManagerTest {

	private static final Logger LOGGER = Logger.getLogger(JDTQuarkusManager.class.getSimpleName());
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

	protected static QuarkusProjectInfo getQuarkusProjectInfoFromMavenProject(String projectName)
			throws CoreException, Exception, JavaModelException {
		return getQuarkusProjectInfoFromMavenProject(projectName, QuarkusPropertiesScope.classpath);
	}

	protected static QuarkusProjectInfo getQuarkusProjectInfoFromMavenProject(String projectName,
			QuarkusPropertiesScope scope) throws CoreException, Exception, JavaModelException {
		IJavaProject javaProject = loadMavenProject(projectName);
		QuarkusProjectInfo info = JDTQuarkusManager.getInstance().getQuarkusProjectInfo(javaProject, scope,
				DocumentationConverter.DEFAULT_CONVERTER, ClasspathKind.SRC, new NullProgressMonitor());
		return info;
	}

	public static IJavaProject loadMavenProject(String projectName) throws CoreException, Exception {
		// Load existing "hibernate-orm-resteasy" maven project
		IPath path = new Path(new File("projects/maven/" + projectName + "/.project").getAbsolutePath());
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

	private static void waitForBackgroundJobs(IProgressMonitor monitor) throws Exception {
		JobHelpers.waitForJobsToComplete(monitor);
	}
}

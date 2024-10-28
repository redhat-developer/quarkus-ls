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
package com.redhat.qute.jdt;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
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

import com.redhat.qute.jdt.internal.JavaUtils;
import com.redhat.qute.jdt.internal.JobHelpers;
import com.redhat.qute.jdt.internal.ls.JDTUtilsLSImpl;
import com.redhat.qute.jdt.utils.IJDTUtils;

/**
 * Qute project test.
 *
 * @author Angelo ZERR
 *
 */
public class QuteProjectTest {

	private static final Logger LOGGER = Logger.getLogger(QuteProjectTest.class.getSimpleName());
	private static Level oldLevel;

	protected static IJDTUtils JDT_UTILS = JDTUtilsLSImpl.getInstance();

	public static class QuteMavenProjectName {

		public static String quarkus3 = "quarkus3";
		public static String quarkus_renarde_todo = "quarkus-renarde-todo";
		public static String qute_java17 = "qute-java17";
		public static String qute_messages = "qute-messages";
		public static String qute_quickstart = "qute-quickstart";
		public static String qute_record = "qute-record";
		public static String roq_blog = "roq-blog";
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

	public static void setJDTUtils(IJDTUtils newUtils) {
		JDT_UTILS = newUtils;
	}

	public static IJDTUtils getJDTUtils() {
		return JDT_UTILS;
	}

	public static IJavaProject loadMavenProject(String mavenProject) throws CoreException, Exception {
		// Load existing "hibernate-orm-resteasy" maven project
		return loadJavaProject(mavenProject, "maven");
	}

	public static IJavaProject loadGradleProject(String gradleProject) throws CoreException, Exception {
		return loadJavaProject(gradleProject, "gradle");
	}

	public static IJavaProject loadMavenProjectFromSubFolder(String mavenProject, String subFolder) throws Exception {
		return loadJavaProject(mavenProject, java.nio.file.Paths.get("maven", subFolder).toString());
	}

	private static IJavaProject loadJavaProject(String projectName, String parentDirName)
			throws CoreException, Exception {
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
			IWorkspaceRunnable runnable = monitor -> monitor.done();
			IProgressMonitor monitor = new NullProgressMonitor();
			JavaCore.run(runnable, null, monitor);
			waitForBackgroundJobs(monitor);
			JobHelpers.waitUntilIndexesReady();
		}
		return JavaModelManager.getJavaModelManager().getJavaModel().getJavaProject(description.getName());
	}

	private static File copyProjectToWorkingDirectory(String projectName, String parentDirName) throws IOException {
		File from = new File("projects/" + parentDirName + "/" + projectName);
		File to = new File(JavaUtils.getWorkingProjectDirectory(),
				java.nio.file.Paths.get(parentDirName, projectName).toString());

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

	private static void createFile(IFile file, String contents) throws CoreException {
		createParentFolders(file);
		file.refreshLocal(IResource.DEPTH_ZERO, null);
		InputStream fileContents = new ByteArrayInputStream(contents.getBytes());
		if (file.exists()) {
			file.setContents(fileContents, IResource.NONE, null);
		} else {
			file.create(fileContents, true, null);
		}
	}

	private static void createParentFolders(final IResource resource) throws CoreException {
		if (resource == null || resource.exists())
			return;
		if (!resource.getParent().exists())
			createParentFolders(resource.getParent());
		switch (resource.getType()) {
		case IResource.FOLDER:
			((IFolder) resource).create(IResource.NONE, true, new NullProgressMonitor());
			break;
		case IResource.PROJECT:
			((IProject) resource).create(new NullProgressMonitor());
			((IProject) resource).open(new NullProgressMonitor());
			break;
		}
	}

	private static void updateFile(IFile file, String content) throws CoreException {
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
			throws CoreException {
		IFile file = getFile(configFileName, javaProject);
		updateFile(file, content);
	}

	protected static void deleteFile(String configFileName, IJavaProject javaProject)
			throws IOException, CoreException {
		IFile file = getFile(configFileName, javaProject);
		file.delete(true, new NullProgressMonitor());
	}

	private static IFile getFile(String configFileName, IJavaProject javaProject) throws JavaModelException {
		IPath output = javaProject.getOutputLocation();
		IPath filePath = output.append(configFileName);
		return ResourcesPlugin.getWorkspace().getRoot().getFile(filePath);
	}
}

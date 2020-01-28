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
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;

/**
 * Java utilities.
 * 
 * @author Angelo ZERR
 *
 */
public class JavaUtils {

	private JavaUtils() {

	}

	/**
	 * Create a Java project with the given JAR.
	 * 
	 * @param projectName the Java project name
	 * @param jars        the JARS paths list
	 * @return the Java project
	 * @throws Exception
	 */
	public static IJavaProject createJavaProject(String projectName, String[] jars) throws Exception {
		IProject testProject = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		createJavaProject(testProject, new Path(getWorkingProjectDirectory().getAbsolutePath()).append(projectName),
				"src", "bin", jars, new NullProgressMonitor());
		waitForBackgroundJobs();
		return JavaCore.create(testProject);
	}

	private static void waitForBackgroundJobs() {

	}

	private static IProject createJavaProject(IProject project, IPath projectLocation, String src, String bin,
			String[] jars, IProgressMonitor monitor) throws CoreException, OperationCanceledException {
		if (project.exists()) {
			return project;
		}
		IProjectDescription description = ResourcesPlugin.getWorkspace().newProjectDescription(project.getName());
		if (projectLocation != null) {
			description.setLocation(projectLocation);
		}
		project.create(description, monitor);
		project.open(monitor);

		// Turn into Java project
		description = project.getDescription();
		description.setNatureIds(new String[] { JavaCore.NATURE_ID });
		project.setDescription(description, monitor);

		IJavaProject javaProject = JavaCore.create(project);
		// configureJVMSettings(javaProject);

		// Add build output folder
		if (StringUtils.isNotBlank(bin)) {
			IFolder output = project.getFolder(bin);
			if (!output.exists()) {
				output.create(true, true, monitor);
			}
			javaProject.setOutputLocation(output.getFullPath(), monitor);
		}

		List<IClasspathEntry> classpaths = new ArrayList<>();
		// Add source folder
		if (StringUtils.isNotBlank(src)) {
			IFolder source = project.getFolder(src);
			if (!source.exists()) {
				source.create(true, true, monitor);
			}
			IPackageFragmentRoot root = javaProject.getPackageFragmentRoot(source);
			IClasspathEntry srcClasspath = JavaCore.newSourceEntry(root.getPath());
			classpaths.add(srcClasspath);
		}

		// Add library
		if (jars != null) {
			for (String jar : jars) {
				IPackageFragmentRoot root = javaProject.getPackageFragmentRoot(jar);
				IClasspathEntry libClasspath = JavaCore.newLibraryEntry(root.getPath(), null, null);
				classpaths.add(libClasspath);
			}
		}

		// Find default JVM
		// IClasspathEntry jre = JavaRuntime.getDefaultJREContainerEntry();
		// classpaths.add(jre);

		// Add JVM to project class path
		javaProject.setRawClasspath(classpaths.toArray(new IClasspathEntry[0]), monitor);

		return project;

	}

	public static File getWorkingProjectDirectory() throws IOException {
		File dir = new File("target", "workingProjects");
		FileUtils.forceMkdir(dir);
		return dir;
	}

	/**
	 * Returns the JAR path.
	 * 
	 * @param jar the JAR name.
	 * 
	 * @return the JAR path.
	 */
	public static String getJarPath(String jar) {
		java.nio.file.Path jarPath = Paths.get("jars", jar);
		return jarPath.toAbsolutePath().toString();
	}
}
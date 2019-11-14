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

import static com.redhat.quarkus.jdt.internal.core.QuarkusConstants.QUARKUS_EXTENSION_PROPERTIES;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJarEntryResource;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.ExternalJavaProject;

import com.redhat.quarkus.commons.QuarkusPropertiesScope;
import com.redhat.quarkus.jdt.internal.core.utils.JDTQuarkusSearchUtils;

/**
 * A Java project which collect all Quarkus deployment JARs.
 * 
 * @author Angelo ZERR
 *
 */
public class QuarkusDeploymentJavaProject extends ExternalJavaProject {

	/**
	 * Artifact resolver API
	 *
	 */
	public static interface ArtifactResolver {

		String getArtifact(String groupId, String artifactId, String version);

		String getSources(String groupId, String artifactId, String version);
	}

	public static final ArtifactResolver DEFAULT_ARTIFACT_RESOLVER = new MavenArtifactResolver();

	private final IJavaProject rootProject;

	public QuarkusDeploymentJavaProject(IJavaProject rootProject, ArtifactResolver artifactResolver,
			boolean excludeTestCode) throws JavaModelException {
		super(createDeploymentClasspath(rootProject, artifactResolver, excludeTestCode));
		this.rootProject = rootProject;
	}

	/**
	 * Returns the classpath of deployment JARs.
	 * 
	 * @param project          the quarkus project
	 * @param artifactResolver the artifact resolver to use to download deployment
	 *                         JARs.
	 * @param excludeTestCode
	 * @param downloadSources
	 * @return the classpath of deployment JARs.
	 * @throws JavaModelException
	 */
	private static IClasspathEntry[] createDeploymentClasspath(IJavaProject project, ArtifactResolver artifactResolver,
			boolean excludeTestCode) throws JavaModelException {
		List<IClasspathEntry> deploymentJarEntries = new ArrayList<>();
		IClasspathEntry[] entries = project.getResolvedClasspath(true);
		List<String> existingJars = Stream.of(entries)
				// filter entry to collect only JAR
				.filter(entry -> entry.getEntryKind() == IClasspathEntry.CPE_LIBRARY)
				// filter Quarkus deployment JAR marked as test scope. Ex:
				// 'quarkus-core-deployment' can be marked as test scope, we must exclude them
				// to avoid to ignore it in the next step.
				.filter(entry -> !excludeTestCode || (excludeTestCode && !entry.isTest())) //
				.map(entry -> entry.getPath().lastSegment()).collect(Collectors.toList());
		for (IClasspathEntry entry : entries) {
			if (excludeTestCode && entry.isTest()) {
				continue;
			}
			switch (entry.getEntryKind()) {

			case IClasspathEntry.CPE_LIBRARY:

				try {
					String jarPath = entry.getPath().toOSString();
					IPackageFragmentRoot root = project.getPackageFragmentRoot(jarPath);
					if (root != null) {
						IJarEntryResource resource = JDTQuarkusSearchUtils.findPropertiesResource(root,
								QUARKUS_EXTENSION_PROPERTIES);
						if (resource != null) {
							Properties properties = new Properties();
							properties.load(resource.getContents());
							// deployment-artifact=io.quarkus\:quarkus-undertow-deployment\:0.21.1
							String deploymentArtifact = properties.getProperty("deployment-artifact");
							String[] result = deploymentArtifact.split(":");
							String groupId = result[0];
							String artifactId = result[1];
							String version = result[2];
							// Get or download deployment JAR
							String deploymentJarFile = artifactResolver.getArtifact(groupId, artifactId, version);
							if (deploymentJarFile != null) {
								IPath deploymentJarFilePath = new Path(deploymentJarFile);
								String deploymentJarName = deploymentJarFilePath.lastSegment();
								if (!existingJars.contains(deploymentJarName)) {
									// The *-deployment JAR is not included in the classpath project, add it.
									existingJars.add(deploymentJarName);
									IPath sourceAttachmentPath = null;
									// Get or download deployment sources JAR
									String sourceJarFile = artifactResolver.getSources(groupId, artifactId, version);
									if (sourceJarFile != null) {
										sourceAttachmentPath = new Path(sourceJarFile);
									}
									deploymentJarEntries.add(JavaCore.newLibraryEntry(deploymentJarFilePath,
											sourceAttachmentPath, null));
								}
							}
						}
					}
				} catch (Exception e) {
					// do nothing
				}

				break;
			}
		}
		// Add the Quarkus project in classpath to resolve dependencies of deployment
		// Quarkus JARs.
		deploymentJarEntries.add(JavaCore.newProjectEntry(project.getProject().getLocation()));
		return deploymentJarEntries.toArray(new IClasspathEntry[deploymentJarEntries.size()]);
	}

	/**
	 * Returns the java elements to search according the scope:
	 * 
	 * <ul>
	 * <li>sources scope: only Quarkus Java project</li>
	 * <li>classpatch scope:
	 * <ul>
	 * <li>the Quarkus project</li>
	 * <li>all deployment JARs</li>
	 * </ul>
	 * </li>
	 * </ul>
	 *
	 * @param propertiesScope
	 * 
	 * @return the java elements to search
	 * @throws JavaModelException
	 */
	public IJavaElement[] getElementsToSearch(QuarkusPropertiesScope propertiesScope) throws JavaModelException {
		if (propertiesScope == QuarkusPropertiesScope.sources) {
			return new IJavaElement[] { rootProject };
		}
		IPackageFragmentRoot[] roots = super.getPackageFragmentRoots();
		IJavaElement[] elements = new IJavaElement[1 + roots.length];
		elements[0] = rootProject;
		for (int i = 0; i < roots.length; i++) {
			elements[i + 1] = roots[i];
		}
		return elements;
	}
}

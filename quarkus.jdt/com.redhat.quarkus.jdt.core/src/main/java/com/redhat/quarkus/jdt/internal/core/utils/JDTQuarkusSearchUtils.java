/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.quarkus.jdt.internal.core.utils;

import static com.redhat.quarkus.jdt.internal.core.QuarkusConstants.CONFIG_PROPERTY_ANNOTATION;
import static com.redhat.quarkus.jdt.internal.core.QuarkusConstants.CONFIG_ROOT_ANNOTATION;

import java.util.HashSet;

import org.eclipse.jdt.core.IJarEntryResource;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.search.BasicSearchEngine;
import org.eclipse.jdt.internal.core.search.JavaSearchScope;

import com.redhat.quarkus.jdt.internal.core.QuarkusDeploymentJavaProject;

/**
 * JDT Quarkus search utilities.
 * 
 * @author Angelo ZERR
 *
 */
public class JDTQuarkusSearchUtils {

	private JDTQuarkusSearchUtils() {

	}

	/**
	 * Returns the JDT search pattern to search fields annotated with
	 * Quarkus @ConfigRoot and @ConfigProperty.
	 * 
	 * @return the JDT search pattern to search fields annotated with
	 *         Quarkus @ConfigRoot and @ConfigProperty.
	 */
	public static SearchPattern createQuarkusConfigSearchPattern() {
		// Pattern to search @ConfigRoot annotation
		SearchPattern configRootPattern = SearchPattern.createPattern(CONFIG_ROOT_ANNOTATION,
				IJavaSearchConstants.ANNOTATION_TYPE, IJavaSearchConstants.ANNOTATION_TYPE_REFERENCE,
				SearchPattern.R_EXACT_MATCH);
		// Pattern to search @ConfigProperty annotation
		SearchPattern configPropertyPattern = SearchPattern.createPattern(CONFIG_PROPERTY_ANNOTATION,
				IJavaSearchConstants.ANNOTATION_TYPE, IJavaSearchConstants.ANNOTATION_TYPE_REFERENCE,
				SearchPattern.R_EXACT_MATCH);
		return SearchPattern.createOrPattern(configRootPattern, configPropertyPattern);
	}

	/**
	 * Returns the JDT search scope for Quarkus project:
	 * 
	 * <ul>
	 * <li>search Quarkus properties from the Quarkus JAR of the Quarkus project
	 * classpath. Ex: quarkus-hibernate-orm.jar which is declared in the
	 * dependencies of the pom.xml</li>
	 * <li>search Quarkus properties from the Quarkus JAR deployment declared in
	 * META-INF/quarkus-extension.properties of Quarkus JAR. Ex:
	 * quarkus-hibernate-orm-deployment.jar which is declared in
	 * META-INF/quarkus-extension.properties of quarkus-hibernate-orm.jar as
	 * property:
	 * deployment-artifact=io.quarkus\:quarkus-hibernate-orm-deployment\:0.21.1
	 * </ul>
	 * 
	 * @param project the Quarkus project
	 * @return the JDT search scope for Quarkus project.
	 * @throws JavaModelException
	 */
	public static IJavaSearchScope createQuarkusSearchScope(IJavaProject project) throws JavaModelException {
		// Create a Jav aproject which collect all deployments JARs.
		QuarkusDeploymentJavaProject fakeProject = new QuarkusDeploymentJavaProject(project,
				QuarkusDeploymentJavaProject.MAVEN_ARTIFACT_RESOLVER);
		// Search in the given project and deployment JAR's.
		return createJavaSearchScope(fakeProject, false, fakeProject.getElementsToSearch(),
				/* IJavaSearchScope.SOURCES | */ IJavaSearchScope.APPLICATION_LIBRARIES);
	}

	public static IJarEntryResource findPropertiesResource(IPackageFragmentRoot packageRoot, String propertiesFileName)
			throws JavaModelException {
		Object[] resources = packageRoot.getNonJavaResources();
		if (resources != null) {
			for (Object object : resources) {
				if (object instanceof IJarEntryResource) {
					IJarEntryResource res = (IJarEntryResource) object;
					if ("META-INF".equals(res.getName())) {
						IJarEntryResource[] children = res.getChildren();
						if (children != null) {
							for (IJarEntryResource r : children) {
								if (propertiesFileName.equals(r.getName())) {
									return r;
								}
							}
						}
						return null;
					}
				}
			}
		}
		return null;
	}

	/**
	 * This code is the same than
	 * {@link BasicSearchEngine#createJavaSearchScope(boolean, IJavaElement[], boolean)}.
	 * It overrides {@link JavaSearchScope#packageFragmentRoot(String, int, String)}
	 * to search the first the package root (JAR) from the given fake project.
	 * 
	 * @param fakeProject
	 * @param excludeTestCode
	 * @param elements
	 * @param includeMask
	 * @return
	 */
	private static IJavaSearchScope createJavaSearchScope(IJavaProject fakeProject, boolean excludeTestCode,
			IJavaElement[] elements, int includeMask) {
		HashSet projectsToBeAdded = new HashSet(2);
		for (int i = 0, length = elements.length; i < length; i++) {
			IJavaElement element = elements[i];
			if (element instanceof JavaProject) {
				projectsToBeAdded.add(element);
			}
		}
		JavaSearchScope scope = new JavaSearchScope(excludeTestCode) {

			@Override
			public IPackageFragmentRoot packageFragmentRoot(String resourcePathString, int jarSeparatorIndex,
					String jarPath) {
				// Search at first in the fake project the package root to avoid creating a non
				// existing IProject (because fake project doesn't exists)
				try {
					IPackageFragmentRoot[] roots = fakeProject.getPackageFragmentRoots();
					for (IPackageFragmentRoot root : roots) {
						if (resourcePathString.startsWith(root.getPath().toOSString())) {
							return root;
						}
					}
				} catch (JavaModelException e) {
					// ignore
				}
				// Not found...
				return super.packageFragmentRoot(resourcePathString, jarSeparatorIndex, jarPath);
			}
		};
		for (int i = 0, length = elements.length; i < length; i++) {
			IJavaElement element = elements[i];
			if (element != null) {
				try {
					if (projectsToBeAdded.contains(element)) {
						scope.add((JavaProject) element, includeMask, projectsToBeAdded);
					} else {
						scope.add(element);
					}
				} catch (JavaModelException e) {
					// ignore
				}
			}
		}
		return scope;
	}

}

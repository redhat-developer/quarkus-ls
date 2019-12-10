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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.search.BasicSearchEngine;
import org.eclipse.jdt.internal.core.search.JavaSearchScope;
import org.eclipse.jdt.internal.corext.util.JavaModelUtil;
import org.eclipse.lsp4j.Location;

import com.redhat.microprofile.commons.ClasspathKind;
import com.redhat.microprofile.commons.MicroProfileProjectInfo;
import com.redhat.microprofile.commons.MicroProfileProjectInfoParams;
import com.redhat.microprofile.commons.MicroProfilePropertiesScope;
import com.redhat.microprofile.commons.MicroProfilePropertyDefinitionParams;
import com.redhat.microprofile.jdt.core.utils.IJDTUtils;
import com.redhat.microprofile.jdt.internal.core.FakeJavaProject;
import com.redhat.microprofile.jdt.internal.core.PropertiesCollector;
import com.redhat.microprofile.jdt.internal.core.PropertiesProviderRegistry;
import com.redhat.microprofile.jdt.internal.core.utils.JDTMicroProfileUtils;

/**
 * MicroProfile properties manager used to:
 * 
 * <ul>
 * <li>collect MicroProfile, Quarkus properties</li>
 * <li>find Java definition from a given property</li>
 * </ul>
 * 
 * @author Angelo ZERR
 *
 */
public class PropertiesManager {

	private static final PropertiesManager INSTANCE = new PropertiesManager();

	private static final Logger LOGGER = Logger.getLogger(PropertiesManager.class.getName());

	public static PropertiesManager getInstance() {
		return INSTANCE;
	}

	private PropertiesManager() {

	}

	public MicroProfileProjectInfo getMicroProfileProjectInfo(MicroProfileProjectInfoParams params, IJDTUtils utils,
			IProgressMonitor progress) throws JavaModelException, CoreException {
		IFile file = utils.findFile(params.getUri());
		if (file == null) {
			throw new UnsupportedOperationException(String.format("Cannot find IFile for '%s'", params.getUri()));
		}
		return getMicroProfileProjectInfo(file, params.getScopes(), progress);
	}

	public MicroProfileProjectInfo getMicroProfileProjectInfo(IFile file, List<MicroProfilePropertiesScope> scopes,
			IProgressMonitor progress) throws JavaModelException, CoreException {
		String projectName = file.getProject().getName();
		IJavaProject javaProject = JavaModelManager.getJavaModelManager().getJavaModel().getJavaProject(projectName);
		ClasspathKind classpathKind = JDTMicroProfileUtils.getClasspathKind(file, javaProject);
		return getMicroProfileProjectInfo(javaProject, scopes, classpathKind, progress);
	}

	public MicroProfileProjectInfo getMicroProfileProjectInfo(IJavaProject javaProject,
			List<MicroProfilePropertiesScope> scopes, ClasspathKind classpathKind, IProgressMonitor monitor)
			throws JavaModelException, CoreException {
		MicroProfileProjectInfo info = createInfo(javaProject, classpathKind);
		if (classpathKind == ClasspathKind.NONE) {
			info.setProperties(Collections.emptyList());
			return info;
		}
		long startTime = System.currentTimeMillis();
		if (LOGGER.isLoggable(Level.INFO)) {
			LOGGER.info("Start computing MicroProfile properties for '" + info.getProjectURI() + "' project.");
		}
		PropertiesCollector collector = new PropertiesCollector(info);
		SearchContext context = new SearchContext(javaProject);
		try {
			beginSearch(context);
			// Create pattern
			SearchPattern pattern = createSearchPattern();
			SearchEngine engine = new SearchEngine();
			IJavaSearchScope scope = createSearchScope(javaProject, scopes, classpathKind == ClasspathKind.SRC);
			engine.search(pattern, new SearchParticipant[] { SearchEngine.getDefaultSearchParticipant() }, scope,
					new SearchRequestor() {

						@Override
						public void acceptSearchMatch(SearchMatch match) throws CoreException {
							collectProperties(match, context, collector, monitor);
						}
					}, monitor);
		} finally {
			endSearch(context);
			if (LOGGER.isLoggable(Level.INFO)) {
				LOGGER.info("End computing MicroProfile properties for '" + info.getProjectURI() + "' project in "
						+ (System.currentTimeMillis() - startTime) + "ms.");
			}
		}
		return info;
	}

	private void beginSearch(SearchContext context) {
		for (IPropertiesProvider provider : getPropertiesProviders()) {
			provider.begin(context);
		}
	}

	private void endSearch(SearchContext context) {
		for (IPropertiesProvider provider : getPropertiesProviders()) {
			provider.end(context);
		}
	}

	private void collectProperties(SearchMatch match, SearchContext context, PropertiesCollector collector,
			IProgressMonitor monitor) {
		for (IPropertiesProvider provider : getPropertiesProviders()) {
			provider.collectProperties(match, context, collector, monitor);
		}
	}

	private static MicroProfileProjectInfo createInfo(IJavaProject javaProject, ClasspathKind classpathKind) {
		MicroProfileProjectInfo info = new MicroProfileProjectInfo();
		info.setProjectURI(JDTMicroProfileUtils.getProjectURI(javaProject));
		info.setClasspathKind(classpathKind);
		return info;
	}

	private IJavaSearchScope createSearchScope(IJavaProject project, List<MicroProfilePropertiesScope> scopes,
			boolean excludeTestCode) throws JavaModelException {
		int searchScope = 0;
		for (MicroProfilePropertiesScope scope : scopes) {
			switch (scope) {
			case sources:
				searchScope = searchScope | IJavaSearchScope.SOURCES;
				break;
			case dependencies:
				searchScope = searchScope | IJavaSearchScope.APPLICATION_LIBRARIES;
				break;
			}
		}
		List<IClasspathEntry> newClasspathEntries = new ArrayList<>();
		for (IPropertiesProvider provider : getPropertiesProviders()) {
			provider.contributeToClasspath(project, excludeTestCode, ArtifactResolver.DEFAULT_ARTIFACT_RESOLVER,
					newClasspathEntries);
		}
		if (!newClasspathEntries.isEmpty()) {
			FakeJavaProject fakeProject = new FakeJavaProject(project,
					newClasspathEntries.toArray(new IClasspathEntry[newClasspathEntries.size()]));
			return createJavaSearchScope(fakeProject, excludeTestCode, fakeProject.getElementsToSearch(scopes),
					searchScope);
		}
		return BasicSearchEngine.createJavaSearchScope(excludeTestCode, new IJavaElement[] { project });
	}

	private SearchPattern createSearchPattern() {
		SearchPattern leftPattern = null;
		for (IPropertiesProvider provider : getPropertiesProviders()) {
			if (leftPattern == null) {
				leftPattern = provider.createSearchPattern();
			} else {
				SearchPattern rightPattern = provider.createSearchPattern();
				if (rightPattern != null) {
					leftPattern = SearchPattern.createOrPattern(leftPattern, rightPattern);
				}
			}
		}
		return leftPattern;
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

	List<IPropertiesProvider> getPropertiesProviders() {
		return PropertiesProviderRegistry.getInstance().getPropertiesProviders();
	}

	// ---------------------------------- Properties definition

	public Location findPropertyLocation(MicroProfilePropertyDefinitionParams params, IJDTUtils utils,
			IProgressMonitor progress) throws JavaModelException, CoreException {
		IFile file = utils.findFile(params.getUri());
		if (file == null) {
			throw new UnsupportedOperationException(String.format("Cannot find IFile for '%s'", params.getUri()));
		}
		return findPropertyLocation(file, params.getSourceType(), params.getSourceField(), params.getSourceMethod(),
				utils, progress);
	}

	public Location findPropertyLocation(IFile file, String sourceType, String sourceField, String sourceMethod,
			IJDTUtils utils, IProgressMonitor progress) throws JavaModelException, CoreException {
		IMember fieldOrMethod = findDeclaredProperty(file, sourceType, sourceField, sourceMethod, progress);
		if (fieldOrMethod != null) {
			IClassFile classFile = fieldOrMethod.getClassFile();
			if (classFile != null) {
				// Try to download source if required
				if (utils != null) {
					utils.discoverSource(classFile, progress);
				}
			}
			return utils.toLocation(fieldOrMethod);
		}
		return null;
	}

	/**
	 * Returns the Java field from the given property source
	 * 
	 * @param file           the application.properties file
	 * @param propertySource the property source to find
	 * @param progress       the progress monitor.
	 * @return the Java field from the given property source
	 * @throws JavaModelException
	 * @throws CoreException
	 */
	public IMember findDeclaredProperty(IFile file, String sourceType, String sourceField, String sourceMethod,
			IProgressMonitor progress) throws JavaModelException, CoreException {
		String projectName = file.getProject().getName();
		IJavaProject javaProject = JavaModelManager.getJavaModelManager().getJavaModel().getJavaProject(projectName);
		return findDeclaredProperty(javaProject, sourceType, sourceField, sourceMethod, progress);
	}

	/**
	 * Returns the Java field from the given property source
	 * 
	 * @param javaProject  the Java project
	 * @param sourceType   the source type (class or interface)
	 * @param sourceField  the source field and null otherwise.
	 * @param sourceMethod the source method and null otherwise.
	 * @param progress     the progress monitor.
	 * @return the Java field from the given property sources
	 * @throws JavaModelException
	 */
	public IMember findDeclaredProperty(IJavaProject javaProject, String sourceType, String sourceField,
			String sourceMethod, IProgressMonitor progress) throws JavaModelException {
		if (sourceType == null) {
			return null;
		}
		// Try to find type with standard classpath
		IType type = javaProject.findType(sourceType, progress);
		if (type == null) {
			// Not found, type could be included in deployment JAR which is not in classpath
			// Try to find type from deployment JAR
			List<IClasspathEntry> newClasspathEntries = new ArrayList<>();
			for (IPropertiesProvider provider : getPropertiesProviders()) {
				provider.contributeToClasspath(javaProject, false, ArtifactResolver.DEFAULT_ARTIFACT_RESOLVER,
						newClasspathEntries);
			}
			if (!newClasspathEntries.isEmpty()) {
				FakeJavaProject fakeProject = new FakeJavaProject(javaProject,
						newClasspathEntries.toArray(new IClasspathEntry[newClasspathEntries.size()]));
				type = fakeProject.findType(sourceType, progress);
			}
		}
		if (type == null) {
			return null;
		}
		if (sourceField != null) {
			return type.getField(sourceField);
		}
		if (sourceMethod != null) {
			int startBracketIndex = sourceMethod.indexOf('(');
			String methodName = sourceMethod.substring(0, startBracketIndex);
			// Method signature has been generated with JDT API, so we are sure that we have
			// a ')' character.
			int endBracketIndex = sourceMethod.indexOf(')');
			String methodSignature = sourceMethod.substring(startBracketIndex + 1, endBracketIndex);
			String[] paramTypes = methodSignature.isEmpty() ? CharOperation.NO_STRINGS
					: Signature.getParameterTypes(methodSignature);
			return JavaModelUtil.findMethod(methodName, paramTypes, false, type);
		}
		return type;
	}

}

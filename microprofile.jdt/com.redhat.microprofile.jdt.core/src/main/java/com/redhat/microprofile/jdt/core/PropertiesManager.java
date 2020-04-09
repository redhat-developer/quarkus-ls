/*******************************************************************************
* Copyright (c) 2019-2020 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.microprofile.jdt.core;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
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
import com.redhat.microprofile.commons.DocumentFormat;
import com.redhat.microprofile.commons.MicroProfileProjectInfo;
import com.redhat.microprofile.commons.MicroProfileProjectInfoParams;
import com.redhat.microprofile.commons.MicroProfilePropertiesScope;
import com.redhat.microprofile.commons.MicroProfilePropertyDefinitionParams;
import com.redhat.microprofile.jdt.core.utils.IJDTUtils;
import com.redhat.microprofile.jdt.core.utils.JDTMicroProfileUtils;
import com.redhat.microprofile.jdt.internal.core.FakeJavaProject;
import com.redhat.microprofile.jdt.internal.core.PropertiesCollector;
import com.redhat.microprofile.jdt.internal.core.PropertiesProviderRegistry;

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
		if (file == null || file.getProject() == null) {
			// The uri doesn't belong to an Eclipse project
			return MicroProfileProjectInfo.EMPTY_PROJECT_INFO;
		}
		// The uri belong to an Eclipse project
		if (!(JavaProject.hasJavaNature(file.getProject()))) {
			// The uri doesn't belong to a Java project
			return createInfo(file.getProject(), ClasspathKind.NONE);
		}
		return getMicroProfileProjectInfo(file, params.getScopes(), utils, params.getDocumentFormat(), progress);
	}

	public MicroProfileProjectInfo getMicroProfileProjectInfo(IFile file, List<MicroProfilePropertiesScope> scopes,
			IJDTUtils utils, DocumentFormat documentFormat, IProgressMonitor progress)
			throws JavaModelException, CoreException {
		String projectName = file.getProject().getName();
		IJavaProject javaProject = JavaModelManager.getJavaModelManager().getJavaModel().getJavaProject(projectName);
		ClasspathKind classpathKind = JDTMicroProfileUtils.getClasspathKind(file, javaProject);
		return getMicroProfileProjectInfo(javaProject, scopes, classpathKind, utils, documentFormat, progress);
	}

	public MicroProfileProjectInfo getMicroProfileProjectInfo(IJavaProject javaProject,
			List<MicroProfilePropertiesScope> scopes, ClasspathKind classpathKind, IJDTUtils utils,
			DocumentFormat documentFormat, IProgressMonitor monitor) throws JavaModelException, CoreException {
		MicroProfileProjectInfo info = createInfo(javaProject.getProject(), classpathKind);
		if (classpathKind == ClasspathKind.NONE) {
			info.setProperties(Collections.emptyList());
			return info;
		}
		long startTime = System.currentTimeMillis();
		if (LOGGER.isLoggable(Level.INFO)) {
			LOGGER.info("Start computing MicroProfile properties for '" + info.getProjectURI() + "' project.");
		}
		SubMonitor mainMonitor = SubMonitor.convert(monitor,
				"Scanning properties for '" + javaProject.getProject().getName() + "' project", 100);
		try {
			boolean excludeTestCode = classpathKind == ClasspathKind.SRC;

			// Step1 (50%) : get the java project used for the search
			IJavaProject javaProjectForSearch = configureSearchClasspath(javaProject, excludeTestCode, scopes,
					mainMonitor.split(50));
			if (mainMonitor.isCanceled()) {
				throw new OperationCanceledException();
			}

			// Step2 (50%) : scan Java classes from the search classpath
			scanJavaClasses(javaProjectForSearch, excludeTestCode, documentFormat, scopes, info, utils,
					mainMonitor.split(50));
			if (mainMonitor.isCanceled()) {
				throw new OperationCanceledException();
			}
		} finally {
			if (LOGGER.isLoggable(Level.INFO)) {
				LOGGER.info("End computing MicroProfile properties for '" + info.getProjectURI() + "' project in "
						+ (System.currentTimeMillis() - startTime) + "ms.");
			}
			mainMonitor.done();
		}
		return info;
	}

	/**
	 * Configure the classpath used for the search of MicroProfile properties. At
	 * this step we can add new JARs to use for the search (ex : for Quarkus we add
	 * deployment JAR where Quarkus properties are defined).
	 * 
	 * @param javaProject     the original Java project
	 * @param excludeTestCode true if test must be excluded and false otherwise.
	 * @param scopes
	 * @param mainMonitor     the main progress monitor.
	 * @return the Java project which hosts original JARs and new JARs to use for
	 *         the search.
	 * @throws JavaModelException
	 */
	public IJavaProject configureSearchClasspath(IJavaProject javaProject, boolean excludeTestCode,
			List<MicroProfilePropertiesScope> scopes, IProgressMonitor monitor) throws JavaModelException {
		SubMonitor mainMonitor = SubMonitor.convert(monitor);
		// Get the java project used for the search
		mainMonitor.subTask("Configuring search classpath");
		int length = getPropertiesProviders().size();
		SubMonitor subMonitor = mainMonitor.setWorkRemaining(length + 1);
		subMonitor.split(1); // give feedback to the user that something is happening
		try {
			return getJavaProject(javaProject, excludeTestCode, scopes, subMonitor);
		} finally {
			subMonitor.done();
		}
	}

	/**
	 * Execute the Java search to collect MicroProfile, Quarkus, etc properties.
	 * 
	 * @param javaProjectForSearch Java project which hosts original JARs and new
	 *                             JARs to use for the search.
	 * @param excludeTestCode      true if test must be excluded and false
	 *                             otherwise.
	 * @param documentFormat       the document format to use to format Javadoc (in
	 *                             Markdown for instance)
	 * @param scopes               the scopes
	 * @param info                 the project information to update.
	 * @param utils                the JDT LS utilities
	 * @param mainMonitor          the main progress monitor.
	 * @throws JavaModelException
	 * @throws CoreException
	 */
	private void scanJavaClasses(IJavaProject javaProjectForSearch, boolean excludeTestCode,
			DocumentFormat documentFormat, List<MicroProfilePropertiesScope> scopes, MicroProfileProjectInfo info,
			IJDTUtils utils, SubMonitor mainMonitor) throws JavaModelException, CoreException {
		// Create JDT Java search pattern, engine and scope
		mainMonitor.subTask("Scanning Java classes");
		SubMonitor subMonitor = mainMonitor.setWorkRemaining(100);
		try {
			subMonitor.split(5); // give feedback to the user that something is happening

			SearchPattern pattern = createSearchPattern();
			SearchEngine engine = new SearchEngine();
			IJavaSearchScope scope = createSearchScope(javaProjectForSearch, scopes, excludeTestCode, subMonitor);

			// Execute the search
			PropertiesCollector collector = new PropertiesCollector(info, scopes);
			SearchContext context = new SearchContext(javaProjectForSearch, collector, utils, documentFormat, scopes);
			beginSearch(context, subMonitor);
			engine.search(pattern, new SearchParticipant[] { SearchEngine.getDefaultSearchParticipant() }, scope,
					new SearchRequestor() {

						@Override
						public void acceptSearchMatch(SearchMatch match) throws CoreException {
							collectProperties(match, context, subMonitor);
						}
					}, subMonitor);
			endSearch(context, subMonitor);
		} finally {
			subMonitor.done();
		}
	}

	private void beginSearch(SearchContext context, IProgressMonitor monitor) {
		for (IPropertiesProvider provider : getPropertiesProviders()) {
			provider.beginSearch(context, monitor);
		}
	}

	private void endSearch(SearchContext context, IProgressMonitor monitor) {
		for (IPropertiesProvider provider : getPropertiesProviders()) {
			provider.endSearch(context, monitor);
		}
	}

	private void collectProperties(SearchMatch match, SearchContext context, IProgressMonitor monitor) {
		for (IPropertiesProvider provider : getPropertiesProviders()) {
			provider.collectProperties(match, context, monitor);
		}
	}

	private static MicroProfileProjectInfo createInfo(IProject project, ClasspathKind classpathKind) {
		MicroProfileProjectInfo info = new MicroProfileProjectInfo();
		info.setProjectURI(JDTMicroProfileUtils.getProjectURI(project));
		info.setClasspathKind(classpathKind);
		if (classpathKind == ClasspathKind.NONE) {
			info.setProperties(Collections.emptyList());
		}
		return info;
	}

	private IJavaSearchScope createSearchScope(IJavaProject project, List<MicroProfilePropertiesScope> scopes,
			boolean excludeTestCode, IProgressMonitor monitor) throws JavaModelException {
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
		if (project instanceof FakeJavaProject) {
			// Extra classpath (search must be done for external JAR not included in the
			// classpath like Quarkus deployment JARs)
			FakeJavaProject fakeProject = (FakeJavaProject) project;
			return createJavaSearchScope(fakeProject, excludeTestCode, fakeProject.getElementsToSearch(scopes),
					searchScope);
		}
		// Standard Java Search in the project.
		// The search scope is used to search in src, jars
		return BasicSearchEngine.createJavaSearchScope(excludeTestCode, new IJavaElement[] { project }, searchScope);
	}

	/**
	 * Returns the java project used for search. This java project is the original
	 * java project with extra JARs which can be added by a properties provoder (ex
	 * : deployment JAR for Quarkus).
	 * 
	 * <p>
	 * To avoid disturbing the classpath of the origin java project, a fake java
	 * project is created with the origin java project and extras JARs.
	 * </p>
	 * 
	 * @param javaProject     the origin java project
	 * @param excludeTestCode true if test must me excluded and false otherwise.
	 * @param scopes
	 * @param monitor         the progress monitor.
	 * @return the java project used for search.
	 * @throws JavaModelException
	 */
	private IJavaProject getJavaProject(IJavaProject javaProject, boolean excludeTestCode,
			List<MicroProfilePropertiesScope> scopes, SubMonitor monitor) throws JavaModelException {
		if (javaProject instanceof FakeJavaProject) {
			// The java project is already resolved
			return javaProject;
		}
		SubMonitor mainMonitor = monitor;
		BuildingScopeContext context = new BuildingScopeContext(javaProject, excludeTestCode, scopes,
				ArtifactResolver.DEFAULT_ARTIFACT_RESOLVER);
		beginBuildingScope(context, mainMonitor);
		contributeToClasspath(context, mainMonitor);
		endBuildingScope(context, mainMonitor);
		List<IClasspathEntry> searchClasspathEntries = context.getSearchClassPathEntries();
		if (!searchClasspathEntries.isEmpty()) {
			return new FakeJavaProject(javaProject, searchClasspathEntries);
		}
		return javaProject;
	}

	private void beginBuildingScope(BuildingScopeContext context, IProgressMonitor monitor) {
		for (IPropertiesProvider provider : getPropertiesProviders()) {
			provider.beginBuildingScope(context, monitor);
		}
	}

	private void contributeToClasspath(BuildingScopeContext context, SubMonitor mainMonitor)
			throws OperationCanceledException, JavaModelException {
		int length = getPropertiesProviders().size();
		for (int i = 0; i < length; i++) {
			mainMonitor.subTask("Contributing to classpath for provider (" + (i + 1) + "/" + length + ")");
			SubMonitor subMonitor = mainMonitor.split(1);
			IPropertiesProvider provider = getPropertiesProviders().get(i);
			provider.contributeToClasspath(context, subMonitor);
			subMonitor.done();
		}
	}

	private void endBuildingScope(BuildingScopeContext context, IProgressMonitor monitor) {
		for (IPropertiesProvider provider : getPropertiesProviders()) {
			provider.endBuildingScope(context, monitor);
		}
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
		String projectName = file.getProject().getName();
		IJavaProject javaProject = JavaModelManager.getJavaModelManager().getJavaModel().getJavaProject(projectName);
		return findPropertyLocation(javaProject, params.getSourceType(), params.getSourceField(),
				params.getSourceMethod(), utils, progress);
	}

	public Location findPropertyLocation(IJavaProject javaProject, String sourceType, String sourceField,
			String sourceMethod, IJDTUtils utils, IProgressMonitor progress) throws JavaModelException, CoreException {
		IMember fieldOrMethod = findDeclaredProperty(javaProject, sourceType, sourceField, sourceMethod, progress);
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
	 * @param javaProject  the Java project
	 * @param sourceType   the source type (class or interface)
	 * @param sourceField  the source field and null otherwise.
	 * @param sourceMethod the source method and null otherwise.
	 * @param monitor      the progress monitor.
	 * @return the Java field from the given property sources
	 * @throws JavaModelException
	 */
	public IMember findDeclaredProperty(IJavaProject javaProject, String sourceType, String sourceField,
			String sourceMethod, IProgressMonitor monitor) throws JavaModelException {
		String title = getMonitorTitle(javaProject, sourceType, sourceField, sourceMethod);
		SubMonitor mainMonitor = SubMonitor.convert(monitor, title, 100);
		try {
			if (sourceType == null) {
				return null;
			}
			IJavaProject fakeProject = null;
			if (javaProject instanceof FakeJavaProject) {
				fakeProject = javaProject;
				javaProject = ((FakeJavaProject) fakeProject).getRootProject();
			}
			// Step1 (20%) : try to find type with the standard classpath
			mainMonitor.subTask("Finding type with the standard classpath");
			SubMonitor subMonitor = mainMonitor.split(20).setWorkRemaining(100);
			subMonitor.split(5); // give feedback to the user that something is happening
			IType type = javaProject.findType(sourceType, subMonitor);
			subMonitor.done();
			if (mainMonitor.isCanceled()) {
				throw new OperationCanceledException();
			}

			// Step2 (80%) : try to find type with the search classpath
			mainMonitor.subTask("Finding type with the search classpath");
			subMonitor = mainMonitor.split(80).setWorkRemaining(100);
			subMonitor.split(5); // give feedback to the user that something is happening
			if (type == null) {
				// Not found, type could be included in deployment JAR which is not in classpath
				// Try to find type from deployment JAR
				if (fakeProject == null) {
					fakeProject = configureSearchClasspath(javaProject, false,
							MicroProfilePropertiesScope.SOURCES_AND_DEPENDENCIES, subMonitor);
				}
				if (mainMonitor.isCanceled()) {
					throw new OperationCanceledException();
				}
				type = fakeProject.findType(sourceType, subMonitor);
			}
			subMonitor.done();
			if (mainMonitor.isCanceled()) {
				throw new OperationCanceledException();
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
				String methodSignature = sourceMethod.substring(startBracketIndex, endBracketIndex + 1);
				String[] paramTypes = methodSignature.isEmpty() ? CharOperation.NO_STRINGS
						: Signature.getParameterTypes(methodSignature);

				// try findMethod for non constructor. If result is null, findMethod for
				// constructor
				IMethod method = JavaModelUtil.findMethod(methodName, paramTypes, false, type);
				return method != null ? method : JavaModelUtil.findMethod(methodName, paramTypes, true, type);
			}
			return type;
		} finally {
			mainMonitor.done();
		}
	}

	private static String getMonitorTitle(IJavaProject javaProject, String sourceType, String sourceField,
			String sourceMethod) {
		StringBuilder title = new StringBuilder("Finding declared property");
		if (sourceField == null && sourceMethod == null) {
			title.append(" for type '");
			title.append(sourceType);
			title.append("'");
		} else {
			if (sourceField != null) {
				title.append(" for field '");
				title.append(sourceField);
				title.append("'");
			} else {
				title.append(" for method '");
				title.append(sourceMethod);
				title.append("'");
			}
			title.append(" of the type '");
			title.append(sourceType);
			title.append("'");
		}
		title.append(" in the '");
		title.append(javaProject.getProject().getName());
		title.append("' project ");
		return title.toString();
	}

}

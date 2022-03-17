/*******************************************************************************
* Copyright (c) 2022 Red Hat Inc. and others.
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License v. 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
* which is available at https://www.apache.org/licenses/LICENSE-2.0.
*
* SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.jdt.internal.template.datamodel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;

import com.redhat.qute.commons.QuteProjectScope;
import com.redhat.qute.commons.datamodel.DataModelParameter;
import com.redhat.qute.commons.datamodel.DataModelProject;
import com.redhat.qute.commons.datamodel.DataModelTemplate;
import com.redhat.qute.commons.datamodel.resolvers.NamespaceResolverInfo;
import com.redhat.qute.jdt.internal.AbstractQuteExtensionPointRegistry;
import com.redhat.qute.jdt.template.datamodel.IDataModelProvider;
import com.redhat.qute.jdt.template.datamodel.SearchContext;
import com.redhat.qute.jdt.utils.JDTQuteProjectUtils;

/**
 * Registry to handle instances of {@link IDataModelProvider}
 *
 * @author Angelo ZERR
 */
public class DataModelProviderRegistry extends AbstractQuteExtensionPointRegistry<IDataModelProvider> {

	private static final Logger LOGGER = Logger.getLogger(DataModelProviderRegistry.class.getName());

	private static final String DATA_MODEL_PROVIDERS_EXTENSION_POINT_ID = "dataModelProviders";

	private static final String NAMESPACES_ATTR = "namespaces";

	private static final String DESCRIPTION_ATTR = "description";

	private static final String URL_ATTR = "url";

	private static final DataModelProviderRegistry INSTANCE = new DataModelProviderRegistry();

	private DataModelProviderRegistry() {
		super();
	}

	public static DataModelProviderRegistry getInstance() {
		return INSTANCE;
	}

	@Override
	public String getProviderExtensionId() {
		return DATA_MODEL_PROVIDERS_EXTENSION_POINT_ID;
	}

	@Override
	protected IDataModelProvider createProvider(IConfigurationElement ce) throws CoreException {
		IDataModelProvider provider = super.createProvider(ce);
		String namespaces = ce.getAttribute(NAMESPACES_ATTR);
		if (StringUtils.isNotEmpty(namespaces)) {
			String description = ce.getAttribute(DESCRIPTION_ATTR);
			String url = ce.getAttribute(URL_ATTR);
			NamespaceResolverInfo info = new NamespaceResolverInfo();
			info.setNamespaces(Arrays.asList(namespaces.split(",")));
			info.setDescription(description);
			info.setUrl(url);
			provider.setNamespaceResolverInfo(info);
		}
		return provider;
	}

	/**
	 * Returns the data model project for the given java project.
	 * 
	 * @param javaProject the java project.
	 * @param scopes      the scopes used to scan Java classes.
	 * @param monitor     the progress monitor.
	 * @return the data model project for the given java project.
	 * 
	 * @throws CoreException
	 */
	public DataModelProject<DataModelTemplate<DataModelParameter>> getDataModelProject(IJavaProject javaProject,
			List<QuteProjectScope> scopes, IProgressMonitor monitor) throws CoreException {
		DataModelProject<DataModelTemplate<DataModelParameter>> project = new DataModelProject<DataModelTemplate<DataModelParameter>>();
		project.setTemplates(new ArrayList<>());
		project.setNamespaceResolverInfos(new HashMap<>());
		project.setValueResolvers(new ArrayList<>());
		collectDataModel(project, javaProject, scopes, monitor);
		return project;
	}

	private void collectDataModel(DataModelProject<DataModelTemplate<DataModelParameter>> project,
			IJavaProject javaProject, List<QuteProjectScope> scopes, IProgressMonitor monitor) throws CoreException {
		long startTime = System.currentTimeMillis();
		if (LOGGER.isLoggable(Level.INFO)) {
			LOGGER.info("Start collecting Qute data model for '" + JDTQuteProjectUtils.getProjectUri(javaProject)
					+ "' project.");
		}
		SubMonitor mainMonitor = SubMonitor.convert(monitor,
				"Scanning data model for '" + javaProject.getProject().getName() + "' project in '" + scopes.stream() //
						.map(QuteProjectScope::name) //
						.collect(Collectors.joining("+")) //
						+ "'",
				100);
		try {
			boolean excludeTestCode = true;

			// scan Java classes from the search classpath
			scanJavaClasses(javaProject, excludeTestCode, scopes, project, mainMonitor.split(100));
			if (mainMonitor.isCanceled()) {
				throw new OperationCanceledException();
			}
		} finally {
			if (LOGGER.isLoggable(Level.INFO)) {
				LOGGER.info("End collecting Qute data model for '" + JDTQuteProjectUtils.getProjectUri(javaProject)
						+ "' project in " + (System.currentTimeMillis() - startTime) + "ms.");
			}
			mainMonitor.done();
		}
	}

	private void scanJavaClasses(IJavaProject javaProject, boolean excludeTestCode, List<QuteProjectScope> scopes,
			DataModelProject<DataModelTemplate<DataModelParameter>> project, SubMonitor mainMonitor)
			throws JavaModelException, CoreException {
		// Create JDT Java search pattern, engine and scope
		mainMonitor.subTask("Scanning Java classes");
		SubMonitor subMonitor = mainMonitor.setWorkRemaining(100);
		try {
			subMonitor.split(5); // give feedback to the user that something is happening

			SearchPattern pattern = createSearchPattern();
			SearchEngine engine = new SearchEngine();
			IJavaSearchScope scope = createSearchScope(javaProject, scopes, excludeTestCode, subMonitor);

			// Execute the search
			SearchContext context = new SearchContext(javaProject, project, null, scopes);
			beginSearch(context, subMonitor);
			engine.search(pattern, new SearchParticipant[] { SearchEngine.getDefaultSearchParticipant() }, scope,
					new SearchRequestor() {

						@Override
						public void acceptSearchMatch(SearchMatch match) throws CoreException {
							// We collect only references from java code and not from JavaDoc

							// --> In this case @Named will be collected :
							// @Named
							// private String foo;

							// --> In this case @Named will not be collected :
							// /* Demonstrate {@link Named} */
							// private String foo;

							if (!match.isInsideDocComment()) {
								collectDataModel(match, context, subMonitor);
							}
						}
					}, subMonitor);
			endSearch(context, subMonitor);
		} finally {
			subMonitor.done();
		}
	}

	private void beginSearch(SearchContext context, IProgressMonitor monitor) {
		for (IDataModelProvider provider : getProviders()) {
			provider.beginSearch(context, monitor);
		}
	}

	private void endSearch(SearchContext context, IProgressMonitor monitor) {
		for (IDataModelProvider provider : getProviders()) {
			provider.endSearch(context, monitor);
		}
	}

	private SearchPattern createSearchPattern() {
		SearchPattern leftPattern = null;
		for (IDataModelProvider provider : getProviders()) {
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

	private void collectDataModel(SearchMatch match, SearchContext context, IProgressMonitor monitor) {
		for (IDataModelProvider provider : getProviders()) {
			try {
				provider.collectDataModel(match, context, monitor);
			} catch (Exception e) {
				LOGGER.log(Level.SEVERE,
						"Error while collecting data model with the provider '" + provider.getClass().getName() + "'.",
						e);
			}
		}
	}

	private IJavaSearchScope createSearchScope(IJavaProject project, List<QuteProjectScope> scopes,
			boolean excludeTestCode, IProgressMonitor monitor) throws JavaModelException {
		int searchScope = 0;
		for (QuteProjectScope scope : scopes) {
			switch (scope) {
			case sources:
				searchScope = searchScope | IJavaSearchScope.SOURCES;
				break;
			case dependencies:
				searchScope = searchScope | IJavaSearchScope.APPLICATION_LIBRARIES;
				break;
			}
		}
		// Standard Java Search in the project.
		// The search scope is used to search in src, jars
		return SearchEngine.createJavaSearchScope(excludeTestCode, new IJavaElement[] { project }, searchScope);
	}
}

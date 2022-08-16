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
package com.redhat.qute.jdt.internal.template;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.TypeNameMatch;
import org.eclipse.jdt.core.search.TypeNameMatchRequestor;
import org.eclipse.jdt.internal.core.search.BasicSearchEngine;

import com.redhat.qute.commons.JavaTypeInfo;
import com.redhat.qute.commons.JavaTypeKind;
import com.redhat.qute.jdt.internal.resolver.AbstractTypeResolver;
import com.redhat.qute.jdt.utils.JDTTypeUtils;

/**
 * Java types search for a given pattern and project Uri.
 * 
 * @author Angelo ZERR
 *
 */
public class JavaTypesSearch {

	private static final Logger LOGGER = Logger.getLogger(JavaTypesSearch.class.getName());

	private final IJavaProject javaProject;

	private final String packageName;

	private final String typeName;

	private final IJavaSearchScope scope;

	public JavaTypesSearch(String pattern, IJavaProject javaProject) {
		this.javaProject = javaProject;

		String typeName = pattern;
		String packageName = null;
		int searchScope = IJavaSearchScope.SOURCES;
		IType innerClass = null;
		if (StringUtils.isNotEmpty(typeName)) {
			searchScope = IJavaSearchScope.SOURCES | IJavaSearchScope.APPLICATION_LIBRARIES
					| IJavaSearchScope.SYSTEM_LIBRARIES;
			int index = typeName.lastIndexOf('.');
			if (index != -1) {
				// ex : pattern = org.acme.qute.It
				// -> packageName = org.acme.qute
				// -> typeName = It
				packageName = typeName.substring(0, index);
				typeName = typeName.substring(index + 1, typeName.length());
				// support for inner class
				try {
					innerClass = javaProject.findType(packageName);
					if (innerClass != null) {
						packageName = null;
					}
				} catch (JavaModelException e) {
					LOGGER.log(Level.SEVERE, "Error while getting inner class for '" + packageName + "'.", e);
				}
			}
		}

		typeName += "*";
		this.typeName = typeName;
		this.packageName = packageName;
		this.scope = BasicSearchEngine.createJavaSearchScope(true,
				new IJavaElement[] { innerClass != null ? innerClass : javaProject }, searchScope);
	}

	public List<JavaTypeInfo> search(IProgressMonitor monitor) throws JavaModelException {
		List<JavaTypeInfo> javaTypes = new ArrayList<>();
		collectPackages(javaTypes);
		collectClassesAndInterfaces(monitor, javaTypes);
		return javaTypes;
	}

	private void collectPackages(List<JavaTypeInfo> javaTypes) {
		if (packageName != null) {
			Set<String> subPackages = new HashSet<>();
			try {
				// Loop for package root
				IPackageFragmentRoot[] packageFragmentRoots = javaProject.getPackageFragmentRoots();
				for (int i = 0; i < packageFragmentRoots.length; i++) {
					fillWithSubPackages(packageName, packageFragmentRoots[i], subPackages);
				}
			} catch (JavaModelException e) {
				LOGGER.log(Level.SEVERE, "Error while collecting sub packages for '" + packageName + "'.", e);
			}

			for (String subPackageName : subPackages) {
				JavaTypeInfo packageInfo = new JavaTypeInfo();
				packageInfo.setJavaTypeKind(JavaTypeKind.Package);
				packageInfo.setSignature(subPackageName);
				javaTypes.add(packageInfo);
			}
		}
	}

	private void fillWithSubPackages(String packageName, IPackageFragmentRoot packageFragmentRoot,
			Set<String> subPackages) {
		try {
			IJavaElement[] allPackages = packageFragmentRoot.getChildren();
			for (int i = 0; i < allPackages.length; i++) {
				IPackageFragment packageFragment = (IPackageFragment) allPackages[i];
				String subPackageName = packageFragment.getElementName();
				if (subPackageName.startsWith(packageName)) {
					subPackages.add(subPackageName);
				}
			}
		} catch (JavaModelException e) {
			LOGGER.log(Level.SEVERE, "Error while collecting sub packages for '" + packageName + "' in '"
					+ packageFragmentRoot.getElementName() + "'.", e);
		}
	}

	private void collectClassesAndInterfaces(IProgressMonitor monitor, List<JavaTypeInfo> javaTypes)
			throws JavaModelException {
		// Collect classes and interfaces according to the type name
		SearchEngine engine = new SearchEngine();
		engine.searchAllTypeNames(packageName == null ? null : packageName.toCharArray(), //
				SearchPattern.R_EXACT_MATCH, //
				typeName.toCharArray(), //
				SearchPattern.R_PATTERN_MATCH | SearchPattern.R_CASE_SENSITIVE, //
				IJavaSearchConstants.CLASS_AND_INTERFACE, //
				scope, //
				new TypeNameMatchRequestor() {

					@Override
					public void acceptTypeNameMatch(TypeNameMatch match) {
						IType type = (IType) match.getType();
						String typeSignature = AbstractTypeResolver.resolveJavaTypeSignature(type);

						JavaTypeInfo classInfo = new JavaTypeInfo();
						classInfo.setSignature(typeSignature);
						javaTypes.add(classInfo);

						try {
							classInfo.setJavaTypeKind(JDTTypeUtils.getJavaTypeKind(type));
						} catch (JavaModelException e) {
							LOGGER.log(Level.SEVERE, "Error while collecting Java Types for '" + packageName
									+ " package and Java type '" + typeName + "'.", e);
						}
					}
				}, //
				IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH, //
				monitor);
	}

}

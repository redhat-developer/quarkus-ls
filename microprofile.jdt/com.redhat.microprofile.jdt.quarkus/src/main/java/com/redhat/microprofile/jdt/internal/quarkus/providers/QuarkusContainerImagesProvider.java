/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.microprofile.jdt.internal.quarkus.providers;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchPattern;

import com.redhat.microprofile.jdt.core.AbstractPropertiesProvider;
import com.redhat.microprofile.jdt.core.BuildingScopeContext;
import com.redhat.microprofile.jdt.core.SearchContext;

/**
 * Properties provider to collect Quarkus properties from Quarkus container images jib, docker and s2i.
 * </p>
 * 
 * @author Angelo ZERR
 * @see <a href=
 *      "https://quarkus.io/guides/container-image">https://quarkus.io/guides/container-image</a>
 */
public class QuarkusContainerImagesProvider extends AbstractPropertiesProvider {

	@Override
	public void beginBuildingScope(BuildingScopeContext context, IProgressMonitor monitor) {
		// Quarkus container like:
		// - quarkus.container-image.group
		// - quarkus.container-image.registry
		// - quarkus.container-image.username
		// - quarkus.container-image.password
		// comes from the quarkus-container-image-deployment dependency.

		// User defines in pom.xml Quarkus extenions
		// - quarkus-container-image-jib
		// - quarkus-container-image-docker
		// - quarkus-container-image-s2i

		// which are linked to deployments Quarkus artifacts:
		// - quarkus-container-image-jib-deployment
		// - quarkus-container-image-docker-deployment
		// - quarkus-container-image-s2i-deployment

		// Those deployments artifacts have dependency to
		// quarkus-container-image-deployment which defines
		// quarkus.container-image.group, etc
		// The dependencies of quarkus-container-image-jib-deployment artifact must be
		// downloaded (yo add quarkus-container-image-deployment in the classpath)
		QuarkusContext quarkusContext = QuarkusContext.getQuarkusContext(context);
		quarkusContext.collectDependenciesFor("quarkus-container-image-jib-deployment");
		quarkusContext.collectDependenciesFor("quarkus-container-image-docker-deployment");
		quarkusContext.collectDependenciesFor("quarkus-container-image-s2i-deployment");
	}

	@Override
	public void collectProperties(SearchMatch match, SearchContext context, IProgressMonitor monitor) {

	}

	@Override
	protected String[] getPatterns() {
		return null;
	}

	@Override
	protected SearchPattern createSearchPattern(String pattern) {
		return null;
	}

}

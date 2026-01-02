/*******************************************************************************
* Copyright (c) 2026 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.jdt.internal.extensions.renarde;

import static com.redhat.qute.jdt.internal.extensions.renarde.RenardeJavaConstants.RENARDE_CONTROLLER_TYPE;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchPattern;

import com.redhat.qute.jdt.template.datamodel.AbstractDataModelProvider;
import com.redhat.qute.jdt.template.datamodel.SearchContext;
import com.redhat.qute.jdt.utils.JDTTypeUtils;

/**
 * m renarde support.
 * 
 * @author Angelo ZERR
 * 
 * @see <a href=
 *      "https://docs.quarkiverse.io/quarkus-renarde/dev/advanced.html#localisation">Localisation
 *      / Internationalisation</a>
 *
 */
public class MNamespaceResolverSupport extends AbstractDataModelProvider {

	@Override
	protected boolean isNamespaceAvailable(String namespace, SearchContext context, IProgressMonitor monitor) {
		// m namespace is available only for renarde project
		IJavaProject javaProject = context.getJavaProject();
		return JDTTypeUtils.findType(javaProject, RENARDE_CONTROLLER_TYPE) != null;
	}

	@Override
	public void collectDataModel(SearchMatch match, SearchContext context, IProgressMonitor monitor) {

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

/*******************************************************************************
* Copyright (c) 2024 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.jdt.internal.extensions.roq;

import static com.redhat.qute.jdt.internal.extensions.roq.RoqUtils.isRoqProject;

import java.util.Arrays;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchPattern;

import com.redhat.qute.commons.datamodel.DataModelParameter;
import com.redhat.qute.commons.datamodel.DataModelTemplate;
import com.redhat.qute.commons.datamodel.DataModelTemplateMatcher;
import com.redhat.qute.jdt.template.datamodel.AbstractDataModelProvider;
import com.redhat.qute.jdt.template.datamodel.SearchContext;

/**
 * Inject 'site' and 'page' as data model parameters for all Qute templates
 * which belong to a Roq application.
 */
public class RoqDataModelProvider extends AbstractDataModelProvider {

	@Override
	public void beginSearch(SearchContext context, IProgressMonitor monitor) {
		if (!isRoqProject(context.getJavaProject())) {
			// It is not a Roq application, don't inject site and page.
			return;
		}

		DataModelTemplate<DataModelParameter> roqTemplate = new DataModelTemplate<DataModelParameter>();
		roqTemplate.setTemplateMatcher(new DataModelTemplateMatcher(Arrays.asList("**/**")));

		// site
		DataModelParameter site = new DataModelParameter();
		site.setKey("site");
		site.setSourceType("io.quarkiverse.roq.frontmatter.runtime.model.Site");
		roqTemplate.addParameter(site);

		// page
		DataModelParameter page = new DataModelParameter();
		page.setKey("page");
		page.setSourceType("io.quarkiverse.roq.frontmatter.runtime.model.Page");
		roqTemplate.addParameter(page);

		context.getDataModelProject().getTemplates().add(roqTemplate);
	}

	@Override
	public void collectDataModel(SearchMatch match, SearchContext context, IProgressMonitor monitor) {
		// Do nothing
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

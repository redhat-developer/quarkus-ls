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
package com.redhat.qute.jdt.template.datamodel;

import java.util.List;

import org.eclipse.jdt.core.IJavaProject;

import com.redhat.qute.commons.QuteProjectScope;
import com.redhat.qute.commons.datamodel.DataModelParameter;
import com.redhat.qute.commons.datamodel.DataModelProject;
import com.redhat.qute.commons.datamodel.DataModelTemplate;
import com.redhat.qute.jdt.utils.IJDTUtils;

/**
 * The search context used to collect properties.
 *
 * @author Angelo ZERR
 *
 */
public class SearchContext extends BaseContext {
	private final DataModelProject<DataModelTemplate<DataModelParameter>> dataModelProject;
	private final IJDTUtils utils;

	public SearchContext(IJavaProject javaProject,
			DataModelProject<DataModelTemplate<DataModelParameter>> dataModelProject, IJDTUtils utils,
			List<QuteProjectScope> scopes) {
		super(javaProject, scopes);
		this.dataModelProject = dataModelProject;
		this.utils = utils;
	}

	public DataModelProject<DataModelTemplate<DataModelParameter>> getDataModelProject() {
		return dataModelProject;
	}

	/**
	 * Returns the JDT utilities.
	 *
	 * @return the JDT utilities.
	 */
	public IJDTUtils getUtils() {
		return utils;
	}

}

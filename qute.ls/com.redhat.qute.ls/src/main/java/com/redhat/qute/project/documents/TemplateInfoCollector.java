/*******************************************************************************
* Copyright (c) 2023 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.project.documents;

import java.util.ArrayList;
import java.util.List;

import com.redhat.qute.parser.template.ASTVisitor;
import com.redhat.qute.parser.template.Parameter;
import com.redhat.qute.parser.template.sections.InsertSection;

/**
 * Template information collector to collect:
 * 
 * <ul>
 * <li>insert parameters declared in the template '{#insert name}'</li>
 * </ul>
 * 
 * @author Angelo ZERR
 *
 */
public class TemplateInfoCollector extends ASTVisitor {

	private SearchInfoQuery query = null;

	private List<Parameter> insertParameters;

	public TemplateInfoCollector(SearchInfoQuery query) {
		this.query = query;
	}

	@Override
	public boolean visit(InsertSection section) {
		String insertParameter = query.getInsertParameter();
		if (insertParameter != null) {
			Parameter parameter = section.getParameterAtIndex(0);
			if (parameter != null) {
				if (SearchInfoQuery.ALL.equals(insertParameter) || insertParameter.equals(parameter.getValue())) {
					if (insertParameters == null) {
						insertParameters = new ArrayList<>();
					}
					insertParameters.add(parameter);
				}
			}
		}
		return super.visit(section);
	}

	/**
	 * Returns the collected insert parameters which matches the search query.
	 * 
	 * @return the collected insert parameters which matches the search query.
	 */
	public List<Parameter> getInsertParameters() {
		return insertParameters;
	}

}

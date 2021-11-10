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
package com.redhat.qute.project.datamodel;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.redhat.qute.commons.datamodel.DataModelParameter;
import com.redhat.qute.commons.datamodel.DataModelProject;
import com.redhat.qute.commons.datamodel.DataModelTemplate;

public class ExtendedDataModelProject extends DataModelProject<ExtendedDataModelTemplate> {

	public ExtendedDataModelProject(DataModelProject<DataModelTemplate<DataModelParameter>> project) {
		super.setTemplates(createTemplates(project.getTemplates()));
		super.setValueResolvers(project.getValueResolvers());
	}

	private List<ExtendedDataModelTemplate> createTemplates(List<DataModelTemplate<DataModelParameter>> templates) {
		if (templates == null || templates.isEmpty()) {
			return Collections.emptyList();
		}
		return templates.stream() //
				.map(template -> {
					return new ExtendedDataModelTemplate(template);
				}) //
				.collect(Collectors.toList());
	}

}

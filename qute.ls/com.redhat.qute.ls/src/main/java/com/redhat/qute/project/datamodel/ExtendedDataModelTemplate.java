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
import com.redhat.qute.commons.datamodel.DataModelTemplate;

public class ExtendedDataModelTemplate extends DataModelTemplate<ExtendedDataModelParameter> {

	public ExtendedDataModelTemplate(DataModelTemplate<DataModelParameter> template) {
		super.setTemplateUri(template.getTemplateUri());
		super.setSourceType(template.getSourceType());
		super.setSourceMethod(template.getSourceMethod());
		super.setSourceField(template.getSourceField());
		super.setParameters(createParameters(template.getParameters(), this));
	}

	private List<ExtendedDataModelParameter> createParameters(List<DataModelParameter> parameters,
			ExtendedDataModelTemplate template) {
		if (parameters == null) {
			return Collections.emptyList();
		}
		return parameters.stream() //
				.map(parameter -> new ExtendedDataModelParameter(parameter, template)) //
				.collect(Collectors.toList());
	}
}

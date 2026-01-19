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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.redhat.qute.commons.QuteJavaDefinitionParams;
import com.redhat.qute.commons.datamodel.DataModelFragment;
import com.redhat.qute.commons.datamodel.DataModelParameter;
import com.redhat.qute.commons.datamodel.DataModelTemplate;

public class ExtendedDataModelTemplate extends DataModelTemplate<ExtendedDataModelParameter>
		implements DataModelSourceProvider {

	private boolean initialized;

	public ExtendedDataModelTemplate(DataModelTemplate<DataModelParameter> template) {
		this(template.getTemplateUri());
		super.setSourceType(template.getSourceType());
		super.setSourceMethod(template.getSourceMethod());
		super.setSourceField(template.getSourceField());
		super.setParameters(createParameters(template.getParameters(), this));
		super.setFragments(createFragments(template.getFragments(), this));
	}

	public ExtendedDataModelTemplate(String templateUri) {
		super.setTemplateUri(templateUri);
	}

	public void addParameter(ExtendedDataModelParameter parameter) {
		List<ExtendedDataModelParameter> parameters = super.getParameters();
		if (parameters == null) {
			parameters = new ArrayList<>();
			super.setParameters(parameters);
		}
		parameters.add(parameter);
	}

	private List<DataModelFragment<ExtendedDataModelParameter>> createFragments(
			List<DataModelFragment<DataModelParameter>> fragments, ExtendedDataModelTemplate template) {
		if (fragments == null) {
			return Collections.emptyList();
		}
		return fragments.stream() //
				.map(fragment -> new ExtendedDataModelFragment(fragment)) //
				.collect(Collectors.toList());
	}

	protected static List<ExtendedDataModelParameter> createParameters(List<DataModelParameter> parameters,
			DataModelSourceProvider template) {
		if (parameters == null) {
			return new ArrayList<>();
		}
		return parameters.stream() //
				.map(parameter -> new ExtendedDataModelParameter(parameter, template)) //
				.collect(Collectors.toList());
	}

	@Override
	public QuteJavaDefinitionParams toJavaDefinitionParams(String projectUri) {
		String sourceType = getSourceType();
		String sourceField = getSourceField();
		String sourceMethod = getSourceMethod();

		QuteJavaDefinitionParams params = new QuteJavaDefinitionParams(sourceType, projectUri);
		params.setSourceField(sourceField);
		params.setSourceMethod(sourceMethod);
		return params;
	}

	public boolean isInitialized() {
		return initialized;
	}

	public void setInitialized(boolean initialized) {
		this.initialized = initialized;
	}
}

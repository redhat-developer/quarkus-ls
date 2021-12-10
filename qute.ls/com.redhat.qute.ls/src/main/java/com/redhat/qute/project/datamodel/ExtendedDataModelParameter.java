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

import com.redhat.qute.commons.QuteJavaDefinitionParams;
import com.redhat.qute.commons.datamodel.DataModelParameter;
import com.redhat.qute.parser.template.JavaTypeInfoProvider;
import com.redhat.qute.parser.template.Node;

public class ExtendedDataModelParameter extends DataModelParameter implements JavaTypeInfoProvider {

	private final ExtendedDataModelTemplate template;

	public ExtendedDataModelParameter(DataModelParameter parameter, ExtendedDataModelTemplate template) {
		super.setKey(parameter.getKey());
		super.setSourceType(parameter.getSourceType());
		super.setDataMethodInvocation(parameter.isDataMethodInvocation());
		this.template = template;
	}

	@Override
	public String getJavaType() {
		return getSourceType();
	}

	@Override
	public Node getJavaTypeOwnerNode() {
		return null;
	}

	public ExtendedDataModelTemplate getTemplate() {
		return template;
	}

	public QuteJavaDefinitionParams toJavaDefinitionParams(String projectUri) {
		ExtendedDataModelTemplate dataModelTemplate = getTemplate();
		String sourceType = dataModelTemplate.getSourceType();
		String sourceField = dataModelTemplate.getSourceField();
		String sourceMethod = dataModelTemplate.getSourceMethod();
		String sourceParameter = getKey();

		QuteJavaDefinitionParams params = new QuteJavaDefinitionParams(sourceType, projectUri);
		params.setSourceField(sourceField);
		params.setSourceMethod(sourceMethod);
		params.setSourceParameter(sourceParameter);
		params.setDataMethodInvocation(isDataMethodInvocation());
		return params;
	}

}

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

import com.redhat.qute.commons.datamodel.DataModelParameter;
import com.redhat.qute.parser.template.JavaTypeInfoProvider;
import com.redhat.qute.parser.template.Node;

public class ExtendedDataModelParameter extends DataModelParameter implements JavaTypeInfoProvider {

	private final ExtendedDataModelTemplate template;

	public ExtendedDataModelParameter(DataModelParameter parameter, ExtendedDataModelTemplate template) {
		super.setKey(parameter.getKey());
		super.setSourceType(parameter.getSourceType());
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

}

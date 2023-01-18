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
package com.redhat.qute.project.datamodel;

import com.redhat.qute.commons.QuteJavaDefinitionParams;
import com.redhat.qute.commons.datamodel.DataModelFragment;
import com.redhat.qute.commons.datamodel.DataModelParameter;

/**
 * Data model fragment which implements {@link DataModelSourceProvider} used to
 * go to the Java definition of the fragment.
 * 
 * @author Angelo ZERR
 *
 */
public class ExtendedDataModelFragment extends DataModelFragment<ExtendedDataModelParameter>
		implements DataModelSourceProvider {

	public ExtendedDataModelFragment(DataModelFragment<DataModelParameter> fragment) {
		super.setId(fragment.getId());
		super.setSourceType(fragment.getSourceType());
		super.setSourceMethod(fragment.getSourceMethod());
		super.setParameters(ExtendedDataModelTemplate.createParameters(fragment.getParameters(), this));
	}

	@Override
	public QuteJavaDefinitionParams toJavaDefinitionParams(String projectUri) {
		String sourceType = getSourceType();
		String sourceField = null;
		String sourceMethod = getSourceMethod();

		QuteJavaDefinitionParams params = new QuteJavaDefinitionParams(sourceType, projectUri);
		params.setSourceField(sourceField);
		params.setSourceMethod(sourceMethod);
		return params;
	}

	@Override
	public String getSourceField() {
		return null;
	}
}

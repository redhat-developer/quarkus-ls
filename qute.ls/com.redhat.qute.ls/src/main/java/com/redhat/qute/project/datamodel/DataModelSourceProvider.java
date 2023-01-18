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

import java.util.List;

import com.redhat.qute.commons.QuteJavaDefinitionParams;

/**
 * Data model source provider API implemented by data model template / fragment.
 * 
 * @author Angelo ZERR
 *
 */
public interface DataModelSourceProvider {

	String getSourceType();

	String getSourceField();

	String getSourceMethod();

	QuteJavaDefinitionParams toJavaDefinitionParams(String projectUri);

	List<ExtendedDataModelParameter> getParameters();

}

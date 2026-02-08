/*******************************************************************************
* Copyright (c) 2026 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.project.extensions;

import java.nio.file.Path;

import com.redhat.qute.project.datamodel.ExtendedDataModelTemplate;

/**
 * Participant for providing custom data model in Qute templates.
 * 
 * @author Angelo ZERR
 */
public interface DataModelTemplateParticipant extends BaseParticpant {

	ExtendedDataModelTemplate contributeToDataModel(String templateUri, Path templatePath,
			ExtendedDataModelTemplate dataModelTemplate);

}

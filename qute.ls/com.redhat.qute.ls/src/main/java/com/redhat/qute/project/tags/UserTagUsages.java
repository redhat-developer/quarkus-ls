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
package com.redhat.qute.project.tags;

import com.redhat.qute.parser.template.Parameter;
import com.redhat.qute.project.usages.ParameterUsages;
import com.redhat.qute.utils.UserTagUtils;

/**
 * Stores all usages of a given user tag across templates.
 *
 * <p>
 * Usages are grouped by template URI.
 * </p>
 */
public class UserTagUsages extends ParameterUsages {

	public UserTagUsages(String tagName) {
	}

	@Override
	protected boolean isMatchParameter(String name, Parameter p) {
		return super.isMatchParameter(name, p)
				|| (UserTagUtils.IT_OBJECT_PART_NAME.equals(name) && !p.hasValueAssigned());
	}
}

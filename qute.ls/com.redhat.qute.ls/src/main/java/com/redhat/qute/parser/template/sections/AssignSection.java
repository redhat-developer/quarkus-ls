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
package com.redhat.qute.parser.template.sections;

import java.util.List;

import com.redhat.qute.parser.template.Parameter;
import com.redhat.qute.parser.template.Section;

/**
 * Base class for #set and #let section.
 * 
 * @author Angelo ZERR
 * 
 * @see https://quarkus.io/guides/qute-reference#letset-section
 *
 */
public abstract class AssignSection extends Section {

	public AssignSection(String tag, int start, int end) {
		super(tag, start, end);
	}

	protected void initializeParameters(List<Parameter> parameters) {
		// All parameters can have expression (ex : {#set myParent=order.item.parent
		// isActive=false age=10}
		parameters.forEach(parameter -> {
			parameter.setCanHaveExpression(parameter.hasValueAssigned());
		});
	}
}

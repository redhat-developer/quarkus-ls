/*******************************************************************************
* Copyright (c) 2022 Red Hat Inc. and others.
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
import com.redhat.qute.parser.template.SectionKind;

/**
 * A custom section can be:
 * 
 * <ul>
 * <li>a user tag</li>
 * <li>an include/insert section</li>
 * </ul>
 * 
 * @author Angelo ZERR
 *
 */
public class CustomSection extends Section {

	public CustomSection(String tag, int start, int end) {
		super(tag, start, end);
	}

	@Override
	public SectionKind getSectionKind() {
		return SectionKind.CUSTOM;
	}

	@Override
	protected void initializeParameters(List<Parameter> parameters) {
		// All parameters can have expression (ex : {#user name=order.item.parent
		// isActive=false age=10}
		parameters.forEach(parameter -> {
			parameter.setCanHaveExpression(parameter.hasValueAssigned());
		});
	}
}

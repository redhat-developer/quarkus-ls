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

import com.redhat.qute.parser.template.Parameter;
import com.redhat.qute.parser.template.ParametersInfo;
import com.redhat.qute.parser.template.Section;
import com.redhat.qute.parser.template.SectionKind;

/**
 * With section AST node.
 * 
 * <code>
	{#with item.parent}
	  <h1>{name}</h1>  
	  <p>{description}</p> 
	{/with}
 * </code>
 * 
 * @author Angelo ZERR
 * 
 * @see https://quarkus.io/guides/qute-reference#with_section
 *
 */
public class WithSection extends Section {

	public static final String TAG = "with";

	private static final String OBJECT = "object";

	private static final ParametersInfo PARAMETER_INFOS = ParametersInfo.builder() //
			.addParameter(OBJECT) //
			.build();

	public WithSection(int start, int end) {
		super(TAG, start, end);
	}

	@Override
	public SectionKind getSectionKind() {
		return SectionKind.WITH;
	}

	public Parameter getObjectParameter() {
		if (getParameters().isEmpty()) {
			return null;
		}
		return getParameterAtIndex(0);
	}

	@Override
	public ParametersInfo getParametersInfo() {
		return PARAMETER_INFOS;
	}
}

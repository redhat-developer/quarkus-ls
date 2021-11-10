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

/**
 * Base class for #switch and #when section.
 * 
 * @author Angelo ZERR
 * 
 * @see https://quarkus.io/guides/qute-reference#when_section
 *
 */
public abstract class BaseWhenSection extends Section {

	private static final String VALUE = "value";

	private static final ParametersInfo PARAMETER_INFOS = ParametersInfo.builder() //
			.addParameter(VALUE) //
			.build();

	public BaseWhenSection(String tag, int start, int end) {
		super(tag, start, end);
	}

	public Parameter getValueParameter() {
		if (getParameters().isEmpty()) {
			return null;
		}
		return getParameterAtIndex(0);
	}

	public ParametersInfo getParametersInfo() {
		return PARAMETER_INFOS;
	}

}

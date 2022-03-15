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
package com.redhat.qute.parser;

import com.redhat.qute.parser.template.ParametersContainer;

/**
 * String parameters container.
 * 
 * @author Angelo ZERR
 *
 */
public class StringParametersContainer implements ParametersContainer {

	private final String text;

	public StringParametersContainer(String text) {
		this.text = text;
	}

	@Override
	public int getStartParametersOffset() {
		return 0;
	}

	@Override
	public int getEndParametersOffset() {
		return text.length();
	}

	@Override
	public String getTemplateContent() {
		return text;
	}

	@Override
	public CancelChecker getCancelChecker() {
		return null;
	}
}

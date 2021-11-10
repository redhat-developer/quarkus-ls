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

import com.redhat.qute.parser.template.Section;
import com.redhat.qute.parser.template.SectionKind;

public class IfSection extends Section {

	public static final String TAG = "if";
	
	public IfSection(int start, int end) {
		super(TAG, start, end);
	}
	
	@Override
	public SectionKind getSectionKind() {
		return SectionKind.IF;
	}

}

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

import com.redhat.qute.parser.template.SectionKind;

/**
 * Switch section AST node.
 * 
 * <code>
	{#switch person.name}
  		{#case 'John'} 
    		Hey John!
  		{#case 'Mary'}
    		Hey Mary!
	{/switch}
 * </code>
 * 
 * @author Angelo ZERR
 * 
 * @see https://quarkus.io/guides/qute-reference#when_section
 *
 */
public class SwitchSection extends BaseWhenSection {

	public static final String TAG = "switch";

	public SwitchSection(int start, int end) {
		super(TAG, start, end);
	}

	@Override
	public SectionKind getSectionKind() {
		return SectionKind.SWITCH;
	}
}

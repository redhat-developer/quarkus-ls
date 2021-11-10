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
 * Set section AST node.
 * 
 * <code>
 	{#set myParent=order.item.parent isActive=false age=10} 
    	<h1>{myParent.name}</h1>
    	Is active: {isActive}
    	Age: {age}
  	{/set}
 * </code>
 * 
 * @author Angelo ZERR
 * 
 * @see https://quarkus.io/guides/qute-reference#letset-section
 *
 */
public class SetSection extends AssignSection {

	public static final String TAG = "set";

	public SetSection(int start, int end) {
		super(TAG, start, end);
	}

	@Override
	public SectionKind getSectionKind() {
		return SectionKind.SET;
	}

}

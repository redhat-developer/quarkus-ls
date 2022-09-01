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
 * Is section AST node.
 * 
 * <code>
 * 	{#when items.size}
 *  	{#is 1} 
 *   		There is exactly one item!
 *  	{#is > 10} 
 *   		There are more than 10 items!
 *  	{#else} 
 *    		There are 2 -10 items!
 *	{/when}
 * </code>
 * 
 * @author Angelo ZERR
 * 
 * @see https://quarkus.io/guides/qute-reference#when_section
 *
 */
public class IsSection extends CaseSection {

	public static final String TAG = "is";

	public IsSection(int start, int end) {
		super(TAG, start, end);
	}

	@Override
	public SectionKind getSectionKind() {
		return SectionKind.IS;
	}

}

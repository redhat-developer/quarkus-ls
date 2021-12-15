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
 * Else section AST node.
 * 
 * <code>
 	{#for item in items} 
  		{item.name}
  	{#else}
  		No items.
	{/for}
 * </code>
 *
 * <code>
 	{#if item.age > 10}
  		This item is very old.
	{#else if item.age > 5}
  		This item is quite old.
	{#else if item.age > 2}
  		This item is old.
	{#else}
  		This item is not old at all!
	{/if}
 * </code>
 * 
 * @author Angelo ZERR
 * 
 * @see https://quarkus.io/guides/qute-reference#loop_section
 * @see https://quarkus.io/guides/qute-reference#if_section
 *
 */
public class ElseSection extends AssignSection {

	public static final String TAG = "else";

	public ElseSection(int start, int end) {
		super(TAG, start, end);
	}

	@Override
	public SectionKind getSectionKind() {
		return SectionKind.ELSE;
	}

}

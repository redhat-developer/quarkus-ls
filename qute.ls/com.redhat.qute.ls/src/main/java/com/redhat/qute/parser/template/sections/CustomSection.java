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

import com.redhat.qute.parser.template.ASTVisitor;
import com.redhat.qute.parser.template.Parameter;
import com.redhat.qute.parser.template.Section;
import com.redhat.qute.parser.template.SectionKind;
import com.redhat.qute.parser.template.SectionMetadata;

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

	private static final String ISOLATED_DOC = "By default, a tag template cannot reference the data from the parent context.\n"
			+ //
			"Qute executes the tag as an isolated template, i.e. without access to the context of the template that calls the tag\n."
			+ //
			" However, sometimes it might be useful to change the default behavior and disable the isolation. " + //
			"In this case, just add `_isolated=false` or `_unisolated` argument to the call site, for example " + //
			"```qute-html\n{#itemDetail item showImage=true _isolated=false /}``` or ```qute-html\n{#itemDetail item showImage=true _unisolated /}```.";

	private static final String IGNORE_FRAGEMNTS = "If you want to reference a fragment from the same template, skip the part before `$`, i.e. something like ```qute-html\n{#include $item_aliases /}```.";

	private static final List<SectionMetadata> PARAMETER_METADATA = List.of(//
			new SectionMetadata("_isolated", Boolean.class.getName(), ISOLATED_DOC), //
			new SectionMetadata("_unisolated", Boolean.class.getName(), ISOLATED_DOC), //
			new SectionMetadata("_ignoreFragments", Boolean.class.getName(), IGNORE_FRAGEMNTS) //
	);

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

		// For user tag, one parameter can be an expression (for 'it')
		boolean hasIt = false;
		for (Parameter parameter : parameters) {
			if (parameter.hasValueAssigned()) {
				parameter.setCanHaveExpression(true);
			} else if (!hasIt) {
				parameter.setCanHaveExpression(true);
				hasIt = true;
			}
		}
	}

	@Override
	protected void accept0(ASTVisitor visitor) {
		boolean visitChildren = visitor.visit(this);
		if (visitChildren) {
			List<Parameter> parameters = getParameters();
			for (Parameter parameter : parameters) {
				acceptChild(visitor, parameter);
			}
			acceptChildren(visitor, getChildren());
		}
		visitor.endVisit(this);
	}

	@Override
	public List<SectionMetadata> getParameterMetadata() {
		return PARAMETER_METADATA;
	}
}

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

import java.util.List;

import com.redhat.qute.parser.template.ASTVisitor;
import com.redhat.qute.parser.template.Parameter;
import com.redhat.qute.parser.template.ParametersInfo;
import com.redhat.qute.parser.template.Section;
import com.redhat.qute.parser.template.SectionKind;

/**
 * When section AST node.
 * 
 * <code>
	{#when items.size}
  		{#is 1} 
    		There is exactly one item!
  		{#is > 10} 
    		There are more than 10 items!
  		{#else} 
    		There are 2 -10 items!
	{/when}
 * </code>
 * 
 * @author Angelo ZERR
 * 
 * @see https://quarkus.io/guides/qute-reference#when_section
 *
 */
public class WhenSection extends Section {

	public static final String TAG = "when";

	private static final String VALUE = "value";

	private static final ParametersInfo PARAMETER_INFOS = ParametersInfo.builder() //
			.addParameter(VALUE) //
			.build();

	public WhenSection(int start, int end) {
		this(TAG, start, end);
	}

	WhenSection(String tag, int start, int end) {
		super(tag, start, end);
	}

	@Override
	public SectionKind getSectionKind() {
		return SectionKind.WHEN;
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

	@Override
	public List<SectionKind> getBlockLabels() {
		return List.of(SectionKind.IS, SectionKind.CASE, SectionKind.ELSE);
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
}

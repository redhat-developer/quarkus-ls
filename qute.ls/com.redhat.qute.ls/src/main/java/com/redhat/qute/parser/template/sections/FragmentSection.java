/*******************************************************************************
* Copyright (c) 2023 Red Hat Inc. and others.
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
import com.redhat.qute.parser.template.ParameterInfo;
import com.redhat.qute.parser.template.ParametersInfo;
import com.redhat.qute.parser.template.Section;
import com.redhat.qute.parser.template.SectionKind;

/**
 * Fragment section AST node.
 * 
 * <code>
 	{#fragment id=item_aliases} 
	<h2>Aliases</h2>
	<ol>
    	{#for alias in aliases}
    	<li>{alias}</li>
    	{/for}
	</ol>
	{/fragment}
 * </code>
 * 
 * @author Angelo ZERR
 * 
 * @see https://quarkus.io/guides/qute-reference#fragments
 */
public class FragmentSection extends Section {

	public static final String TAG = "fragment";

	private static final String ID = "id";

	private static final String RENDERED = "rendered";

	private static final ParametersInfo PARAMETER_INFOS = ParametersInfo.builder() //
			.addParameter(ID) //
			.addParameter(new ParameterInfo(RENDERED, null, true)) //
			.build();

	public FragmentSection(int start, int end) {
		super(TAG, start, end);
	}

	@Override
	public SectionKind getSectionKind() {
		return SectionKind.FRAGMENT;
	}

	@Override
	public ParametersInfo getParametersInfo() {
		return PARAMETER_INFOS;
	}

	@Override
	protected void initializeParameters(List<Parameter> parameters) {
		parameters.forEach(parameter -> {
			parameter.setCanHaveExpression(false);
		});
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

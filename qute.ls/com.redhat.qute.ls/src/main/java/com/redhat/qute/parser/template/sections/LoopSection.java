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

import static com.redhat.qute.parser.template.ParameterInfo.EMPTY;

import java.util.Arrays;
import java.util.List;

import com.redhat.qute.parser.template.Node;
import com.redhat.qute.parser.template.Parameter;
import com.redhat.qute.parser.template.ParameterInfo;
import com.redhat.qute.parser.template.ParametersInfo;
import com.redhat.qute.parser.template.Section;
import com.redhat.qute.parser.template.SectionMetadata;

/**
 * Loop section AST node.
 * 
 * <code>
 	{#each items}
  		{it.name} 
	{/each}
 * </code>
 * 
 * <code>
 	{#for item in items} 
  		{item.name}
	{/for}
 * </code>
 * 
 * @author Angelo ZERR
 * 
 * @see https://quarkus.io/guides/qute-reference#loop_section
 *
 */
public abstract class LoopSection extends Section {

	private static final String DEFAULT_ALIAS = "it";

	private static final String ALIAS = "alias";

	private static final String IN = "in";

	private static final String ITERABLE = "iterable";

	private static final int ALIAS_PARAMETER_INDEX = 0;

	private static final int ITERABLE_PARAMETER_INDEX = 2;

	private static final List<SectionMetadata> METADATA = Arrays.asList(//
			new SectionMetadata("count", Integer.class.getName()), //
			new SectionMetadata("index", Integer.class.getName()), //
			new SectionMetadata("indexParity", String.class.getName()), //
			new SectionMetadata("hasNext", Boolean.class.getName()), //
			new SectionMetadata("isOdd", Boolean.class.getName()), //
			new SectionMetadata("odd", Boolean.class.getName()), //
			new SectionMetadata("isEven", Boolean.class.getName()), //
			new SectionMetadata("even", Boolean.class.getName()));

	private static final ParametersInfo PARAMETER_INFOS = ParametersInfo.builder() //
			.addParameter(ALIAS, EMPTY) //
			.addParameter(IN, EMPTY) //
			.addParameter(new ParameterInfo(ITERABLE, null, true)) //
			.build();

	public LoopSection(String tag, int start, int end) {
		super(tag, start, end);
	}

	@Override
	public List<SectionMetadata> getMetadata() {
		return METADATA;
	}

	public String getAlias() {
		Parameter alias = getAliasParameter();
		if (alias != null) {
			return alias.getValue();
		}
		return DEFAULT_ALIAS;
	}

	public Parameter getAliasParameter() {
		int nbParameters = getParameters().size();
		if (nbParameters >= 3) {
			return getParameterAtIndex(ALIAS_PARAMETER_INDEX);
		}
		return null;
	}

	public Parameter getIterableParameter() {
		int nbParameters = getParameters().size();
		if (nbParameters >= 2) {
			Parameter iterable = getParameterAtIndex(ITERABLE_PARAMETER_INDEX);
			if (iterable != null) {
				return iterable;
			}
		} else {
			Parameter iterable = getParameterAtIndex(0);
			if (iterable != null) {
				return iterable;
			}
		}
		return null;
	}

	public boolean isInAlias(int offset) {
		Parameter parameter = getAliasParameter();
		if (parameter == null) {
			return false;
		}
		return Node.isIncluded(parameter, offset);
	}

	@Override
	public boolean isIterable() {
		return true;
	}

	@Override
	public ParametersInfo getParametersInfo() {
		return PARAMETER_INFOS;
	}
}

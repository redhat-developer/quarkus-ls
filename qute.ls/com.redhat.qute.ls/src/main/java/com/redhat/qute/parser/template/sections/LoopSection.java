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
import java.util.stream.Collectors;

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

	/**
	 * Constant value for iteration metadata prefix indicating that the alias
	 * suffixed with a question mark should be used.
	 */
	public static final String ITERATION_METADATA_PREFIX_ALIAS_QM = "<alias?>";

	/**
	 * Constant value for iteration metadata prefix indicating that the alias
	 * suffixed with an underscore should be used.
	 */
	public static final String ITERATION_METADATA_PREFIX_ALIAS_UNDERSCORE = "<alias_>";

	/**
	 * Constant value for iteration metadata prefix indicating that no prefix should
	 * be used.
	 */
	public static final String ITERATION_METADATA_PREFIX_NONE = "<none>";

	private static final String DEFAULT_ALIAS = "it";

	private static final String ALIAS = "alias";

	private static final String IN = "in";

	private static final String ITERABLE = "iterable";

	private static final int ALIAS_PARAMETER_INDEX = 0;

	private static final int ITERABLE_PARAMETER_INDEX = 2;

	private List<SectionMetadata> metadata;

	private static final List<SectionMetadata> METADATA = Arrays.asList(//
			new SectionMetadata("count", Integer.class.getName(), "`count` - 1-based index"), //
			new SectionMetadata("index", Integer.class.getName(), "`index` - zero-based index"), //
			new SectionMetadata("indexParity", String.class.getName(),
					"`indexParity` - outputs `odd` or `even` based on the zero-based index value"), //
			new SectionMetadata("hasNext", Boolean.class.getName(),
					"`hasNext` - true if the iteration has more elements"), //
			new SectionMetadata("isLast", Boolean.class.getName(), "`isLast` - true if hasNext == false"), //
			new SectionMetadata("isFirst", Boolean.class.getName(), "`isFirst` - true if count == 1"), //
			new SectionMetadata("odd", Boolean.class.getName(), "`odd` - true if the zero-based index is odd"), //
			new SectionMetadata("even", Boolean.class.getName(), "`even` - true if the zero-based index is even"));

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
		if (metadata == null) {
			metadata = loadMetadata();
		}
		return metadata;
	}

	private synchronized List<SectionMetadata> loadMetadata() {
		if (metadata != null) {
			return metadata;
		}
		String metadataPrefix = getOwnerTemplate().getConfiguration().getIterationMetadataPrefix();
		String prefix = prefixValue(getAlias(), metadataPrefix);
		if (prefix == null) {
			metadata = METADATA;
		} else {
			metadata = METADATA.stream() //
					.map(m -> new SectionMetadata(prefix + m.getName(), m.getJavaType(), m.getDescription())) //
					.collect(Collectors.toList());
		}
		return metadata;
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

	/**
	 * @see https://github.com/quarkusio/quarkus/blob/85316bccfc8a751893ed443c03663fa19f86a560/independent-projects/qute/core/src/main/java/io/quarkus/qute/LoopSectionHelper.java#L233
	 * 
	 * @param alias
	 * @param metadataPrefix
	 * @return
	 */
	private static String prefixValue(String alias, String metadataPrefix) {
		if (metadataPrefix == null || ITERATION_METADATA_PREFIX_NONE.equals(metadataPrefix)) {
			return null;
		} else if (ITERATION_METADATA_PREFIX_ALIAS_UNDERSCORE.equals(metadataPrefix)) {
			return alias + "_";
		} else if (ITERATION_METADATA_PREFIX_ALIAS_QM.equals(metadataPrefix)) {
			return alias + "?";
		} else {
			return metadataPrefix;
		}
	}
}

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
package com.redhat.qute.parser.template;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.redhat.qute.parser.parameter.ParameterParser;

/**
 * Base class for Qute section (ex : #for, #if, #custom)
 * 
 * @author Angelo ZERR
 * 
 * @see https://quarkus.io/guides/qute-reference#sections
 *
 */
public class Section extends Node implements ParametersContainer {

	private final String tag;

	private int startTagCloseOffset;

	private int endTagOpenOffset;

	private int endTagCloseOffset;

	private boolean selfClosed;

	private List<Parameter> parameters;

	public Section(String tag, int start, int end) {
		super(start, end);
		this.tag = tag;
		this.endTagOpenOffset = NULL_VALUE;
		this.startTagCloseOffset = NULL_VALUE;
		this.endTagCloseOffset = NULL_VALUE;
	}

	@Override
	public NodeKind getKind() {
		return NodeKind.Section;
	}

	// ---------------------------- Start tag methods

	/**
	 * Returns the offset which opens the section start tag.
	 * 
	 * <p>
	 * |{#let name=value}
	 * </p>
	 * 
	 * 
	 * @return the offset which opens the section start tag.
	 */
	public int getStartTagOpenOffset() {
		return getStart();
	}

	/**
	 * Returns the offset before the start tag name of the section.
	 * 
	 * <p>
	 * {|#let name=value}
	 * </p>
	 * 
	 * 
	 * @return the offset before the start tag name of the section.
	 */
	public int getStartTagNameOpenOffset() {
		return getStartTagOpenOffset() + 1;
	}

	/**
	 * Returns the offset after the start tag name of the section.
	 * 
	 * <p>
	 * {#let| name=value}
	 * </p>
	 * 
	 * 
	 * @return the offset after the start tag name of the section.
	 */
	public int getStartTagNameCloseOffset() {
		if (!hasTag()) {
			// {#|
			return getStartTagNameOpenOffset() + 1;
		}
		// {#let|
		return getStartTagNameOpenOffset() + 1 + tag.length();
	}

	/**
	 * Returns the offset which closes the section start tag and -1 otherwise.
	 * 
	 * <p>
	 * {#let name=value|}
	 * </p>
	 * 
	 * 
	 * @return the offset which closes the section start tag and -1 otherwise.
	 */
	public int getStartTagCloseOffset() {
		return startTagCloseOffset;
	}

	void setStartTagCloseOffset(int startTagCloseOffset) {
		this.startTagCloseOffset = startTagCloseOffset;
	}

	/**
	 * Returns true if the start tag section is closed and false otherwise.
	 * 
	 * @return true if the start tag section is closed and false otherwise.
	 */
	public boolean isStartTagClosed() {
		// {#let name=value} -> will returns true
		// {#let name=value -> will returns false
		return startTagCloseOffset != NULL_VALUE;
	}

	/**
	 * Returns true if the given offset is in the start tag section name (ex : in
	 * #each) and false otherwise.
	 * 
	 * @param offset the offset.
	 * 
	 * @return true if the given offset is in the start tag section name (ex : in
	 *         #each) and false otherwise.
	 */
	public boolean isInStartTagName(int offset) {
		if (!isStartTagClosed()) {
			// cases
			// - {#|
			// - {|#
			return true;
		}
		if (offset > getStart() && offset <= getStartTagNameCloseOffset()) {
			// cases:
			// - {#each| }
			// - {|#each }
			return true;
		}
		return false;
	}

	// ---------------------------- End tag methods

	/**
	 * Returns the offset which opens the section end tag and -1 otherwise.
	 * 
	 * <p>
	 * |{\let}
	 * </p>
	 * 
	 * @return the offset which opens the section end tag and -1 otherwise.
	 */
	public int getEndTagOpenOffset() {
		return endTagOpenOffset;
	}

	/**
	 * Returns the offset before the end tag name of the section.
	 * 
	 * <p>
	 * {|\let}
	 * </p>
	 * 
	 * 
	 * @return the offset before the end tag name of the section.
	 */
	public int getEndTagNameOpenOffset() {
		return getEndTagOpenOffset() + 1; // {|/
	}

	void setEndTagOpenOffset(int endTagOpenOffset) {
		this.endTagOpenOffset = endTagOpenOffset;
	}

	/**
	 * Returns the offset which closes the section end tag and -1 otherwise.
	 * 
	 * <p>
	 * {\let|}
	 * </p>
	 * 
	 * 
	 * @return the offset which closes the section end tag and -1 otherwise.
	 */
	public int getEndTagCloseOffset() {
		return endTagCloseOffset;
	}

	void setEndTagCloseOffset(int endTagCloseOffset) {
		this.endTagCloseOffset = endTagCloseOffset;
	}

	/**
	 * Returns true if the given offset is in the end tag section name (ex : in
	 * \each) and false otherwise.
	 * 
	 * @param offset the offset.
	 * 
	 * @return true if the given offset is in the end tag section name (ex : in
	 *         \each) and false otherwise.
	 */
	public boolean isInEndTagName(int offset) {
		return isInEndTagName(offset, false);
	}

	private boolean isInEndTagName(int offset, boolean afterBackSlash) {
		if (!hasEndTag()) {
			return false;
		}
		if (offset >= endTagOpenOffset + (afterBackSlash ? 1 : 0) && offset < getEnd()) {
			// cases:
			// - {|/each}
			// - {/|each}
			// - {/ea|ch}
			// - {/each|}
			// - {|/}
			// - {/|}
			return true;
		}
		return false;
	}

	/**
	 * Returns true if has an end tag.
	 *
	 *
	 * @return true if has an end tag.
	 */
	public boolean hasEndTag() {
		return getEndTagOpenOffset() != NULL_VALUE;
	}

	// ---------------------------- Parameters methods

	/**
	 * Returns parameters of the section.
	 * 
	 * @return parameters of the section.
	 */
	public List<Parameter> getParameters() {
		if (parameters == null) {
			this.parameters = parseParameters();
		}
		return this.parameters;
	}

	/**
	 * Returns the parameter at the given index and null otherwise.
	 * 
	 * @param index the parameter index.
	 * 
	 * @return the parameter at the given index and null otherwise.
	 */
	public Parameter getParameterAtIndex(int index) {
		List<Parameter> parameters = getParameters();
		if (parameters.size() > index) {
			return parameters.get(index);
		}
		return null;
	}

	/**
	 * Returns the parameter at the given offset and null otherwise.
	 * 
	 * @param offset the offset.
	 * 
	 * @return the parameter at the given offset and null otherwise.
	 */
	public Parameter getParameterAtOffset(int offset) {
		if (!isInParameters(offset)) {
			return null;
		}
		List<Parameter> parameters = getParameters();
		return (Parameter) Node.findNodeAt(parameters.stream().map(param -> (Node) param).collect(Collectors.toList()),
				offset);
	}

	private synchronized List<Parameter> parseParameters() {
		if (parameters != null) {
			return parameters;
		}
		List<Parameter> parameters = ParameterParser.parse(this, getOwnerTemplate().getCancelChecker());
		initializeParameters(parameters);
		return parameters;
	}

	protected void initializeParameters(List<Parameter> parameters) {
		List<ParameterInfo> infos = getParametersInfo().get(ParametersInfo.MAIN_BLOCK_NAME);
		if (parameters.size() == infos.size()) {
			for (int j = 0; j < infos.size(); j++) {
				ParameterInfo info = infos.get(j);
				if (!info.hasDefaultValue()) {
					parameters.get(j).setCanHaveExpression(true);
				}
			}
		} else {
			int i = 0;
			for (int j = 0; j < infos.size(); j++) {
				ParameterInfo info = infos.get(j);
				if (info.getDefaultValue() == null) {
					if (parameters.size() > i) {
						parameters.get(i).setCanHaveExpression(true);
						i++;
					} else {
						break;
					}
				}
			}
		}
	}

	/**
	 * Returns the start offset of the parameters expression section.
	 * 
	 * <p>
	 * {#let |name1=value1 name2=value2}
	 * </p>
	 * 
	 * 
	 * @return the start offset of the parameters expression section.
	 */
	@Override
	public int getStartParametersOffset() {
		return getStartTagNameCloseOffset() + 1;
	}

	/**
	 * Returns the end offset of the parameters expression section.
	 * 
	 * <p>
	 * {#let name1=value1 name2=value2|}
	 * </p>
	 * 
	 * 
	 * @return the end offset of the parameters expression section.
	 */
	@Override
	public int getEndParametersOffset() {
		if (!isStartTagClosed()) {
			return getEnd();
		}
		return getStartTagCloseOffset();
	}

	/**
	 * Returns true if the given offset is inside parameter expression of the
	 * section and false otherwise.
	 * 
	 * @param offset the offset.
	 * 
	 * @return true if the given offset is inside parameter expression of the
	 *         section and false otherwise.
	 */
	public boolean isInParameters(int offset) {
		// cases:
		// - {#each |}
		// - {#for it|em in }
		// - {#for item in |}
		return offset >= getStartParametersOffset() && offset <= getEndParametersOffset();
	}

	@Deprecated()
	public Expression getExpressionParameter() {
		// Try to remove this method
		return new ExpressionParameter(getStartParametersOffset(), getEndParametersOffset(), this);
	}

	public ParametersInfo getParametersInfo() {
		return ParametersInfo.EMPTY;
	}

	// ---------------------------- Other methods

	/**
	 * Returns the section tag name and null otherwise.
	 * 
	 * @return the section tag name and null otherwise.
	 */
	public String getTag() {
		return tag;
	}

	/**
	 * Returns true if the section has a tag and null otherwise.
	 * 
	 * @return true if the section has a tag and null otherwise.
	 */
	public boolean hasTag() {
		return tag != null;
	}

	public boolean isSelfClosed() {
		return selfClosed;
	}

	void setSelfClosed(boolean selfClosed) {
		this.selfClosed = selfClosed;
	}

	@Override
	public String getNodeName() {
		return "#" + getTag();
	}

	public SectionKind getSectionKind() {
		return SectionKind.CUSTOM;
	}

	public List<SectionMetadata> getMetadata() {
		return Collections.emptyList();
	}

	public JavaTypeInfoProvider getMetadata(String name) {
		Optional<SectionMetadata> metadata = getMetadata().stream() //
				.filter(m -> name.equals(m.getName())) //
				.findFirst();
		return metadata.isPresent() ? metadata.get() : null;
	}

	public boolean isIterable() {
		return false;
	}
}

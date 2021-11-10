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

public class Section extends Node implements ParametersContainer {

	private final String tag;

	private int startTagOpenOffset;

	private int startTagCloseOffset;

	private int endTagOpenOffset;

	private int endTagCloseOffset;

	private boolean selfClosed;

	private List<Parameter> parameters;

	public Section(String tag, int start, int end) {
		super(start, end);
		this.tag = tag;
	}

	@Override
	public NodeKind getKind() {
		return NodeKind.Section;
	}

	public int getStartTagOpenOffset() {
		return startTagOpenOffset;
	}

	public int getAfterStartTagOpenOffset() {
		if (hasTag()) {
			return getStartTagOpenOffset();
		}
		return getStartTagOpenOffset() + tag.length();
	}

	private boolean hasTag() {
		return tag == null;
	}

	void setStartTagOpenOffset(int startTagOpenOffset) {
		this.startTagOpenOffset = startTagOpenOffset;
	}

	public int getStartTagCloseOffset() {
		return startTagCloseOffset;
	}

	void setStartTagCloseOffset(int startTagCloseOffset) {
		this.startTagCloseOffset = startTagCloseOffset;
	}

	public String getTag() {
		return tag;
	}

	public int getEndTagOpenOffset() {
		return endTagOpenOffset;
	}

	void setEndTagOpenOffset(int endTagOpenOffset) {
		this.endTagOpenOffset = endTagOpenOffset;
	}

	public int getEndTagCloseOffset() {
		return endTagCloseOffset;
	}

	void setEndTagCloseOffset(int endTagCloseOffset) {
		this.endTagCloseOffset = endTagCloseOffset;
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

	public boolean isInStartTagName(int offset) {
		if (startTagOpenOffset == NULL_VALUE || startTagCloseOffset == NULL_VALUE) {
			// case {#|
			return true;
		}
		if (offset >= startTagOpenOffset && offset <= getAfterStartTagOpenOffset()) {
			// case {#each | }
			return true;
		}
		return false;
	}

	public boolean isInStartTag(int offset) {
		if (startTagOpenOffset == NULL_VALUE || startTagCloseOffset == NULL_VALUE) {
			// case <|
			return true;
		}
		if (offset > startTagOpenOffset && offset <= startTagCloseOffset) {
			// case <bean | >
			return true;
		}
		return false;
	}

	public boolean isInEndTag(int offset) {
		return isInEndTag(offset, false);
	}

	public boolean isInEndTag(int offset, boolean afterBackSlash) {
		if (endTagOpenOffset == NULL_VALUE) {
			// case >|
			return false;
		}
		if (offset > endTagOpenOffset + (afterBackSlash ? 1 : 0) && offset < getEnd()) {
			// case </bean | >
			return true;
		}
		return false;
	}

	/**
	 * Returns true if has a start tag.
	 *
	 * In our source-oriented DOM, a lone end tag will cause a node to be created in
	 * the tree, unlike well-formed-only DOMs.
	 *
	 * @return true if has a start tag.
	 */
	public boolean hasStartTag() {
		return getStartTagOpenOffset() != NULL_VALUE;
	}

	/**
	 * Returns true if has an end tag.
	 *
	 * In our source-oriented DOM, sometimes Elements are "ended", even without an
	 * explicit end tag in the source.
	 *
	 * @return true if has an end tag.
	 */
	public boolean hasEndTag() {
		return getEndTagOpenOffset() != NULL_VALUE;
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

	public List<Parameter> getParameters() {
		if (parameters == null) {
			this.parameters = parseParameters();
		}
		return this.parameters;
	}

	public Parameter getParameterAtIndex(int index) {
		List<Parameter> parameters = getParameters();
		if (parameters.size() > index) {
			return parameters.get(index);
		}
		return null;
	}

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

	public boolean isIterable() {
		return false;
	}

	@Override
	public int getStartParametersOffset() {
		return getAfterStartTagOpenOffset();
	}

	@Override
	public int getEndParametersOffset() {
		return getStartTagCloseOffset();
	}

	public boolean isInParameters(int offset) {
		return offset > getStartParametersOffset() && offset <= getEndParametersOffset();
	}

	@Deprecated()
	public Expression getExpressionParameter() {
		// Try to remove this method
		ExpressionParameter expression = new ExpressionParameter(getStartParametersOffset(), getEndParametersOffset());
		expression.setParent(this);
		return expression;
	}

	public ParametersInfo getParametersInfo() {
		return ParametersInfo.EMPTY;
	}
}

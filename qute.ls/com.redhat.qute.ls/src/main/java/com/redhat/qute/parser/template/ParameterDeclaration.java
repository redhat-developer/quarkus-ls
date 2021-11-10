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

import java.util.ArrayList;
import java.util.List;

public class ParameterDeclaration extends Node implements ParametersContainer, JavaTypeInfoProvider {

	private int startContent;

	private int endContent;

	ParameterDeclaration(int start, int end) {
		super(start, end);
	}

	@Override
	public NodeKind getKind() {
		return NodeKind.ParameterDeclaration;
	}

	public int getStartContent() {
		return startContent;
	}

	void setStartContent(int startContent) {
		this.startContent = startContent;
	}

	public int getEndContent() {
		return endContent;
	}

	void setEndContent(int endContent) {
		this.endContent = endContent;
	}

	public String getNodeName() {
		return "#parameter-declaration";
	}

	public String getJavaType() {
		Template template = getOwnerTemplate();
		int classNameStart = getClassNameStart();
		int classNameEnd = getClassNameEnd();
		return template.getText(classNameStart, classNameEnd);
	}

	public int getClassNameStart() {
		return getStartContent();
	}

	public int getClassNameEnd() {
		Template template = getOwnerTemplate();
		String text = template.getText();
		for (int i = getStartContent(); i < getEndContent(); i++) {
			char c = text.charAt(i);
			if (c == ' ') {
				return i;
			}
		}
		return getEndContent();
	}

	public boolean isInClassName(int offset) {
		int classNameStart = getClassNameStart();
		int classNameEnd = getClassNameEnd();
		return offset >= classNameStart && offset <= classNameEnd;
	}

	public String getAlias() {
		int aliasStart = getAliasStart();
		if (aliasStart == -1) {
			return null;
		}
		int aliasEnd = getAliasEnd();
		Template template = getOwnerTemplate();
		return template.getText(aliasStart, aliasEnd);
	}

	public int getAliasStart() {
		Template template = getOwnerTemplate();
		String text = template.getText();
		for (int i = getStartContent(); i < getEndContent(); i++) {
			char c = text.charAt(i);
			if (c == ' ' && (i + 1) < getEndContent()) {
				return i + 1;
			}
		}
		return -1;
	}

	public int getAliasEnd() {
		return getEndContent();
	}

	public boolean isInAlias(int offset) {
		int aliasStart = getAliasStart();
		int aliasEnd = getAliasEnd();
		return offset >= aliasStart && offset <= aliasEnd;
	}
	
	@Override
	public Node getJavaTypeOwnerNode() {
		return this;
	}

	@Override
	public int getStartParametersOffset() {
		return getStartContent();
	}

	@Override
	public int getEndParametersOffset() {
		return getEndContent();
	}

	public List<RangeOffset> getClassNameRanges() {
		List<RangeOffset> ranges = new ArrayList<>();
		Template template = getOwnerTemplate();
		String text = template.getText();
		// get start range
		int start = getStartContent();
		int end = start;
		boolean diamon = false;
		for (; end < getEndContent(); end++) {
			char c = text.charAt(end);
			if (c == ' ') {
				break;
			} else if (c == '<') {
				diamon = true;
				break;
			}
		}
		ranges.add(new RangeOffset(start, end));

		if (diamon) {
			end++;
			start = end;
			for (; end < getEndContent(); end++) {
				char c = text.charAt(end);
				if (c == ' ' || c == '>') {
					break;
				}
			}
			ranges.add(new RangeOffset(start, end));
		}
		return ranges;
	}

	public RangeOffset getClassNameRange(int offset) {
		List<RangeOffset> ranges = getClassNameRanges();
		for (RangeOffset range : ranges) {
			if (Node.isIncluded(range.getStart(), range.getEnd(), offset)) {
				return range;
			}
		}
		return null;
	}
}

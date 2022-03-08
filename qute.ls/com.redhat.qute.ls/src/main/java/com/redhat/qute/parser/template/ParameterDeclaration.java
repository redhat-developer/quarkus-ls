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

	public static class JavaTypeRangeOffset extends RangeOffset {

		private final boolean inGeneric;
		private final boolean genericClosed;

		public JavaTypeRangeOffset(int start, int end) {
			this(start, end, false, false);
		}

		public JavaTypeRangeOffset(int start, int end, boolean inGeneric, boolean genericClosed) {
			super(start, end);
			this.inGeneric = inGeneric;
			this.genericClosed = genericClosed;
		}

		public boolean isInGeneric() {
			return inGeneric;
		}

		public boolean isGenericClosed() {
			return genericClosed;
		}
	}

	ParameterDeclaration(int start, int end) {
		super(start, end);
	}

	@Override
	public NodeKind getKind() {
		return NodeKind.ParameterDeclaration;
	}

	public int getStartContent() {
		return super.getStart() + 2;
	}

	public int getEndContent() {
		int index = super.getEnd() - 1;
		if (isClosed()) {
			// }
			return index;
		}
		String text = super.getOwnerTemplate().getText();
		char c = text.charAt(index);
		while (c == '\r' || c == '\n') {
			index--;
			c = text.charAt(index);
		}
		return index + 1;
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

	public boolean isInJavaTypeName(int offset) {
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
		if (aliasStart == -1) {
			return false;
		}
		int aliasEnd = getAliasEnd();
		return offset >= aliasStart && offset <= aliasEnd;
	}

	public boolean hasAlias() {
		return getAliasStart() != -1;
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

	public List<JavaTypeRangeOffset> getJavaTypeNameRanges() {
		List<JavaTypeRangeOffset> ranges = new ArrayList<>();
		Template template = getOwnerTemplate();
		String text = template.getText();
		// get start range
		int end = getEndContent();
		int startType = getStartContent();
		int endType = startType;
		boolean diamon = false;
		for (; endType < end; endType++) {
			char c = text.charAt(endType);
			if (isSpace(c) || c == '[') {
				break;
			} else if (c == '<') {
				diamon = true;
				break;
			}
		}

		ranges.add(new JavaTypeRangeOffset(startType, endType));

		if (diamon) {
			boolean genericClosed = false;
			endType++;
			startType = endType;
			for (; endType < end; endType++) {
				char c = text.charAt(endType);
				if (isSpace(c) || c == '>') {
					genericClosed = c == '>';
					break;
				} else if (c == ',') {
					ranges.add(new JavaTypeRangeOffset(startType, endType, true, true));
					startType = endType + 1;
				}
			}
			ranges.add(new JavaTypeRangeOffset(startType, endType, true, genericClosed));
		}
		return ranges;
	}

	private static boolean isSpace(char c) {
		return c == '\r' || c == '\n' || c == ' ';
	}

	public JavaTypeRangeOffset getJavaTypeNameRange(int offset) {
		List<JavaTypeRangeOffset> ranges = getJavaTypeNameRanges();
		for (JavaTypeRangeOffset range : ranges) {
			if (Node.isIncluded(range.getStart(), range.getEnd(), offset)) {
				return range;
			}
		}
		return null;
	}

	@Override
	protected void accept0(ASTVisitor visitor) {
		visitor.visit(this);
		visitor.endVisit(this);
	}
}

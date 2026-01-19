/*******************************************************************************
* Copyright (c) 2026 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.parser.yaml;

/**
 * YAML Comment node.
 */
public class YamlComment extends YamlNode {

	private int startContentOffset = NULL_VALUE;
	private int endContentOffset = NULL_VALUE;

	public YamlComment(int start, int end) {
		super(start, end);
	}

	@Override
	public YamlNodeKind getKind() {
		return YamlNodeKind.YamlComment;
	}

	@Override
	public String getNodeName() {
		return "comment";
	}

	public int getStartContentOffset() {
		return startContentOffset;
	}

	public void setStartContentOffset(int startContentOffset) {
		this.startContentOffset = startContentOffset;
	}

	public int getEndContentOffset() {
		return endContentOffset;
	}

	public void setEndContentOffset(int endContentOffset) {
		this.endContentOffset = endContentOffset;
	}

	public String getContent() {
		YamlDocument doc = getOwnerDocument();
		if (doc == null || startContentOffset == NULL_VALUE || endContentOffset == NULL_VALUE) {
			return null;
		}
		return doc.getText(startContentOffset, endContentOffset);
	}

	@Override
	protected void accept0(YamlASTVisitor visitor) {
		boolean visitChildren = visitor.visit(this);
		if (visitChildren) {
			acceptChildren(visitor, getChildren());
		}
		visitor.endVisit(this);
	}
}
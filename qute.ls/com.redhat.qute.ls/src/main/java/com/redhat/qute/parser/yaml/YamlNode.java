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

import java.util.List;

import com.redhat.qute.parser.NodeBase;

public abstract class YamlNode extends NodeBase<YamlNode> {

	public YamlNode(int start, int end) {
		super(start, end);
	}

	public YamlDocument getOwnerDocument() {
		YamlNode node = getParent();
		while (node != null) {
			if (node.getKind() == YamlNodeKind.YamlDocument) {
				return (YamlDocument) node;
			}
			node = node.getParent();
		}
		return null;
	}

	@Override
	protected void setStart(int start) {
		super.setStart(start);
	}

	@Override
	protected void setEnd(int end) {
		super.setEnd(end);
	}

	@Override
	protected void setClosed(boolean closed) {
		super.setClosed(closed);
	}

	@Override
	protected void setParent(YamlNode parent) {
		super.setParent(parent);
	}

	@Override
	protected void addChild(YamlNode child) {
		super.addChild(child);
	}

	public final void accept(YamlASTVisitor visitor) {
		if (visitor == null) {
			throw new IllegalArgumentException();
		}
		if (visitor.preVisit2(this)) {
			accept0(visitor);
		}
		visitor.postVisit(this);
	}

	protected abstract void accept0(YamlASTVisitor visitor);

	protected final void acceptChild(YamlASTVisitor visitor, YamlNode child) {
		if (child == null) {
			return;
		}
		child.accept(visitor);
	}

	protected final void acceptChildren(YamlASTVisitor visitor, List<YamlNode> children) {
		for (YamlNode child : children) {
			child.accept(visitor);
		}
	}

	public abstract YamlNodeKind getKind();
}
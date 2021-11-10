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
package com.redhat.qute.parser.expression;

import com.redhat.qute.parser.expression.Parts.PartKind;
import com.redhat.qute.parser.template.Expression;
import com.redhat.qute.parser.template.Node;
import com.redhat.qute.parser.template.NodeKind;
import com.redhat.qute.parser.template.Section;

public abstract class Part extends Node {

	private String textContent;

	public Part(int start, int end) {
		super(start, end);
	}

	public String getPartName() {
		return getTextContent();
	}

	@Override
	public String getNodeName() {
		return "#part";
	}

	@Override
	public NodeKind getKind() {
		return NodeKind.ExpressionPart;
	}

	@Override
	public Parts getParent() {
		return (Parts) super.getParent();
	}

	public abstract PartKind getPartKind();

	public String getTextContent() {
		if (textContent != null) {
			return textContent;
		}
		return textContent = getOwnerTemplate().getText(getStart(), getEnd());
	}

	@Override
	public String toString() {
		return getPartName();
	}

	public boolean isLast() {
		Parts parts = getParent();
		return parts.getPartIndex(this) == parts.getChildCount() - 1;
	}

	/**
	 * Returns the parent section of the object part and null otherwise.
	 * 
	 * @return the parent section of the object part and null otherwise.
	 */
	public Section getParentSection() {
		Parts parts = getParent();
		Expression expression = parts.getParent();
		Node parent = expression.getParent();
		if (parent.getKind() == NodeKind.Parameter) {
			// Its' a parameter which belongs to a section (ex : items):
			// {#for item in items}
			// In this case we must get the parent of the parameter section
			Node ownerSection = parent.getParent();
			parent = ownerSection.getParent();
		}
		if (parent.getKind() == NodeKind.Section) {
			return (Section) parent;
		}
		return null;
	}

	public String getNamespace() {
		Parts parts = getParent();
		Part part = parts.getChild(0);
		if (this == part) {
			return null;
		}
		return (part.getPartKind() == PartKind.Namespace) ? part.getPartName() : null;
	}
}

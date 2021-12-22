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

/**
 * Base class for part (object, property, method part).
 * 
 * @author Anngelo ZERR
 *
 */
public abstract class Part extends Node {

	private String partName;

	public Part(int start, int end) {
		super(start, end);
	}

	/**
	 * Returns the part name.
	 * 
	 * @return the part name.
	 */
	public String getPartName() {
		if (partName != null) {
			return partName;
		}
		return partName = getOwnerTemplate().getText(getStart(), getEndName());
	}

	/**
	 * Returns the offset of the part name end.
	 * 
	 * @return the offset of the part name end.
	 */
	public int getEndName() {
		return getEnd();
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

	@Override
	public String toString() {
		return getPartName();
	}

	public boolean isLast() {
		Parts parts = getParent();
		return parts.getPartIndex(this) == parts.getChildCount() - 1;
	}

	@Override
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
		// the expression part can have an expression part as parent (method part called
		// on in an another method parameter)
		// loop for parent node to retrieve the parent section.
		while (parent != null) {
			if (parent.getKind() == NodeKind.Section) {
				return (Section) parent;
			}
			parent = parent.getParent();
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

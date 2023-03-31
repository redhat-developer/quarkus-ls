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

import com.redhat.qute.parser.template.ASTVisitor;
import com.redhat.qute.parser.template.Expression;
import com.redhat.qute.parser.template.Node;
import com.redhat.qute.parser.template.NodeKind;

public class Parts extends Node {

	public static enum PartKind {
		Namespace, //
		Object, //
		Property, //
		Method;
	}

	public Parts(int start, int end) {
		super(start, end);
	}

	@Override
	public String getNodeName() {
		return "#parts";
	}

	@Override
	public NodeKind getKind() {
		return NodeKind.ExpressionParts;
	}

	/**
	 * Returns the namespace of the parts and null otherwise.
	 * 
	 * @return the namespace of the parts and null otherwise.
	 */
	public String getNamespace() {
		NamespacePart namespacePart = getNamespacePart();
		return namespacePart != null ? namespacePart.getPartName() : null;
	}

	/**
	 * Returns the namespace part of the parts and null otherwise.
	 * 
	 * @return the namespace part of the parts and null otherwise.
	 */
	public NamespacePart getNamespacePart() {
		if (super.getChildCount() == 0) {
			return null;
		}
		Part firstPart = getChild(0);
		if (firstPart.getPartKind() == PartKind.Namespace) {
			return (NamespacePart) firstPart;
		}
		return null;
	}

	/**
	 * Returns the object part of the parts and null otherwise.
	 * 
	 * @return the object part of the parts and null otherwise.
	 */
	public ObjectPart getObjectPart() {
		if (super.getChildCount() == 0) {
			return null;
		}
		Part firstPart = getChild(0);
		switch (firstPart.getPartKind()) {
			case Object:
				return (ObjectPart) firstPart;
			case Namespace:
				if (super.getChildCount() == 1) {
					return null;
				}
				Part secondPart = getChild(1);
				return secondPart.getPartKind() == PartKind.Object ? (ObjectPart) secondPart : null;
			default:
				return null;
		}
	}

	/**
	 * Returns the method part of the parts and null otherwise.
	 * 
	 * @return the method part of the parts and null otherwise.
	 */
	public MethodPart getMethodPart() {
		if (super.getChildCount() == 0) {
			return null;
		}
		Part firstPart = getChild(0);
		switch (firstPart.getPartKind()) {
			case Method:
				return (MethodPart) firstPart;
			case Namespace:
				if (super.getChildCount() == 1) {
					return null;
				}
				Part secondPart = getChild(1);
				return secondPart.getPartKind() == PartKind.Method ? (MethodPart) secondPart : null;
			default:
				return null;
		}
	}

	@Override
	public Part getChild(int index) {
		return (Part) super.getChild(index);
	}

	void addPart(Part part) {
		super.addChild(part);
		super.setEnd(part.getEnd());
	}

	public void addDot(int tokenOffset) {
		super.setEnd(tokenOffset + 1);
	}

	public void addColonSpace(int tokenOffset) {
		super.setEnd(tokenOffset + 1);
	}

	public Part getPartAt(int offset) {
		Node node = super.findNodeAt(offset);
		if (node != null && node.getKind() == NodeKind.ExpressionPart) {
			return (Part) node;
		}
		return null;
	}

	void setExpressionParent(Expression expression) {
		super.setParent(expression);
	}

	@Override
	public Expression getParent() {
		return (Expression) super.getParent();
	}

	public int getPartIndex(Part part) {
		return super.getChildren().indexOf(part);
	}

	public Part getPreviousPart(Part part) {
		int partIndex = getPreviousPartIndex(part);
		return partIndex != -1 ? (Part) super.getChild(partIndex) : null;
	}

	private int getPreviousPartIndex(Part part) {
		return part != null ? super.getChildren().indexOf(part) - 1 : super.getChildCount() - 1;
	}

	public Part getNextPart(Part part) {
		int partIndex = getNextPartIndex(part);
		return partIndex != -1 ? (Part) super.getChild(partIndex) : null;
	}

	private int getNextPartIndex(Part part) {
		if (part == null) {
			return -1;
		}
		int index = super.getChildren().indexOf(part) + 1;
		return super.getChildCount() > index ? index : -1;
	}

	@Override
	public void setEnd(int end) {
		super.setEnd(end);
	}

	public String getContent() {
		return getOwnerTemplate().getText(getStart(), getEnd());
	}

	@Override
	protected void accept0(ASTVisitor visitor) {
		boolean visitChildren = visitor.visit(this);
		if (visitChildren) {
			acceptChildren(visitor, getChildren());
		}
		visitor.endVisit(this);
	}

}

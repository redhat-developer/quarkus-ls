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

import com.redhat.qute.parser.expression.ObjectPart;

public class Parameter extends Node implements JavaTypeInfoProvider {

	private String name = null;

	private String value = null;

	private int startName;

	private int endName;

	private int startValue = -1;

	private int endValue = -1;

	private ExpressionParameter expression;

	private boolean canHaveExpression;

	private int assignOffset = -1;

	private ParametersContainer container;

	public Parameter(int start, int end) {
		super(start, end);
		this.startName = start;
		this.endName = end;
	}

	public int getStartName() {
		return startName;
	}

	public int getEndName() {
		return endName;
	}

	public int getStartValue() {
		return startValue;
	}

	public void setStartValue(int startValue) {
		this.startValue = startValue;
	}

	public int getEndValue() {
		return endValue;
	}

	public void setEndValue(int endValue) {
		this.endValue = endValue;
		super.setEnd(endValue);
	}

	@Override
	public String getNodeName() {
		return getName();
	}

	@Override
	public NodeKind getKind() {
		return NodeKind.Parameter;
	}

	public void setParameterParent(ParametersContainer container) {
		if (container instanceof Node) {
			super.setParent((Node) container);
		} else {
			this.container = container;
		}

	}

	@Override
	public String toString() {
		return getName() + "=" + getValue();
	}

	/**
	 * Returns the parameter name.
	 * 
	 * @return the parameter name.
	 */
	public String getName() {
		if (!hasValueAssigned()) {
			// ex : {#if foo?? }
			Expression expression = getJavaTypeExpression();
			ObjectPart objectPart = expression != null ? expression.getObjectPart() : null;
			if (objectPart != null) {
				return objectPart.getPartName();
			}
		}
		if (name == null) {
			name = getText(getStartName(), getEndName());
		}
		return name;
	}

	public String getValue() {
		if (value == null) {
			if (hasValueAssigned()) {
				value = getText(getStartValue(), getEndValue());
			} else {
				value = getName();
			}
		}
		return value;
	}

	private String getText(int start, int end) {
		Template template = getOwnerTemplate();
		if (template != null) {
			return template.getText(start, end);
		}
		return container.getTemplateContent().substring(start, end);
	}

	public boolean hasValueAssigned() {
		return startValue != -1;
	}

	@Override
	public Expression getJavaTypeExpression() {
		if (!canHaveExpression()) {
			return null;
		}
		if (expression != null) {
			return expression;
		}
		// Parameter has name only, the expression is the name
		// ex : items in {#each items}
		int startExpression = getStartName();
		int endExpression = getEndName();
		if (hasValueAssigned()) {
			// Parameter has value, the expression is the value
			// ex : myParent=item.name in {#set myParent=item.name}
			startExpression = getStartValue();
			endExpression = getEndValue();
		}
		expression = new ExpressionParameter(startExpression, endExpression, getOwnerSection());
		expression.setParent(this);
		expression.setClosed(true);
		return expression;
	}

	public Section getOwnerSection() {
		Node parent = super.getParent();
		if (parent != null && parent.getKind() == NodeKind.Section) {
			return (Section) parent;
		}
		return null;
	}

	@Override
	public Node getJavaTypeOwnerNode() {
		return this;
	}

	public void setCanHaveExpression(boolean canHaveExpression) {
		this.canHaveExpression = canHaveExpression;
	}

	public boolean canHaveExpression() {
		return canHaveExpression;
	}

	public boolean isInName(int offset) {
		return Node.isIncluded(getStartName(), getEndName(), offset);
	}

	public boolean isInValue(int offset) {
		if (isAfterAssign(offset)) {
			return true;
		}
		if (!hasValueAssigned()) {
			return false;
		}
		return Node.isIncluded(getStartValue(), getEndValue(), offset);
	}

	public boolean isAfterAssign(int offset) {
		return offset == assignOffset + 1;
	}

	@Override
	protected void accept0(ASTVisitor visitor) {
		boolean visitChildren = visitor.visit(this);
		if (visitChildren) {
			Expression expression = getJavaTypeExpression();
			if (expression != null) {
				acceptChild(visitor, expression);
			}
		}
		visitor.endVisit(this);
	}

	public void setAssignOffset(int assignOffset) {
		this.assignOffset = assignOffset;
		super.setEnd(assignOffset);
	}

	/**
	 * Returns true if the last part of the expression parameter is optional (ends
	 * with ??) and false otherwise.
	 * 
	 * <p>
	 * {#let bar=foo??}
	 * {#if foo??}
	 * </p>
	 * 
	 * @return true if the last part of the expression parameter is optional (ends
	 *         with ??) and false otherwise.
	 */
	public boolean isOptional() {
		Expression expression = getJavaTypeExpression();
		return expression != null ? expression.isOptional() : false;
	}
}

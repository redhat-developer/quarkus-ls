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
import com.redhat.qute.parser.template.Parameter;
import com.redhat.qute.parser.template.Section;

/**
 * Base class for part (object, property, method part).
 * 
 * @author Anngelo ZERR
 *
 */
public abstract class Part extends Node {

	private String partName;

	private Boolean optional;

	public Part(int start, int end) {
		super(start, end);
	}

	@Override
	public String getNodeName() {
		return "#part";
	}

	@Override
	public NodeKind getKind() {
		return NodeKind.ExpressionPart;
	}

	/**
	 * Returns the start offset of the part name.
	 * 
	 * @return the start offset of the part name.
	 */
	public int getStartName() {
		return getStart();
	}

	/**
	 * Returns the end offset of the part name.
	 * 
	 * @return the end offset of the part name.
	 */
	public int getEndName() {
		computeOptionalIfNeeded();
		return getEnd();
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
		return partName = getOwnerTemplate().getText(getStartName(), getEndName());
	}

	/**
	 * Compute the optional information ( ex: valueNotFound?? means that
	 * valueNotFound is optional) if needed.
	 */
	private void computeOptionalIfNeeded() {
		if (canBeOptional() && optional == null) {
			int endName = getEnd();
			computeOptional(endName);
		}
	}

	/**
	 * Compute the optional information ( ex: valueNotFound?? means that
	 * valueNotFound is optional).
	 * 
	 * @param end the end name offset.
	 */
	private synchronized void computeOptional(int end) {
		if (optional != null) {
			return;
		}

		// valueNotFound??
		// See https://quarkus.io/guides/qute-reference#strict_rendering
		if (end - getStart() > 2) {
			String text = getOwnerTemplate().getText();
			if (text.charAt(end - 1) == '?' && text.charAt(end - 2) == '?') {
				// ex : {valueNotFound??}
				// adjust the end name offset and set optional flag to true
				setEnd(end - 2);
				optional = true; // valueNotFound??
				return;
			}
		}
		optional = false; // valueNotFound
	}

	/**
	 * Returns true if the part name is optional (ex : valueNotFound??) and false
	 * otherwise.
	 * 
	 * @return true if the part name is optional (ex : valueNotFound??) and false
	 *         otherwise.
	 */
	public boolean isOptional() {
		computeOptionalIfNeeded();
		return optional != null ? optional : false;
	}

	/**
	 * Returns true if the part (ex: object, property part) can be optional and
	 * false otherwise.
	 * 
	 * @return true if the part (ex: object, property part) can be optional and
	 *         false otherwise.
	 */
	protected boolean canBeOptional() {
		return false;
	}

	@Override
	public Parts getParent() {
		return (Parts) super.getParent();
	}

	/**
	 * Returns true if the part is the last from the parent parts and false
	 * otherwise.
	 * 
	 * @return true if the part is the last from the parent parts and false
	 *         otherwise.
	 */
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

	/**
	 * Returns the namespace part and null otherwise.
	 * 
	 * @return the namespace part and null otherwise.
	 */
	public String getNamespace() {
		Parts parts = getParent();
		int index = parts.getPartIndex(this);
		if (index != 1) {
			return null;
		}
		Part part = parts.getChild(0);
		if (this == part) {
			return null;
		}
		return (part.getPartKind() == PartKind.Namespace) ? part.getPartName() : null;
	}

	@Override
	public String toString() {
		return getPartName();
	}

	/**
	 * Returns the owner parameter of the object part and null otherwise.
	 * 
	 * <p>
	 * {#if foo?? }
	 * </p>
	 *
	 * <p>
	 * {#let foo='bar' }
	 * </p>
	 * 
	 * @return the owner parameter of the object part and null otherwise.
	 */
	public Parameter getOwnerParameter() {
		Expression expression = getParent().getParent();
		return expression != null ? expression.getOwnerParameter() : null;
	}
	/**
	 * Returns the part kind.
	 * 
	 * @return the part kind.
	 */
	public abstract PartKind getPartKind();

}

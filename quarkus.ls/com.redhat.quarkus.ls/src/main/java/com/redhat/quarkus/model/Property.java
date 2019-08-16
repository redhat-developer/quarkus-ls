/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.quarkus.model;

/**
 * The property node
 * 
 * @author Angelo ZERR
 *
 */
public class Property extends Node {

	private Node key;
	private Node delimiterAssign;
	private Node value;

	/**
	 * Returns the node key and null otherwise.
	 * 
	 * @return the node key and null otherwise.
	 */
	public Node getKey() {
		return key;
	}

	void setKey(Node key) {
		this.key = key;
		key.parent = this;
	}

	/**
	 * Returns the node value and null otherwise.
	 * 
	 * @return the node value and null otherwise.
	 */
	public Node getValue() {
		return value;
	}

	void setValue(Node value) {
		this.value = value;
		value.parent = this;
	}

	/**
	 * Returns the delimiter assign and null otherwise.
	 * 
	 * @return the delimiter assign and null otherwise.
	 */
	public Node getDelimiterAssign() {
		return delimiterAssign;
	}

	void setDelimiterAssign(Node delimiterAssign) {
		this.delimiterAssign = delimiterAssign;
		this.delimiterAssign.parent = this;
	}

	/**
	 * Returns the property name and null otherwise.
	 * 
	 * @return the property name and null otherwise.
	 */
	public String getPropertyName() {
		Node key = getKey();
		if (key == null) {
			return null;
		}
		return key.getText();
	}

	/**
	 * Returns the property value and null otherwise.
	 * 
	 * @return the property value and null otherwise.
	 */
	public String getPropertyValue() {
		Node value = getValue();
		if (value == null) {
			return null;
		}
		return value.getText();
	}

	@Override
	public Node findNodeAt(int offset) {
		Node key = getKey();
		if (key == null) {
			return this;
		}
		if (key.getEnd() == -1) {
			return key;
		}
		Node assign = getDelimiterAssign();
		if (assign == null) {
			return key;
		}
		if (offset >= assign.getStart()) {
			Node value = getValue();
			return value != null ? value : assign;
		}
		return key;
	}

	@Override
	public NodeType getNodeType() {
		return NodeType.PROPERTY;
	}
}

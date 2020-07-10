/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lsp4mp.model;

/**
 * The property node
 * 
 * @author Angelo ZERR
 *
 */
public class Property extends Node {

	private PropertyKey key;
	private Node delimiterAssign;
	private PropertyValue value;

	/**
	 * Returns the node key and null otherwise.
	 * 
	 * @return the node key and null otherwise.
	 */
	public PropertyKey getKey() {
		return key;
	}

	void setKey(PropertyKey key) {
		this.key = key;
		key.parent = this;
	}

	/**
	 * Returns the node value and null otherwise.
	 * 
	 * @return the node value and null otherwise.
	 */
	public PropertyValue getValue() {
		return value;
	}

	void setValue(PropertyValue value) {
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
	 * Returns the property key (profile + property name) and null otherwise.
	 * 
	 * @return the property key (profile + property name) and null otherwise.
	 */
	public String getPropertyKey() {
		Node key = getKey();
		if (key == null) {
			return null;
		}
		return key.getText();
	}

	/**
	 * Returns the profile of the property key and null otherwise.
	 * 
	 * <ul>
	 * <li>'%dev.key' will return 'dev'.</li>
	 * <li>'key' will return null.</li>
	 * </ul>
	 * 
	 * @return the profile of the property key and null otherwise.
	 */
	public String getProfile() {
		PropertyKey key = getKey();
		if (key == null) {
			return null;
		}
		return key.getProfile();
	}

	/**
	 * Returns the property name without the profile of the property key and null
	 * otherwise.
	 * 
	 * For multiline property names, this method returns the property name
	 * with the backslashes and newlines removed.
	 * 
	 * <ul>
	 * <li>'%dev.' will return null.</li>
	 * <li>'%dev.key' will return 'key'.</li>
	 * <li>'key' will return 'key'.</li>
	 * <li>
	 *   'key1.\
	 *    key2.\
	 *    key3' will return 'key1.key2.key3'
	 * </li>
	 * </ul>
	 * 
	 * @return the property name without the profile of the property key and null
	 *         otherwise.
	 */
	public String getPropertyName() {
		PropertyKey key = getKey();
		if (key == null) {
			return null;
		}
		return key.getPropertyName();
	}

	/**
	 * Returns the property name with the profile of the property key and null
	 * otherwise.
	 * 
	 * For multiline property names, this method returns the property name
	 * with the profile, with backslashes and newlines removed.
	 * 
	 * <ul>
	 * <li>'%dev.' will return '%dev.'.</li>
	 * <li>'%dev.key' will return '%dev.key'.</li>
	 * <li>'key' will return 'key'.</li>
	 * <li>
	 *   '%dev.\'
	 *   'key1.\
	 *    key2' will return '%dev.key1.key2'
	 * </li>
	 * </ul>
	 * 
	 * @return the property name with the profile of the property key and null
	 *         otherwise.
	 */
	public String getPropertyNameWithProfile() {
		PropertyKey key = getKey();
		if (key == null) {
			return null;
		}
		return key.getPropertyNameWithProfile();
	}

	/**
	 * Returns the property value and null otherwise.
	 * 
	 * For multiline property values, this method returns the property value
	 * with backslashes and newlines removed.
	 * 
	 * @return the property value and null otherwise.
	 */
	public String getPropertyValue() {
		PropertyValue value = getValue();
		if (value == null) {
			return null;
		}
		return value.getValue();
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
		if (assign.getStart() == offset) {
			return assign;
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

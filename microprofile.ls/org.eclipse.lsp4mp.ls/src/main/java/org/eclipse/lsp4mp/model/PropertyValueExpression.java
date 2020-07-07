/*******************************************************************************
* Copyright (c) 2020 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lsp4mp.model;

/**
 * Represents a portion of the property value that refers to the value of
 * another property.
 * 
 * When properties file is processed, the reference is replaced with the value
 * of the other property. In the properties file, it has the form:
 * <code>${other.property.name}</code>
 */
public class PropertyValueExpression extends Node {

	private Property referencedProperty;

	@Override
	public NodeType getNodeType() {
		return NodeType.PROPERTY_VALUE_EXPRESSION;
	}

	/**
	 * Returns the text that this Node contains.
	 * 
	 * Removes backslashes, and newlines. Doesn't not resolve the reference to
	 * another property.
	 */
	public String getValue() {
		String text = getText(true);
		return text != null ? text.trim() : null;
	}

	/**
	 * Get the <code>Property</code> that this property value expression refers to,
	 * or return null if it can't be found.
	 * 
	 * @return the <code>Property</code> that this property value expressions refers
	 *         to, or null if it can't be found.
	 */
	public Property getReferencedProperty() {
		// $ without {}
		if (referencedProperty == null) {
			String propName = getReferencedPropertyName();
			if (propName == null) {
				return null;
			}
			// TODO: resolve referenced property
		}
		return referencedProperty;
	}

	public void setReferencedProperty(Property referencedProperty) {
		this.referencedProperty = referencedProperty;
	}

	/**
	 * Get the name of the referenced property, or null if brackets were missing in
	 * the property expression.
	 * 
	 * Does not check if the referenced property exists.
	 * 
	 * @return the name of the referenced property, or null if brackets were missing
	 *         in the property expression.
	 */
	private String getReferencedPropertyName() {
		String val = getValue();
		if (val.length() < 3) {
			return null; // Just a $
		}
		return val.substring(2, val.length() - 1); // ${server.port} ==> server.port
	}

}
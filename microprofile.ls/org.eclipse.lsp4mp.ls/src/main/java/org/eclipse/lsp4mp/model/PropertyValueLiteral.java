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
 * Represents text in a property value that should be interpreted literally.
 */
public class PropertyValueLiteral extends Node {

	@Override
	public NodeType getNodeType() {
		return NodeType.PROPERTY_VALUE_LITERAL;
	}

	/**
	 * Returns the text this node contains and null otherwise.
	 * 
	 * If this node covers more than one line, the backslashes and newlines are
	 * removed.
	 * 
	 * @return the text this node contains and null otherwise
	 */
	public String getValue() {
		String text = getText(true);
		return text != null ? text.trim() : null;
	}

}
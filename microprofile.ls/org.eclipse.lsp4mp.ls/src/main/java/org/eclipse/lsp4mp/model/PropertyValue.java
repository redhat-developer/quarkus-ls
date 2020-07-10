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

import java.util.ArrayList;
import java.util.List;

/**
 * The property value node
 * 
 * @author Angelo ZERR
 *
 */
public class PropertyValue extends Node {

	@Override
	public NodeType getNodeType() {
		return NodeType.PROPERTY_VALUE;
	}

	/**
	 * Returns the property value and null otherwise.
	 * 
	 * For multiline property values, this method returns the property value
	 * with backslashes and newlines removed.
	 * 
	 * @return the property value and null otherwise
	 */
	public String getValue() {
		String text = getText(true);
		return text != null ? text.trim() : null;
	}

	/**
	 * Returns the leaf node that is at the given offset, or null if no child of
	 * this node is at the given offset
	 * 
	 * @return the child node that is at the given offset
	 */
	@Override
	public Node findNodeAt(int offset) {
		if (offset < getStart() || getEnd() < offset) {
			return null;
		}
		for (Node child : getChildren()) {
			if (child.getEnd() > offset && child.getStart() <= offset) {
				return child;
			}
		}
		return null;
	}

}

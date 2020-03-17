/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.microprofile.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import org.eclipse.lsp4j.Position;

import com.redhat.microprofile.ls.commons.BadLocationException;
import com.redhat.microprofile.ls.commons.TextDocument;

/**
 * Node class
 * 
 * @author Angelo ZERR
 *
 */
public abstract class Node {

	private List<Node> children;

	/**
	 * Node type
	 *
	 */
	public static enum NodeType {
		DOCUMENT, PROPERTY, PROPERTY_KEY, PROPERTY_VALUE, COMMENTS, ASSIGN;
	}

	private int start, end;

	Node parent;

	public Node() {
		this.start = -1;
		this.end = -1;
	}

	/**
	 * Returns the start offset of the node and -1 otherwise.
	 * 
	 * @return the start offset of the node and -1 otherwise.
	 */
	public int getStart() {
		return start;
	}

	void setStart(int start) {
		this.start = start;
	}

	/**
	 * Returns the end offset of the node and -1 otherwise.
	 * 
	 * @return the end offset of the node and -1 otherwise.
	 */
	public int getEnd() {
		return end;
	}

	void setEnd(int end) {
		this.end = end;
	}

	/**
	 * Returns the text of the node
	 * 
	 * @return the text of the node
	 */
	public String getText() {
		if (start == -1 || end == -1) {
			return null;
		}
		return getOwnerModel().getText(start, end).replace("\\\n", "");
	}

	/**
	 * Returns the owner properties model
	 * 
	 * @return the owner properties model
	 */
	public PropertiesModel getOwnerModel() {
		Node parent = this.parent;
		while (parent != null) {
			if (parent.getNodeType() == NodeType.DOCUMENT) {
				return (PropertiesModel) parent;
			}
			parent = parent.parent;
		}
		return null;
	}

	/**
	 * Returns the children of the node.
	 * 
	 * @return the children of the node.
	 */
	public List<Node> getChildren() {
		return children != null ? children : Collections.emptyList();
	}

	/**
	 * Add node
	 * 
	 * @param node the node to add
	 */
	void addNode(Node node) {
		if (children == null) {
			children = new ArrayList<>();
		}
		node.parent = this;
		children.add(node);
	}

	/**
	 * Returns the node type
	 * 
	 * @return the node type
	 */
	public abstract NodeType getNodeType();

	/**
	 * Returns the owner text document
	 * 
	 * @return the owner text document
	 */
	public TextDocument getDocument() {
		return getOwnerModel().getDocument();
	}

	/**
	 * Return the node at the given offset and null otherwise.
	 * 
	 * @param offset the offset
	 * @return the node at the given offset and null otherwise.
	 */
	public Node findNodeAt(int offset) {
		List<Node> children = getChildren();
		int idx = findFirst(children, c -> offset < c.getStart()) - 1;
		if (idx >= 0) {
			Node child = children.get(idx);
			if (isIncluded(child, offset)) {
				return child.findNodeAt(offset);
			}
		}
		return this;
	}

	/**
	 * Returns true if the node included the given offset and false otherwise.
	 * 
	 * @param node
	 * @param offset
	 * @return true if the node included the given offset and false otherwise.
	 */
	public static boolean isIncluded(Node node, int offset) {
		if (node == null) {
			return false;
		}
		return isIncluded(node.getStart(), node.getEnd(), offset);
	}

	public static boolean isIncluded(int start, int end, int offset) {
		return offset >= start && offset <= end;
	}

	/**
	 * Takes a sorted array and a function p. The array is sorted in such a way that
	 * all elements where p(x) is false are located before all elements where p(x)
	 * is true.
	 * 
	 * @returns the least x for which p(x) is true or array.length if no element
	 *          full fills the given function.
	 */
	private static <T> int findFirst(List<T> array, Function<T, Boolean> p) {
		int low = 0, high = array.size();
		if (high == 0) {
			return 0; // no children
		}
		while (low < high) {
			int mid = (int) Math.floor((low + high) / 2);
			if (p.apply(array.get(mid))) {
				high = mid;
			} else {
				low = mid + 1;
			}
		}
		return low;
	}

	public Node findNodeAt(Position position) throws BadLocationException {
		int offset = getDocument().offsetAt(position);
		return findNodeAt(offset);
	}
	
	public Node getParent() {
		return parent;
	}
}

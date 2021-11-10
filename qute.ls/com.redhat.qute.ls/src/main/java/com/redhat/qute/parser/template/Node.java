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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public abstract class Node {

	/**
	 * Null value used for offset.
	 */
	protected static final int NULL_VALUE = -1;

	private int start;
	private int end;
	private boolean closed;
	private Node parent;
	private List<Node> children;

	public Node(int start, int end) {
		this.start = start;
		this.end = end;
		this.closed = false;
	};

	/**
	 * Returns the owner document and null otherwise.
	 * 
	 * @return the owner document and null otherwise.
	 */
	public Template getOwnerTemplate() {
		Node node = parent;
		while (node != null) {
			if (node.getKind() == NodeKind.Template) {
				return (Template) node;
			}
			node = node.getParent();
		}
		return null;
	}

	public int getStart() {
		return start;
	}

	public int getEnd() {
		return end;
	}

	protected void setEnd(int end) {
		this.end = end;
	}

	void setClosed(boolean closed) {
		this.closed = closed;
	}

	public boolean isClosed() {
		return closed;
	}

	protected void addChild(Node child) {
		child.setParent(this);
		if (children == null) {
			children = new ArrayList<>();
		}
		children.add(child);
	}

	protected void setParent(Node parent) {
		this.parent = parent;
	}

	public Node getParent() {
		return parent;
	}

	public List<Node> getChildren() {
		if (children == null) {
			return Collections.emptyList();
		}
		return children;
	}

	/**
	 * Returns node child at the given index.
	 * 
	 * @param index
	 * @return node child at the given index.
	 */
	public Node getChild(int index) {
		return getChildren().get(index);
	}

	public int getChildCount() {
		return getChildren().size();
	}

	public abstract String getNodeName();

	public Node findNodeAt(int offset) {
		List<Node> children = getChildren();
		Node node = findNodeAt(children, offset);
		return node != null ? node : this;
	}

	public static Node findNodeAt(List<Node> children, int offset) {
		int idx = findFirst(children, c -> offset <= c.start) - 1;
		if (idx >= 0) {
			Node child = children.get(idx);
			if (isIncluded(child, offset)) {
				return child.findNodeAt(offset);
			}
		}
		return null;
	}

	/**
	 * Returns the node before
	 */
	public Node findNodeBefore(int offset) {
		List<Node> children = getChildren();
		int idx = findFirst(children, c -> offset <= c.start) - 1;
		if (idx >= 0) {
			Node child = children.get(idx);
			if (offset > child.start) {
				if (offset < child.end) {
					return child.findNodeBefore(offset);
				}
				Node lastChild = child.getLastChild();
				if (lastChild != null && lastChild.end == child.end) {
					return child.findNodeBefore(offset);
				}
				return child;
			}
		}
		return this;
	}

	public Node getLastChild() {
		return this.children != null && this.children.size() > 0 ? this.children.get(this.children.size() - 1) : null;
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

	public abstract NodeKind getKind();

}
/*******************************************************************************
* Copyright (c) 2026 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public abstract class NodeBase<T extends NodeBase<T>> {

	/**
	 * Null value used for offset.
	 */
	protected static final int NULL_VALUE = -1;

	private int start;
	private int end;
	private boolean closed;
	private T parent;
	private List<T> children;

	public NodeBase(int start, int end) {
		this.start = start;
		this.end = end;
		this.closed = false;
	};

	public int getStart() {
		return start;
	}

	protected void setStart(int start) {
		this.start = start;
	}

	public int getEnd() {
		return end;
	}

	protected void setEnd(int end) {
		this.end = end;
	}

	protected void setClosed(boolean closed) {
		this.closed = closed;
	}

	public boolean isClosed() {
		return closed;
	}

	protected void setParent(T parent) {
		this.parent = parent;
	}

	public T getParent() {
		return parent;
	}

	protected void addChild(T child) {
		child.setParent((T) this);
		if (children == null) {
			children = new ArrayList<>();
		}
		children.add(child);
	}

	public List<T> getChildren() {
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
	public T getChild(int index) {
		return getChildren().get(index);
	}

	public int getChildCount() {
		return getChildren().size();
	}

	public T getLastChild() {
		return this.children != null && this.children.size() > 0 ? this.children.get(this.children.size() - 1) : null;
	}

	public T findNodeAt(int offset) {
		List<T> children = getChildren();
		T node = findNodeAt(children, offset);
		return node != null ? node : (T) this;
	}

	/**
	 * Returns the node before
	 */
	public T findNodeBefore(int offset) {
		List<T> children = getChildren();
		int idx = findFirst(children, c -> offset <= c.getStart()) - 1;
		if (idx >= 0) {
			T child = children.get(idx);
			if (offset > child.getStart()) {
				if (offset < child.getEnd()) {
					return child.findNodeBefore(offset);
				}
				T lastChild = child.getLastChild();
				if (lastChild != null && lastChild.getEnd() == child.getEnd()) {
					return child.findNodeBefore(offset);
				}
				return child;
			}
		}
		return (T) this;
	}

	public T getNextSibling() {
		T parentNode = getParent();
		if (parentNode == null) {
			return null;
		}
		List<T> children = parentNode.getChildren();
		int nextIndex = children.indexOf(this) + 1;
		return nextIndex < children.size() ? children.get(nextIndex) : null;
	}

	public abstract String getNodeName();

	// Static methods

	public static <T extends NodeBase> T findNodeAt(List<T> children, int offset) {
		int idx = findFirst(children, c -> offset <= c.getStart()) - 1;
		if (idx >= 0) {
			T child = children.get(idx);
			if (isIncluded(child, offset)) {
				return (T) child.findNodeAt(offset);
			}
		}
		return null;
	}

	/**
	 * Returns true if the node included the given offset and false otherwise.
	 * 
	 * @param node
	 * @param offset
	 * @return true if the node included the given offset and false otherwise.
	 */
	public static <T extends NodeBase> boolean isIncluded(T node, int offset) {
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

}
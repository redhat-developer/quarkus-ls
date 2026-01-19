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

import java.util.List;

import com.redhat.qute.parser.NodeBase;

public abstract class Node extends NodeBase<Node> {

	/**
	 * Null value used for offset.
	 */
	protected static final int NULL_VALUE = -1;

	public Node(int start, int end) {
		super(start, end);
	}

	@Override
	protected void setStart(int start) {
		super.setStart(start);
	}

	@Override
	protected void setEnd(int end) {
		super.setEnd(end);
	}

	@Override
	protected void setClosed(boolean closed) {
		super.setClosed(closed);
	}

	@Override
	protected void setParent(Node parent) {
		super.setParent(parent);
	}

	@Override
	protected void addChild(Node child) {
		super.addChild(child);
	}

	/**
	 * Returns the owner document and null otherwise.
	 * 
	 * @return the owner document and null otherwise.
	 */
	public Template getOwnerTemplate() {
		Node node = getParent();
		while (node != null) {
			if (node.getKind() == NodeKind.Template) {
				return (Template) node;
			}
			node = node.getParent();
		}
		return null;
	}

	public Section getNextSiblingSection() {
		Node parentNode = getParent();
		if (parentNode == null) {
			return null;
		}
		List<Node> children = parentNode.getChildren();
		int nextIndex = children.indexOf(this) + 1;
		if (nextIndex >= children.size()) {
			return null;
		}
		for (int i = nextIndex; i < children.size(); i++) {
			Node node = children.get(i);
			if (node.getKind() == NodeKind.Section) {
				return (Section) node;
			}
		}
		return null;
	}

	/**
	 * Returns the orphan end section after the given offset which matches the given
	 * tagName and null otherwise.
	 * 
	 * The following sample sample with tagName=foo will returns the <\foo> orphan
	 * end section:
	 * <p>
	 * | {/foo}
	 * </p>
	 * 
	 * @param offset  the offset.
	 * @param tagName the tag name.
	 * 
	 * @return the orphan end section after the given offset which matches the given
	 *         tagName and null otherwise.
	 */
	public Section getOrphanEndSection(int offset, String tagName) {
		return getOrphanEndSection(offset, tagName, false);
	}

	/**
	 * Returns the orphan end section after the given offset which matches the given
	 * tagName and the first orphan end section otherwise and null otherwise.
	 * 
	 * The following sample sample with tagName=bar will returns the <\foo> orphan
	 * end section:
	 * <p>
	 * | {/foo}
	 * </p>
	 * 
	 * @param offset    the offset.
	 * @param tagName   the tag name.
	 * @param anyOrphan true if any orphan should be returned and false otherwise.
	 * 
	 * @return the orphan end section after the given offset which matches the given
	 *         tagName and the first orphan end section otherwise and null
	 *         otherwise.
	 */
	public Section getOrphanEndSection(int offset, String tagName, boolean anyOrphan) {
		Section nextSection = getNextSiblingSection();
		if (nextSection == null) {
			return null;
		}
		// for| {/for}
		if ((anyOrphan && nextSection.isOrphanEndTag()) || nextSection.isOrphanEndTagOf(tagName)) {
			return nextSection;
		}
		return null;
	}

	/**
	 * Returns the parent section of the node and null otherwise.
	 * 
	 * @return the parent section of the node and null otherwise.
	 */
	public Section getParentSection() {
		return getParentSection(false);
	}

	/**
	 * Returns the parent section of the node and null otherwise.
	 * 
	 * @return the parent section of the node and null otherwise.
	 */
	public Section getParentSection(boolean excludeSupportUnterminatedSection) {
		Node parent = getParent();
		while (parent != null && parent.getKind() != NodeKind.Template) {
			if (parent != null && parent.getKind() == NodeKind.Section) {
				Section parentSection = (Section) parent;
				if (!excludeSupportUnterminatedSection) {
					// we don't exclude #let, #include which can be not closed
					return parentSection;
				}
				if (parentSection.hasEndTag()) {
					// {#for}foo{/for}
					// here #for is the parent section of foo
					return parentSection;
				}
				if (!parentSection.canSupportUnterminatedSection()) {
					// {#for}foo
					// here #for is the parent section of foo
					// But
					// {#let}foo
					// here #let is NOT the parent section of foo
					return parentSection;
				}
			}
			parent = parent.getParent();
		}
		return null;
	}

	/**
	 * Accepts the given visitor on a visit of the current node.
	 *
	 * @param visitor the visitor object
	 * @exception IllegalArgumentException if the visitor is null
	 */
	public final void accept(ASTVisitor visitor) {
		if (visitor == null) {
			throw new IllegalArgumentException();
		}
		// begin with the generic pre-visit
		if (visitor.preVisit2(this)) {
			// dynamic dispatch to internal method for type-specific visit/endVisit
			accept0(visitor);
		}
		// end with the generic post-visit
		visitor.postVisit(this);
	}

	/**
	 * Accepts the given visitor on a type-specific visit of the current node. This
	 * method must be implemented in all concrete AST node types.
	 * <p>
	 * General template for implementation on each concrete ASTNode class:
	 * 
	 * <pre>
	 * <code>
	 * boolean visitChildren = visitor.visit(this);
	 * if (visitChildren) {
	 *    // visit children in normal left to right reading order
	 *    acceptChild(visitor, getProperty1());
	 *    acceptChildren(visitor, rawListProperty);
	 *    acceptChild(visitor, getProperty2());
	 * }
	 * visitor.endVisit(this);
	 * </code>
	 * </pre>
	 * 
	 * Note that the caller (<code>accept</code>) take cares of invoking
	 * <code>visitor.preVisit(this)</code> and <code>visitor.postVisit(this)</code>.
	 * </p>
	 *
	 * @param visitor the visitor object
	 */
	protected abstract void accept0(ASTVisitor visitor);

	/**
	 * Accepts the given visitor on a visit of the current node.
	 * <p>
	 * This method should be used by the concrete implementations of
	 * <code>accept0</code> to traverse optional properties. Equivalent to
	 * <code>child.accept(visitor)</code> if <code>child</code> is not
	 * <code>null</code>.
	 * </p>
	 *
	 * @param visitor the visitor object
	 * @param child   the child AST node to dispatch too, or <code>null</code> if
	 *                none
	 */
	protected final void acceptChild(ASTVisitor visitor, Node child) {
		if (child == null) {
			return;
		}
		child.accept(visitor);
	}

	/**
	 * Accepts the given visitor on a visit of the given live list of child nodes.
	 * <p>
	 * This method must be used by the concrete implementations of
	 * <code>accept</code> to traverse list-values properties; it encapsulates the
	 * proper handling of on-the-fly changes to the list.
	 * </p>
	 *
	 * @param visitor  the visitor object
	 * @param children the child AST node to dispatch too, or <code>null</code> if
	 *                 none
	 */
	protected final void acceptChildren(ASTVisitor visitor, List<Node> children) {
		for (Node child : children) {
			child.accept(visitor);
		}
	}

	/**
	 * Returns the node kind.
	 * 
	 * @return the node kind.
	 */
	public abstract NodeKind getKind();

}
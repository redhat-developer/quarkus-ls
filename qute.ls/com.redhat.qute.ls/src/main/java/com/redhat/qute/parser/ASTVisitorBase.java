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

/**
 * A visitor for abstract syntax trees.
 * 
 * @author Angelo ZERR
 *
 */
public abstract class ASTVisitorBase<T extends NodeBase<T>> {

	/**
	 * Visits the given AST node prior to the type-specific visit (before
	 * <code>visit</code>).
	 * <p>
	 * The default implementation does nothing. Subclasses may reimplement.
	 * </p>
	 *
	 * @param node the node to visit
	 *
	 * @see #preVisit2(NodeBase)
	 */
	public void preVisit(T node) {
		// default implementation: do nothing
	}

	/**
	 * Visits the given AST node prior to the type-specific visit (before
	 * <code>visit</code>).
	 * <p>
	 * The default implementation calls {@link #preVisit(NodeBase)} and then returns
	 * true. Subclasses may reimplement.
	 * </p>
	 *
	 * @param node the node to visit
	 * @return <code>true</code> if <code>visit(node)</code> should be called, and
	 *         <code>false</code> otherwise.
	 * @see #preVisit(NodeBase)
	 */
	public boolean preVisit2(T node) {
		preVisit(node);
		return true;
	}

	/**
	 * Visits the given AST node following the type-specific visit (after
	 * <code>endVisit</code>).
	 * <p>
	 * The default implementation does nothing. Subclasses may reimplement.
	 * </p>
	 *
	 * @param node the node to visit
	 */
	public void postVisit(T node) {
		// default implementation: do nothing
	}
}

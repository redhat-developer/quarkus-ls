/*******************************************************************************
* Copyright (c) 2022 Red Hat Inc. and others.
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

import com.redhat.qute.parser.expression.MethodPart;
import com.redhat.qute.parser.expression.NamespacePart;
import com.redhat.qute.parser.expression.ObjectPart;
import com.redhat.qute.parser.expression.Parts;
import com.redhat.qute.parser.expression.PropertyPart;
import com.redhat.qute.parser.template.sections.CaseSection;
import com.redhat.qute.parser.template.sections.CustomSection;
import com.redhat.qute.parser.template.sections.EachSection;
import com.redhat.qute.parser.template.sections.ElseSection;
import com.redhat.qute.parser.template.sections.ForSection;
import com.redhat.qute.parser.template.sections.IfSection;
import com.redhat.qute.parser.template.sections.IncludeSection;
import com.redhat.qute.parser.template.sections.InsertSection;
import com.redhat.qute.parser.template.sections.IsSection;
import com.redhat.qute.parser.template.sections.LetSection;
import com.redhat.qute.parser.template.sections.SetSection;
import com.redhat.qute.parser.template.sections.SwitchSection;
import com.redhat.qute.parser.template.sections.WhenSection;
import com.redhat.qute.parser.template.sections.WithSection;

/**
 * A visitor for abstract syntax trees.
 * 
 * @author Angelo ZERR
 *
 */
public abstract class ASTVisitor {

	/**
	 * Visits the given AST node prior to the type-specific visit (before
	 * <code>visit</code>).
	 * <p>
	 * The default implementation does nothing. Subclasses may reimplement.
	 * </p>
	 *
	 * @param node the node to visit
	 *
	 * @see #preVisit2(Node)
	 */
	public void preVisit(Node node) {
		// default implementation: do nothing
	}

	/**
	 * Visits the given AST node prior to the type-specific visit (before
	 * <code>visit</code>).
	 * <p>
	 * The default implementation calls {@link #preVisit(Node)} and then returns
	 * true. Subclasses may reimplement.
	 * </p>
	 *
	 * @param node the node to visit
	 * @return <code>true</code> if <code>visit(node)</code> should be called, and
	 *         <code>false</code> otherwise.
	 * @see #preVisit(ASTNode)
	 * @since 3.5
	 */
	public boolean preVisit2(Node node) {
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
	public void postVisit(Node node) {
		// default implementation: do nothing
	}

	/**
	 * Visits the given type-specific AST node.
	 * <p>
	 * The default implementation does nothing and return true. Subclasses may
	 * reimplement.
	 * </p>
	 *
	 * @param node the node to visit
	 * @return <code>true</code> if the children of this node should be visited, and
	 *         <code>false</code> if the children of this node should be skipped
	 */
	public boolean visit(Template node) {
		return true;
	}

	/**
	 * Visits the given type-specific AST node.
	 * <p>
	 * The default implementation does nothing and return true. Subclasses may
	 * reimplement.
	 * </p>
	 *
	 * @param node the node to visit
	 * @return <code>true</code> if the children of this node should be visited, and
	 *         <code>false</code> if the children of this node should be skipped
	 */
	public boolean visit(ParameterDeclaration node) {
		return true;
	}

	/**
	 * Visits the given type-specific AST node.
	 * <p>
	 * The default implementation does nothing and return true. Subclasses may
	 * reimplement.
	 * </p>
	 *
	 * @param node the node to visit
	 * @return <code>true</code> if the children of this node should be visited, and
	 *         <code>false</code> if the children of this node should be skipped
	 */
	public boolean visit(CData node) {
		return true;
	}

	/**
	 * Visits the given type-specific AST node.
	 * <p>
	 * The default implementation does nothing and return true. Subclasses may
	 * reimplement.
	 * </p>
	 *
	 * @param node the node to visit
	 * @return <code>true</code> if the children of this node should be visited, and
	 *         <code>false</code> if the children of this node should be skipped
	 */
	public boolean visit(Comment node) {
		return true;
	}

	/**
	 * Visits the given type-specific AST node.
	 * <p>
	 * The default implementation does nothing and return true. Subclasses may
	 * reimplement.
	 * </p>
	 *
	 * @param node the node to visit
	 * @return <code>true</code> if the children of this node should be visited, and
	 *         <code>false</code> if the children of this node should be skipped
	 */
	public boolean visit(Text node) {
		return true;
	}

	/**
	 * Visits the given type-specific AST node.
	 * <p>
	 * The default implementation does nothing and return true. Subclasses may
	 * reimplement.
	 * </p>
	 *
	 * @param node the node to visit
	 * @return <code>true</code> if the children of this node should be visited, and
	 *         <code>false</code> if the children of this node should be skipped
	 */
	public boolean visit(CaseSection node) {
		return true;
	}

	/**
	 * Visits the given type-specific AST node.
	 * <p>
	 * The default implementation does nothing and return true. Subclasses may
	 * reimplement.
	 * </p>
	 *
	 * @param node the node to visit
	 * @return <code>true</code> if the children of this node should be visited, and
	 *         <code>false</code> if the children of this node should be skipped
	 */
	public boolean visit(CustomSection node) {
		return true;
	}

	/**
	 * Visits the given type-specific AST node.
	 * <p>
	 * The default implementation does nothing and return true. Subclasses may
	 * reimplement.
	 * </p>
	 *
	 * @param node the node to visit
	 * @return <code>true</code> if the children of this node should be visited, and
	 *         <code>false</code> if the children of this node should be skipped
	 */
	public boolean visit(EachSection node) {
		return true;
	}

	/**
	 * Visits the given type-specific AST node.
	 * <p>
	 * The default implementation does nothing and return true. Subclasses may
	 * reimplement.
	 * </p>
	 *
	 * @param node the node to visit
	 * @return <code>true</code> if the children of this node should be visited, and
	 *         <code>false</code> if the children of this node should be skipped
	 */
	public boolean visit(ElseSection node) {
		return true;
	}

	/**
	 * Visits the given type-specific AST node.
	 * <p>
	 * The default implementation does nothing and return true. Subclasses may
	 * reimplement.
	 * </p>
	 *
	 * @param node the node to visit
	 * @return <code>true</code> if the children of this node should be visited, and
	 *         <code>false</code> if the children of this node should be skipped
	 */
	public boolean visit(ForSection node) {
		return true;
	}

	/**
	 * Visits the given type-specific AST node.
	 * <p>
	 * The default implementation does nothing and return true. Subclasses may
	 * reimplement.
	 * </p>
	 *
	 * @param node the node to visit
	 * @return <code>true</code> if the children of this node should be visited, and
	 *         <code>false</code> if the children of this node should be skipped
	 */
	public boolean visit(IfSection node) {
		return true;
	}

	/**
	 * Visits the given type-specific AST node.
	 * <p>
	 * The default implementation does nothing and return true. Subclasses may
	 * reimplement.
	 * </p>
	 *
	 * @param node the node to visit
	 * @return <code>true</code> if the children of this node should be visited, and
	 *         <code>false</code> if the children of this node should be skipped
	 */
	public boolean visit(IncludeSection node) {
		return true;
	}

	/**
	 * Visits the given type-specific AST node.
	 * <p>
	 * The default implementation does nothing and return true. Subclasses may
	 * reimplement.
	 * </p>
	 *
	 * @param node the node to visit
	 * @return <code>true</code> if the children of this node should be visited, and
	 *         <code>false</code> if the children of this node should be skipped
	 */
	public boolean visit(InsertSection node) {
		return true;
	}

	/**
	 * Visits the given type-specific AST node.
	 * <p>
	 * The default implementation does nothing and return true. Subclasses may
	 * reimplement.
	 * </p>
	 *
	 * @param node the node to visit
	 * @return <code>true</code> if the children of this node should be visited, and
	 *         <code>false</code> if the children of this node should be skipped
	 */
	public boolean visit(IsSection node) {
		return true;
	}

	/**
	 * Visits the given type-specific AST node.
	 * <p>
	 * The default implementation does nothing and return true. Subclasses may
	 * reimplement.
	 * </p>
	 *
	 * @param node the node to visit
	 * @return <code>true</code> if the children of this node should be visited, and
	 *         <code>false</code> if the children of this node should be skipped
	 */
	public boolean visit(LetSection node) {
		return true;
	}

	/**
	 * Visits the given type-specific AST node.
	 * <p>
	 * The default implementation does nothing and return true. Subclasses may
	 * reimplement.
	 * </p>
	 *
	 * @param node the node to visit
	 * @return <code>true</code> if the children of this node should be visited, and
	 *         <code>false</code> if the children of this node should be skipped
	 */
	public boolean visit(SetSection node) {
		return true;
	}

	/**
	 * Visits the given type-specific AST node.
	 * <p>
	 * The default implementation does nothing and return true. Subclasses may
	 * reimplement.
	 * </p>
	 *
	 * @param node the node to visit
	 * @return <code>true</code> if the children of this node should be visited, and
	 *         <code>false</code> if the children of this node should be skipped
	 */
	public boolean visit(SwitchSection node) {
		return true;
	}

	/**
	 * Visits the given type-specific AST node.
	 * <p>
	 * The default implementation does nothing and return true. Subclasses may
	 * reimplement.
	 * </p>
	 *
	 * @param node the node to visit
	 * @return <code>true</code> if the children of this node should be visited, and
	 *         <code>false</code> if the children of this node should be skipped
	 */
	public boolean visit(WhenSection node) {
		return true;
	}

	/**
	 * Visits the given type-specific AST node.
	 * <p>
	 * The default implementation does nothing and return true. Subclasses may
	 * reimplement.
	 * </p>
	 *
	 * @param node the node to visit
	 * @return <code>true</code> if the children of this node should be visited, and
	 *         <code>false</code> if the children of this node should be skipped
	 */
	public boolean visit(WithSection node) {
		return true;
	}

	/**
	 * Visits the given type-specific AST node.
	 * <p>
	 * The default implementation does nothing and return true. Subclasses may
	 * reimplement.
	 * </p>
	 *
	 * @param node the node to visit
	 * @return <code>true</code> if the children of this node should be visited, and
	 *         <code>false</code> if the children of this node should be skipped
	 */
	public boolean visit(Parameter node) {
		return true;
	}

	/**
	 * Visits the given type-specific AST node.
	 * <p>
	 * The default implementation does nothing and return true. Subclasses may
	 * reimplement.
	 * </p>
	 *
	 * @param node the node to visit
	 * @return <code>true</code> if the children of this node should be visited, and
	 *         <code>false</code> if the children of this node should be skipped
	 */
	public boolean visit(Expression node) {
		return true;
	}

	/**
	 * Visits the given type-specific AST node.
	 * <p>
	 * The default implementation does nothing and return true. Subclasses may
	 * reimplement.
	 * </p>
	 *
	 * @param node the node to visit
	 * @return <code>true</code> if the children of this node should be visited, and
	 *         <code>false</code> if the children of this node should be skipped
	 */
	public boolean visit(Parts node) {
		return true;
	}

	/**
	 * Visits the given type-specific AST node.
	 * <p>
	 * The default implementation does nothing and return true. Subclasses may
	 * reimplement.
	 * </p>
	 *
	 * @param node the node to visit
	 * @return <code>true</code> if the children of this node should be visited, and
	 *         <code>false</code> if the children of this node should be skipped
	 */
	public boolean visit(NamespacePart node) {
		return true;
	}

	/**
	 * Visits the given type-specific AST node.
	 * <p>
	 * The default implementation does nothing and return true. Subclasses may
	 * reimplement.
	 * </p>
	 *
	 * @param node the node to visit
	 * @return <code>true</code> if the children of this node should be visited, and
	 *         <code>false</code> if the children of this node should be skipped
	 */
	public boolean visit(ObjectPart node) {
		return true;
	}

	/**
	 * Visits the given type-specific AST node.
	 * <p>
	 * The default implementation does nothing and return true. Subclasses may
	 * reimplement.
	 * </p>
	 *
	 * @param node the node to visit
	 * @return <code>true</code> if the children of this node should be visited, and
	 *         <code>false</code> if the children of this node should be skipped
	 */
	public boolean visit(PropertyPart node) {
		return true;
	}

	/**
	 * Visits the given type-specific AST node.
	 * <p>
	 * The default implementation does nothing and return true. Subclasses may
	 * reimplement.
	 * </p>
	 *
	 * @param node the node to visit
	 * @return <code>true</code> if the children of this node should be visited, and
	 *         <code>false</code> if the children of this node should be skipped
	 */
	public boolean visit(MethodPart node) {
		return true;
	}

	/**
	 * End of visit the given type-specific AST node.
	 * <p>
	 * The default implementation does nothing. Subclasses may reimplement.
	 * </p>
	 *
	 * @param node the node to visit
	 */
	public void endVisit(Template node) {
		// default implementation: do nothing
	}

	/**
	 * End of visit the given type-specific AST node.
	 * <p>
	 * The default implementation does nothing. Subclasses may reimplement.
	 * </p>
	 *
	 * @param node the node to visit
	 */
	public void endVisit(ParameterDeclaration node) {
		// default implementation: do nothing
	}

	/**
	 * End of visit the given type-specific AST node.
	 * <p>
	 * The default implementation does nothing. Subclasses may reimplement.
	 * </p>
	 *
	 * @param node the node to visit
	 */
	public void endVisit(CData node) {
		// default implementation: do nothing
	}

	/**
	 * End of visit the given type-specific AST node.
	 * <p>
	 * The default implementation does nothing. Subclasses may reimplement.
	 * </p>
	 *
	 * @param node the node to visit
	 */
	public void endVisit(Comment node) {
		// default implementation: do nothing
	}

	/**
	 * End of visit the given type-specific AST node.
	 * <p>
	 * The default implementation does nothing. Subclasses may reimplement.
	 * </p>
	 *
	 * @param node the node to visit
	 */
	public void endVisit(Text node) {
		// default implementation: do nothing
	}

	/**
	 * End of visit the given type-specific AST node.
	 * <p>
	 * The default implementation does nothing. Subclasses may reimplement.
	 * </p>
	 *
	 * @param node the node to visit
	 */
	public void endVisit(CaseSection node) {
		// default implementation: do nothing
	}

	/**
	 * End of visit the given type-specific AST node.
	 * <p>
	 * The default implementation does nothing. Subclasses may reimplement.
	 * </p>
	 *
	 * @param node the node to visit
	 */
	public void endVisit(CustomSection node) {
		// default implementation: do nothing
	}

	/**
	 * End of visit the given type-specific AST node.
	 * <p>
	 * The default implementation does nothing. Subclasses may reimplement.
	 * </p>
	 *
	 * @param node the node to visit
	 */
	public void endVisit(EachSection node) {
		// default implementation: do nothing
	}

	/**
	 * End of visit the given type-specific AST node.
	 * <p>
	 * The default implementation does nothing. Subclasses may reimplement.
	 * </p>
	 *
	 * @param node the node to visit
	 */
	public void endVisit(ElseSection node) {
		// default implementation: do nothing
	}

	/**
	 * End of visit the given type-specific AST node.
	 * <p>
	 * The default implementation does nothing. Subclasses may reimplement.
	 * </p>
	 *
	 * @param node the node to visit
	 */
	public void endVisit(ForSection node) {
		// default implementation: do nothing
	}

	/**
	 * End of visit the given type-specific AST node.
	 * <p>
	 * The default implementation does nothing. Subclasses may reimplement.
	 * </p>
	 *
	 * @param node the node to visit
	 */
	public void endVisit(IfSection node) {
		// default implementation: do nothing
	}

	/**
	 * End of visit the given type-specific AST node.
	 * <p>
	 * The default implementation does nothing. Subclasses may reimplement.
	 * </p>
	 *
	 * @param node the node to visit
	 */
	public void endVisit(IncludeSection node) {
		// default implementation: do nothing
	}

	/**
	 * End of visit the given type-specific AST node.
	 * <p>
	 * The default implementation does nothing. Subclasses may reimplement.
	 * </p>
	 *
	 * @param node the node to visit
	 */
	public void endVisit(InsertSection node) {
		// default implementation: do nothing
	}

	/**
	 * End of visit the given type-specific AST node.
	 * <p>
	 * The default implementation does nothing. Subclasses may reimplement.
	 * </p>
	 *
	 * @param node the node to visit
	 */
	public void endVisit(IsSection node) {
		// default implementation: do nothing
	}

	/**
	 * End of visit the given type-specific AST node.
	 * <p>
	 * The default implementation does nothing. Subclasses may reimplement.
	 * </p>
	 *
	 * @param node the node to visit
	 */
	public void endVisit(LetSection node) {
		// default implementation: do nothing
	}

	/**
	 * End of visit the given type-specific AST node.
	 * <p>
	 * The default implementation does nothing. Subclasses may reimplement.
	 * </p>
	 *
	 * @param node the node to visit
	 */
	public void endVisit(SetSection node) {
		// default implementation: do nothing
	}

	/**
	 * End of visit the given type-specific AST node.
	 * <p>
	 * The default implementation does nothing. Subclasses may reimplement.
	 * </p>
	 *
	 * @param node the node to visit
	 */
	public void endVisit(SwitchSection node) {
		// default implementation: do nothing
	}

	/**
	 * End of visit the given type-specific AST node.
	 * <p>
	 * The default implementation does nothing. Subclasses may reimplement.
	 * </p>
	 *
	 * @param node the node to visit
	 */
	public void endVisit(WhenSection node) {
		// default implementation: do nothing
	}

	/**
	 * End of visit the given type-specific AST node.
	 * <p>
	 * The default implementation does nothing. Subclasses may reimplement.
	 * </p>
	 *
	 * @param node the node to visit
	 */
	public void endVisit(WithSection node) {
		// default implementation: do nothing
	}

	/**
	 * End of visit the given type-specific AST node.
	 * <p>
	 * The default implementation does nothing. Subclasses may reimplement.
	 * </p>
	 *
	 * @param node the node to visit
	 */
	public void endVisit(Parameter node) {
		// default implementation: do nothing
	}

	/**
	 * End of visit the given type-specific AST node.
	 * <p>
	 * The default implementation does nothing. Subclasses may reimplement.
	 * </p>
	 *
	 * @param node the node to visit
	 */
	public void endVisit(Expression node) {
		// default implementation: do nothing
	}

	/**
	 * End of visit the given type-specific AST node.
	 * <p>
	 * The default implementation does nothing. Subclasses may reimplement.
	 * </p>
	 *
	 * @param node the node to visit
	 */
	public void endVisit(Parts node) {
		// default implementation: do nothing
	}

	/**
	 * End of visit the given type-specific AST node.
	 * <p>
	 * The default implementation does nothing. Subclasses may reimplement.
	 * </p>
	 *
	 * @param node the node to visit
	 */
	public void endVisit(NamespacePart node) {
		// default implementation: do nothing
	}

	/**
	 * End of visit the given type-specific AST node.
	 * <p>
	 * The default implementation does nothing. Subclasses may reimplement.
	 * </p>
	 *
	 * @param node the node to visit
	 */
	public void endVisit(ObjectPart node) {
		// default implementation: do nothing
	}

	/**
	 * End of visit the given type-specific AST node.
	 * <p>
	 * The default implementation does nothing. Subclasses may reimplement.
	 * </p>
	 *
	 * @param node the node to visit
	 */
	public void endVisit(PropertyPart node) {
		// default implementation: do nothing
	}

	/**
	 * End of visit the given type-specific AST node.
	 * <p>
	 * The default implementation does nothing. Subclasses may reimplement.
	 * </p>
	 *
	 * @param node the node to visit
	 */
	public void endVisit(MethodPart node) {
		// default implementation: do nothing
	}
}

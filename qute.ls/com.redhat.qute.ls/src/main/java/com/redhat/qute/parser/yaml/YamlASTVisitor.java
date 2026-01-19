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
package com.redhat.qute.parser.yaml;

import com.redhat.qute.parser.ASTVisitorBase;

/**
 * A visitor for YAML AST.
 */
public abstract class YamlASTVisitor extends ASTVisitorBase<YamlNode> {

	public boolean visit(YamlDocument node) {
		return true;
	}

	public boolean visit(YamlMapping node) {
		return true;
	}

	public boolean visit(YamlSequence node) {
		return true;
	}

	public boolean visit(YamlScalar node) {
		return true;
	}

	public boolean visit(YamlProperty node) {
		return true;
	}

	public boolean visit(YamlComment node) {
		return true;
	}

	public void endVisit(YamlDocument node) {
		// default implementation: do nothing
	}

	public void endVisit(YamlMapping node) {
		// default implementation: do nothing
	}

	public void endVisit(YamlSequence node) {
		// default implementation: do nothing
	}

	public void endVisit(YamlScalar node) {
		// default implementation: do nothing
	}

	public void endVisit(YamlProperty node) {
		// default implementation: do nothing
	}

	public void endVisit(YamlComment node) {
		// default implementation: do nothing
	}
}
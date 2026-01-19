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

import com.redhat.qute.parser.yaml.scanner.YamlTokenType;

/**
 * YAML Scalar node (string, number, boolean, null).
 */
public class YamlScalar extends YamlNode {

	private YamlTokenType scalarType;
	private boolean quoted;

	public YamlScalar(int start, int end, YamlTokenType scalarType) {
		super(start, end);
		this.scalarType = scalarType;
		this.quoted = false;
	}

	@Override
	public YamlNodeKind getKind() {
		return YamlNodeKind.YamlScalar;
	}

	@Override
	public String getNodeName() {
		return "scalar";
	}

	public YamlTokenType getScalarType() {
		return scalarType;
	}

	public void setScalarType(YamlTokenType scalarType) {
		this.scalarType = scalarType;
	}

	public boolean isQuoted() {
		return quoted;
	}

	public void setQuoted(boolean quoted) {
		this.quoted = quoted;
	}

	public String getValue() {
		YamlDocument doc = getOwnerDocument();
		if (doc == null) {
			return null;
		}
		return doc.getText(getStart(), getEnd());
	}

	@Override
	protected void accept0(YamlASTVisitor visitor) {
		boolean visitChildren = visitor.visit(this);
		if (visitChildren) {
			acceptChildren(visitor, getChildren());
		}
		visitor.endVisit(this);
	}
}
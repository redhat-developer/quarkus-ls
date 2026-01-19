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

/**
 * YAML Mapping node (like an object with key-value pairs).
 */
public class YamlMapping extends YamlCollectionNode {

	public YamlMapping(int start, int end, boolean flowStyle) {
		super(start, end, flowStyle);
	}

	@Override
	public YamlNodeKind getKind() {
		return YamlNodeKind.YamlMapping;
	}

	@Override
	public String getNodeName() {
		return "mapping";
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
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
 * YAML Property node (key-value pair in a mapping).
 */
public class YamlProperty extends YamlNode {

	private YamlNode key;
	private YamlNode value;
	private int colonOffset = NULL_VALUE;

	public YamlProperty(int start, int end) {
		super(start, end);
	}

	@Override
	public YamlNodeKind getKind() {
		return YamlNodeKind.YamlProperty;
	}

	@Override
	public String getNodeName() {
		return "property";
	}

	public YamlNode getKey() {
		return key;
	}

	public void setKey(YamlNode key) {
		this.key = key;
		if (key != null) {
			key.setParent(this);
		}
	}

	public YamlNode getValue() {
		return value;
	}

	public void setValue(YamlNode value) {
		this.value = value;
		if (value != null) {
			value.setParent(this);
		}
	}

	public int getColonOffset() {
		return colonOffset;
	}

	public void setColonOffset(int colonOffset) {
		this.colonOffset = colonOffset;
	}

	@Override
	protected void accept0(YamlASTVisitor visitor) {
		boolean visitChildren = visitor.visit(this);
		if (visitChildren) {
			if (key != null) {
				acceptChild(visitor, key);
			}
			if (value != null) {
				acceptChild(visitor, value);
			}
		}
		visitor.endVisit(this);
	}
}
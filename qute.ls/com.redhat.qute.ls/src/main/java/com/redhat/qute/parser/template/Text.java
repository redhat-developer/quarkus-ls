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

public class Text extends Node {

	Text(int start, int end) {
		super(start, end);
		super.setClosed(true);
	}

	@Override
	public NodeKind getKind() {
		return NodeKind.Text;
	}

	@Override
	public String getNodeName() {
		return "#text";
	}
}

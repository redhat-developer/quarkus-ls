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
package com.redhat.qute.parser.parameter.scanner;

public enum TokenType {

	ParameterName, //
	Assign, //
	ParameterValue,
	// Other token types
	Whitespace, //
	Unknown, //
	EOS;
}

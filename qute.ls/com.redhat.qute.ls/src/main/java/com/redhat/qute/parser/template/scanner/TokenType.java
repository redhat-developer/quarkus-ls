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
package com.redhat.qute.parser.template.scanner;

public enum TokenType {

	// String token types
	StartString, //
	String, //
	EndString, //

	// Comment token types
	StartComment, //
	Comment, //
	EndComment, //

	// Unparsed Character Data
	CDATATagOpen, //
	CDATATagClose, //
	CDATAOldTagOpen, //
	CDATAOldTagClose, //
	CDATAContent,

	// Expressions token types
	StartExpression, //
	// Expression, //
	EndExpression, //

	// Section tag token types
	StartTagOpen, //
	StartTag, //
	StartTagSelfClose, //
	StartTagClose, //
	EndTagOpen, //
	EndTag, //
	EndTagClose, //
	ParameterTag, //

	// Parameter declaration
	StartParameterDeclaration, //
	ParameterDeclaration, //
	EndParameterDeclaration, //

	// Other token types
	Content, //
	Whitespace, //
	Unknown, //
	EOS;
}

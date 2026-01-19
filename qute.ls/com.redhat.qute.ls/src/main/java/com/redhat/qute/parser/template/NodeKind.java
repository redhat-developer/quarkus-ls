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

public enum NodeKind {

	// Template nodes
	Template, //
	ParameterDeclaration, //
	Section, //
	Expression, //
	Comment, //
	CData, //
	Text, //

	// Expression nodes
	ExpressionParts, //
	ExpressionPart,

	// Parameter in section
	Parameter, //

	// Language injection
	LanguageInjection;
}

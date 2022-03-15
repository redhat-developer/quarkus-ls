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

/**
 * Expression inside section parameters. Expression parameter can be:
 * 
 * <ul>
 * <li>a parameter name:
 * <p>
 * {#each EXPRESSION_PARAMETER}
 * </p>
 * </li>
 * <li>a parameter value:
 * <p>
 * {#let name=EXPRESSION_PARAMETER}
 * </p>
 * </li>
 * </ul>
 * 
 * @author Angelo ZERR
 *
 */
public class ExpressionParameter extends Expression {

	// The own section of the expression (ex : #let of {#let
	// name=EXPRESSION_PARAMETER})
	private final Section ownerSection;

	ExpressionParameter(int start, int end, Section ownerSection) {
		super(start, end);
		this.ownerSection = ownerSection;
		super.setParent(ownerSection);
	}

	/**
	 * Returns the parent section of the owner section of the expression parameter
	 * and null otherwise.
	 * 
	 * @return the parent section of the owner section of the expression parameter
	 *         and null otherwise.
	 */
	@Override
	public Section getParentSection() {
		if (ownerSection != null) {
			return ownerSection.getParentSection();
		}
		return super.getParentSection();
	}
	
	@Override
	public boolean canSupportInfixNotation() {
		return false;
	}
}

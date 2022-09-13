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

/**
 * Operators to be used in #case or #is section within a #switch or #when
 * section
 *
 * @see https://quarkus.io/guides/qute-reference#when_operators
 *
 */

public class CaseOperator extends Operator {

	private final boolean multi;

	public CaseOperator(String name, String documentation, String aliasFor, boolean multi) {
		super(name, documentation, aliasFor);
		this.multi = multi;
	}

	public boolean isMulti() {
		return multi;
	}
}
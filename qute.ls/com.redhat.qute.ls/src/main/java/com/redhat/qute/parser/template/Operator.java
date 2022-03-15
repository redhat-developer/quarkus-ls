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
 * Operator.
 * 
 * @author Angelo ZERR
 *
 */
public class Operator {

	private final String name;
	private final String[] aliases;

	public Operator(String name, String[] aliases) {
		this.name = name;
		this.aliases = aliases;
	}

	/**
	 * Returns the operator name (ex : "eq")
	 * 
	 * @return the operator name (ex : "eq")
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the aliases (ex : for "eq", aliases are "==", "is")
	 * 
	 * @return the aliases (ex : for "eq", aliases are "==", "is")
	 */
	public String[] getAliases() {
		return aliases;
	}
}

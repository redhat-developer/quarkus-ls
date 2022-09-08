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

	private final String documentation;

	private final String aliasFor;

	public Operator(String name, String documentation, String aliasFor) {
		this.name = name;
		this.documentation = documentation + (aliasFor != null ? " (alias for `" + aliasFor + "`)" : "");
		this.aliasFor = aliasFor;
	}

	/**
	 * Returns the operator name (ex : "eq")
	 * 
	 * @return the operator name (ex : "eq")
	 */
	public String getName() {
		return name;
	}

	public String getDocumentation() {
		return documentation;
	}

	public String getAliasFor() {
		return aliasFor;
	}
}

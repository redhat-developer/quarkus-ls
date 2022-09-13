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

import java.util.List;

/**
 * Operator.
 * 
 * @author Angelo ZERR
 *
 */
public class Operator implements DocumentableItem {

	private final String name;

	private final String aliasFor;

	private final String description;

	private List<String> sample;

	private String url;

	public Operator(String name, String description, String aliasFor) {
		this.name = name;
		this.description = description + (aliasFor != null ? " (alias for `" + aliasFor + "`)" : "");
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

	/**
	 * Returns the operator that the current operator is for (ex : for "<", aliasFor
	 * is "lt")
	 * 
	 * @return the operator that the current operator is for (ex : for "<", aliasFor
	 *         is "lt")
	 */
	public String getAliasFor() {
		return aliasFor;
	}

	/**
	 * Set a sample with the operator.
	 *
	 * @param sample a sample with the operator.
	 */
	public void setSample(List<String> sample) {
		this.sample = sample;
	}

	/**
	 * Returns a sample with the operator and null otherwise.
	 *
	 * @return a sample with the operator and null otherwise.
	 */
	public List<String> getSample() {
		return sample;
	}

	/**
	 * Set the documentation Url of the operator.
	 *
	 * @param url the documentation Url of the operator.
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * Returns the documentation Url of the operator and null otherwise.
	 *
	 * @return the documentation Url of the operator and null otherwise.
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * Returns the description of the operator and null otherwise.
	 *
	 * @return the description of the operator and null otherwise.
	 */
	public String getDescription() {
		return description;
	}
}

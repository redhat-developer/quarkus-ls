/*******************************************************************************
* Copyright (c) 2022 Red Hat Inc. and others.
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License v. 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
* which is available at https://www.apache.org/licenses/LICENSE-2.0.
*
* SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.commons.datamodel.resolvers;

import java.util.List;

/**
 * The namespace resolver information.
 * 
 * @author Angelo ZERR
 *
 */
public class NamespaceResolverInfo {

	private List<String> namespaces;

	private String description;

	private String url;

	/**
	 * Returns the supported namespaces (ex : inject, cdi).
	 * 
	 * @return the supported namespaces (ex : inject, cdi).
	 */
	public List<String> getNamespaces() {
		return namespaces;
	}

	/**
	 * Set the supported namespaces (ex : inject, cdi).
	 * 
	 * @param namespaces the supported namespaces.
	 */
	public void setNamespaces(List<String> namespaces) {
		this.namespaces = namespaces;
	}

	/**
	 * Returns the description of the namespace resolver.
	 * 
	 * @return the description of the namespace resolver.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Set the description of the namespace resolver.
	 * 
	 * @param description the description of the namespace resolver.
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Returns the documentation url of the namespace resolver.
	 * 
	 * @return the documentation url of the namespace resolver.
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * Set the documentation url of the namespace resolver.
	 * 
	 * @param url the documentation url of the namespace resolver.
	 */
	public void setUrl(String url) {
		this.url = url;
	}

}

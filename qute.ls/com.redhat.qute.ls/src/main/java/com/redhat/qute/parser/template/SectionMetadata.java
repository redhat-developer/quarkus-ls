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
 * Metadata for Qute section.
 * 
 * @author Angelo ZERR
 *
 */
public class SectionMetadata implements JavaTypeInfoProvider {

	private final String name;
	private final String type;
	private final String description;

	public SectionMetadata(String name, String type) {
		this(name, type, null);
	}

	public SectionMetadata(String name, String type, String description) {
		this.name = name;
		this.type = type;
		this.description = description;
	}

	/**
	 * Returns the metadata name.
	 * 
	 * Example:
	 * 
	 * <p>
	 * item_count
	 * </p>
	 * 
	 * @return the metadata name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the Java type of the metadata.
	 * 
	 * Example:
	 * 
	 * <p>
	 * java.lang.Integer
	 * </p>
	 * 
	 * @return the Java type of the metadata.
	 */
	public String getType() {
		return type;
	}

	/**
	 * Returns the description of the metadata and null otherwise.
	 * 
	 * @return the description of the metadata and null otherwise.
	 */
	public String getDescription() {
		return description;
	}

	@Override
	public String getJavaType() {
		return getType();
	}

	@Override
	public Node getJavaTypeOwnerNode() {
		return null;
	}

}

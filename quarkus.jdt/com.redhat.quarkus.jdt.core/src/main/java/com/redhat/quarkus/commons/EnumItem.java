/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.quarkus.commons;

/**
 * Enumeration item.
 * 
 * @author Angelo ZERR
 *
 */
public class EnumItem {

	private String name;

	private String docs;

	public EnumItem() {
		this(null, null);
	}

	public EnumItem(String name, String docs) {
		this.name = name;
		this.docs = docs;
	}

	/**
	 * Returns the enumeration name.
	 * 
	 * @return the enumeration name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Set the the enumeration name.
	 * 
	 * @param name the enumeration name.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Returns the enumeration documentation and null otherwise.
	 * 
	 * @return the enumeration documentation and null otherwise.
	 */
	public String getDocs() {
		return docs;
	}

	/**
	 * Set the enumeration documentation and null otherwise.
	 * 
	 * @param docs the enumeration documentation and null otherwise.
	 */
	public void setDocs(String docs) {
		this.docs = docs;
	}

}

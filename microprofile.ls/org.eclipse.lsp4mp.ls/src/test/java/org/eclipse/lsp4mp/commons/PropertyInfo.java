/*******************************************************************************
* Copyright (c) 2019-2020 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lsp4mp.commons;

import org.eclipse.lsp4mp.commons.metadata.ItemMetadata;

/**
 * Property information which contains:
 * 
 * <ul>
 * <li>a MicroProfile property</li>
 * <li>a profile</li>
 * <ul>
 * 
 * @author Angelo ZERR
 *
 */
public class PropertyInfo {

	private final ItemMetadata property;

	private final String profile;

	public PropertyInfo(ItemMetadata property, String profile) {
		this.property = property;
		this.profile = profile;
	}

	/**
	 * Returns the MicroProfile property and null otherwise.
	 * 
	 * @return the MicroProfile property and null otherwise.
	 */
	public ItemMetadata getProperty() {
		return property;
	}

	/**
	 * Returns the profile.
	 * 
	 * @return the profile.
	 */
	public String getProfile() {
		return profile;
	}

}

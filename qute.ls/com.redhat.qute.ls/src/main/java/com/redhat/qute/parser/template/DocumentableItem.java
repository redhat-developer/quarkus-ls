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
 * DocumentableItem.
 *
 */
public interface DocumentableItem {

	/**
	 * Returns the description of the item and null otherwise.
	 * 
	 * @return the description of the item and null otherwise.
	 */
	String getDescription();

	/**
	 * Returns a sample with the item and null otherwise.
	 * 
	 * @return a sample with the item and null otherwise.
	 */
	List<String> getSample();

	/**
	 * Returns the documentation Url of the item and null otherwise.
	 * 
	 * @return the documentation Url of the item and null otherwise.
	 */
	String getUrl();
}
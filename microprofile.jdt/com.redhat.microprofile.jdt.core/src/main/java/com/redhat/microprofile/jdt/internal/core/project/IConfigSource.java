/*******************************************************************************
* Copyright (c) 2020 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.microprofile.jdt.internal.core.project;

import java.util.Set;

/**
 * Configuration file API
 * 
 * @author Angelo ZERR
 *
 */
public interface IConfigSource {

	/**
	 * Returns the property from the given <code>key</code> and null otherwise.
	 * 
	 * @param key the key
	 * @return the property from the given <code>key</code> and null otherwise.
	 */
	String getProperty(String key);

	/**
	 * Returns the property as Integer from the given <code>key</code> and null
	 * otherwise.
	 * 
	 * @param key the key
	 * @return the property as Integer from the given <code>key</code> and null
	 *         otherwise.
	 */
	Integer getPropertyAsInt(String key);
	
	/**
	 * Returns all property keys defined in the config.
	 * 
	 * @return all property keys defined in the config.
	 */
	Set<String> getPropertyKeys();
}

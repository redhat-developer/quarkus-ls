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
package com.redhat.qute.commons.datamodel;

import org.eclipse.xtext.xbase.lib.util.ToStringBuilder;

/**
 * A parameter information for a data model template {@link DataModelTemplate}.
 * 
 * @author Angelo ZERR
 *
 */
public class DataModelParameter {

	private String key;

	private String sourceType;

	/**
	 * Returns the parameter key.
	 * 
	 * @return the parameter key.
	 */
	public String getKey() {
		return key;
	}

	/**
	 * Set the parameter key.
	 * 
	 * @param key the parameter key.
	 */
	public void setKey(String key) {
		this.key = key;
	}

	/**
	 * Returns the Java source type.
	 * 
	 * @return the Java source type.
	 */
	public String getSourceType() {
		return sourceType;
	}

	/**
	 * Set the Java source type.
	 * 
	 * @param sourceType the Java source type.
	 */
	public void setSourceType(String sourceType) {
		this.sourceType = sourceType;
	}

	@Override
	public String toString() {
		ToStringBuilder b = new ToStringBuilder(this);
		b.add("key", this.key);
		b.add("sourceType", this.sourceType);
		return b.toString();
	}
}

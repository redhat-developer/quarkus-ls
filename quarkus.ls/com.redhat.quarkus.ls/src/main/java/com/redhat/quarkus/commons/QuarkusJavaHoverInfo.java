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

import org.eclipse.lsp4j.Range;

/**
 * Quarkus Hover Information
 * 
 * This class represents the return object for the
 * <code>quarkus.java.hover</code> command
 */
public class QuarkusJavaHoverInfo {

	private String propertyKey;
	private String propertyValue;
	private Range range;

	/**
	 * Creates a new <code>QuarkusJavaHoverInfo</code> object
	 * @param propertyKey   the property key being hovered
	 * @param propertyValue the property value of the property key being hovered
	 * @param range         the range of the property key being hovered
	 */
	public QuarkusJavaHoverInfo(String propertyKey, String propertyValue, Range range) {
		this.propertyKey = propertyKey;
		this.propertyValue = propertyValue;
		this.range = range;
	}
	
	/**
	 * Returns the property key
	 * 
	 * @return the property key
	 */
	public String getPropertyKey() {
		return propertyKey;
	}
	
	/**
	 * Returns the property value for the property key
	 * @return the property value for the property key
	 */
	public String getPropertyValue() {
		return propertyValue;
	}

	/**
	 * Returns the range of the property key
	 * @return the range of the property key
	 */
	public Range getRange() {
		return range;
	}
}
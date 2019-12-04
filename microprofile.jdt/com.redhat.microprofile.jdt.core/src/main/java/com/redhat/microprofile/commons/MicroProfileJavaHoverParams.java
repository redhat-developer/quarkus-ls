/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.microprofile.commons;

import org.eclipse.lsp4j.Position;

/**
 * Quarkus Java codelens parameters.
 * 
 * @author Angelo ZERR
 *
 */
public class MicroProfileJavaHoverParams {

	private String uri;
	private Position position;
	
	public MicroProfileJavaHoverParams() {
		
	}

	public MicroProfileJavaHoverParams(String uri, Position position) {
		this();
		setUri(uri);
		setPosition(position);
	}

	/**
	 * Returns the java file uri.
	 * 
	 * @return the java file uri.
	 */
	public String getUri() {
		return uri;
	}

	/**
	 * Set the java file uri.
	 * 
	 * @param uri the java file uri.
	 */
	public void setUri(String uri) {
		this.uri = uri;
	}

	/**
	 * Returns the hover position
	 * 
	 * @return the hover position
	 */
	public Position getPosition() {
		return position;
	}

	/**
	 * Sets the hover position
	 * 
	 * @param position the hover position
	 */
	public void setPosition(Position position) {
		this.position = position;
	}
}
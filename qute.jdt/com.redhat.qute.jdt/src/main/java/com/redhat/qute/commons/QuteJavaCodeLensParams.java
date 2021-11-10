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
package com.redhat.qute.commons;

/**
 * Qute Java codelens parameters.
 *
 * @author Angelo ZERR
 *
 */
public class QuteJavaCodeLensParams {

	private String uri;

	public QuteJavaCodeLensParams() {

	}

	public QuteJavaCodeLensParams(String uri) {
		this();
		setUri(uri);
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

}

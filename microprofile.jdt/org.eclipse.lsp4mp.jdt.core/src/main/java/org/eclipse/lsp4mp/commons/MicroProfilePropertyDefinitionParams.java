/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lsp4mp.commons;

/**
 * MicroProfile property definition parameters to retrieve the definition of the
 * MicroProfile property in Java class field or Java method.
 * 
 * @author Angelo ZERR
 *
 */
public class MicroProfilePropertyDefinitionParams {

	private String uri;

	private String sourceType;

	private String sourceField;

	private String sourceMethod;

	/**
	 * Returns the application.properties URI.
	 * 
	 * @return the application.properties URI
	 */
	public String getUri() {
		return uri;
	}

	/**
	 * Set the application.properties URI
	 * 
	 * @param uri the application.properties URI
	 */
	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getSourceType() {
		return sourceType;
	}

	public void setSourceType(String sourceType) {
		this.sourceType = sourceType;
	}

	public String getSourceField() {
		return sourceField;
	}

	public void setSourceField(String sourceField) {
		this.sourceField = sourceField;
	}

	public String getSourceMethod() {
		return sourceMethod;
	}

	public void setSourceMethod(String sourceMethod) {
		this.sourceMethod = sourceMethod;
	}

}

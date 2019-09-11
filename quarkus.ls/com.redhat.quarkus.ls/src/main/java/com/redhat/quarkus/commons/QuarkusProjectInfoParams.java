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

import java.util.List;

/**
 * Quarkus project information parameters.
 * 
 * @author Angelo ZERR
 *
 */
public class QuarkusProjectInfoParams {

	private String uri;

	private List<String> documentationFormat;

	private QuarkusPropertiesScope scope;

	public QuarkusProjectInfoParams() {
	}

	public QuarkusProjectInfoParams(String uri, List<String> documentationFormat) {
		setUri(uri);
		setDocumentationFormat(documentationFormat);
	}

	/**
	 * Returns the uri of the application.properties file.
	 * 
	 * @return the uri of the application.properties file.
	 */
	public String getUri() {
		return uri;
	}

	/**
	 * Set the uri of the application.properties file.
	 * 
	 * @param uri the uri of the application.properties file.
	 */
	public void setUri(String uri) {
		this.uri = uri;
	}

	/**
	 * Client supports the following content formats for the documentation property.
	 * The order describes the preferred format of the client.
	 */
	public List<String> getDocumentationFormat() {
		return this.documentationFormat;
	}

	/**
	 * Client supports the following content formats for the documentation property.
	 * The order describes the preferred format of the client.
	 */
	public void setDocumentationFormat(final List<String> documentationFormat) {
		this.documentationFormat = documentationFormat;
	}

	/**
	 * Returns the search scope to collect the Quarkus properties.
	 * 
	 * @return the search scope to collect the Quarkus properties.
	 */
	public QuarkusPropertiesScope getScope() {
		return scope;
	}

	/**
	 * Set the search scope to collect the Quarkus properties.
	 * 
	 * @param scope the search scope to collect the Quarkus properties.
	 */
	public void setScope(QuarkusPropertiesScope scope) {
		this.scope = scope;
	}
}

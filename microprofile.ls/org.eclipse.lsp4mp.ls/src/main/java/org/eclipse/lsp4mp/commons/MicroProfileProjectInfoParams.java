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

import java.util.List;

/**
 * MicroProfile project information parameters.
 * 
 * @author Angelo ZERR
 *
 */
public class MicroProfileProjectInfoParams {

	private String uri;

	private List<MicroProfilePropertiesScope> scopes;

	private DocumentFormat documentFormat;

	public MicroProfileProjectInfoParams() {
	}

	public MicroProfileProjectInfoParams(String uri) {
		setUri(uri);
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
	 * Returns the search scope to collect the MicroProfile properties.
	 * 
	 * @return the search scope to collect the MicroProfile properties.
	 */
	public List<MicroProfilePropertiesScope> getScopes() {
		return scopes;
	}

	/**
	 * Set the search scope to collect the MicroProfile properties.
	 * 
	 * @param scope the search scope to collect the MicroProfile properties.
	 */
	public void setScopes(List<MicroProfilePropertiesScope> scopes) {
		this.scopes = scopes;
	}

	/**
	 * Set the document format for description.
	 * 
	 * @param documentFormat the document format for description.
	 */
	public void setDocumentFormat(DocumentFormat documentFormat) {
		this.documentFormat = documentFormat;
	}

	/**
	 * Returns the document format for description.
	 * 
	 * @return the document format for description.
	 */
	public DocumentFormat getDocumentFormat() {
		return documentFormat;
	}
}

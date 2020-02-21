/*******************************************************************************
* Copyright (c) 2020 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.microprofile.commons;

import java.util.List;

/**
 * MicroProfile Java diagnostics parameters.
 * 
 * @author Angelo ZERR
 *
 */
public class MicroProfileJavaDiagnosticsParams {

	private List<String> uris;

	private DocumentFormat documentFormat;

	public MicroProfileJavaDiagnosticsParams() {
		this(null);
	}

	public MicroProfileJavaDiagnosticsParams(List<String> uris) {
		setUris(uris);
	}

	/**
	 * Returns the java file uris list.
	 * 
	 * @return the java file uris list.
	 */
	public List<String> getUris() {
		return uris;
	}

	/**
	 * Set the java file uris list.
	 * 
	 * @param uris the java file uris list.
	 */
	public void setUris(List<String> uris) {
		this.uris = uris;
	}

	public DocumentFormat getDocumentFormat() {
		return documentFormat;
	}

	public void setDocumentFormat(DocumentFormat documentFormat) {
		this.documentFormat = documentFormat;
	}

}
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
package com.redhat.qute.commons;

/**
 * Represents the parameters needed to fetch the javadocs from the Java language
 * server component.
 * 
 * @author datho7561
 */
public class QuteJavadocParams {

	private String sourceType;
	private String projectUri;
	private String memberName;
	private String signature;
	private DocumentFormat documentFormat;

	public QuteJavadocParams() {
		// needed for gson
	}

	public QuteJavadocParams(String sourceType, String projectUri, String memberName, String signature,
			DocumentFormat documentFormat) {
		this.sourceType = sourceType;
		this.projectUri = projectUri;
		this.memberName = memberName;
		this.signature = signature;
		this.documentFormat = documentFormat;
	}

	/**
	 * Returns the fully qualified name of the class from which the Javadocs should
	 * be retrieved.
	 * 
	 * @return the fully qualified name of the class from which the Javadocs should
	 *         be retrieved
	 */
	public String getSourceType() {
		return this.sourceType;
	}

	/**
	 * Returns the uri of the project where the Javadocs should be retrieved from.
	 * 
	 * @return the uri of the project where the Javadocs should be retrieved from
	 */
	public String getProjectUri() {
		return this.projectUri;
	}

	/**
	 * Returns the name of the field or method to get the documentation of.
	 * 
	 * @return the name of the field or method to get the documentation of
	 */
	public String getMemberName() {
		return this.memberName;
	}

	/**
	 * Returns the signature of the field or method for which the documentation is
	 * being retrieved.
	 * 
	 * @return the signature of the field or method for which the documentation is
	 *         being retrieved
	 */
	public String getSignature() {
		return this.signature;
	}

	/**
	 * Returns the document format that should be used to represent the
	 * documentation.
	 * 
	 * @return the document format that should be used to represent the
	 *         documentation
	 */
	public DocumentFormat getDocumentFormat() {
		return this.documentFormat;
	}

}

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
package com.redhat.qute.commons.usertags;

/**
 * Binary user tag information.
 * 
 * @author Angelo ZERR
 *
 */
public class UserTagInfo {

	private String fileName;

	private String uri;

	private String content;

	/**
	 * Returns the file name.
	 * 
	 * @return the file name.
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * Set the file name.
	 * 
	 * @param fileName the file name.o
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * Returns the user tag binary uri.
	 * 
	 * @return the user tag binary uri.
	 */
	public String getUri() {
		return uri;
	}

	/**
	 * Set the user tag binary uri.
	 * 
	 * @param uri the user tag binary uri.
	 */
	public void setUri(String uri) {
		this.uri = uri;
	}

	/**
	 * Returns the user tag template content.
	 * 
	 * @return the user tag template content.
	 */
	public String getContent() {
		return content;
	}

	/**
	 * Set the user tag template content.
	 * 
	 * @param content the user tag template content.
	 */
	public void setContent(String content) {
		this.content = content;
	}

}

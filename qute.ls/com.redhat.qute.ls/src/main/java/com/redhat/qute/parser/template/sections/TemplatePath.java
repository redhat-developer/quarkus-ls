/*******************************************************************************
* Copyright (c) 2026 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.parser.template.sections;

import java.nio.file.Path;

/**
 * Template path info.
 */
public class TemplatePath {

	private final String uri;
	private final boolean exists;

	public TemplatePath(Path templatePath, boolean exists) {
		this(templatePath.toUri().toASCIIString(), exists);
	}

	public TemplatePath(String uri, boolean valid) {
		this.uri = uri;
		this.exists = valid;
	}

	/**
	 * Returns the file, java (jdt) uri of the template.
	 * 
	 * @return the file, java (jdt) uri of the template.
	 */
	public String getUri() {
		return uri;
	}

	/**
	 * Returns true if the template exists and false otherwise.
	 * 
	 * @return true if the template exists and false otherwise.
	 */
	public boolean isExists() {
		return exists;
	}

}

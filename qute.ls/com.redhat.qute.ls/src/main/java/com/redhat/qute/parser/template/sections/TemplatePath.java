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

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Template path info.
 */
public class TemplatePath {

	private final String uri;
	private final String templateId;
	private final boolean exists;

	public TemplatePath(Path templatePath, String templateId) {
		this(templatePath, templateId, Files.exists(templatePath));
	}

	public TemplatePath(Path templatePath, String templateId, boolean exists) {
		this(templatePath.toUri().toASCIIString(), templateId, exists);
	}

	public TemplatePath(String uri, String templateId, boolean valid) {
		this.uri = uri;
		this.templateId = templateId;
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
	
	public String getTemplateId() {
		return templateId;
	}

	/**
	 * Returns true if the template exists and false otherwise.
	 * 
	 * @return true if the template exists and false otherwise.
	 */
	public boolean isExists() {
		return exists;
	}

	@Override
	public int hashCode() {
		return Objects.hash(templateId, uri);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TemplatePath other = (TemplatePath) obj;
		return Objects.equals(templateId, other.templateId) && Objects.equals(uri, other.uri);
	}

	
}

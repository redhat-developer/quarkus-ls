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
package com.redhat.qute.project.extensions.config;

import java.io.IOException;
import java.nio.file.Path;

import com.redhat.qute.commons.TemplateRootPath;

/**
 * Represents a .qute configuration file marking a template root directory.
 *
 * <p>
 * A .qute file in a template root directory indicates configuration for all templates
 * in that directory and subdirectories, such as whether to use alternative expression
 * syntax via the {@code alt-expr-syntax} property.
 * </p>
 *
 * @author Angelo ZERR
 */
public class DotQuteFile extends PropertiesFile {

	private final TemplateRootPath templateRootPath;

	/**
	 * Creates a new .qute file and loads its content.
	 *
	 * @param quteFile the path to the .qute file
	 * @param templateRootPath the template root path associated with this .qute file
	 * @param propertiesFileName the file name info
	 * @throws IOException if an error occurs reading the file
	 */
	public DotQuteFile(Path quteFile, TemplateRootPath templateRootPath, PropertiesFileName propertiesFileName) throws IOException {
		super(quteFile, propertiesFileName);
		this.templateRootPath = templateRootPath;
	}

	/**
	 * Returns the template root path associated with this .qute file.
	 *
	 * @return the template root path
	 */
	public TemplateRootPath getTemplateRootPath() {
		return templateRootPath;
	}

	/**
	 * Gets the alt-expr-syntax configuration value.
	 *
	 * @return true if alternative expression syntax is enabled
	 */
	public boolean isAltExprSyntax() {
		String value = getProperty("alt-expr-syntax");
		return "true".equals(value);
	}

}

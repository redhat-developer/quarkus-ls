/*******************************************************************************
* Copyright (c) 2023 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.project.documents;

import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.redhat.qute.commons.FileUtils;
import com.redhat.qute.project.QuteProject;
import com.redhat.qute.utils.IOUtils;

/**
 * Qute template document closed.
 * 
 * @author Angelo ZERR
 *
 */
public class QuteClosedTextDocument extends QuteReadOnlyTextDocument {

	private static final Logger LOGGER = Logger.getLogger(QuteClosedTextDocument.class.getName());

	private final Path templatePath;

	private String relativePath;

	public QuteClosedTextDocument(Path templatePath, QuteProject project) {
		super(FileUtils.toUri(templatePath), project.getTemplateId(templatePath), getContent(templatePath), project);
		this.templatePath = templatePath;
	}

	private static String getContent(Path templatePath) {
		try {
			return IOUtils.getContent(templatePath);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Error while loading template '" + templatePath.toUri().toASCIIString() + "'.", e);
			return "";
		}
	}

	@Override
	public Path getTemplatePath() {
		return templatePath;
	}

	@Override
	public String getOrigin() {
		return null;
	}

	@Override
	public String getRelativePath() {
		if (relativePath == null && getProject().getProjectFolder() != null) {
			this.relativePath = getProject().getUri() + '/'
					+ getProject().getProjectFolder().relativize(templatePath).toString().replace('\\', '/');
		}
		return relativePath;
	}

}
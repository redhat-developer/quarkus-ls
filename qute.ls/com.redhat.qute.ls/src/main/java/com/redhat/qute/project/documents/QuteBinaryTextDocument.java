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
import java.util.Map;

import com.redhat.qute.commons.TemplateRootPath;
import com.redhat.qute.commons.binary.BinaryTemplate;
import com.redhat.qute.project.QuteProject;

/**
 * Qute binary template document.
 * 
 * @author Angelo ZERR
 *
 */
public class QuteBinaryTextDocument extends QuteReadOnlyTextDocument {

	private final BinaryTemplate binaryTemplate;

	private final String binaryName;

	private final Map<String, String> properties;

	private final Character expressionCommand;

	public QuteBinaryTextDocument(BinaryTemplate binaryTemplate, String binaryName, Map<String, String> properties,
			Character expressionCommand, QuteProject project) {
		super(binaryTemplate.getUri(), binaryTemplate.getPath(), binaryTemplate.getContent(), project);
		this.binaryTemplate = binaryTemplate;
		this.binaryName = binaryName;
		this.properties = properties;
		this.expressionCommand = expressionCommand;
	}

	@Override
	public boolean isBinary() {
		return true;
	}

	@Override
	public Path getTemplatePath() {
		return null;
	}

	@Override
	public String getOrigin() {
		return binaryName;
	}

	@Override
	public String getRelativePath() {
		return getOrigin() + "!/" + binaryTemplate.getPath();
	}

	@Override
	public String getProperty(String name) {
		if (properties == null) {
			return null;
		}
		return properties.get(name);
	}

	@Override
	public void reparseTemplate() {
		super.template = loadTemplate(binaryTemplate.getUri(), binaryTemplate.getPath(), binaryTemplate.getContent());
	}

	@Override
	public Character getExpressionCommand() {
		return expressionCommand;
	}

	@Override
	public TemplateRootPath getTemplateRootPath() {
		return null;
	}
}
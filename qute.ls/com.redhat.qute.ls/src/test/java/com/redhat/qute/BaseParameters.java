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
package com.redhat.qute;

import java.util.Collection;
import java.util.Collections;

import com.redhat.qute.parser.injection.InjectionDetector;

public class BaseParameters {

	private String fileUri;
	private String templateId;
	private String projectUri;
	private String templateBaseDir;
	private Collection<InjectionDetector> injectionDetectors;

	public BaseParameters() {
		setFileUri(QuteAssert.FILE_URI);
		setTemplateBaseDir(QuteAssert.TEMPLATE_BASE_DIR);
		setInjectionDetectors(Collections.emptyList());
	}

	public String getFileUri() {
		return fileUri;
	}

	public void setFileUri(String fileUri) {
		this.fileUri = fileUri;
	}

	public String getTemplateId() {
		return templateId;
	}

	public void setTemplateId(String templateId) {
		this.templateId = templateId;
	}

	public String getProjectUri() {
		return projectUri;
	}

	public void setProjectUri(String projectUri) {
		this.projectUri = projectUri;
	}

	public String getTemplateBaseDir() {
		return templateBaseDir;
	}

	public void setTemplateBaseDir(String templateBaseDir) {
		this.templateBaseDir = templateBaseDir;
	}

	public Collection<InjectionDetector> getInjectionDetectors() {
		return injectionDetectors;
	}

	public void setInjectionDetectors(Collection<InjectionDetector> injectionDetectors) {
		this.injectionDetectors = injectionDetectors;
	}

}

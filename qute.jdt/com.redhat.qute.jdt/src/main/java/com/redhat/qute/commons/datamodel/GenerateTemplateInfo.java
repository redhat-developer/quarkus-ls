/*******************************************************************************
* Copyright (c) 2021 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.commons.datamodel;

import java.util.List;

/**
 * Template information used to generate a Qute template.
 * 
 * @author Angelo ZERR
 *
 */
public class GenerateTemplateInfo {

	private String projectUri;

	private String templateFileUri;

	private String templateFilePath;

	private List<DataModelParameter> parameters;

	/**
	 * Returns the Qute project Uri.
	 * 
	 * @return the Qute project Uri.
	 */
	public String getProjectUri() {
		return projectUri;
	}

	/**
	 * Set the Qute project Uri.
	 * 
	 * @param projectUri the Qute project Uri.
	 */
	public void setProjectUri(String projectUri) {
		this.projectUri = projectUri;
	}

	/**
	 * Returns the Qute template file Uri.
	 * 
	 * @return the Qute template file Uri.
	 */
	public String getTemplateFileUri() {
		return templateFileUri;
	}

	/**
	 * Set the Qute template file Uri.
	 * 
	 * @param templateFileUri the Qute template file Uri.
	 */
	public void setTemplateFileUri(String templateFileUri) {
		this.templateFileUri = templateFileUri;
	}

	/**
	 * Returns the Qute template file path.
	 * 
	 * @return the Qute template file path.
	 */
	public String getTemplateFilePath() {
		return templateFilePath;
	}

	/**
	 * Set the Qute template file path.
	 * 
	 * @param templateFilePath the Qute template file path.
	 */
	public void setTemplateFilePath(String templateFilePath) {
		this.templateFilePath = templateFilePath;
	}

	/**
	 * Returns the data model parameter list.
	 * 
	 * @return the data model parameter list.
	 */
	public List<DataModelParameter> getParameters() {
		return parameters;
	}

	/**
	 * Set the data model parameter list.
	 * 
	 * @param parameters the data model parameter list.
	 */
	public void setParameters(List<DataModelParameter> parameters) {
		this.parameters = parameters;
	}

}

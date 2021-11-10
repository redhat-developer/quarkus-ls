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
package com.redhat.qute.commons;

/**
 * Qute project parameter used to retrieve a {@link ProjectInfo} from Java side.
 * 
 * @author Angelo ZERR
 *
 */
public class QuteProjectParams {

	private String templateFileUri;

	public QuteProjectParams(String templateFileUri) {
		setTemplateFileUri(templateFileUri);
	}

	/**
	 * Returns the template file Uri.
	 * 
	 * @return the template file Uri.
	 */
	public String getTemplateFileUri() {
		return templateFileUri;
	}

	/**
	 * Set the template file Uri.
	 * 
	 * @param templateFileUri the template file Uri.
	 */
	public void setTemplateFileUri(String templateFileUri) {
		this.templateFileUri = templateFileUri;
	}

}

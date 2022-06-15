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
package com.redhat.qute.project;

import java.util.concurrent.CompletableFuture;

import com.redhat.qute.commons.ProjectInfo;
import com.redhat.qute.parser.template.Template;

/**
 * Template information provider for a given template.
 *
 * @author Angelo ZERR
 *
 */
public interface TemplateInfoProvider {

	/**
	 * Returns the current parsed template.
	 *
	 * @return the current parsed template
	 */
	Template getTemplate();

	/**
	 * Returns the owner project information of the template.
	 *
	 * @return the owner project information of the template.
	 */
	CompletableFuture<ProjectInfo> getProjectInfoFuture();

	/**
	 * Returns the owner project uri of the template.
	 *
	 * @return the owner project uri of the template.
	 */
	String getProjectUri();

	/**
	 * Returns the template id.
	 *
	 * @return the template id.
	 */
	String getTemplateId();

}

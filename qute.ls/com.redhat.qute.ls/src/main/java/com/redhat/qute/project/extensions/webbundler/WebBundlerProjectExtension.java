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
package com.redhat.qute.project.extensions.webbundler;

import com.redhat.qute.commons.config.webbundler.WebBundlerConfig;
import com.redhat.qute.project.datamodel.ExtendedDataModelProject;
import com.redhat.qute.project.extensions.AbstractProjectExtension;
import com.redhat.qute.project.extensions.ProjectExtensionContext;

/**
 * Qute project extension for Web Bundler integration.
 * 
 * @see <a href=
 *      "https://github.com/quarkiverse/quarkus-web-bundler">quarkus-web-bundler</a>
 */
public class WebBundlerProjectExtension extends AbstractProjectExtension {

	public WebBundlerProjectExtension() {
		super(WebBundlerConfig.PROJECT_FEATURE);
	}

	@Override
	protected void initialize(ExtendedDataModelProject dataModelProject, boolean onLoad, boolean enabled,
			ProjectExtensionContext context) {

	}

}

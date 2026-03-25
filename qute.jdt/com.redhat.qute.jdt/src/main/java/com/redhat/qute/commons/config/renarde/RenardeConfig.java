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
package com.redhat.qute.commons.config.renarde;

import com.redhat.qute.commons.ProjectFeature;

/**
 * Configuration for Renarde integration.
 * 
 * @see <a href=
 *      "https://github.com/quarkiverse/quarkus-renarde">quarkus-renarde</a>
 * 
 */
public class RenardeConfig {

	public static final String EXTENSION_ID = "renarde";

	public static final ProjectFeature PROJECT_FEATURE = new ProjectFeature(EXTENSION_ID);

	// ---------------------- Java Renarde classes

	public static final String RENARDE_CONTROLLER_TYPE = "io.quarkiverse.renarde.Controller";

}

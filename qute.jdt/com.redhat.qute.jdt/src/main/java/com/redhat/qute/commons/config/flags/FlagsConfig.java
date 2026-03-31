/*******************************************************************************
* Copyright (c) 2024 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.commons.config.flags;

import com.redhat.qute.commons.ProjectFeature;

/**
 * Configuration for Flags integration.
 * 
 * @see <a href=
 *      "https://quarkus.io/blog/quarkus-feature-flags/">quarkus-feature-flags</a>
 *
 */
public class FlagsConfig {

	private FlagsConfig() {
	}

	public static final String EXTENSION_ID = "flags";

	public static final ProjectFeature PROJECT_FEATURE = new ProjectFeature(EXTENSION_ID);

	public static final String FLAG_NAMESPACE_RESOLVER_CLASS = "io.quarkiverse.flags.qute.FlagNamespaceResolver";
}

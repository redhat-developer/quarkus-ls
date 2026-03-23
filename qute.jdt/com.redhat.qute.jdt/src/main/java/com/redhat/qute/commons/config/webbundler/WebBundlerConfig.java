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
package com.redhat.qute.commons.config.webbundler;

import com.redhat.qute.commons.ProjectFeature;
import com.redhat.qute.commons.config.PropertyConfig;

/**
 * Configuration for Web Bundler integration.
 * 
 * @see <a href=
 *      "https://github.com/quarkiverse/quarkus-web-bundler">quarkus-web-bundler</a>
 *
 */
public class WebBundlerConfig {

	private WebBundlerConfig() {
	}

	public static final String EXTENSION_ID = "web-bundler";

	public static final ProjectFeature PROJECT_FEATURE = new ProjectFeature(EXTENSION_ID);

	// ---------------------- Java Web Bundler classes

	public static final String BUNDLE_CLASS = "io.quarkiverse.web.bundler.runtime.Bundle";

	// ---------------------- Web Bundler configuration properties

	/**
	 * The directory in the resources which serves as root for the web assets
	 * 
	 * @see <a href=
	 *      "https://docs.quarkiverse.io/quarkus-web-bundler/dev/config-reference.html#quarkus-web-bundler_quarkus-web-bundler-web-root"
	 *      >quarkus.web-bundler.web-root</a>
	 */
	public static final PropertyConfig WEB_ROOT = new PropertyConfig("quarkus.web-bundler.web-root", "web");

	/**
	 * The directory in the project root dir which contains web assets (relative to
	 * the project root)
	 * 
	 * @see <a href=
	 *      "https://docs.quarkiverse.io/quarkus-web-bundler/dev/config-reference.html#quarkus-web-bundler_quarkus-web-bundler-project-web-dir"
	 *      >quarkus.web-bundler.project-web-dir</a>
	 */
	public static final PropertyConfig WEB_DIR = new PropertyConfig("quarkus.web-bundler.project-web-dir", "web");

	/**
	 * Indicate if this directory contains qute tags (as .html files) This is only
	 * available if the Quarkus Qute extension is in the project.
	 * 
	 * @see <a href=
	 *      "https://docs.quarkiverse.io/quarkus-web-bundler/dev/config-reference.html#quarkus-web-bundler_quarkus-web-bundler-bundle-bundle-qute-tags"
	 *      >quarkus.web-bundler.bundle."bundle".qute-tags</a>
	 */
	public static final PropertyConfig QUTE_TAGS = new PropertyConfig("quarkus.web-bundler.bundle.*.qute-tags", "");

}

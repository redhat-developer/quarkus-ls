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
package com.redhat.qute.commons.config.roq;

import com.redhat.qute.commons.ProjectFeature;
import com.redhat.qute.commons.config.PropertyConfig;

/**
 * Configuration for Roq integration.
 * 
 * @see <a href="https://github.com/quarkiverse/quarkus-roq">quarkus-roq</a>
 * 
 */
public class RoqConfig {

	public static final String EXTENSION_ID = "roq";

	public static final ProjectFeature PROJECT_FEATURE = new ProjectFeature(EXTENSION_ID);

	// ---------------------- Java Roq classes

	public static final String DATA_MAPPING_ANNOTATION = "io.quarkiverse.roq.data.runtime.annotations.DataMapping";

	public static final String SITE_CLASS = "io.quarkiverse.roq.frontmatter.runtime.model.Site";

	public static final String DOCUMENT_PAGE_CLASS = "io.quarkiverse.roq.frontmatter.runtime.model.DocumentPage";

	public static final String NORMAL_PAGE_CLASS = "io.quarkiverse.roq.frontmatter.runtime.model.NormalPage";

	// ---------------------- Roq configuration properties

	/**
	 * Path to the Roq site directory (relative to the project root).
	 * 
	 * @see <a href=
	 *      "https://docs.quarkiverse.io/quarkus-roq/dev/index.html#quarkus-roq_quarkus-roq-dir">quarkus.roq.dir</a>
	 */
	public static final PropertyConfig ROQ_DIR = new PropertyConfig("quarkus.roq.dir", "");

	/**
	 * The directory which contains content (pages and collections) in the Roq site
	 * directory.
	 * 
	 * @see <a href=
	 *      "https://docs.quarkiverse.io/quarkus-roq/dev/index.html#quarkus-roq-frontmatter_site-content-dir">site.content-dir</a>
	 */
	public static final PropertyConfig SITE_CONTENT_DIR = new PropertyConfig("site.content.dir", "content");

	/**
	 * This will be used to replace :theme when resolving layouts (e.g. layout:
	 * :theme/main.html).
	 * 
	 * @see <a href=
	 *      "https://docs.quarkiverse.io/quarkus-roq/dev/index.html#quarkus-roq-frontmatter_site-theme">site.theme</a>
	 */
	public static final PropertyConfig SITE_THEME = new PropertyConfig("site.theme", null);

	/**
	 * The location of the Roq data files relative to the quarkus.roq.dir.
	 * 
	 * @see <a href=
	 *      "https://docs.quarkiverse.io/quarkus-roq/dev/index.html#quarkus-roq-data_quarkus-roq-data-dir">quarkus.roq.data.dir</a>
	 */
	public static final PropertyConfig ROQ_DATA_DIR = new PropertyConfig("quarkus.roq.data.dir", "data");

}
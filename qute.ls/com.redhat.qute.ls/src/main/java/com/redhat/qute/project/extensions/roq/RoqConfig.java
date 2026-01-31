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
package com.redhat.qute.project.extensions.roq;

import com.redhat.qute.project.extensions.config.PropertyConfig;

/**
 * Configuration properties for Roq integration.
 * 
 * <p>
 * Defines Quarkus configuration properties that control Roq's behavior,
 * particularly the location of Roq directories and data files.
 * </p>
 * 
 * <h3>Configuration in application.properties:</h3>
 * 
 * <pre>
 * # Root directory for Roq content (default: project root)
 * quarkus.roq.dir=
 * 
 * # Directory containing data files (default: "data")
 * quarkus.roq.data.dir=data
 * </pre>
 * 
 * <h3>Directory Structure Example:</h3>
 * 
 * <pre>
 * project-root/
 * └── data/                      ← quarkus.roq.data.dir
 *     ├── authors.yaml           → accessible as {inject:authors} in templates
 *     ├── posts.json             → accessible as {inject:posts} in templates
 *     └── site-config.yaml       → accessible as {inject:site-config} in templates
 * </pre>
 */
public class RoqConfig {

	/**
	 * Root directory for Roq content.
	 * 
	 * <p>
	 * <b>Property:</b> {@code quarkus.roq.dir}<br>
	 * <b>Default:</b> {@code ""} (empty = project root)
	 * </p>
	 */
	public static final PropertyConfig ROQ_DIR = new PropertyConfig("quarkus.roq.dir", "");

	/**
	 * Directory containing Roq data files (YAML, JSON).
	 * 
	 * <p>
	 * <b>Property:</b> {@code quarkus.roq.data.dir}<br>
	 * <b>Default:</b> {@code "data"}
	 * </p>
	 * 
	 * <p>
	 * Files in this directory are automatically loaded and made available in Qute
	 * templates using the {@code inject:} namespace. For example,
	 * {@code authors.yaml} becomes accessible as {@code {inject:authors}} in
	 * templates.
	 * </p>
	 */
	public static final PropertyConfig ROQ_DATA_DIR = new PropertyConfig("quarkus.roq.data.dir", "data");
	
	// See https://docs.quarkiverse.io/quarkus-roq/dev/index.html#quarkus-roq-frontmatter_site-content-dir
	public static final PropertyConfig ROQ_CONTENT_DIR = new PropertyConfig("site.content.dir", "content");
	
	
}
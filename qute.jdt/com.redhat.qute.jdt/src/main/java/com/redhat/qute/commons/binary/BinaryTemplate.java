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
package com.redhat.qute.commons.binary;

/**
 * Binary template loaded from a JAR entry (e.g. <code>templates/hello.html</code>).
 *
 * @author Angelo ZERR
 *
 */
public class BinaryTemplate {

	private String path;

	private String uri;

	private String content;

	/**
	 * Returns the template path relative to the <code>templates/</code> entry of
	 * the JAR (e.g. <code>tags/search-button.html</code>,
	 * <code>partials/roq-series.html</code>).
	 *
	 * @return the template path relative to the <code>templates/</code> JAR entry.
	 */
	public String getPath() {
		return path;
	}

	/**
	 * Sets the template path relative to the <code>templates/</code> entry of the
	 * JAR.
	 *
	 * @param path the template path relative to the <code>templates/</code> JAR
	 *             entry.
	 */
	public void setPath(String path) {
		this.path = path;
	}

	/**
	 * Returns the URI of the template resource inside the JAR, as provided by the
	 * language client. The URI scheme depends on the IDE:
	 *
	 * <ul>
	 * <li>VS Code / JDT: <code>jdt://jarentry/</code> scheme</li>
	 * <li>IntelliJ IDEA: different scheme</li>
	 * </ul>
	 *
	 * <p>
	 * The URI is used by the language server to identify and open the template
	 * resource inside the JAR. For example (VS Code / JDT):
	 * </p>
	 *
	 * <pre>
	 * jdt://jarentry/templates/tags/search-button.html?=roq-blog/C:%5C/Users%5C/...%5C/quarkus-roq-plugin-lunr-2.1.0.BETA2.jar=/maven.groupId=/io.quarkiverse.roq=/=/maven.artifactId=/quarkus-roq-plugin-lunr=/...
	 * </pre>
	 *
	 * @return the URI of the template resource inside the JAR.
	 */
	public String getUri() {
		return uri;
	}

	/**
	 * Sets the URI of the template resource inside the JAR.
	 *
	 * @param uri the URI of the template resource inside the JAR.
	 */
	public void setUri(String uri) {
		this.uri = uri;
	}

	/**
	 * Returns the template content.
	 *
	 * @return the template content.
	 */
	public String getContent() {
		return content;
	}

	/**
	 * Sets the template content.
	 *
	 * @param content the template content.
	 */
	public void setContent(String content) {
		this.content = content;
	}

}
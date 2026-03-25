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

import java.util.List;
import java.util.Map;

/**
 * Binary template information loaded from a JAR. It contains:
 *
 * <ul>
 * <li>the list of {@link BinaryTemplate} loaded from the JAR
 * <code>templates/</code> entry.</li>
 * <li>the properties loaded from the JAR <code>application.properties</code>
 * root entry.</li>
 * </ul>
 *
 * @author Angelo ZERR
 *
 */
public class BinaryTemplateInfo {

	private String binaryName;

	private List<BinaryTemplate> templates;

	private Map<String, String> properties;

	/**
	 * Returns the name of the JAR binary from which the templates were loaded.
	 *
	 * @return the JAR binary name (e.g. {@code quarkus-qute-2.x.jar}), or
	 *         {@code null} if not set.
	 */
	public String getBinaryName() {
		return binaryName;
	}

	/**
	 * Sets the name of the JAR binary from which the templates were loaded.
	 *
	 * @param binaryName the JAR binary name (e.g. {@code quarkus-qute-2.x.jar}).
	 */
	public void setBinaryName(String binaryName) {
		this.binaryName = binaryName;
	}

	/**
	 * Returns the list of binary templates loaded from the JAR
	 * <code>templates/</code> entry.
	 *
	 * @return the list of binary templates loaded from the JAR
	 *         <code>templates/</code> entry.
	 */
	public List<BinaryTemplate> getTemplates() {
		return templates;
	}

	/**
	 * Sets the list of binary templates loaded from the JAR <code>templates/</code>
	 * entry.
	 *
	 * @param templates the list of binary templates.
	 */
	public void setTemplates(List<BinaryTemplate> templates) {
		this.templates = templates;
	}

	/**
	 * Returns the properties loaded from the JAR root
	 * <code>application.properties</code> entry.
	 *
	 * @return the properties loaded from the JAR root
	 *         <code>application.properties</code> entry.
	 */
	public Map<String, String> getProperties() {
		return properties;
	}

	/**
	 * Sets the properties loaded from the JAR root
	 * <code>application.properties</code> entry.
	 *
	 * @param properties the properties map.
	 */
	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}

}
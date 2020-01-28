/*******************************************************************************
* Copyright (c) 2020 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.microprofile.commons;

import java.util.List;

/**
 * Stores labels for the project located at a specific project uri
 * 
 * @author dakwon
 *
 */
public class ProjectLabelInfoEntry {
	private final String uri;
	private final List<String> labels;

	public ProjectLabelInfoEntry(String uri, List<String> labels) {
		this.uri = uri;
		this.labels = labels;
	}

	/**
	 * Returns the project uri
	 * @return the project uri
	 */
	public String getUri() {
		return uri;
	}

	/**
	 * Returns the labels for the current project uri
	 * @return the labels for the current project uri
	 */
	public List<String> getLabels() {
		return labels;
	}
}
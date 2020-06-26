/*******************************************************************************
* Copyright (c) 2020 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.microprofile.commons;

import java.util.Collections;
import java.util.List;

/**
 * Stores labels for the project located at a specific project uri
 * 
 * @author dakwon
 *
 */
public class ProjectLabelInfoEntry {
	public static final ProjectLabelInfoEntry EMPTY_PROJECT_INFO = new ProjectLabelInfoEntry("", "",
			Collections.emptyList());

	private final String uri;
	private final String name;
	private final List<String> labels;

	public ProjectLabelInfoEntry(String uri, String name, List<String> labels) {
		this.uri = uri;
		this.name = name;
		this.labels = labels;
	}

	/**
	 * Returns the project uri
	 * 
	 * @return the project uri
	 */
	public String getUri() {
		return uri;
	}

	/**
	 * Returns the name of the project
	 * 
	 * @return The name of this project
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the labels for the current project uri
	 * 
	 * @return the labels for the current project uri
	 */
	public List<String> getLabels() {
		return labels;
	}

	/**
	 * Returns true if the project has the given label and false otherwise.
	 * 
	 * @param label the label.
	 * @return true if the project has the given label and false otherwise.
	 */
	public boolean hasLabel(String label) {
		return labels != null && labels.contains(label);
	}
}
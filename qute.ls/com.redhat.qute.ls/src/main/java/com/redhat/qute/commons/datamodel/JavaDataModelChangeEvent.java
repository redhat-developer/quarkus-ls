/*******************************************************************************
* Copyright (c) 2021 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.commons.datamodel;

import java.util.Set;

/**
 * The Qute project properties change event.
 *
 * @author Angelo ZERR
 *
 */
public class JavaDataModelChangeEvent {

	public static class ProjectChangeInfo {

		private String uri;
		private Set</* Full qualified name of Java class */ String> sources;

		public ProjectChangeInfo() {
			
		}
		
		public ProjectChangeInfo(String uri) {
			this.uri = uri;			
		}

		public String getUri() {
			return uri;
		}

		public void setSources(Set<String> sources) {
			this.sources = sources;
		}

		public Set<String> getSources() {
			return sources;
		}

	}

	private Set<ProjectChangeInfo> projects;

	/**
	 * Returns the project URIs impacted by the type scope changed.
	 *
	 * @return the project URIs impacted by the type scope changed.
	 */
	public Set<ProjectChangeInfo> getProjects() {
		return projects;
	}

	/**
	 * Set the project URIs impacted by the type scope changed.
	 *
	 * @param projectURIs the project URIs impacted by the type scope changed.
	 */
	public void setProjects(Set<ProjectChangeInfo> projects) {
		this.projects = projects;
	}

}

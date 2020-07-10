/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lsp4mp.commons;

import java.util.List;
import java.util.Set;

/**
 * The MicroProfile project properties change event.
 * 
 * @author Angelo ZERR
 *
 */
public class MicroProfilePropertiesChangeEvent {

	private List<MicroProfilePropertiesScope> type;

	private Set<String> projectURIs;

	/**
	 * Returns the search scope to collect the MicroProfile properties.
	 * 
	 * @return the search scope to collect the MicroProfile properties.
	 */
	public List<MicroProfilePropertiesScope> getType() {
		return type;
	}

	/**
	 * Set the search scope to collect the MicroProfile properties.
	 * 
	 * @param type the search scope to collect the MicroProfile properties.
	 */
	public void setType(List<MicroProfilePropertiesScope> type) {
		this.type = type;
	}

	/**
	 * Returns the project URIs impacted by the type scope changed.
	 * 
	 * @return the project URIs impacted by the type scope changed.
	 */
	public Set<String> getProjectURIs() {
		return projectURIs;
	}

	/**
	 * Set the project URIs impacted by the type scope changed.
	 * 
	 * @param projectURIs the project URIs impacted by the type scope changed.
	 */
	public void setProjectURIs(Set<String> projectURIs) {
		this.projectURIs = projectURIs;
	}

}

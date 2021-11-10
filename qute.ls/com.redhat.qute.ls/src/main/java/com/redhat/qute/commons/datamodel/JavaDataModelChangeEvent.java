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

	private Set<String> projectURIs;

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
